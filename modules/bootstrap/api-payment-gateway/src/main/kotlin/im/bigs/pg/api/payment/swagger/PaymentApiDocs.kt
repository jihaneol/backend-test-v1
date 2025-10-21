package im.bigs.pg.api.payment.swagger

import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import im.bigs.pg.api.payment.dto.PaymentResponse
import im.bigs.pg.api.payment.dto.QueryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.apache.coyote.BadRequestException
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

@Tag(name = "Payments", description = "결제 생성 및 조회 API")
interface PaymentApiDocs {

    @Operation(summary = "결제 생성", description = "결제를 생성하고 요약 정보를 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "결제 생성 및 승인 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = PaymentResponse::class,
                        ),
                    )
                ]
            ), ApiResponse(
                responseCode = "400", description = "요청 데이터 유효성 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ProblemDetail::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "401", description = "인증 실패",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                name = "API-KEY 헤더 없음", summary = "API-KEY 헤더 없음"
                            ),
                            ExampleObject(
                                name = "API-KEY 포맷 오류", summary = "API-KEY 포맷 오류"
                            ),
                            ExampleObject(
                                name = "미등록 API-KEY", summary = "미등록 API-KEY"
                            )
                        ]
                    )
                ]
            ), ApiResponse(
                responseCode = "422", description = "PG사 카드 정보 유효성 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                name = "STOLEN_OR_LOST", summary = "도난 또는 분실된 카드입니다.",
                                value = """ { "code":1001, "errorCode":"STOLEN_OR_LOST", "message":"도난 또는 분실된 카드입니다.", "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438", } """
                            ),
                            ExampleObject(
                                name = "INSUFFICIENT_LIMIT", summary = "한도가 초과되었습니다.",
                                value = """ { "code":1002, "errorCode":"INSUFFICIENT_LIMIT", "message":"한도가 초과되었습니다.", "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438", } """
                            ),
                            ExampleObject(
                                name = "EXPIRED_OR_BLOCKED", summary = "정지되었거나 만료된 카드입니다.",
                                value = """ { "code":1003, "errorCode":"EXPIRED_OR_BLOCKED", "message":"정지되었거나 만료된 카드입니다.", "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438", } """
                            ),
                            ExampleObject(
                                name = "TAMPERED_CARD", summary = "위조 또는 변조된 카드입니다.",
                                value = """ { "code":1004, "errorCode":"TAMPERED_CARD", "message":"위조 또는 변조된 카드입니다.", "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438", } """
                            ),
                            ExampleObject(
                                name = "TAMPERED_CARD", summary = "위조 또는 변조된 카드입니다. (허용되지 않은 카드)",
                                value = """ { "code":1005, "errorCode":"TAMPERED_CARD", "message":"위조 또는 변조된 카드입니다. (허용되지 않은 카드)", "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438", } """
                            ),
                        ]
                    )
                ]
            )
        ]
    )
    fun create(@Valid req: CreatePaymentRequest): ResponseEntity<PaymentResponse>

    @Operation(summary = "결제 조회", description = "커서 기반 페이지네이션 + 통계를 포함합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [
                    Content(
                        mediaType = "application/json", schema = Schema(implementation = QueryResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "요청 데이터 유효성 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BadRequestException::class)
                    )
                ]
            )
        ]
    )
    fun query(
        @Parameter(description = "제휴사 ID", example = "1") @Positive partnerId: Long?,
        @Parameter(description = "결제 상태 (APPROVED, CANCELED)", example = "APPROVED") status: String?,
        @Parameter(description = "조회 시작 (yyyy-MM-dd HH:mm:ss)") from: LocalDateTime?,
        @Parameter(description = "조회 종료 (yyyy-MM-dd HH:mm:ss)") to: LocalDateTime?,
        @Parameter(description = "인코딩된 다음 페이지 조회 커서") cursor: String?,
        @Parameter(description = "페이지 크기") @Min(1) limit: Int
    ): ResponseEntity<QueryResponse>
}