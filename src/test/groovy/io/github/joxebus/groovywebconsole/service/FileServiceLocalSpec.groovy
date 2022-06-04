package io.github.joxebus.groovywebconsole.service

import groovy.util.logging.Slf4j
import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.github.joxebus.groovywebconsole.service.common.FileService
import io.github.joxebus.groovywebconsole.service.common.impl.FileServiceLocal
import io.github.joxebus.groovywebconsole.util.ScriptEndOfFileTrait
import io.micronaut.http.server.types.files.SystemFile
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
class FileServiceLocalSpec extends Specification implements ScriptEndOfFileTrait {

    @Shared
    String baseUrl = "test"
    @Shared
    String fileUploadFolder = "build/tmp/test/upload_folder/"

    @Shared
    ScriptFileGeneratorService scriptFileGeneratorService
    @Shared
    FileService fileService

    def setupSpec() {
        fileService = new FileServiceLocal(
                baseUrl: baseUrl,
                fileUploadFolder: fileUploadFolder

        )

        scriptFileGeneratorService = new ScriptFileGeneratorService()
        fileService.init()
    }

    def cleanupSpec() {
        log.info "Deleting folder $fileUploadFolder"
        File tempFolder = new File(fileUploadFolder)
        tempFolder.deleteDir()
    }

    def "Test upload file"() {
        given:
        String script = 'println "Test upload file"'
        SystemFile systemFile = scriptFileGeneratorService.generateGroovyCodeFile(script)

        when:
        FileResponse fileResponse = fileService.upload(systemFile)
        File file = new File(fileUploadFolder, fileResponse.url.replace(baseUrl+"/", ""))

        then:
        fileResponse.uploaded
        withEof(script) == file.text

    }

    def "Test download file"() {
        given:
        String script = 'println "Test download file"'
        SystemFile systemFile = scriptFileGeneratorService.generateGroovyCodeFile(script)
        FileResponse fileResponse = fileService.upload(systemFile)
        String filename = fileResponse.url.replace(baseUrl+"/", "")

        when:
        byte[] bytes = fileService.download(filename)
        File file = new File(fileUploadFolder, UUID.randomUUID().toString())
        file.bytes = bytes

        then:
        fileResponse.uploaded
        withEof(script) == file.text
    }
}
