package im.bigs.pg.external.pg.exception

sealed class PgAuthException(message: String) : RuntimeException(message)
class ApiKeyMissing : PgAuthException("API-KEY 헤더 없음")
class ApiKeyInvalidFormat : PgAuthException("API-KEY 포맷 오류(UUID 아님)")
class ApiKeyUnregistered : PgAuthException("미등록 API-KEY")
class UnauthorizedGeneric : PgAuthException("인증 실패(401)")

data class PgErrorBody(
    val code: Int,
    val errorCode: String,
    val message: String,
    val referenceId: String
)

class PgUnprocessed(val body: PgErrorBody) : RuntimeException(body.message)
