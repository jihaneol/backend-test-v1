package im.bigs.pg.external.pg

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.external.pg.exception.PgErrorBody
import im.bigs.pg.external.pg.exception.PgUnprocessed
import im.bigs.pg.external.pg.enc.PgEnc
import im.bigs.pg.external.pg.config.PgProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import kotlin.jvm.java

@Component
class PgClient(
    private val props: PgProperties,
    private val webClient: WebClient,
) : PgClientOutPort {

    override fun supports(partnerId: Long): Boolean {
        return true
    }

    override fun approve(request: PgApproveRequest): PgApproveResult {

        val enc = PgEnc.encryptToEnc(props.clients.apiKey, props.clients.iv, toJson(TestPgRequest(amount = request.amount)))

        return webClient.post()
            .uri("/api/v1/pay/credit-card")
            .bodyValue(mapOf("enc" to enc))
            .retrieve()
            .onStatus({ it.value() == 422 }) { res ->
                res.bodyToMono(PgErrorBody::class.java)
                    .flatMap { Mono.error(PgUnprocessed(it)) }
            }
            .bodyToMono(PgApproveResult::class.java)
            .block() ?: error("Could not retrieve approved card")
    }

    private fun toJson(request: TestPgRequest): String {
        val mapper = jacksonObjectMapper()
            .registerKotlinModule()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL) // null 제외(옵션)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 날짜를 ISO 문자열로

        return mapper.writeValueAsString(request)
    }
}

/** PG 승인 요청 최소 정보. */
data class TestPgRequest(
    val cardNumber: String = "1111-1111-1111-1111",
    val birthDate: String = "19900101",
    val expiry: String = "1227",
    val password: Int = 12,
    val amount: BigDecimal,
)
