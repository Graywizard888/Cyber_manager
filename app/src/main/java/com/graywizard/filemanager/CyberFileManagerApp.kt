package com.graywizard.filemanager

import android.app.Application
import android.content.Context

class CyberFileManagerApp : Application() {
    
    companion object {
        lateinit var instance: CyberFileManagerApp
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize native libraries
        System.loadLibrary("archive_extractor")
    }
    
    fun getAppContext(): Context = applicationContext
}
