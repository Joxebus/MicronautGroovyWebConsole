package io.github.joxebus.groovywebconsole.service.common.impl

import com.dropbox.core.DbxDownloader
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.github.joxebus.groovywebconsole.service.common.FileService
import io.github.joxebus.groovywebconsole.service.common.FileServiceCache
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.http.server.types.files.SystemFile

import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@CompileStatic
@Requires(env = "dropbox")
@Slf4j
class FileServiceDropbox implements FileService, FileServiceCache {

    private DbxClientV2 client;
    private String fileUploadPrefix

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
        initCache()
    }

    @Override
    FileResponse upload(String filename, File file) {
        FileResponse fileResponse = new FileResponse()
        try {
            String name = filename ?: UUID.randomUUID().toString()
            FileMetadata metadata = client.files().uploadBuilder(fileUploadPrefix.concat(name))
                    .uploadAndFinish(file.newInputStream())

            log.debug("Saving file into url: {}", metadata.getPathLower())

            saveToCache(name, file.getBytes())

            fileResponse.uploaded = true
            fileResponse.url = "${baseUrl}/${name}"
            log.info("File [${filename}] successfully saved")
        } catch(Exception e) {
            fileResponse.uploaded = false
            fileResponse.error = FileResponse.newError(e.getMessage())
            log.error("Error: file cannot be uploaded", e)
        }
        fileResponse
    }

    @Override
    SystemFile download(String filename) {
        SystemFile response
        try {
            File downloaded = new File(fileCacheFolder, filename)
            if(!downloaded.exists()) {
                log.info("Downloading [{}] from dropbox", filename)
                DbxDownloader download = client.files().download(fileUploadPrefix.concat(filename))
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                download.download(out)
                downloaded.bytes = out.toByteArray()
                out.close()

                saveToCache(filename, downloaded.bytes)

            } else {
                log.info("Reading [{}] from cache", filename)
            }
            response = new SystemFile(downloaded)
            response.attach(filename)
        } catch(Exception e) {
            log.error('Error: The file cannot be download', e)
        }
        response
    }
}
