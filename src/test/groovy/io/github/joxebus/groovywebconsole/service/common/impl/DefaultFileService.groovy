package io.github.joxebus.groovywebconsole.service.common.impl


import io.micronaut.context.annotation.Requires

import javax.inject.Singleton

@Singleton
@Requires(env = "test")
class DefaultFileService extends FileServiceLocal {

}
