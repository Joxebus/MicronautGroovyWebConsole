package io.github.joxebus.groovywebconsole.pojo

class FileResponse {

    String url
    boolean uploaded
    FileResponseError error

    static class FileResponseError {
        String message
    }

    static FileResponseError newError(String errorMessage) {
        new FileResponseError(message: errorMessage)
    }
}
