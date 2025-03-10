package com.practicum.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val COURSE_URL = "https://practicum.yandex.ru"
        const val USER_AGREEMENT_URL = "https://yandex.ru/legal/practicum_offer/"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nightSwitcher: Switch
    private lateinit var backButton: FrameLayout
    private lateinit var shareButton: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences(App.PREFERENCES, MODE_PRIVATE)
        nightSwitcher = findViewById(R.id.night)
        backButton = findViewById(R.id.back_button)
        shareButton = findViewById(R.id.share)

        nightSwitcher.isChecked = (applicationContext as App).darkTheme
        nightSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)
            sharedPreferences.edit()
                .putString(App.NIGHT_THEME, (applicationContext as App).darkTheme.toString())
                .apply()
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, COURSE_URL)
            startActivity(Intent.createChooser(shareIntent, null))
        }

        val supportButton = findViewById<FrameLayout>(R.id.support)
        supportButton.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))

            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.emailTo)))
            supportIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject))
            supportIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text))
            startActivity(supportIntent)
        }

        val userAgreementButton = findViewById<FrameLayout>(R.id.user_agreement)
        userAgreementButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(USER_AGREEMENT_URL))
            startActivity(shareIntent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}