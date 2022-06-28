package com.crystal.worldchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.crystal.worldchat.R
import com.crystal.worldchat.chats.ChatViewModel
import com.crystal.worldchat.databinding.FragmentAddChatBinding
import com.crystal.worldchat.datas.Chat
import com.crystal.worldchat.datas.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class AddChatFragment: Fragment() {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: FragmentAddChatBinding
    private lateinit var mainNavi: BottomNavigationView
    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(this).get(ChatViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainNavi =  requireActivity().findViewById(R.id.main_navi) as BottomNavigationView
        mainNavi.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_chat, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEvents()
    }

    private fun setupEvents() {
        
        binding.addChatButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val information = binding.informationEditText.text.toString()
            
            if (title.isNotEmpty() && information.isNotEmpty()) {
                val chat = Chat()
                val me = User()
                me.uid = auth.currentUser!!.uid
                
                chat.title = title
                chat.opened = true
                chat.information = information

                chatViewModel.addChatRoom(chat)
                chatViewModel.addChatRoomUser(chat.id, me)

                backStack()
                
            } else {
                Toast.makeText(requireContext(), "대화방 이름과 정보를 입력하여 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelImageButton.setOnClickListener { 
            backStack()
        }
        
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