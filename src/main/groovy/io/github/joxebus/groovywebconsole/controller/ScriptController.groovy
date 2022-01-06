package io.github.joxebus.groovywebconsole.controller

import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.github.joxebus.groovywebconsole.service.ScriptExecutorService
import io.github.joxebus.groovywebconsole.service.ScriptFileGeneratorService
import io.github.joxebus.groovywebconsole.service.common.FileService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.server.types.files.SystemFile

@Controller("/script")
class ScriptController {

    private final static String GROOVY_MIME_TYPE = "text/x-groovy"
    final ScriptExecutorService scriptExecutorService
    final ScriptFileGeneratorService scriptFileGeneratorService
    final FileService fileService

    ScriptController(ScriptExecutorService scriptExecutorService,
                     ScriptFileGeneratorService scriptFileGeneratorService,
                     FileService fileService) {
        this.scriptExecutorService = scriptExecutorService
        this.scriptFileGeneratorService = scriptFileGeneratorService
        this.fileService = fileService
    }

    @Post(value ="/execute", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    HttpResponse<Map> executeScript(@Body Map groovyScript) {
        Map result = scriptExecutorService.execute(groovyScript.code)
        if(result.error) {
            result.remove('exception')
            HttpResponse.badRequest(result)
        } else {
            HttpResponse.ok(result)
        }

    }

    @Post(value ="/download", consumes = MediaType.APPLICATION_JSON, produces = GROOVY_MIME_TYPE)
    SystemFile downloadScript(@Body Map groovyScript){
        scriptFileGeneratorService.generateGroovyCodeFile(groovyScript.code)
    }

    @Post(value ="/upload", consumes = MediaType.APPLICATION_JSON, produces = GROOVY_MIME_TYPE)
    HttpResponse<FileResponse> upload(@Body Map groovyScript) {
        SystemFile systemFile = scriptFileGeneratorService.generateGroovyCodeFile(groovyScript.code)
        FileResponse fileResponse = fileService.upload(systemFile)
        if(fileResponse.error) {
            HttpResponse.serverError(fileResponse)
        } else {
            HttpResponse.ok(fileResponse)
        }
    }
}
