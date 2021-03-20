package io.github.joxebus.groovywebconsole.service

import io.github.joxebus.groovywebconsole.exception.ScriptSecurityException
import io.github.joxebus.groovywebconsole.security.ScriptSecurityManager
import io.github.joxebus.groovywebconsole.util.ScriptEndOfFileTrait
import spock.lang.Shared
import spock.lang.Specification

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
        String script = """
        @groovy.transform.TupleConstructor
        class BankAccount {
           BigDecimal amount = 0.0
           
           BankAccount plus(BankAccount account) {
              this.amount += account.amount
              this
           }
           
           BankAccount minus(BankAccount account) {
              this.amount -= account.amount
              this
           }
           
           BankAccount multiply(BankAccount account) {
              this.amount *= account.amount
              this
           }
           
           BankAccount div(BankAccount account) {
              this.amount /= account.amount
              this
           }
           
           BigDecimal balance() { amount }
        }
        
        
        def account = new BankAccount()
        
        account + new BankAccount(100) 
        println account.balance()
        
        account - new BankAccount(50) 
        println account.balance()
        
        account * new BankAccount(3)
        println account.balance()
        
        account / new BankAccount(50)
        println account.balance()
        """

        when:
        Map result = scriptExecutorService.execute(script)

        then:
        result.output == withEof("100.0\n50.0\n150.0\n3.0")

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
