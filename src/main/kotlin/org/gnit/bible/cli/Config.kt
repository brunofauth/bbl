package org.gnit.bible.cli

import java.io.File
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

@Serializable
data class Config(
        val translation: org.gnit.bible.Translation = DEFAULT_TRANSLATION,
        val searchResult: Int = 100,
        val randomlyShow: RandomlyShow = RandomlyShow.verse
)

const val configFileName = "config.json"

fun brokenOs(msg: String): Nothing {
    System.err.println("Os is broken: $msg")
    exitProcess(1)
}

fun getConfigFolderLinux(): File {
    val dataHome: File? =
            System.getenv("XDG_DATA_HOME")?.let { File(it) }
                    ?: System.getenv("HOME")?.let { File(it).resolve("./local/share") }

    when (dataHome) {
        null -> brokenOs("\$HOME is not set")
        else -> return dataHome
    }
}

fun getConfigFolder(): File {
    when (System.getProperty("os.name")?.contains("linux", ignoreCase = true)) {
        true -> return getConfigFolderLinux()
        false -> return File(System.getProperty("user.home")).resolve(".bbl")
        null -> brokenOs("system has no 'os.name'")
    }
}

fun readConfigFromFileSystem(): Config {
    val configFolder = getConfigFolder()
    configFolder.mkdirs()
    val configFile = configFolder.resolve(configFileName)

    if (!configFile.exists()) {
        logger.debug("config file NOT found at $configFile. loading default configuration")
        return Config()
    }

    logger.debug("config file found at $configFile")
    return (runCatching { Json.decodeFromString<Config>(configFile.readText()) }
            .onFailure { error ->
                logger.error(
                        "error occurred while trying open and parse config file at $configFile, error: ${error.message}"
                )
            }
            .getOrDefault(Config()))
}
