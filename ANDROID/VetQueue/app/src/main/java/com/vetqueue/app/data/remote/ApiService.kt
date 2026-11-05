package com.vetqueue.app.data.remote

import com.vetqueue.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Maps directly onto the "Important API Endpoints" table in the Part 1
 * document (Section 5, API and Database Design). Point BuildConfig.API_BASE_URL
 * (app/build.gradle.kts) at your hosted backend - any REST API + database
 * (custom ASP.NET Core / Node.js + PostgreSQL, or a hosted BaaS) that
 * implements these routes will work.
 */
interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("api/pets")
    suspend fun getPets(@Query("userId") userId: String): Response<List<PetDto>>

    @POST("api/pets")
    suspend fun createPet(@Body body: PetDto): Response<PetDto>

    @PUT("api/pets/{id}")
    suspend fun updatePet(@Path("id") id: String, @Body body: PetDto): Response<PetDto>

    @DELETE("api/pets/{id}")
    suspend fun deletePet(@Path("id") id: String): Response<Unit>

    @GET("api/clinics")
    suspend fun getClinics(): Response<List<ClinicDto>>

    @GET("api/appointments")
    suspend fun getAppointments(@Query("userId") userId: String): Response<List<AppointmentDto>>

    @POST("api/appointments")
    suspend fun createAppointment(@Body body: AppointmentDto): Response<AppointmentDto>

    @PUT("api/appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: String, @Body body: AppointmentDto): Response<AppointmentDto>

    @DELETE("api/appointments/{id}")
    suspend fun cancelAppointment(@Path("id") id: String): Response<Unit>

    @GET("api/queues/{clinicId}")
    suspend fun getQueue(@Path("clinicId") clinicId: String): Response<List<QueueEntryDto>>

    @POST("api/queues/join")
    suspend fun joinQueue(@Body body: QueueJoinRequest): Response<QueueEntryDto>

    @DELETE("api/queues/leave")
    suspend fun leaveQueue(@Query("queueId") queueId: String): Response<Unit>

    @GET("api/notifications")
    suspend fun getNotifications(@Query("userId") userId: String): Response<List<NotificationDto>>

    @GET("api/reminders")
    suspend fun getReminders(@Query("userId") userId: String): Response<List<ReminderDto>>

    @POST("api/reminders")
    suspend fun createReminder(@Body body: ReminderDto): Response<ReminderDto>
}
