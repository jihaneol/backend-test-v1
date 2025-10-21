package im.bigs.pg.api.exception

import im.bigs.pg.external.pg.exception.*
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(PgAuthException::class)
    fun handlePgAuth(ex: PgAuthException): ProblemDetail {
        val pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        pd.detail = ex.message
        // 필요시 커스텀 코드 부여
        pd.setProperty("errorCode", when (ex) {
            is ApiKeyMissing -> "API_KEY_MISSING"
            is ApiKeyInvalidFormat -> "API_KEY_INVALID_FORMAT"
            is ApiKeyUnregistered -> "API_KEY_UNREGISTERED"
            is UnauthorizedGeneric -> "UNAUTHORIZED"
        })
        log.warn("[PG TEST 인증 실패] : {}", ex.message)
        return pd
    }

    @ExceptionHandler(PgUnprocessed::class)
    fun handlePg422(ex: PgUnprocessed): ResponseEntity<PgErrorBody> {
        log.warn("[PG TEST] ErrorCode={}, message={}", ex.body.errorCode, ex.body.message)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBinding(ex: MethodArgumentNotValidException): ProblemDetail {
        log.debug("Validation failure", ex)
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Validation failure", ex.message)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ProblemDetail {
        log.debug("Constraint violation", ex)
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Constraint violation", ex.message)
    }

     @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ProblemDetail {
        log.error("[ILLEGALSTATE error] : {}", ex.message)
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "ILLEGAL_STATE", ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception): ProblemDetail {
        log.error("[Unexpected error] : {}", ex.message)
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex.message?: "Unexpected error")
    }

    private fun createProblemDetail(status: HttpStatus, title: String, message: String?): ProblemDetail {
        return ProblemDetail.forStatus(status).apply {
            this.title = title
            this.detail = message
            this.setProperty("timestamp", LocalDateTime.now())
        }
    }

}
