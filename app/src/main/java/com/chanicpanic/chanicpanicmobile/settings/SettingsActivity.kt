/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame, SettingsFragment())
                .commit()
    }

    override fun onPreferenceDisplayDialog(caller: PreferenceFragmentCompat, pref: Preference?): Boolean {
        val dialog: PreferenceDialogFragmentCompat =
        when (pref) {
            is CardSkinPreference -> CardSkinPreferenceDialog.newInstance(pref.key)
            else -> return false
        }
        dialog.setTargetFragment(caller, 0)
        dialog.show(supportFragmentManager, null)
        return true
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit()
        return true
    }

}