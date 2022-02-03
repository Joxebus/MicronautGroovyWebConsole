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

    synchronized Map execute(String scriptText) {
        log.info("Start execution of the script")
        Map result = [:]
        System.setSecurityManager(scriptSecurityManager)
        String encoding = 'UTF-8'
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(stream, true, encoding)
        PrintStream defaultOut = System.out

        Binding binding = new Binding([out: printStream])
        GroovyShell shell = createShell(binding)
        long startTime = System.currentTimeMillis()
        try {
            System.setOut(printStream)
            shell.evaluate(scriptText)
            result.put("output", stream.toString())
        } catch(Exception | Error e) {
            e.printStackTrace(printStream)
            result.put("error", stream.toString())
            result.put("exception", e)
        } finally {
            System.setOut(defaultOut)
            long finishTime = System.currentTimeMillis() - startTime
            result.put("executionTime", "Execution time: $finishTime ms".toString())
            log.info("Finish script execution in $finishTime ms")
        }
        result
    }

    private GroovyShell createShell(Binding binding) {
        ClassLoader parentClassLoader = ScriptExecutorService.class.getClassLoader()
        ScriptSecurityClassLoader scriptClassLoader = new ScriptSecurityClassLoader(parentClassLoader)
        new GroovyShell(scriptClassLoader, binding)
    }
}
