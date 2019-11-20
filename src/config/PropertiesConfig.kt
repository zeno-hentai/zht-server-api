package config

import com.natpryce.konfig.*
import java.io.File


class ZHTApiProperties(private val config: Configuration) {
    fun <T>getProperty(name: String, propertyType: PropertyType<T>): T{
        return config[Key(name, propertyType)]
    }

    private fun <T> lazyPropertyByName(name: String, propertyType: PropertyType<T>) = lazy {
        getProperty(name, propertyType)
    }

    val debugTestProperty by lazyPropertyByName("debug.test.property", stringType)
    val apiAdminSecret by lazyPropertyByName("api.admin.secret", stringType)
    val dbUrl by lazyPropertyByName("db.url", stringType)
    val dbDriver by lazyPropertyByName("db.driver", stringType)
    val dbCreateTables by lazyPropertyByName("db.createTables", booleanType)
    val authSalt by lazyPropertyByName("auth.salt", stringType)
    val logLevel by lazyPropertyByName("log.level", enumType<org.slf4j.event.Level>())
    val fileManagerClass by lazyPropertyByName("service.file.class", stringType)
    val kvServiceClass by lazyPropertyByName("service.kv.class", stringType)
}

val ZHTConfig: ZHTApiProperties by lazy {
    val env = System.getProperty("ZHT_ENV") ?: "default"
    val configFile: String? = System.getenv("ZHT_CONFIG")
    var config: Configuration = ConfigurationProperties.systemProperties() overriding
            EnvironmentVariables()
    if(configFile != null){
        println("Usuing config file: $configFile")
        config = config overriding ConfigurationProperties.fromFile(File(configFile))
    }
    config = config overriding ConfigurationProperties.fromResource("config/$env.properties")
    ZHTApiProperties(config)
}