package io.github.joxebus.groovywebconsole.security

import javax.inject.Singleton
import java.security.Permission

@Singleton
class ScriptSecurityManager extends SecurityManager {

    @Override
    void checkExit(int status) {
        throw new SecurityException("Your script contains operations not allowed")
    }

    @Override
    void checkPermission(Permission perm) {
        // verify permission
    }
}
