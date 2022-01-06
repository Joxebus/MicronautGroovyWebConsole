package io.github.joxebus.groovywebconsole.controller

import io.github.joxebus.groovywebconsole.service.common.FileService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.views.ModelAndView

@Controller("/")
class HomeController {

    FileService fileService

    HomeController(FileService fileService) {
        this.fileService = fileService
    }

    @Get('/')
    ModelAndView home(){
        home(null)
    }

    @Get('/{uuid}')
    ModelAndView home(String uuid){
        String groovyScript = "// Write your code here"
        if(uuid) {
            byte[] bytes = fileService.download(uuid)
            groovyScript = bytes ? new String(bytes) : groovyScript
        }

        new ModelAndView("home/home", [groovyScript: groovyScript])
    }

}
