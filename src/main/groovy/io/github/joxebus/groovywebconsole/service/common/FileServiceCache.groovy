package io.github.joxebus.groovywebconsole.service.common


import groovy.util.logging.Slf4j

@Slf4j
trait FileServiceCache {

    String fileCacheFolder

    void initCache() {
        File file = new File(fileCacheFolder)
        log.info("Using folder [$fileCacheFolder] to cache files")
        if(!file.exists()) {
            log.warn("File folder not found, trying to create new folder")
            if(!file.mkdirs()) {
                log.error("File cache folder cannot be created")
                System.exit(1)
            }
            log.info("File folder created in location: [$fileCacheFolder]")
        }
    }

    void saveToCache(String filename, byte[] response) {
        log.debug("Saving copy on cache for file ${filename}")
        FileOutputStream fos = new FileOutputStream(new File(fileCacheFolder, filename))
        fos.write(response)
        fos.close()
    }
}
