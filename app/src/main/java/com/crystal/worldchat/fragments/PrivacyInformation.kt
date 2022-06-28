package com.crystal.worldchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crystal.worldchat.R
import com.crystal.worldchat.databinding.FragmentTermsOfServiceBinding

class PrivacyInformation : Fragment() {

    private lateinit var binding: FragmentTermsOfServiceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_terms_of_service, container, false)

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = "개인정보취급방침"

        binding.notificationText.text = getText(R.string.privacy_information)
        setupEvents()

    }

    private fun setupEvents() {
        binding.backImageButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }



}