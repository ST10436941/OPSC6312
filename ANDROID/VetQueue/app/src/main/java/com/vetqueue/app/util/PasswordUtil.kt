package com.vetqueue.app.util

import android.util.Base64
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Handles secure password hashing. Passwords are NEVER stored in plain text.
 *
 * Prefers PBKDF2WithHmacSHA256 with a per-user random salt and 10,000
 * iterations. That algorithm is only reliably registered as a
 * SecretKeyFactory provider from API 26 onward though - on older devices
 * (this app's minSdk is 24) requesting it throws NoSuchAlgorithmException,
 * so we fall back to PBKDF2WithHmacSHA1 (available since API 1) in that
 * case. The algorithm actually used is stored alongside the hash so
 * verification always uses the same algorithm the password was hashed
 * with, regardless of which device registered it.
 *
 * Stored format: "algorithm:salt:hash" (salt and hash are Base64), stored
 * in the "Password Hash" column of the ERD in the Part 1 document.
 */
object PasswordUtil {

    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH = 256
    private const val PREFERRED_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val FALLBACK_ALGORITHM = "PBKDF2WithHmacSHA1"

    /** Hashes [password] with a freshly generated salt. Returns "algorithm:salt:hash". */
    fun hash(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val algorithm = resolveAvailableAlgorithm()
        val hash = pbkdf2(password.toCharArray(), salt, algorithm)
        return "$algorithm:${Base64.encodeToString(salt, Base64.NO_WRAP)}:${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }

    /** Verifies [password] against a previously stored "algorithm:salt:hash" value. */
    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 3) return false
        val (algorithm, saltPart, hashPart) = parts
        return try {
            val salt = Base64.decode(saltPart, Base64.NO_WRAP)
            val expectedHash = Base64.decode(hashPart, Base64.NO_WRAP)
            val actualHash = pbkdf2(password.toCharArray(), salt, algorithm)
            actualHash.contentEquals(expectedHash)
        } catch (e: Exception) {
            // Malformed stored value or unavailable algorithm on this device - fail closed.
            false
        }
    }

    /** Picks SHA256 when the device's crypto provider supports it, otherwise SHA1. */
    private fun resolveAvailableAlgorithm(): String {
        return try {
            SecretKeyFactory.getInstance(PREFERRED_ALGORITHM)
            PREFERRED_ALGORITHM
        } catch (e: NoSuchAlgorithmException) {
            FALLBACK_ALGORITHM
        }
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray, algorithm: String): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(algorithm)
        return factory.generateSecret(spec).encoded
    }
}