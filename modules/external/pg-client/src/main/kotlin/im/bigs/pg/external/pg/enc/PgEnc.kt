package im.bigs.pg.external.pg.enc

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object PgEnc {
    private const val GCM_TAG_LEN_BITS = 128 // 16 bytes

    /**
     * 평문 JSON을 AES-256-GCM으로 암호화해 enc(Base64URL(ciphertext||tag)) 반환
     * @param apiKey 문서의 API-KEY(UTF-8)
     * @param ivBase64Url 12바이트 IV(Base64URL, 무패딩)
     * @param plaintextJson 평문 JSON
     */
    fun encryptToEnc(apiKey: String, ivBase64Url: String, plaintextJson: String): String {
        val keyBytes = apiKeyToAesKeyBytes(apiKey) // 32 bytes
        val iv = b64UrlDecodeNoPad(ivBase64Url) // 12 bytes
        require(iv.size == 12) { "IV must be 12 bytes for GCM" }

        return b64UrlEncodeNoPad(encryptAesGcm(keyBytes, iv, plaintextJson))
    }

    /** API-KEY(문자열) → SHA-256 → 32바이트 키 */
    private fun apiKeyToAesKeyBytes(apiKey: String): ByteArray =
        MessageDigest.getInstance("SHA-256")
            .digest(apiKey.toByteArray(StandardCharsets.UTF_8))

    /** Base64URL(무패딩) 디코딩 */
    private fun b64UrlDecodeNoPad(s: String): ByteArray =
        Base64.getUrlDecoder().decode(s)

    /** AES-GCM으로 암호화 */
    private fun encryptAesGcm(keyBytes: ByteArray, iv: ByteArray, plaintextJson: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(keyBytes, "AES"),
            GCMParameterSpec(GCM_TAG_LEN_BITS, iv)
        )
        return cipher.doFinal(plaintextJson.toByteArray(StandardCharsets.UTF_8))
    }

    /** Base64URL(무패딩) 인코딩 */
    private fun b64UrlEncodeNoPad(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}
