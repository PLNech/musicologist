package com.algolia.musicologist

import ai.api.util.BluetoothController
import android.app.Application
import android.util.Log

open class VoiceApplication : Application() {
    private lateinit var bluetoothController: BluetoothController


    private var activitiesCount = 0

    override fun onCreate() {
        super.onCreate()
        bluetoothController = BluetoothControllerImpl(this)
    }

    fun onActivityResume(): Unit {
        if (activitiesCount++ == 0) { // on become foreground
            bluetoothController.start()
        }
    }

    fun onActivityPaused() {
        if (--activitiesCount == 0) { // on become background
            bluetoothController.stop()
        }
    }

    fun isInForeground(): Boolean = activitiesCount > 0
}

private class BluetoothControllerImpl(private val application: VoiceApplication) : BluetoothController(application) {
    companion object {
        const val TAG = "BluetoothController"
    }

    override fun onScoAudioConnected() {
        Log.d(TAG, "Bluetooth sco audio started.")
    }

    override fun onHeadsetDisconnected() {
        Log.d(TAG, "Bluetooth headset disconnected.")
    }

    override fun onHeadsetConnected() {
        Log.d(TAG, "Bluetooth headset connected.")

        if (application.isInForeground() && !isOnHeadsetSco) {
            start()
        }
    }

    override fun onScoAudioDisconnected() {
        Log.d(TAG, "Bluetooth sco audio finished.")
        stop()

        if (application.isInForeground()) {
            start()
        }
    }

    override fun start(): Boolean {
        Log.d(TAG, "Bluetooth controller started listening.")
        return super.start()
    }

    override fun stop() {
        Log.d(TAG, "Bluetooth controller stopped listening.")
        super.stop()
    }
}

