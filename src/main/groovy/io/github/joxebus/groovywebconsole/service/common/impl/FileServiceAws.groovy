package io.github.joxebus.groovywebconsole.service.common.impl


import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
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
@Requires(env = "aws")
@Slf4j
class FileServiceAws implements FileService, FileServiceCache {

    private String fileUploadPrefix
    private String bucketName
    private AWSCredentials credentials
    private AmazonS3 s3client

    @Value('${micronaut.server.baseUrl}')
    private String baseUrl

    FileServiceAws(@Value('${aws.access.key}') String awsAccessKey,
                   @Value('${aws.secret.key}') String awsSecretKey,
                   @Value('${aws.region}') String region,
                   @Value('${aws.bucket.name}') String bucketName,
                   @Value('${file.upload.prefix}') String fileUploadPrefix,
                   @Value('${file.cache.folder}') String fileCacheFolder) {
        this.bucketName = bucketName
        this.fileUploadPrefix = fileUploadPrefix
        this.fileCacheFolder = fileCacheFolder
        credentials = new BasicAWSCredentials(
                awsAccessKey,
                awsSecretKey
        );

        s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.valueOf(region))
                .build();
    }

    @PostConstruct
    private final void init() {
        initCache()

        if(!s3client.doesBucketExist(bucketName)) {
            log.info("Creating bucket [bucketName] ")
            s3client.createBucket(bucketName)
        }

    }

    @Override
    FileResponse upload(String filename, File file) {
        FileResponse fileResponse = new FileResponse()
        try {
            if(!file) {
                throw new RuntimeException("Failed to create empty file");
            }
            String name = filename ?: UUID.randomUUID().toString()
            s3client.putObject(
                    bucketName,
                    fileUploadPrefix.concat(name),
                    file)

            log.debug("Saving file into bucket: ${bucketName}")

            saveToCache(name, file.getBytes())

            fileResponse.uploaded = true
            fileResponse.url = "${baseUrl}/${name}"
            log.info("File [${name}] successfully saved")
        } catch (SdkClientException e) {
            fileResponse.uploaded = false
            fileResponse.error = FileResponse.newError(e.getMessage())
            log.error("There was an error trying to create file on S3 Bucket", e)
        }
        fileResponse
    }

    @Override
    SystemFile download(String filename) {
        SystemFile response
        try {
            File downloaded = new File(fileCacheFolder, filename)
            if(!downloaded.exists()) {
                log.info("Downloading [{}] from aws", filename)
                S3Object s3object = s3client.getObject(bucketName, fileUploadPrefix.concat(filename))
                S3ObjectInputStream inputStream = s3object.getObjectContent()

                downloaded.bytes = inputStream.bytes
                saveToCache(filename, downloaded.bytes)

            } else {
                log.info("Reading [{}] from cache", filename)
            }
            response = new SystemFile(downloaded)
            response.attach(filename)
        } catch(SdkClientException e) {
            log.error("There was an error trying to get the file from S3", e)
        }
        response
    }


}
