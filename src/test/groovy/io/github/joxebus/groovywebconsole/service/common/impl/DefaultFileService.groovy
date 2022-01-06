package io.github.joxebus.groovywebconsole.service.common.impl

import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.github.joxebus.groovywebconsole.service.common.FileService
import io.micronaut.http.server.types.files.SystemFile

import javax.inject.Singleton

@Singleton
class DefaultFileService implements FileService {
    @Override
    FileResponse upload(SystemFile file) {
        return null
    }

    @Override
    byte[] download(String filename) {
        return new byte[0]
    }
}
