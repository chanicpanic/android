/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.chanicpanic.chanicpanicmobile.R

class PrivacyPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_privacy, rootKey)
        findPreference<Preference>(getString(R.string.PREF_PRIVACY_KEY))?.onPreferenceClickListener = Preference.OnPreferenceClickListener { true }
        findPreference<Preference>(getString(R.string.PREF_TERMS_KEY))?.onPreferenceClickListener = Preference.OnPreferenceClickListener { true }
    }

    override fun onStart() {
        super.onStart()
        (activity as? AppCompatActivity)?.supportActionBar?.run {
            title = getString(R.string.privacy)
            show()
        }
    }
}