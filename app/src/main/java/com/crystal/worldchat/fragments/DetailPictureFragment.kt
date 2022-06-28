package com.crystal.worldchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.crystal.worldchat.R
import com.crystal.worldchat.databinding.FragmentDetailPictureBinding

class DetailPictureFragment : Fragment() {

    private lateinit var binding: FragmentDetailPictureBinding
    private var imageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUri = arguments?.getString("imageURI")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_picture, container, false)

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupEvents()

    }

    private fun setupEvents() {
        binding.backImageButton.setOnClickListener {
            findNavController().popBackStack()
        }

        if (imageUri != null) {
            Glide.with(requireContext()).load(imageUri).into(binding.imageView)
        }

    }


}