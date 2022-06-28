package com.crystal.worldchat.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.crystal.worldchat.R
import com.crystal.worldchat.adapters.MessageAdapter
import com.crystal.worldchat.adapters.UserAdapter
import com.crystal.worldchat.chats.ChatViewModel
import com.crystal.worldchat.databinding.FragmentChatDetailBinding
import com.crystal.worldchat.datas.*
import com.crystal.worldchat.fcmServices.FirebaseViewModel
import com.crystal.worldchat.users.UserViewModel
import com.crystal.worldchat.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlin.collections.HashMap

class ChatDetailFragment : Fragment() {

    private val auth = Firebase.auth
    private lateinit var binding: FragmentChatDetailBinding
    private lateinit var chat: Chat
    private lateinit var adapter: MessageAdapter
    private lateinit var userAdapter: UserAdapter
    private lateinit var mainTab: BottomNavigationView
    private lateinit var layoutManager: LinearLayoutManager
    private var imageUri: Uri? = null
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private val chatViewModel by lazy {
        ViewModelProvider(this).get(ChatViewModel::class.java)
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                imageUri = result.data?.data    // 이미지 경로 원본
                Glide.with(this).load(imageUri).into(binding.imageImg)

                binding.imageImg.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "이미지를 불러오질 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainTab = requireActivity().findViewById(R.id.main_navi)
        mainTab.visibility = View.GONE
    }

    override fun onDetach() {
        super.onDetach()
        mainTab.visibility = View.VISIBLE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val chatId = arguments?.getString("chatID")

        chatViewModel.loadChat(chatId!!)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_detail, container, false)

        layoutManager = LinearLayoutManager(context)
        binding.messageRecyclerView.layoutManager = layoutManager

        adapter =
            MessageAdapter(requireContext(), mutableListOf(), auth.currentUser!!.uid, HashMap())
        binding.messageRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.chatLiveData.observe(
            viewLifecycleOwner, Observer { mChat ->
                mChat?.let {
                    chat = mChat
                    updateUI(chat, chat.users)
                }
            }
        )

