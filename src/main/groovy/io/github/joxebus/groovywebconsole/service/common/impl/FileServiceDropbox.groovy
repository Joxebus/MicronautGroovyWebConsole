package io.github.joxebus.groovywebconsole.service.common.impl

import com.dropbox.core.DbxDownloader
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.github.joxebus.groovywebconsole.service.common.FileService
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.http.server.types.files.SystemFile

import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@CompileStatic
@Requires(env = "dropbox")
@Slf4j
class FileServiceDropbox implements FileService {

    private DbxClientV2 client;
    private String fileUploadPrefix
    private String fileCacheFolder

    @Value('${micronaut.server.baseUrl}')
    private String baseUrl

    FileServiceDropbox(@Value('${dropbox.token}') String token,
                       @Value('${file.upload.prefix}') String fileUploadPrefix,
                       @Value('${file.cache.folder}') String fileCacheFolder){
        this.fileUploadPrefix = fileUploadPrefix
        this.fileCacheFolder = fileCacheFolder
        DbxRequestConfig config = DbxRequestConfig.newBuilder("gwc/files").build()
        client = new DbxClientV2(config, token)
    }

    @PostConstruct
    private final void init() {
        File file = new File(fileCacheFolder)
        log.info("Using folder [$fileCacheFolder] to cache files")
        if(!file.exists()) {
            log.warn("File folder not found, trying to create new folder")
            if(!file.mkdirs()) {
                log.error("File cache folder cannot be created")
                System.exit(1)
            }
            log.info("File folder created in location: [$fileCacheFolder]")
        }
    }

    @Override
    FileResponse upload(SystemFile systemFile) {
        FileResponse fileResponse = new FileResponse()
        try {
            if(!systemFile || !systemFile.file) {
                throw new RuntimeException("Failed to create empty file");
            }

            String filename = UUID.randomUUID().toString()
            FileMetadata metadata = client.files().uploadBuilder(fileUploadPrefix.concat(filename))
                    .uploadAndFinish(systemFile.file.newInputStream())

            log.debug("Saving file into url: {}", metadata.getPathLower())

            log.debug("Saving copy on cache for file ${filename}")
            FileOutputStream fos = new FileOutputStream(new File(fileCacheFolder, filename))
            fos.write(systemFile.file.getBytes())
            fos.close()

            fileResponse.uploaded = true
            fileResponse.url = "${baseUrl}/${filename}"
            log.info("File [${filename}] successfully saved")
        } catch(Exception e) {
            fileResponse.uploaded = false
            fileResponse.error = FileResponse.newError(e.getMessage())
            log.error("Error: file cannot be uploaded", e)
        }
        fileResponse

    }

    @Override
    byte[] download(String filename) {
        byte[] response = null
        try {
            File downloaded = new File(fileCacheFolder, filename)
            if(!downloaded.exists()) {
                log.info("Downloading [{}] from dropbox", filename)
                DbxDownloader download = client.files().download(fileUploadPrefix.concat(filename))
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                download.download(out)
                response = out.toByteArray()
                out.close()

                log.debug("Saving copy on cache for file ${filename}")
                FileOutputStream fos = new FileOutputStream(new File(fileCacheFolder, filename))
                fos.write(response)
                fos.close()

            } else {
                log.info("Reading [{}] from cache", filename)
                response = downloaded.bytes
            }
        } catch(Exception e) {
            log.error('Error: The file cannot be download', e)
            null
        }
        response
    }
}
