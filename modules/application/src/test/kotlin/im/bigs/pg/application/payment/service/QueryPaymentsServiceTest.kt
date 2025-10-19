package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentPage
import im.bigs.pg.application.payment.port.out.PaymentSummaryProjection
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.test.*

class 결제조회서비스Test {
    private val paymentRepo = mockk<PaymentOutPort>()
    private lateinit var service: QueryPaymentsService
    private val base = Instant.parse("2024-01-01T00:00:00Z")
    @BeforeEach
    fun init() {
        service = QueryPaymentsService(paymentRepo)
    }

    private fun createPaymentPage(
        cnt: Long = 3, hn: Boolean = false, createAt: LocalDateTime? = null,
        cursorId: Long? = null
    ): PaymentPage {


        val list = mutableListOf<Payment>()
        for (i in cnt downTo 1) {
            list.add(
                createPayment(i, base.plusSeconds(i))
            )
        }

        return PaymentPage(
            items = list,
            hasNext = hn,
            nextCursorCreatedAt = createAt,
            nextCursorId = cursorId
        )
    }

    private fun createPayment(id: Long, instant: Instant): Payment {
        val ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        return Payment(
            id = id,
            partnerId = 1L,
            amount = BigDecimal("1000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("30"),
            netAmount = BigDecimal("970"),
            cardBin = null,
            cardLast4 = "%04d".format(id),
            approvalCode = "A$id",
            approvedAt = ldt,
            status = PaymentStatus.APPROVED,
            createdAt = ldt,
            updatedAt = ldt
        )
    }

    private fun getSummary(items: List<Payment>) =
        PaymentSummaryProjection(
            count = items.size.toLong(),
            totalAmount = items.fold(BigDecimal.ZERO) { acc, p -> acc + p.amount },
            totalNetAmount = items.fold(BigDecimal.ZERO) { acc, p -> acc + p.netAmount },
        )

    @Test
    @DisplayName("커서가 null 일때 -> decode는 null반환 한다.")
    fun cursorIsNull_decode_isNull() {
        val filter = QueryFilter(
            partnerId = 1L,
            status = "APPROVED",
            from = null, to = null, cursor = null,
            limit = 3
        )

        val page = createPaymentPage(cnt = filter.limit.toLong())

        every { paymentRepo.findBy(any()) } returns page
        every { paymentRepo.summary(any()) } returns getSummary(page.items)

        service.query(filter)

        verify { paymentRepo.findBy(withArg {
            assertNull(it.cursorId)
            assertNull(it.cursorCreatedAt)
            assertEquals(it.partnerId, 1L)
            assertEquals(it.status, PaymentStatus.APPROVED)
            assertEquals(it.limit, 3)
        }) }

    }

    @Test
    @DisplayName("hasNext==true면 nextCursor encode를 적용한다.")
    fun hasNextIsTrue_nextCursor_encode() {
        val filter = QueryFilter(
            partnerId = 1L,
            status = "APPROVED",
            from = null, to = null, cursor = null,
            limit = 3
        )

        val last = createPayment(4L, base.plusSeconds(4))
        val page = createPaymentPage(cnt = filter.limit.toLong(),
            hn = true, createAt =  last.createdAt, cursorId = last.id)


        every { paymentRepo.findBy(any()) } returns page
        every { paymentRepo.summary(any()) } returns getSummary(page.items)

        val res = service.query(filter)

        val decoded = String(Base64.getUrlDecoder().decode(res.nextCursor))
        val (millis, idStr) = decoded.split(":", limit = 2)

        assertEquals(last.createdAt.toInstant(ZoneOffset.UTC).toEpochMilli(), millis.toLong())
        assertEquals(last.id, idStr.toLong())

        assertTrue { res.hasNext }
    }

    @Test
    @DisplayName("hasNext=false면 nextCursor는 null이다")
    fun hasNextIsFalse_nextCursor_isNull() {
        val filter = QueryFilter(
            partnerId = 1L,
            status = "APPROVED",
            from = null, to = null, cursor = null,
            limit = 3
        )

        val page = createPaymentPage(cnt = filter.limit.toLong())

        every { paymentRepo.findBy(any()) } returns page
        every { paymentRepo.summary(any()) } returns getSummary(page.items)

        val res = service.query(filter)

        assertFalse { res.hasNext }
        assertNull(res.nextCursor)
    }

    @Test
    @DisplayName("status가 잘못되면 PaymentQuery.status는 null로 응답된다")
    fun statusIsInvalid_status_isNull() {

        val filter = QueryFilter(
            partnerId = 1L,
            status = "INVALID",
            from = null, to = null, cursor = null,
            limit = 3
        )

        val page = createPaymentPage(cnt = filter.limit.toLong())

        every { paymentRepo.findBy(any()) } returns page
        every { paymentRepo.summary(any()) } returns getSummary(page.items)

        service.query(filter)

        verify { paymentRepo.findBy(withArg {
            assertNull(it.status)
        }) }
    }
}