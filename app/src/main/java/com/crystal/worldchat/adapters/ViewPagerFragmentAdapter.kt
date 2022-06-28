package com.crystal.worldchat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.crystal.worldchat.fragments.PrivateTalkFragment
import com.crystal.worldchat.fragments.PublicTalkFragment

class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf(PrivateTalkFragment(), PublicTalkFragment())

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }


}