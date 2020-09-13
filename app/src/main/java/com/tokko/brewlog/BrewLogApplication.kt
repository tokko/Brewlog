package com.tokko.brewlog

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import org.joda.time.DateTime
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class BrewLogApplication : Application(), KodeinAware {
    val scheduler: IScheduler by instance()
    val brewService: IBrewService by instance()
    override fun onCreate() {
        super.onCreate()
        scheduler.schedule()
        //   bootStrap()
    }

    private fun bootStrap() {
        val dryHopAlarmId = "0ffaf6ed-243d-44e9-b800-ef262fd183ed"

        val brew = Brew(
            "Bavarian Mandarin",
            DateTime().withDate(2020, 9, 12).millis,
            mutableListOf(
                DryHopping(
                    DateTime().withDate(2020, 9, 12).plusDays(7).millis,
                    "Bavarian Mandarin",
                    20,
                    false,
                    dryHopAlarmId
                )
            ),
            DateTime().withDate(2020, 9, 12).plusDays(14).millis,
            Long.MAX_VALUE,
            "",
            false,
            "",
            "41f214e8-0e0d-4f18-bfc0-e9e0bdaba331"

        )
        brewService.createBrew(brew)
    }
    override val kodein = Kodein.lazy {
        bind<IFirestoreRepository>() with singleton { FirestoreRepository() }
        bind<IScheduler>() with singleton { Scheduler(instance(), instance(), instance()) }
        bind<AlarmManager>() with provider { this@BrewLogApplication.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
        bind<NotificationManager>() with provider { this@BrewLogApplication.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
        bind<Context>() with singleton { this@BrewLogApplication }
        bind<IBrewService>() with singleton { BrewService(instance(), instance()) }
        bind<Kodein>() with singleton { kodein }
    }

}
