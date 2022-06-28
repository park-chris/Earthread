package com.crystal.worldchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.crystal.worldchat.R
import com.crystal.worldchat.adapters.ViewPagerFragmentAdapter
import com.crystal.worldchat.databinding.FragmentTalkBinding
import com.google.android.material.tabs.TabLayoutMediator

class TalkFragment: Fragment() {

    lateinit var binding: FragmentTalkBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_talk, container, false)

        binding.viewPager.adapter = ViewPagerFragmentAdapter(requireActivity())

        val tabTitles = listOf("내 채팅", "오픈 채팅")

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        binding.tabLayout

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





    }

}