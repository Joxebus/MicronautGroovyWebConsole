package io.github.joxebus.groovywebconsole.controller

import io.github.joxebus.groovywebconsole.service.ScriptExecutorService
import io.github.joxebus.groovywebconsole.service.ScriptFileGeneratorService
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

    ScriptController(ScriptExecutorService scriptExecutorService, ScriptFileGeneratorService scriptFileGeneratorService) {
        this.scriptExecutorService = scriptExecutorService
        this.scriptFileGeneratorService = scriptFileGeneratorService
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
}
