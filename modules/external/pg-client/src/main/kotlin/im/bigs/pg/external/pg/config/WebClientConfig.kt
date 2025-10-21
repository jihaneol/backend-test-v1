package im.bigs.pg.external.pg.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig(
    private val props: PgProperties
) {

    @Bean
    fun paymentWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(5))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000)
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(5))
                conn.addHandlerLast(WriteTimeoutHandler(5))
            }
            .wiretap(true) // reactor-netty 레벨 로깅 (DEBUG 로 찍힘)

        return WebClient.builder()
            .baseUrl(props.baseUrl) // 필요 시
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("API-KEY", props.clients.apiKey)
            .build()
    }

}