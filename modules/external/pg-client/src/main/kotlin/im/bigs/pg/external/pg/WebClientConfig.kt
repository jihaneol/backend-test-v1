package im.bigs.pg.external.pg

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig {
    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)
    private val BASE_URL = "https://api-test-pg.bigs.im"

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
            .baseUrl(BASE_URL) // 필요 시
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter(requestResponseLoggingFilter()) // 커스텀 로깅
            .build()
    }

    /** 민감헤더 마스킹 포함 요청/응답 로깅 */
    @Bean
    fun requestResponseLoggingFilter(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofRequestProcessor { req ->
            val masked = req.headers().toSingleValueMap()
                .mapValues { (k, v) -> if (k.equals(HttpHeaders.AUTHORIZATION, true)) "***" else v }
            log.debug(" {} {} headers={}", req.method(), req.url(), masked)
            Mono.just(req)
        }.andThen(
            ExchangeFilterFunction.ofResponseProcessor { res ->
                log.debug(" status={} headers={}", res.statusCode(), res.headers().asHttpHeaders())
                Mono.just(res)
            }
        )
}
