package io.github.joxebus.groovywebconsole.service.common

import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.micronaut.http.server.types.files.SystemFile


interface FileService {

    default FileResponse upload(SystemFile systemFile) {
        if(!systemFile || !systemFile.file) {
            throw new RuntimeException("Failed to create empty file");
        }

        upload("", systemFile.file)
    }

    FileResponse upload(String filename, File file)

    SystemFile download(String filename)
}