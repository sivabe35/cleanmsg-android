package com.siva.cleanmsg.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Presence of this receiver is required for the SMS role; MMS is out of scope for v1. */
class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
