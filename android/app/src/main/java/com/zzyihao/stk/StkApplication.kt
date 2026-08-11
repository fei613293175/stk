package com.zzyihao.stk

import android.app.Application
import com.zzyihao.stk.di.AppContainer

class StkApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
