package io.github.joxebus.groovywebconsole.controller

import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.github.joxebus.groovywebconsole.service.common.FileService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Part
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.http.server.types.files.SystemFile

@Controller("/image")
class ImageController {
    final FileService fileService

    ImageController(FileService fileService) {
        this.fileService = fileService
    }

    @Post(value ="/upload", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    HttpResponse<FileResponse> uploadImage(@Part CompletedFileUpload file) {
        File image = new File(System.getProperty("java.io.tmpdir"), file.filename)
        image.bytes = file.bytes
        FileResponse fileResponse = fileService.upload(file.filename, image)
        if(fileResponse.error) {
            HttpResponse.serverError(fileResponse)
        } else {
            HttpResponse.ok(fileResponse)
        }
    }

    @Get(value ="/download/{filename}", consumes = MediaType.APPLICATION_JSON, produces = MediaType.IMAGE_PNG)
    SystemFile downloadImage(String filename){
        fileService.download(filename)
    }
}
