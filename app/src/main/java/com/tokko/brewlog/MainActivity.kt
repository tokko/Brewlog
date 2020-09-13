package com.tokko.brewlog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewListFragment())
            .commit()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }


    data class EventShowAddBrewFragment(val i: Int = 0)

    @Subscribe
    fun showAddBrewFragment(event: EventShowAddBrewFragment) {
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewFormFragment())
            .commit()
    }
}