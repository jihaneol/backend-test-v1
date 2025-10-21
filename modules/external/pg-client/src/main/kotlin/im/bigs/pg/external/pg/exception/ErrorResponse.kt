package im.bigs.pg.external.pg.exception

data class PgErrorBody(
    val code: Int,
    val errorCode: String,
    val message: String,
    val referenceId: String
)

class PgUnprocessed(val body: PgErrorBody) : RuntimeException(body.message)
