package io.github.joxebus.groovywebconsole.util

trait ScriptEndOfFileTrait {
    static final String EOF = '\n'

    String withEof(String script) {
        script + EOF
    }
}