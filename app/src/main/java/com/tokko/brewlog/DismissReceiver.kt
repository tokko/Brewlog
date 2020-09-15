package com.tokko.brewlog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.kodein.di.direct
import org.kodein.di.generic.instance

class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { context ->
            intent?.let { intent ->
                val kodein = (context.applicationContext as BrewLogApplication).kodein
                val firestoreRepository: IFirestoreRepository = kodein.direct.instance()
                val alarmId = intent.getStringExtra("alarmId")
                firestoreRepository.getAlarm(alarmId) {
                    it.checked = true
                    firestoreRepository.addAlarm(it)
                }
            }
        }
    }

}