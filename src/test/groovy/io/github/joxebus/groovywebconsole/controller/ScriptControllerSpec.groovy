package io.github.joxebus.groovywebconsole.controller

import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.github.joxebus.groovywebconsole.util.ScriptControllerClient
import io.github.joxebus.groovywebconsole.util.ScriptEndOfFileTrait
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static io.github.joxebus.groovywebconsole.util.FileReaderUtil.getFileContent

class ScriptControllerSpec extends Specification implements ScriptEndOfFileTrait {

    @Shared @AutoCleanup EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
    @Shared ScriptControllerClient client

    def setupSpec(){
        client = embeddedServer.applicationContext.getBean(ScriptControllerClient)
    }

    def "Execute simple script via POST"() {
        given:
        Map params = [code:"println 'hello world'"]

        when:
        def result = client.executeScript(params)

        then:
        result.status == HttpStatus.OK
        result.body.get().output == withEof('hello world')
    }

    def "Execute complex script via POST"() {
        given:
        Map params = [code: getFileContent('test_scripts/bank-account-test.txt')]
        String output = getFileContent('test_scripts/bank-account-test-output.txt')

        when:
        def result = client.executeScript(params)

        then:
        result.status == HttpStatus.OK
        result.body.get().output == withEof(output)
    }

    def "Upload and save script"() {
        given:
        Map params = [code:"println 'Testing upload script'"]

        when:
        def result = client.upload(params)
        FileResponse fileResponse = result.body.get()
        int index = fileResponse.url.lastIndexOf('/') + 1
        String filename = fileResponse.url.substring(index)

        then:
        result.status == HttpStatus.OK
        fileResponse.uploaded
        filename.size() == 36

    }

    def "Download script"() {
        given:
        Map params = [code:"println 'Testing download script'"]

        when:
        def result = client.downloadScript(params)

        then:
        result.status == HttpStatus.OK
        result.body.get() == withEof(params.code)

    }

    def "Return error when operation is not allowed"() {
        when:
        client.executeScript(params)
        then: "We receive in the client a Bad Request"
        def ex = thrown(HttpClientResponseException)
        ex.message.contains("Bad Request")

        where:
        params << [
                [code:'new Thread({ -> println "hey" }).start()'],
                [code:'while (true) { new Thread(() -> println "hola").start() }']
        ]
    }

    def "Return error when operation System.exit it called"() {
        when:
        client.executeScript(params)
        then: "We receive in the client a Bad Request"
        def ex = thrown(HttpClientResponseException)
        ex.message.contains("Bad Request")

        where:
        params << [
                [code:'''def exit = System.&exit(1)'''],
                [code:'System.exit(1)']
        ]
    }
}
