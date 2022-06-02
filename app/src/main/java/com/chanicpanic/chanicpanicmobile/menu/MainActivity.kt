/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.menu

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.chanicpanic.chanicpanicmobile.R

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        findNavController(R.id.fragment).addOnDestinationChangedListener {_, destination, _ ->
            if (destination.id == R.id.mainMenuFragment) {
                window.decorView.systemUiVisibility = SYSTEM_UI or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            } else {
                window.decorView.systemUiVisibility = SYSTEM_UI
            }
        }
    }

    companion object {
        private const val SYSTEM_UI =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
}
