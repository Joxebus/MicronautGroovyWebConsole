package io.github.joxebus.groovywebconsole.security

import io.github.joxebus.groovywebconsole.exception.ScriptSecurityException


class ScriptSecurityClassLoader extends GroovyClassLoader {

    private static String[] CLASSES_NOT_ALLOWED = [
            "java.lang.Thread",
            "java.lang.Runnable"
    ]

    ScriptSecurityClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    Class<?> loadClass(String name) throws ClassNotFoundException    {
        isClassAllowed(name)
        return super.loadClass(name)
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        isClassAllowed(name)
        return super.loadClass(name, resolve)
    }

    private isClassAllowed(String name) {
        CLASSES_NOT_ALLOWED.each {className ->
            if(name.startsWith(className)) {
                throw new ScriptSecurityException("The class [$name] is not allowed on this context.")
            }
        }
    }

}
