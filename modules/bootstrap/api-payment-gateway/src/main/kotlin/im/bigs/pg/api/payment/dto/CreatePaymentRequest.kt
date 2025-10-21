package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import java.math.BigDecimal

@Schema(description = "결제 생성 요청")
data class CreatePaymentRequest(
    @Schema(description = "제휴사 ID(1 이상)", example = "1")
    val partnerId: Long,
    @field:Min(1)
    @Schema(description = "결제 금액(원)", example = "10000")
    val amount: BigDecimal,
    @Schema(description = "카드 BIN(선택)", example = "411111")
    val cardBin: String? = null,
    @Schema(description = "카드 마지막 4자리(선택)", example = "1111")
    val cardLast4: String? = null,
    @Schema(description = "상품명(선택)", example = "무선 이어폰")
    val productName: String? = null,
)
