package com.tokko.brewlog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class Bootreciever : BroadcastReceiver() {
    lateinit var scheduler: IScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        context?.let {
            val koin = (context.applicationContext as BrewLogApplication).getKoin()
            scheduler = koin.get()
            scheduler.schedule()
        }
    }
}