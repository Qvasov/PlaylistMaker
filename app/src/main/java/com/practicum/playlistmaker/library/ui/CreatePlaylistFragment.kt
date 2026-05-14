package com.practicum.playlistmaker.library.ui

import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.databinding.FragmentCreatePlaylistBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaylistFragment : Fragment() {

    private val viewModel: CreatePlaylistViewModel by viewModel()

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private var nameTextWatcher: TextWatcher? = null
    private var descriptionTextWatcher: TextWatcher? = null

    private lateinit var completeDialog: MaterialAlertDialogBuilder
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        completeDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(COMPLETE_CREATE_QUESTION)
            .setMessage(LOST_DATA_MESSAGE)
            .setNeutralButton(CANCEL) { dialog, which -> }
            .setPositiveButton(COMPLETE) { dialog, which ->
                findNavController().navigateUp()
            }

        requireActivity().onBackPressedDispatcher.addCallback { showCompleteDialog() }
        binding.backButton.setOnClickListener { showCompleteDialog() }

        val pickImage =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    imageUri = uri
                    Glide.with(requireContext())
                        .load(uri)
                        .transform(
                            CenterCrop(),
                            RoundedCorners((requireContext().resources.displayMetrics.density * 8 + 0.5f).toInt())
                        )
                        .into(binding.addPhotoImageView)
                }
            }
        binding.addPhotoImageView.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        nameTextWatcher = binding.nameEditText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                binding.nameEditTextHint.visibility = View.VISIBLE
                binding.createTextView.isEnabled = true
            } else {
                binding.nameEditTextHint.visibility = View.GONE
                binding.createTextView.isEnabled = false
            }
        }

        descriptionTextWatcher = binding.descriptionEditText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                binding.descriptionEditTextHint.visibility = View.VISIBLE
            } else {
                binding.descriptionEditTextHint.visibility = View.GONE
            }
        }


        binding.createTextView.setOnClickListener {
            val playlistName = binding.nameEditText.text.toString()
            val playListDescription = binding.descriptionEditText.text.toString()
            if (playlistName.isNotEmpty()) {
                viewModel.createPlaylist(playlistName, playListDescription, imageUri)
                showToast(playlistName)
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.nameEditText.removeTextChangedListener(nameTextWatcher)
        binding.descriptionEditText.removeTextChangedListener(descriptionTextWatcher)
        _binding = null
    }

    private fun showCompleteDialog() {
        if (!binding.nameEditText.text.isNullOrEmpty()
            || !binding.descriptionEditText.text.isNullOrEmpty()
            || imageUri != null
        )
            completeDialog.show()
        else {
            findNavController().navigateUp()

        }
    }

    private fun showToast(playlistName: String) {
        Toast.makeText(
            requireContext(),
            String.format(PLAYLIST_SUCCESSFULLY_CREATED_PATTERN, playlistName),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val COMPLETE_CREATE_QUESTION = "Завершить создание плейлиста?"
        private const val LOST_DATA_MESSAGE = "Все несохраненные данные будут потеряны"
        private const val CANCEL = "Отмена"
        private const val COMPLETE = "Завершить"
        private const val PLAYLIST_SUCCESSFULLY_CREATED_PATTERN = "Плейлист %s создан"

    }
}