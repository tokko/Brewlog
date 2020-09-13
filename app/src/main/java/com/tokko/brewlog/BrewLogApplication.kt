package com.tokko.brewlog

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

class BrewLogApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        bind<IFirestoreRepository>() with singleton { FirestoreRepository() }
    }

}
