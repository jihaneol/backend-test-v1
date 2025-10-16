package im.bigs.pg.api.exception

import im.bigs.pg.common.exception.PgErrorBody
import im.bigs.pg.common.exception.PgUnprocessed
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(PgUnprocessed::class)
    fun handlePg422(ex: PgUnprocessed): ResponseEntity<PgErrorBody> {
        // 로깅 등 추가 가능
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.body)
    }



    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ProblemDetail {
        val pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        pd.title = "ILLEGAL_STATE"
        pd.detail = ex.message
        return pd
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception): ProblemDetail {
        log.error("Unexpected error", ex)
        val pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        pd.title = "UNEXPECTED_ERROR"
        pd.detail = ex.message ?: "Unexpected error"
        return pd
    }

}