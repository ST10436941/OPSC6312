
package com.vetqueue.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class VetQueueSQLiteHelper(context: Context) :
    SQLiteOpenHelper(
        context.applicationContext,
        "vetqueue_native.db",
        null,
        1
    ) {

    private val _changes = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 20
    )

    val changes = _changes.asSharedFlow()

    fun notifyChanged() {
        _changes.tryEmit(Unit)
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE users (
                userId TEXT PRIMARY KEY NOT NULL,
                firstName TEXT NOT NULL,
                surname TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                passwordHash TEXT NOT NULL,
                language TEXT NOT NULL DEFAULT 'English',
                notificationsEnabled INTEGER NOT NULL DEFAULT 1
            )
        """)

        db.execSQL("""
            CREATE TABLE pets (
                petId TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                name TEXT NOT NULL,
                species TEXT NOT NULL,
                breed TEXT DEFAULT '',
                dateOfBirth TEXT DEFAULT '',
                gender TEXT DEFAULT '',
                weight REAL DEFAULT 0,
                notes TEXT DEFAULT '',
                syncStatus TEXT DEFAULT 'synced'
            )
        """)

        db.execSQL("""
            CREATE TABLE clinics (
                clinicId TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                contact TEXT NOT NULL,
                hours TEXT NOT NULL,
                services TEXT NOT NULL,
                currentQueueCount INTEGER DEFAULT 0,
                estWaitMinutes INTEGER DEFAULT 0
            )
        """)

        db.execSQL("""
            CREATE TABLE appointments (
                appointmentId TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                petId TEXT NOT NULL,
                clinicId TEXT NOT NULL,
                service TEXT NOT NULL,
                date TEXT NOT NULL,
                time TEXT NOT NULL,
                status TEXT DEFAULT 'upcoming',
                syncStatus TEXT DEFAULT 'pending'
            )
        """)

        db.execSQL("""
            CREATE TABLE queue_entries (
                queueId TEXT PRIMARY KEY NOT NULL,
                clinicId TEXT NOT NULL,
                userId TEXT NOT NULL,
                petId TEXT NOT NULL,
                queueNumber INTEGER NOT NULL,
                position INTEGER NOT NULL,
                estWaitMinutes INTEGER NOT NULL,
                status TEXT DEFAULT 'waiting'
            )
        """)

        db.execSQL("""
            CREATE TABLE reminders (
                reminderId TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                petId TEXT NOT NULL,
                type TEXT NOT NULL,
                dueDate TEXT NOT NULL,
                status TEXT DEFAULT 'pending',
                syncStatus TEXT DEFAULT 'pending'
            )
        """)

        db.execSQL("""
            CREATE TABLE notifications (
                notificationId TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                message TEXT NOT NULL,
                type TEXT NOT NULL,
                read INTEGER DEFAULT 0,
                createdAt INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE achievements (
                achievementId TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE user_achievements (
                userId TEXT NOT NULL,
                achievementId TEXT NOT NULL,
                unlockedAt INTEGER NOT NULL,
                PRIMARY KEY (userId, achievementId)
            )
        """)

        db.execSQL("CREATE INDEX idx_pets_user ON pets(userId)")
        db.execSQL("CREATE INDEX idx_appointments_user ON appointments(userId)")
        db.execSQL("CREATE INDEX idx_reminders_user ON reminders(userId)")
        db.execSQL("CREATE INDEX idx_notifications_user ON notifications(userId)")
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Future database changes must use non-destructive migrations.
    }
}
