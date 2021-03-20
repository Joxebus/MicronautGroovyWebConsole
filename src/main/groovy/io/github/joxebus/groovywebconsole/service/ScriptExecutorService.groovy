package io.github.joxebus.groovywebconsole.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.github.joxebus.groovywebconsole.security.ScriptSecurityClassLoader
import io.github.joxebus.groovywebconsole.security.ScriptSecurityManager

import javax.inject.Singleton

@Singleton
@CompileStatic
@Slf4j
class ScriptExecutorService {

    final ScriptSecurityManager scriptSecurityManager

    ScriptExecutorService(ScriptSecurityManager scriptSecurityManager) {
        this.scriptSecurityManager = scriptSecurityManager
    }

    Map execute(String scriptText) {
        Map result = [:]
        System.setSecurityManager(scriptSecurityManager)
        String encoding = 'UTF-8'
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(stream, true, encoding)

        Binding binding = new Binding([out: printStream])
        GroovyShell shell = createShell(binding)
        long startTime = System.currentTimeMillis()
        try {
            log.info("Start execution of the script")
            shell.evaluate(scriptText)
            result.put("output", stream.toString())
        } catch(Exception | Error e) {
            log.error("There was an error when executing script", e)
            e.printStackTrace(printStream)
            result.put("error", stream.toString())
            result.put("exception", e)
        } finally {
            long finishTime = System.currentTimeMillis() - startTime
            log.info("Finish script execution in $finishTime ms")
            result.put("executionTime", "Execution time: $finishTime ms".toString())
        }
        result
    }

    private GroovyShell createShell(Binding binding) {
        ClassLoader parentClassLoader = ScriptExecutorService.class.getClassLoader();
        ScriptSecurityClassLoader scriptClassLoader = new ScriptSecurityClassLoader(parentClassLoader);
        new GroovyShell(scriptClassLoader, binding)
    }
}
