package com.tokko.brewlog

import org.kodein.di.Kodein
import org.kodein.di.KodeinAware

interface IBrewService {
    fun createBrew(brew: Brew)
}

class BrewService(override val kodein: Kodein, val firestoreRepository: IFirestoreRepository) :
    IBrewService, KodeinAware {
    override fun createBrew(brew: Brew) {
        brew.dryhops.forEach { dryHopping ->
            firestoreRepository.addAlarm(
                Alarm(
                    brew.id,
                    dryHopping.date,
                    "Dryhop time!",
                    "Dryhop ${brew.name} with ${dryHopping.amount}g of ${dryHopping.type}"
                ).also {
                    dryHopping.alarmId = it.id
                }
            )
        }
        firestoreRepository.addAlarm(
            Alarm(
                brew.id,
                brew.fermentationTime,
                "Bottling time!",
                "Bottle ${brew.name}"
            ).also { brew.bottledAlarmId = it.id }
        )
        firestoreRepository.addBrew(brew)
    }
}