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
import com.crystal.worldchat.R
import com.crystal.worldchat.chats.ChatViewModel
import com.crystal.worldchat.databinding.FragmentParticipateOpenChatBinding
import com.crystal.worldchat.datas.Chat
import com.crystal.worldchat.datas.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ParticipateOpenChatFragment : Fragment() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: FragmentParticipateOpenChatBinding
    private lateinit var mainNavi: BottomNavigationView
    private var chatId: String? = null
    private lateinit var chat: Chat
    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(this).get(ChatViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainNavi = requireActivity().findViewById(R.id.main_navi) as BottomNavigationView
        mainNavi.visibility = View.GONE

        chatId = arguments?.getString("participateChatId")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_participate_open_chat,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.getChatInfo(chatId!!).observe(
            viewLifecycleOwner, Observer { mChat ->

                mChat?.let {

                    chat = mChat
                    updateUI(chat)
                }
            })



        setValues()
        setupEvents()
    }

    private fun setValues() {
    }

    private fun setupEvents() {

        binding.cancelImageButton.setOnClickListener {
            backStack()
        }

        binding.addChatButton.setOnClickListener {
            val user = User()
            user.uid = auth.currentUser!!.uid
            chatViewModel.addChatRoomUser(chat.id, user)

            val bundle = bundleOf("chatID" to chat.id)
            findNavController().popBackStack()
            findNavController().navigate(R.id.chat_detail_fragment, bundle)

        }
    }

    private fun updateUI(chat: Chat) {

        binding.titleText.text = chat.title
        binding.informationText.text = chat.information

        binding.peopleCountText.text = "${chat.users.keys.size}명이 참여중입니다."
    }

    override fun onDestroy() {
        super.onDestroy()

        mainNavi = requireActivity().findViewById(R.id.main_navi)
        mainNavi.visibility = View.VISIBLE
    }

    private fun backStack() {

        mainNavi = requireActivity().findViewById(R.id.main_navi)
        mainNavi.visibility = View.VISIBLE

        findNavController().popBackStack()

    }
}