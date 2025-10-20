package im.bigs.pg.domain.calculation

import im.bigs.pg.domain.enc.PgEnc
import kotlin.test.Test

class 암호화Test {
    @Test
    fun `암호 평문 만들기`() {
        val key = "11111111-1111-4111-8111-111111111111"
        val iv = "AAAAAAAAAAAAAAAA"
        val plaintextJson = """
            {
              "cardNumber": "1111-1111-1111-1111",
              "birthDate": "19900101",
              "expiry": "1224",
              "password": "19",
              "amount": 22200000000000
              }
        """.trimIndent()
        val encryptToEnc = PgEnc.encryptToEnc(key, iv, plaintextJson)

        println(" -> : $encryptToEnc")
    }
}
