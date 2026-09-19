package com.siva.cleanmsg.app.util

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.provider.Telephony
import androidx.core.content.ContextCompat

object SmsRoleUtils {

    fun hasReadSmsPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED

    fun isDefaultSmsApp(context: Context): Boolean =
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

    /**
     * RoleManager (API 29+) is the modern way to request the SMS role; below that,
     * [Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT] is the only available mechanism.
     */
    fun createRequestDefaultSmsAppIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else {
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            }
        }
    }

    /**
     * For the Settings screen's "change/revert default SMS app" action. RoleManager's request
     * intent silently no-ops when the caller already holds the role (verified on-device: the
     * system's RequestRoleActivity finishes immediately with no picker shown), so once we're
     * already default, deep-link to the system's own Default Apps screen instead — the only
     * route that actually lets the user switch away from us.
     */
    fun createChangeDefaultSmsAppIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isDefaultSmsApp(context)) {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        } else {
            createRequestDefaultSmsAppIntent(context)
        }
    }

    fun isSmsRoleAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        val roleManager = context.getSystemService(RoleManager::class.java)
        return roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) ?: false
    }
}
