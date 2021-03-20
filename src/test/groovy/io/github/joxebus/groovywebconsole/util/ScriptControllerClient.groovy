package io.github.joxebus.groovywebconsole.util

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.server.types.files.SystemFile

@Client("/script")
interface ScriptControllerClient {

    @Post("/execute")
    HttpResponse<Map> executeScript(@Body Map groovyScript)
    @Post("/download")
    SystemFile downloadScript(@Body Map groovyScript)
}
