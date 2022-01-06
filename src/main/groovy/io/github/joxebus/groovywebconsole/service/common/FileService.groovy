package io.github.joxebus.groovywebconsole.service.common

import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.micronaut.http.server.types.files.SystemFile


interface FileService {

    FileResponse upload(SystemFile file)

    byte[] download(String filename)
}