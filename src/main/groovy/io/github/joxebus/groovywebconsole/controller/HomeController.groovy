package io.github.joxebus.groovywebconsole.controller


import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.views.ModelAndView

@Controller("/")
class HomeController {

    @Get("/")
    ModelAndView home(){
        new ModelAndView("home/home", [groovyScript: "// Write your code here"])
    }


}
