package im.bigs.pg.external.pg

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.common.exception.PgErrorBody
import im.bigs.pg.common.exception.PgUnprocessed
import im.bigs.pg.domain.enc.PgEnc
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import kotlin.jvm.java

@Component
class PgClient(
    private val webClient: WebClient,
) : PgClientOutPort {
    private val API_KEY = "11111111-1111-4111-8111-111111111112"
    private val IV = "AAAAAAAAAAAAAAAA"

    override fun supports(partnerId: Long): Boolean {
        return true
    }

    override fun approve(request: PgApproveRequest): PgApproveResult {

        val enc = PgEnc.encryptToEnc(API_KEY, IV, toJson(testPgRequest(amount = request.amount)))

        return webClient.post()
            .uri("/api/v1/pay/credit-card")
            .header("API-KEY", API_KEY)
            .bodyValue(mapOf("enc" to enc))
            .retrieve()
            .onStatus({ it.value() == 422 }) { res ->
                res.bodyToMono(PgErrorBody::class.java)
                    .flatMap { Mono.error(PgUnprocessed(it)) }
            }
            .bodyToMono(PgApproveResult::class.java)
            .block() ?: error("Could not retrieve approved card")
    }

    private fun toJson(request: testPgRequest): String {
        val mapper = jacksonObjectMapper()
            .registerKotlinModule()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL) // null 제외(옵션)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 날짜를 ISO 문자열로

        return mapper.writeValueAsString(request)
    }
}

/** PG 승인 요청 최소 정보. */
data class testPgRequest(
    val cardNumber: String = "1111-1111-1111-1112",
    val birthDate: String = "19900101",
    val expiry: String = "1227",
    val password: Int = 12,
    val amount: BigDecimal,
)
