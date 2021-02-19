package io.github.joxebus.groovywebconsole.controller

import io.github.joxebus.groovywebconsole.service.ScriptExecutorService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.views.ModelAndView

@Controller("/")
class HomeController {

    final ScriptExecutorService scriptExecutorService

    HomeController(ScriptExecutorService scriptExecutorService) {
        this.scriptExecutorService = scriptExecutorService
    }

    @Get("/")
    ModelAndView home(){
        new ModelAndView("home/home", [groovyScript: "// Write your code here"])
    }

    @Post(value ="/", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    HttpResponse<Map> executeScript(@Body Map groovyScript) {
        Map result = scriptExecutorService.execute(groovyScript.code)
        HttpResponse.ok(result)
    }
}
