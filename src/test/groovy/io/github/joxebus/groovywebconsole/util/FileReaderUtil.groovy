package io.github.joxebus.groovywebconsole.util

class FileReaderUtil {

    static final String getFileContent(String filename) {
        new File(Thread.currentThread()
                .getContextClassLoader()
                .getResource(filename).toURI()).text
    }
}
