package com.siva.cleanmsg.app

import android.app.Application
import com.siva.cleanmsg.app.di.AppContainer
import com.siva.cleanmsg.app.di.DefaultAppContainer

class CleanMsgApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