        setupEvents()

    }

    private fun setupEvents() {

        binding.exitImageButton.setOnClickListener {
            val dialog = RemoveChatDialog(requireContext())
            dialog.setOnOKClickedListener {

                backStack()
                chatViewModel.removeChatRoom(chat)
            }
            dialog.start()
        }

        binding.chatDetailToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_extra -> {

                    if (!binding.drawer.isDrawerOpen(Gravity.RIGHT)) {
                        binding.drawer.openDrawer(Gravity.RIGHT)
                    }
                    true
                }
                else -> false
            }

        }

        binding.imageButton.setOnClickListener {

            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                    val intentImage = Intent(Intent.ACTION_PICK)
                    intentImage.type = "image/*"
                    getContent.launch(intentImage)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(
                        requireContext(),
                        "저장소 접근 권한을 거부하셨습니다. 이미지를 업로드하기 위해서는 해당 권한이 필요합니다.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check()

        }

        binding.messageSendButton.setOnClickListener {

            if (binding.messageEdt.text.isNotEmpty() || imageUri != null) {

                val comment = Comment()
                comment.message = binding.messageEdt.text.toString()
                comment.uid = auth.currentUser!!.uid
                comment.time = UIUtil.timeStampToString()

                val userMap = HashMap<String, Any>()
                for (key in chat.users.keys) {
                    if (key != auth.currentUser!!.uid) {
                        userMap.put(key, false)
                    } else {
                        userMap.put(key, true)
                    }
                }

                comment.readUser = userMap

                if (imageUri != null) {
                    comment.imageUrl = imageUri.toString()
                    binding.imageImg.visibility = View.GONE
                    imageUri = null
                }

                var currentUser: User? = null

                for (key in chat.users.keys) {
                    if (key == auth.currentUser!!.uid) {
                        currentUser = chat.users.getValue(key)
                    }
                }

                chatViewModel.addComment(chat.id, comment)
                binding.messageEdt.text = null

                var messageName: String? = null

                if (chat.title != null) {
                    messageName = chat.title
                } else {
                    messageName = currentUser?.name!!
                }
                val data = NotificationBody.NotificationData(
                    getString(R.string.app_name), messageName!!, comment.message
                )

                for (key in chat.users.keys) {

                    if (key != auth.currentUser!!.uid) {
                        val token = chat.users.getValue(key)?.token
                        if (token != null) {
                            val body = NotificationBody(token, data)
                            firebaseViewModel.sendNotification(body)

                            firebaseViewModel.myResponse.observe(viewLifecycleOwner) {
                            }
                        }
                    }
                }


            }
        }

        binding.backText.setOnClickListener {
            backStack()
        }
    }


    private fun updateUI(chat: Chat, usersMap: HashMap<String, User?>) {
        adapter = MessageAdapter(requireContext(), chat.comments, auth.currentUser!!.uid, usersMap)

        binding.messageRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : MessageAdapter.OnItemClickListener {
            override fun onItemClick(imageURI: String) {
                val bundle = bundleOf("imageURI" to imageURI)
                findNavController().navigate(R.id.detail_picture_fragment, bundle)
            }

        })


        var count = 0

        for (comment in chat.comments) {
            if (comment.readUser.containsKey(auth.currentUser!!.uid)) {
                count += 1
            } else {
                break
            }

        }

        binding.messageRecyclerView.scrollToPosition(adapter.itemCount - 1)

        val users = mutableListOf<User>()
        chat.users.values.forEach {
            users.add(it!!)
        }

        binding.usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userAdapter = UserAdapter(requireContext(), users)
        userAdapter.setOnSelected(object : UserAdapter.OnSelected {
            override fun onSelectedItem(result: Boolean, uid: String) {
                if (uid != auth.currentUser!!.uid) {
                    if (result) {
                        val dialog = SendMessageDialog(requireContext())
                        dialog.setOnOKClickedListener { isChecked ->
                            if (isChecked) {
                                val chat = Chat()
                                val me = User()
                                val other = User()
                                me.uid = auth.currentUser!!.uid
                                other.uid = uid
                                chat.users.put(uid, other)
                                chat.users.put(auth.currentUser!!.uid, me)

                                var checked = false
                                var existedChatId: String? = null

                                chatViewModel.checkRoom()
                                    .observe(viewLifecycleOwner, Observer { mList ->
                                        mList?.let {
                                            for (mChat in mList) {
                                                if (mChat.users.keys.size == 2 && mChat.users.containsKey(
                                                        me.uid
                                                    ) && mChat.users.containsKey(other.uid)
                                                ) {
                                                    checked = true
                                                    existedChatId = mChat.id
                                                }
                                            }
                                            if (checked) {
                                                val bundle = bundleOf("chatID" to existedChatId)
                                                findNavController().popBackStack()
                                                findNavController().navigate(
                                                    R.id.chat_detail_fragment,
                                                    bundle
                                                )
                                            } else {
                                                chatViewModel.addChatRoom(chat)
                                                chatViewModel.addChatRoomUser(chat.id, me)
                                                chatViewModel.addChatRoomUser(chat.id, other)
                                                val bundle = bundleOf("chatID" to chat.id)
                                                findNavController().popBackStack()
                                                findNavController().navigate(
                                                    R.id.chat_detail_fragment,
                                                    bundle
                                                )
                                            }
                                        }
                                    })

                            }
                        }
                        dialog.start(requireContext())

                    } else {
                        val dialog = UserBlockDialog(requireContext())
                        dialog.setOnOKClickedListener { onlyMessage, blockUid ->
                            chatViewModel.addBlock(chat.users.getValue(blockUid)!!, onlyMessage)
                            Toast.makeText(
                                requireContext(),
                                "해당 사용자를 차단하였습니다. 차단한 사용자는 [마이페이지 - 내가 차단한 유저 관리]에서 확인할 수 있습니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        dialog.start(uid)
                    }
                }
            }

        })
        binding.usersRecyclerView.adapter = userAdapter

        binding.usersRecyclerView.addItemDecoration(
            DividerDecoration(
                requireContext(),
                R.drawable.line_divider_black,
                10,
                10
            )
        )

        chatViewModel.updateCommentReadUser(chat.id)

    }

    private fun backStack() {
        chatViewModel.removeRef()
        findNavController().popBackStack()
    }

}