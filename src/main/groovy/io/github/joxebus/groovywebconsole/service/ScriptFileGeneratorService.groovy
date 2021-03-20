package io.github.joxebus.groovywebconsole.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.http.server.types.files.SystemFile

import javax.inject.Singleton

@Singleton
@CompileStatic
@Slf4j
class ScriptFileGeneratorService {

    private static String DEFAULT_FILENAME = "script"
    private static String SUFFIX_FILENAME = ".groovy"

    SystemFile generateGroovyCodeFile(String scriptText, String filename = DEFAULT_FILENAME) {
        try {
            String fullName = filename.concat(SUFFIX_FILENAME)
            log.info("Creating file [$fullName]")
            File groovyScript = File.createTempFile(filename, SUFFIX_FILENAME)

            groovyScript.withWriter {writer ->
                scriptText.eachLine {line ->
                    writer.println(line)
                }
            }
            log.info("Downloading file [$fullName]")
            new SystemFile(groovyScript).attach(fullName)
        }
         catch (IOException e) {
             log.error("Error trying to generate Groovy script file", e)
             throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE, "error generating file")
        }

    }
}
