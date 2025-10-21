package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "결제 조회 응답(목록 + 요약 + 커서)")
data class QueryResponse(
    @Schema(description = "결제 목록")
    val items: List<PaymentResponse>,
    @Schema(description = "요약 통계")
    val summary: Summary,
    @Schema(description = "다음 페이지 커서(Base64URL)", example = "eyJjcmVhdGVkQXQiOiIyMDI1LTEwLTA4IDA0OjA1OjEyIiwiaWQiOjEwMDJ9")
    val nextCursor: String?,
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,
)

@Schema(description = "요약 통계")
data class Summary(
    @Schema(description = "총 건수", example = "42")
    val count: Long,
    @Schema(description = "총 결제 금액(원)", example = "420000")
    val totalAmount: BigDecimal,
    @Schema(description = "총 정산 금액(원)", example = "409500")
    val totalNetAmount: BigDecimal,
)
