package im.bigs.pg.external.pg

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import im.bigs.pg.application.exception.PgErrorBody
import im.bigs.pg.application.exception.PgUnauthorized
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.enc.PgEnc
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class PgClient(
    private val webClient: WebClient,
) : PgClientOutPort {
    private val API_KEY = "11111111-1111-4111-8111-111111111111"
    private val IV = "AAAAAAAAAAAAAAAA"

    override fun supports(partnerId: Long): Boolean {
        return true
    }

    override fun approve(request: PgApproveRequest): PgApproveResult {
        val enc = PgEnc.encryptToEnc(API_KEY, IV, toJson(request))

        return webClient.post()
            .uri("/api/v1/pay/credit-card")
            .header("API-KEY", API_KEY)
            .bodyValue(mapOf("enc" to enc))
            .retrieve()
            .onStatus({ it.value() == 401 }) { res ->
                res.bodyToMono(PgErrorBody::class.java)
                    .flatMap { Mono.error(PgUnauthorized(it)) }  // ← 에러 바디를 예외에 담음
            }
            .bodyToMono(PgApproveResult::class.java)
            .block() ?: error("Could not retrieve approved card")
    }

    private fun toJson(request: PgApproveRequest): String {
        val mapper = jacksonObjectMapper()
            .registerKotlinModule()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL) // null 제외(옵션)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 날짜를 ISO 문자열로

        return mapper.writeValueAsString(request)
    }
}