package io.github.joxebus.groovywebconsole.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.inject.Singleton

@Singleton
@CompileStatic
@Slf4j
class ScriptExecutorService {

    Map execute(String scriptText) {
        Map result = [:]
        String encoding = 'UTF-8'
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(stream, true, encoding)

        Binding binding = new Binding([out: printStream])
        GroovyShell shell = new GroovyShell(binding)
        long startTime = System.currentTimeMillis()
        try {
            log.info("Start execution of the script")
            shell.evaluate(scriptText)
            result.put("output", stream.toString())
        } catch(Exception | Error e) {
            log.error("There was an error when executing script", e)
            result.put("error", e.getMessage())
        } finally {
            long finishTime = System.currentTimeMillis() - startTime
            log.info("Finish script execution")
            result.put("executionTime", "Execution time: $finishTime ms".toString())
        }
        result
    }
}
