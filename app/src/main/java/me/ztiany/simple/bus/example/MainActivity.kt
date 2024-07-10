package me.ztiany.simple.bus.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(DebugTree())
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.act_frag_container, AFragment()).commit()
        }
    }

}