package io.github.joxebus.groovywebconsole.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.inject.Singleton

@Singleton
@CompileStatic
@Slf4j
class ScriptExecutorService {

    ByteArrayOutputStream execute(String scriptText) {
        String encoding = 'UTF-8'
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(stream, true, encoding)

        Binding binding = new Binding([out: printStream])
        GroovyShell shell = new GroovyShell(binding)
        long startTime = System.currentTimeMillis()
        try {
            log.info("Start execution of the script")
            Script script = shell.parse(scriptText)
            script.run()
        } catch(Exception e) {
            log.error("There was an error when executing script", e)
            printStream.println(e.getMessage())
        } finally {
            long finishTime = System.currentTimeMillis() - startTime
            printStream.println("\nExecution time: $finishTime ms")
            log.info("Finish script execution")
        }

        stream
    }
}
