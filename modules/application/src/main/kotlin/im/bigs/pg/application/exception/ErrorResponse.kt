package im.bigs.pg.application.exception

data class PgErrorBody(
    val code: Int,
    val errorCode: String,
    val message: String,
    val referenceId: String
)

class PgUnauthorized(val body: PgErrorBody)
    : RuntimeException(body.message)