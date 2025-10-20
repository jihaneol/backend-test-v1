package im.bigs.pg.api

import com.jayway.jsonpath.JsonPath
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.infra.persistence.payment.entity.PaymentEntity
import im.bigs.pg.infra.persistence.payment.repository.PaymentJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PaymentControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val paymentRepo: PaymentJpaRepository,
) {
    private val base = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    private val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @BeforeEach
    fun setUp() {

        paymentRepo.deleteAllInBatch()
        val p1 = PaymentEntity(
            partnerId = 1L,
            amount = BigDecimal("10000.00"),
            appliedFeeRate = BigDecimal("0.03"),
            feeAmount = BigDecimal("300.00"),
            netAmount = BigDecimal("9700.00"),
            cardBin = "123456",
            cardLast4 = "7890",
            approvalCode = "A12345",
            approvedAt = base.plusSeconds(1).toInstant(ZoneOffset.UTC),
            status = PaymentStatus.APPROVED.name,
            createdAt = base.plusSeconds(1).toInstant(ZoneOffset.UTC),
            updatedAt = base.plusSeconds(1).toInstant(ZoneOffset.UTC),
        )
        val p2 = PaymentEntity(
            partnerId = 1L,
            amount = BigDecimal("10000.00"),
            appliedFeeRate = BigDecimal("0.03"),
            feeAmount = BigDecimal("300.00"),
            netAmount = BigDecimal("9700.00"),
            cardBin = "123456",
            cardLast4 = "7890",
            approvalCode = "A12345",
            approvedAt = base.plusSeconds(2).toInstant(ZoneOffset.UTC),
            status = PaymentStatus.APPROVED.name,          // enum 값 (예: APPROVED, FAILED, REFUNDED)
            createdAt = base.plusSeconds(2).toInstant(ZoneOffset.UTC),
            updatedAt = base.plusSeconds(2).toInstant(ZoneOffset.UTC),
        )
        val p3 = PaymentEntity(
            partnerId = 1L,
            amount = BigDecimal("10000.00"),
            appliedFeeRate = BigDecimal("0.03"),
            feeAmount = BigDecimal("300.00"),
            netAmount = BigDecimal("9700.00"),
            cardBin = "123456",
            cardLast4 = "7890",
            approvalCode = "A12345",
            approvedAt = base.plusSeconds(3).toInstant(ZoneOffset.UTC),
            status = PaymentStatus.APPROVED.name,          // enum 값 (예: APPROVED, FAILED, REFUNDED)
            createdAt = base.plusSeconds(3).toInstant(ZoneOffset.UTC),
            updatedAt = base.plusSeconds(3).toInstant(ZoneOffset.UTC),
        )
        val p4 = PaymentEntity(
            partnerId = 1L,
            amount = BigDecimal("10000.00"),
            appliedFeeRate = BigDecimal("0.03"),
            feeAmount = BigDecimal("300.00"),
            netAmount = BigDecimal("9700.00"),
            cardBin = "123456",
            cardLast4 = "7890",
            approvalCode = "A12345",
            approvedAt = base.plusSeconds(4).toInstant(ZoneOffset.UTC),
            status = PaymentStatus.APPROVED.name,          // enum 값 (예: APPROVED, FAILED, REFUNDED)
            createdAt = base.plusSeconds(4).toInstant(ZoneOffset.UTC),
            updatedAt = base.plusSeconds(4).toInstant(ZoneOffset.UTC),
        )

        paymentRepo.saveAll(listOf(p1, p2, p3, p4))
        paymentRepo.flush()
    }
    @Test
    @DisplayName("from to 경계 이상일때 400 반환")
    fun whenFromNotBeforeTo_then400() {
        val from = base
        val to   =  base.minusSeconds(5)// 뒤집힘

        mockMvc.get("/api/v1/payments") {
            accept = MediaType.APPLICATION_JSON
            param("partnerId", "1")
            param("status", "APPROVED")
            param("from", dtf.format(from))
            param("to", dtf.format(to))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @DisplayName("limit 3 조회 시 마지막 값 cursor 반환")
    fun limit3_query_cursor_return() {
        // when & then
        val mvcResult = mockMvc.get("/api/v1/payments") {
            accept = MediaType.APPLICATION_JSON
            param("partnerId", "1")
            param("status", "APPROVED")
            param("from", dtf.format(base))
            param("to", dtf.format(base.plusDays(1)))
            param("limit", "3")
        }
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.items.length()") { value(3) }
                jsonPath("$.hasNext") { value(true) }
                jsonPath("$.nextCursor") { exists() }
                jsonPath("$.summary.count") { value(4) }          // 저장 총 건수
                jsonPath("$.summary.totalAmount") { value(40000) }
            }
            .andReturn()

        // nextCursor 디코딩 검증 (createdAtMillis:id 형식 가정)
        val body = mvcResult.response.contentAsString
        val nextCursor = JsonPath.read<String>(body, "$.nextCursor")
        val decoded = String(java.util.Base64.getUrlDecoder().decode(nextCursor))
        val (millis, idStr) = decoded.split(":", limit = 2)

        // 마지막 row(createdAt=base+2초)의 millis 비교
        val expectedMillis = base.plusSeconds(2).atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(expectedMillis, millis.toLong())
        assertTrue(idStr.toLong() > 0)
    }

    @Test
    @DisplayName("마지막 커서 사용시 hasNext=false, cursor = null 반환")
    fun lastCursor_query_return() {
        // when & then
         mockMvc.get("/api/v1/payments") {
            accept = MediaType.APPLICATION_JSON
            param("partnerId", "1")
            param("status", "APPROVED")
            param("from", dtf.format(base))
            param("to", dtf.format(base.plusDays(1)))
            param("cursor", "MTczNTY4OTYwMjAwMDoy")
            param("limit", "3")
        }
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.items.length()") { value(1) }
                jsonPath("$.hasNext") { value(false) }
                jsonPath("$.nextCursor") { doesNotExist()}
            }

    }

}