package com.practicum.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeNightMode().observe(viewLifecycleOwner) {
            binding.night.isChecked = it
            switchTheme(it)
        }

        binding.night.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun switchTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    companion object {
        const val COURSE_URL = "https://practicum.yandex.ru"
        const val USER_AGREEMENT_URL = "https://yandex.ru/legal/practicum_offer/"
    }
}