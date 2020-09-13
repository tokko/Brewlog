package com.tokko.brewlog

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

interface IScheduler {
    fun schedule()

}

class Scheduler(
    val context: Context,
    val alarmManager: AlarmManager,
    val firebaseRepository: IFirestoreRepository
) : IScheduler {
    override fun schedule() {
        firebaseRepository.getAlarms {
            it.forEach {
                val intent = Intent(context, AlarmReciever::class.java).apply {
                    action = it.id
                    putExtra("alarmId", it.id)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.cancel(pendingIntent)
                if (!it.checked)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, it.dateTime, pendingIntent)
                else
                    firebaseRepository.deleteAlarm(it.id)
            }
        }
    }
}

data class Alarm(
    var brewId: String = "",
    var dateTime: Long = 0,
    var headline: String = "",
    var message: String = "",
    var id: String = UUID.randomUUID().toString(),
    var checked: Boolean = false
)