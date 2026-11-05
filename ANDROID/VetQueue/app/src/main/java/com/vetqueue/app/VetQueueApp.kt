package com.vetqueue.app

import android.app.Application
import com.vetqueue.app.data.AppContainer
import com.vetqueue.app.util.SessionManager

/**
 * Application class. Holds a single shared instance of the database-backed
 * repositories (AppContainer) and the session manager so any screen can
 * reach them via (application as VetQueueApp).
 */
class VetQueueApp : Application() {

    lateinit var container: AppContainer
        private set
    lateinit var session: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        session = SessionManager(this)
        instance = this
    }

    companion object {
        lateinit var instance: VetQueueApp
            private set
    }
}
