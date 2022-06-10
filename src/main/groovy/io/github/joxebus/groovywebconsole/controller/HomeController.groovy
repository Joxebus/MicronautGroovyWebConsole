package io.github.joxebus.groovywebconsole.controller

import io.github.joxebus.groovywebconsole.service.common.FileService
import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.server.types.files.SystemFile
import io.micronaut.views.ModelAndView

@Controller("/")
class HomeController {

    @Value('${micronaut.server.baseUrl}')
    private String baseUrl
    @Value('${social.banner}')
    private String defaultImage

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
        String imageUrl = defaultImage
        if(uuid) {
            String imageName = uuid.concat(".png")
            SystemFile code = fileService.download(uuid)
            SystemFile screenShot = fileService.download(imageName)
            imageUrl = screenShot ? baseUrl.concat("/image/download/").concat(imageName) : defaultImage
            groovyScript = code?.file?.text ?: groovyScript
        }

        new ModelAndView("home/home", [groovyScript: groovyScript, imageUrl: imageUrl])
    }

}
