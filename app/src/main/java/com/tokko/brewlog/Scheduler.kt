package com.tokko.brewlog

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.joda.time.DateTime
import java.util.*

interface IScheduler {
    fun schedule()

}

class Scheduler(
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val firebaseRepository: IFirestoreRepository
) : IScheduler {
    override fun schedule() {
        firebaseRepository.getAlarms { alarms ->
            alarms.forEach {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    action = it.id
                    putExtra("alarmId", it.id)
                }
                val flags =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    flags
                )
                alarmManager.cancel(pendingIntent)
                if (!it.checked)
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        DateTime(it.dateTime).withTimeAtStartOfDay().millis,
                        pendingIntent
                    )
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
) {
    fun validate() = headline.isNotBlank() || message.isNotBlank()
}