package com.tokko.brewlog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.kodein.di.direct
import org.kodein.di.generic.instance

class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { c ->
            intent?.let { intent ->
                val kodein = (c.applicationContext as BrewLogApplication).kodein
                val fireStoreRepository: IFirestoreRepository = kodein.direct.instance()
                val alarmId = intent.getStringExtra("alarmId") ?: ""
                fireStoreRepository.getAlarm(alarmId) {
                    it.checked = true
                    fireStoreRepository.addAlarm(it)
                }
            }
        }
    }

}