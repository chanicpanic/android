/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.chanicpanic.chanicpanicmobile.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class CreditsPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_credits, rootKey)
        findPreference<Preference>(getString(R.string.PREF_OPEN_SOURCE_KEY))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source))
            requireContext().startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
            true
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? AppCompatActivity)?.supportActionBar?.run {
            title = getString(R.string.credits)
            show()
        }
    }
}