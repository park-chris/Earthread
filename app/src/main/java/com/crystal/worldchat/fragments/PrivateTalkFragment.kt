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
import com.crystal.worldchat.adapters.ChatAdapter
import com.crystal.worldchat.chats.ChatViewModel
import com.crystal.worldchat.databinding.FragmentViewPagerPrivateBinding
import com.crystal.worldchat.datas.Chat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class PrivateTalkFragment : Fragment() {

    private lateinit var binding: FragmentViewPagerPrivateBinding
    private lateinit var adapter: ChatAdapter
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

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_view_pager_private,
            container,
            false
        )

        binding.privateChatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ChatAdapter(requireContext(), mChats)
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        uid = Firebase.auth.currentUser!!.uid

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.getChatRooms().observe(
            viewLifecycleOwner, Observer { chats ->
                if (chats.size == 0) {
                    binding.notificationText.visibility = View.VISIBLE
                    updateUI(chats)
                } else {
                        binding.notificationText.visibility = View.GONE
                        mChats = chats
                        updateUI(mChats)

                }


            }
        )

    }

    private fun updateUI(chats: ArrayList<Chat>) {

        adapter = ChatAdapter(requireContext(), chats)
        binding.privateChatRecyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : ChatAdapter.OnItemClickListener {
            override fun onItemClick(view: View, chatId: String) {
                val bundle = bundleOf("chatID" to chatId)
                findNavController().navigate(R.id.chat_detail_fragment, bundle)
            }

        })

    }

}