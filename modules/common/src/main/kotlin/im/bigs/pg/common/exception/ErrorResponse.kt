package im.bigs.pg.common.exception

import org.springframework.http.HttpStatus


data class PgErrorBody(
    val code: Int,
    val errorCode: String,
    val message: String,
    val referenceId: String
)

class PgUnprocessed(val body: PgErrorBody) : RuntimeException(body.message)