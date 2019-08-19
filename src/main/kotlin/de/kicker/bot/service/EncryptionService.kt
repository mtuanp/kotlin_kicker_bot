package de.kicker.bot.service

import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets.US_ASCII
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class EncryptionService {

    private val saltedString = "Salted__"
    private val saltedMagicByteArray = saltedString.toByteArray()
    private val hashAlgorithm = "SHA-256"
    private val aes = "AES"
    private val cipherTransformation = "AES/CBC/PKCS5Padding"

    @Throws(Exception::class)
    fun encrypt(text: String, password: String): String {
        val pass = password.toByteArray(US_ASCII)
        val salt = SecureRandom().generateSeed(8)
        val inBytes = text.toByteArray(UTF_8)

        val passAndSalt = pass + salt
        var hash = ByteArray(0)
        var keyAndIv = ByteArray(0)
        var i = 0
        while (i < 3 && keyAndIv.size < 48) {
            val hashData = hash + passAndSalt
            val md = MessageDigest.getInstance(hashAlgorithm)
            hash = md.digest(hashData)
            keyAndIv += hash
            i++
        }

        val keyValue = Arrays.copyOfRange(keyAndIv, 0, 32)
        val iv = Arrays.copyOfRange(keyAndIv, 32, 48)
        val key = SecretKeySpec(keyValue, aes)

        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val data = (saltedMagicByteArray + salt) + cipher.doFinal(inBytes)
        return Base64.getEncoder().encodeToString(data)
    }

    /**
     * Decrypt encrypted text
     *
     * @param encrypted The encrypted data
     * @param password  Password
     * @return Decrypted text
     */
    @Throws(Exception::class)
    fun decrypt(encrypted: String, password: String): String {
        val pass = password.toByteArray(US_ASCII)

        val inBytes = Base64.getDecoder().decode(encrypted)

        val shouldBeMagic = Arrays.copyOfRange(inBytes, 0, saltedMagicByteArray.size)
        if (!Arrays.equals(shouldBeMagic, saltedMagicByteArray)) {
            throw IllegalArgumentException("Initial bytes from input do not match OpenSSL SALTED_MAGIC salt value.")
        }

        val salt = Arrays.copyOfRange(inBytes, saltedMagicByteArray.size, saltedMagicByteArray.size + 8)

        val passAndSalt = pass + salt

        var hash = ByteArray(0)
        var keyAndIv = ByteArray(0)
        var i = 0
        while (i < 3 && keyAndIv.size < 48) {
            val hashData = hash + passAndSalt
            val md = MessageDigest.getInstance(hashAlgorithm)
            hash = md.digest(hashData)
            keyAndIv += hash
            i++
        }

        val keyValue = Arrays.copyOfRange(keyAndIv, 0, 32)
        val key = SecretKeySpec(keyValue, aes)

        val iv = Arrays.copyOfRange(keyAndIv, 32, 48)

        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val clear = cipher.doFinal(inBytes, 16, inBytes.size - 16)
        return String(clear, UTF_8)
    }


}