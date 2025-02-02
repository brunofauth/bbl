package org.gnit.bible.cli

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import org.slf4j.LoggerFactory

@Serializable
data class Config(
    val translation: org.gnit.bible.Translation = DEFAULT_TRANSLATION,
    val searchResult: Int = 100,
    val randomlyShow: RandomlyShow = RandomlyShow.verse
)

const val dataDirName = ".bbl"
const val configFileName = "config.json"

fun readConfigFromFileSystem(): Config {
    val configFile =
        File(System.getProperty("user.home") + File.separator + dataDirName + File.separator + configFileName)

    return if (configFile.exists()) {
        logger.debug("config file found at $configFile")
        runCatching { Json.decodeFromString<Config>(configFile.readText()) }
            .onFailure { error -> logger.error("error occurred while trying open and parse config file at $configFile, error: ${error.message}") }
            .getOrDefault(Config())
    } else {
        logger.debug("config file NOT found at $configFile. loading default configuration")
        Config()
    }
}