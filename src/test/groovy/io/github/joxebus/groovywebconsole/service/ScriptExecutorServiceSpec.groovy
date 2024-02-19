package io.github.joxebus.groovywebconsole.service

import io.github.joxebus.groovywebconsole.exception.ScriptSecurityException
import io.github.joxebus.groovywebconsole.security.ScriptSecurityManager
import io.github.joxebus.groovywebconsole.util.ScriptEndOfFileTrait
import spock.lang.Shared
import spock.lang.Specification

import static io.github.joxebus.groovywebconsole.util.FileReaderUtil.getFileContent

class ScriptExecutorServiceSpec extends Specification implements ScriptEndOfFileTrait {

    @Shared
    ScriptExecutorService scriptExecutorService
    @Shared
    ScriptSecurityManager scriptSecurityManager

    def setupSpec() {
        scriptSecurityManager = new ScriptSecurityManager()
        scriptExecutorService = new ScriptExecutorService(scriptSecurityManager)
    }

    def "Test script execute simple script successful"() {
        given:
        String script = "println 'hello world'"

        when:
        Map result = scriptExecutorService.execute(script)

        then:
        result.output == withEof("hello world")

    }

    def "Test script execute complex script successful"() {
        given:
        String script = getFileContent('test_scripts/bank-account-test.txt')

        when:
        Map result = scriptExecutorService.execute(script)

        then:
        result.output == withEof("100.0\n50.0\n150.0\n3.0")

    }

    def "Test execute [#type] script successful"() {
        given:
        String script = getFileContent("test_scripts/${type}-test.txt")

        String output = getFileContent("test_scripts/${type}-test-output.txt")

        when:
        Map result = scriptExecutorService.execute(script)

        then:
        result.output == withEof(output)

        where:
        type << ['xml', 'yaml', 'json', 'date']

    }

    def "Test thread declarations not allowed"() {
        when:
        Map result = scriptExecutorService.execute(script)

        then:
        result.error.contains("The class [java.lang.Thread] is not allowed on this context")
        result.exception instanceof ScriptSecurityException

        where:
        script << [
                'new Thread({ -> println "hey" }).start()',
                'while (true) { new Thread(() -> println "hola").start() }'
        ]
    }

    def "Test println inside another level produces output expected"() {
        setup:
        String script = '''
        def doSomething = {
          println "Second level text"
          if(true) {
             println "Third level text"
          }
        }
        
        println "First level text"
        doSomething()
        '''
        when:
        Map result = scriptExecutorService.execute(script)

        then:
        result.output == withEof("First level text\nSecond level text\nThird level text")
    }

    def "Test System.exit is not a valid script to execute"() {
        when:
        Map result = scriptExecutorService.execute(script)

        then:
        result.error.contains("Your script contains operations not allowed")
        result.exception instanceof SecurityException

        where:
        script << [
                '''def exit = System.&exit
                exit(1)''',
                '''
                System.exit(1)
                '''
        ]
    }


}
