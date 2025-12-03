package com.whispergames.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.shreshth.whispergames.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Add the fragment to the container
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()

        // Set up the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Set up listeners for custom actions
            findPreference<Preference>("rate_app")?.setOnPreferenceClickListener {
                openUrl("market://details?id=com.shreshth.whispergames")
                true
            }

            findPreference<Preference>("share_app")?.setOnPreferenceClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Check out Whisper Games!")
                    putExtra(Intent.EXTRA_TEXT, "Challenge your quiet skills with Whisper Games! Download it here: https://play.google.com/store/apps/details?id=com.shreshth.whispergames")
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
                true
            }

            findPreference<Preference>("contact_us")?.setOnPreferenceClickListener {
                val contactIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@whispergames.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback for Whisper Games")
                }
                startActivity(contactIntent)
                true
            }
        }

        private fun openUrl(url: String) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                // Handle case where Play Store is not available
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
