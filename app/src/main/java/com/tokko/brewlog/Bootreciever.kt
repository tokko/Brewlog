package com.tokko.brewlog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.generic.instance

class Bootreciever : BroadcastReceiver() {
    lateinit var kodein: Kodein
    lateinit var scheduler: IScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        context?.let {
            kodein = (it.applicationContext as BrewLogApplication).kodein
            scheduler = kodein.direct.instance()
            scheduler.schedule()
        }
    }
}