package io.github.joxebus.groovywebconsole.service

import io.github.joxebus.groovywebconsole.util.ScriptEndOfFileTrait
import io.micronaut.http.server.types.files.SystemFile
import spock.lang.Shared
import spock.lang.Specification

class ScriptFileGeneratorServiceSpec extends Specification implements ScriptEndOfFileTrait {

    @Shared
    ScriptFileGeneratorService scriptFileGeneratorService

    def setupSpec() {
        scriptFileGeneratorService = new ScriptFileGeneratorService()
    }

    def "Test file is generated"() {
        given:
        String script = 'println "Hello world"'

        when:
        SystemFile systemFile = scriptFileGeneratorService.generateGroovyCodeFile(script)

        then:
        systemFile.mediaType.contains('text/plain')
        systemFile.attachmentName == 'script.groovy'
        systemFile.file.bytes
    }

    def "Test file with custom name is generated"() {
        given:
        String script = 'println "Hello world"'

        when:
        SystemFile systemFile = scriptFileGeneratorService.generateGroovyCodeFile(script, "sample")

        then:
        systemFile.mediaType.contains('text/plain')
        systemFile.attachmentName == 'sample.groovy'
        systemFile.file.bytes
    }

    def "Test generated file has the exact content for simple script"() {
        given:
        String script = 'println "Hello world"'

        when:
        SystemFile systemFile = scriptFileGeneratorService.generateGroovyCodeFile(script)

        then:
        systemFile.file.text == withEof(script)
    }

    def "Test generated file has the exact content for complex script"() {
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
        SystemFile systemFile = scriptFileGeneratorService.generateGroovyCodeFile(script)

        then:
        systemFile.file.text == withEof(script)
    }
}
