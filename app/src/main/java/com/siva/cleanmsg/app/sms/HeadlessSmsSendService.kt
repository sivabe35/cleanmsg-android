package com.siva.cleanmsg.app.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Presence of this service is required for the SMS role; quick-reply sending is out of scope for v1. */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopSelf()
        return START_NOT_STICKY
    }
}
