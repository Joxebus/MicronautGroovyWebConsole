package io.github.joxebus.groovywebconsole.pojo

import io.micronaut.serde.annotation.Serdeable

@Serdeable.Serializable
@Serdeable.Deserializable
class FileResponse {

    String url
    boolean uploaded
    FileResponseError error

    @Serdeable.Serializable
    @Serdeable.Deserializable
    static class FileResponseError {
        String message
    }

    static FileResponseError newError(String errorMessage) {
        new FileResponseError(message: errorMessage)
    }
}
