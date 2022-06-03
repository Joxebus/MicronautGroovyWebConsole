package io.github.joxebus.groovywebconsole.util

import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.server.types.files.SystemFile

@Client("/script")
interface ScriptControllerClient {

    @Post("/execute")
    HttpResponse<Map> executeScript(@Body Map groovyScript)

    @Post("/upload")
    HttpResponse<FileResponse> upload(@Body Map groovyScript)

    @Post(value ="/download", consumes = "text/x-groovy")
    HttpResponse<String> downloadScript(@Body Map groovyScript)
}
