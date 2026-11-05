package com.vetqueue.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Lightweight helper for Requirement 3.8 (Offline Mode and Synchronisation). */
object NetworkMonitor {
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
