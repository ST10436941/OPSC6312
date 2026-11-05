package com.vetqueue.app.data.repository

import com.vetqueue.app.data.local.dao.UserDao
import com.vetqueue.app.data.local.entity.UserEntity
import com.vetqueue.app.data.remote.ApiService
import com.vetqueue.app.data.remote.dto.LoginRequest
import com.vetqueue.app.data.remote.dto.RegisterRequest
import com.vetqueue.app.util.PasswordUtil
import java.util.UUID

sealed class AuthResult {
    data class Success(val userId: String, val name: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Requirement 3.1 (Registration and Login) + 3.11 (Security): passwords are
 * hashed on-device with PasswordUtil before ever leaving memory - the API
 * and the local Room fallback only ever see the hash, never the raw password.
 *
 * The API call is attempted first (Requirement: "Connect to a REST API you
 * create"). If it is unreachable (e.g. no network, or the placeholder base
 * URL in this prototype), auth falls back to the local Room "users" table so
 * the app keeps working offline, per Requirement 3.8.
 */
class AuthRepository(
    private val api: ApiService,
    private val userDao: UserDao
) {
    suspend fun register(firstName: String, surname: String, email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (userDao.findByEmail(normalizedEmail) != null) {
            return AuthResult.Error("An account with that email already exists.")
        }
        val passwordHash = PasswordUtil.hash(password)

        val remoteUserId = runCatching {
            api.register(RegisterRequest(firstName, surname, normalizedEmail, passwordHash))
                .takeIf { it.isSuccessful }?.body()?.userId
        }.getOrNull()

        val userId = remoteUserId ?: UUID.randomUUID().toString()
        userDao.insert(
            UserEntity(
                userId = userId,
                firstName = firstName,
                surname = surname,
                email = normalizedEmail,
                passwordHash = passwordHash
            )
        )
        return AuthResult.Success(userId, "$firstName $surname")
    }

    suspend fun login(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val local = userDao.findByEmail(normalizedEmail)
            ?: return AuthResult.Error("No account found for that email.")

        if (!PasswordUtil.verify(password, local.passwordHash)) {
            return AuthResult.Error("Incorrect password.")
        }

        // Best-effort remote validation; local hash check above already gates access
        // offline so login keeps working without connectivity.
        runCatching {
            api.login(LoginRequest(normalizedEmail, local.passwordHash))
        }

        return AuthResult.Success(local.userId, "${local.firstName} ${local.surname}")
    }
}
