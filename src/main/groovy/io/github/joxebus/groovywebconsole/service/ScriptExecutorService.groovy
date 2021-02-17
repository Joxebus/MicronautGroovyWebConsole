package io.github.joxebus.groovywebconsole.service

import groovy.transform.CompileStatic

import javax.inject.Singleton

@Singleton
@CompileStatic
class ScriptExecutorService {

    ByteArrayOutputStream execute(String scriptText) {
        String encoding = 'UTF-8'
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(stream, true, encoding)

        Binding binding = new Binding([out: printStream])
        GroovyShell shell = new GroovyShell(binding)
        def result
        long startTime = System.currentTimeMillis()
        try {
            Script script = shell.parse(scriptText)
            script.run()
        } catch(Exception e) {
            e.printStackTrace()
            printStream.println(e.getMessage())
        } finally {
            long finishTime = System.currentTimeMillis() - startTime
            printStream.println("\nExecution time: $finishTime ms")
        }

        stream
    }
}
