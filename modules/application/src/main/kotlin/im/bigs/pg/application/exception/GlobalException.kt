package im.bigs.pg.application.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PgUnauthorized::class)
    fun handlePg401(ex: PgUnauthorized): ResponseEntity<PgErrorBody> {
        // 로깅 등 추가 가능
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.body)
    }
}