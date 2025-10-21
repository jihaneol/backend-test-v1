package im.bigs.pg.external.pg.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "pg.test")
data class PgProperties(
    val baseUrl: String,
    val clients: ClientProps
) {
    data class ClientProps(
        val apiKey: String,
        val iv: String
    )
}