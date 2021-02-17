package io.github.joxebus.groovywebconsole.controller

import io.github.joxebus.groovywebconsole.service.ScriptExecutorService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.RequestAttribute
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

    @Consumes( [MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON] )
    @Post("/")
    ModelAndView executeScript(@RequestAttribute("code") String groovyScript) {
        def output = scriptExecutorService.execute(groovyScript)
        new ModelAndView("home/home", [groovyScript:groovyScript, output:output])
    }
}
