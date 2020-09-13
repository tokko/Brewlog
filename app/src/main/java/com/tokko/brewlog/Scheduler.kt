package com.tokko.brewlog

import java.util.*

interface IScheduler {
    fun schedule()
    fun addAlarm(alarm: Alarm)

}

class Scheduler(val firebaseRepository: IFirestoreRepository) : IScheduler {
    override fun schedule() {
    }

    override fun addAlarm(alarm: Alarm) {
        firebaseRepository.addAlarm(alarm)
    }

}

data class Alarm(
    var dateTime: Long = 0,
    var headline: String = "",
    var message: String = "",
    var id: String = UUID.randomUUID().toString()
)