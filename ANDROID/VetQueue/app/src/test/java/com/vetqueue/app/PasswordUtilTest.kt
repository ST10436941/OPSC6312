package com.vetqueue.app

import com.vetqueue.app.util.PasswordUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers Requirement 3.1 / 3.11: "Passwords are never stored as plain text —
 * they are securely hashed". Runs under Robolectric so android.util.Base64
 * is available on the plain JVM (used by GitHub Actions CI, no emulator needed).
 */
@RunWith(RobolectricTestRunner::class)
class PasswordUtilTest {

    @Test
    fun `hash does not equal the original plain text password`() {
        val stored = PasswordUtil.hash("MySecret123")
        assertNotEquals("MySecret123", stored)
    }

    @Test
    fun `verify succeeds with the correct password`() {
        val stored = PasswordUtil.hash("MySecret123")
        assertTrue(PasswordUtil.verify("MySecret123", stored))
    }

    @Test
    fun `verify fails with an incorrect password`() {
        val stored = PasswordUtil.hash("MySecret123")
        assertFalse(PasswordUtil.verify("WrongPassword", stored))
    }

    @Test
    fun `two hashes of the same password are different due to random salt`() {
        val first = PasswordUtil.hash("MySecret123")
        val second = PasswordUtil.hash("MySecret123")
        assertNotEquals(first, second)
        // ...but both still verify correctly against the same plain text password
        assertTrue(PasswordUtil.verify("MySecret123", first))
        assertTrue(PasswordUtil.verify("MySecret123", second))
    }

    @Test
    fun `verify returns false for a malformed stored value instead of crashing`() {
        assertFalse(PasswordUtil.verify("anything", "not-a-valid-stored-hash"))
    }
}
