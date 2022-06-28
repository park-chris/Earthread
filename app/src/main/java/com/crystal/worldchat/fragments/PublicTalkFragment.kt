package com.crystal.worldchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R
import com.crystal.worldchat.adapters.OpenChatAdapter
import com.crystal.worldchat.chats.ChatViewModel
import com.crystal.worldchat.databinding.FragmentViewPagerPublicBinding
import com.crystal.worldchat.datas.Chat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PublicTalkFragment: Fragment() {

    lateinit var binding: FragmentViewPagerPublicBinding
    private lateinit var adapter: OpenChatAdapter
    private lateinit var uid: String
    private var mChats: ArrayList<Chat> = arrayListOf()

    private val chatViewModel by lazy {
        ViewModelProvider(this).get(ChatViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_pager_public, container, false)

        binding.publicChatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = OpenChatAdapter(requireContext(), mChats)
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        uid = Firebase.auth.currentUser!!.uid

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.getOpenChatRooms().observe(
            viewLifecycleOwner, Observer { chats ->

                if (chats.size == 0) {
                    binding.notificationText.visibility = View.VISIBLE
                } else {
                    binding.notificationText.visibility = View.GONE
                    mChats = chats
                    updateUI(mChats)

                }


            }
        )

        setupEvents()
    }

    private fun setupEvents() {
        binding.addRoomFloatButton.setOnClickListener {
            findNavController().navigate(R.id.add_chat_fragment)
        }
    }

    private fun updateUI(chats: ArrayList<Chat>) {

        adapter = OpenChatAdapter(requireContext(), chats)
        binding.publicChatRecyclerView.adapter = adapter


        adapter.setOnItemClickListener(object : OpenChatAdapter.OnItemClickListener {
            override fun onItemClick(view: View, chatId: String) {

                val bundle = bundleOf("participateChatId" to chatId)
                findNavController().navigate(R.id.participate_open_chat_fragment, bundle )
            }
        })

    }

}