package com.tokko.brewlog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { c ->
            intent?.let { intent ->
                val koin = (c.applicationContext as BrewLogApplication).getKoin()
                val fireStoreRepository: IFirestoreRepository = koin.get()
                val alarmId = intent.getStringExtra("alarmId") ?: ""
                fireStoreRepository.getAlarm(alarmId) {
                    it.checked = true
                    fireStoreRepository.addAlarm(it)
                }
            }
        }
    }

}