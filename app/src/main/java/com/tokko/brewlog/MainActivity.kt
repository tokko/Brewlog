package com.tokko.brewlog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBrewListFragment()
    }

    override fun onSupportNavigateUp(): Boolean {
        showBrewListFragment()
        return true
    }

    fun showBrewListFragment() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewListFragment())
            .commit()

    }

    fun showAddBrewFragment() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewFormFragment())
            .commit()
    }

    fun brewAdded() {
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewListFragment())
            .commit()
    }
}