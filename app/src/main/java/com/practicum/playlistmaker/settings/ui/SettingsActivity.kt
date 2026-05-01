package com.practicum.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val COURSE_URL = "https://practicum.yandex.ru"
        const val USER_AGREEMENT_URL = "https://yandex.ru/legal/practicum_offer/"
    }

    private lateinit var binding: ActivitySettingsBinding
    private var viewModel: SettingViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, SettingViewModel.getFactory())
            .get(SettingViewModel::class.java)

        binding.night.isChecked = (applicationContext as App).darkTheme
        binding.night.setOnCheckedChangeListener { _, checked ->
            viewModel?.switchTheme(checked)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, COURSE_URL)
            startActivity(Intent.createChooser(shareIntent, null))
        }

        binding.support.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.emailTo)))
            supportIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject))
            supportIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text))
            startActivity(supportIntent)
        }

        binding.userAgreement.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(USER_AGREEMENT_URL))
            startActivity(shareIntent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}