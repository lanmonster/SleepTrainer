package com.lanmon.sleeptrainer.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.lanmon.sleeptrainer.R

class SettingsFragment : PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}
