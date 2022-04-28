package com.tokko.brewlog

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.joda.time.DateTime
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

class BrewLogApplication : Application(), KoinComponent {
    private val scheduler: IScheduler by inject()
    private val brewService: IBrewService by inject()
    var bootreciever: Bootreciever? = null
    var alarmReceiver: AlarmReceiver? = null
    var dismissReceiver: DismissReceiver? = null
    override fun onCreate() {
        super.onCreate()
        startKoin {
            //  androidLogger()
            androidContext(this@BrewLogApplication)
            modules(appModule)
        }
        scheduler.schedule()
        if (bootreciever == null) {
            bootreciever = Bootreciever()
            registerReceiver(bootreciever, IntentFilter(Intent.ACTION_BOOT_COMPLETED))
        }
        if (alarmReceiver == null) {
            alarmReceiver = AlarmReceiver()
            registerReceiver(alarmReceiver, IntentFilter())
        }
        if (dismissReceiver == null) {
            dismissReceiver = DismissReceiver()
            registerReceiver(dismissReceiver, IntentFilter())
        }
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
            null,
            "41f214e8-0e0d-4f18-bfc0-e9e0bdaba331"
        )
        brewService.createBrew(brew)
        val brew2 = Brew(
            "Styrian Wolf",
            DateTime().withDate(2020, 9, 1).millis,
            mutableListOf(),
            DateTime().withDate(2020, 9, 1).plusDays(14).millis,
            Long.MAX_VALUE,
            "",
            false,
            "",
            null,
            "f61f057d-6585-42c6-9ead-2bb6ce788d9e"

        )
        brewService.createBrew(brew2)
    }
}

val appModule = module {
    single<IFirestoreRepository> { FirestoreRepository() }
    single<IScheduler> { Scheduler(get(), get(), get()) }
    factory { androidApplication().getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    factory { androidApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    single<Context> { androidApplication().applicationContext }
    single<IBrewService> { BrewService(get()) }

    viewModel { BrewListViewModel(get()) }
}
