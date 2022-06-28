package com.crystal.worldchat.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crystal.worldchat.R
import com.crystal.worldchat.adapters.BoardImageAdapter
import com.crystal.worldchat.adapters.ReplyAdapter
import com.crystal.worldchat.boards.BoardViewModel
import com.crystal.worldchat.chats.ChatViewModel
import com.crystal.worldchat.databinding.FragmentBoardDetailBinding
import com.crystal.worldchat.datas.*
import com.crystal.worldchat.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class BoardDetailFragment : Fragment() {

    private val auth = Firebase.auth
    private lateinit var board: Board
    private lateinit var mainTab: BottomNavigationView
    private lateinit var binding: FragmentBoardDetailBinding

    private val boardViewModel: BoardViewModel by lazy {
        ViewModelProvider(this).get(BoardViewModel::class.java)
    }

    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(this).get(ChatViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainTab = requireActivity().findViewById(R.id.main_navi)
        mainTab.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val boardId = arguments?.getString("boardID")
        boardViewModel.loadBoard(boardId!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_board_detail, container, false)

        binding.imageRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.boardDetailRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.boardDetailRecyclerView.addItemDecoration(
            DividerDecoration(
                requireContext(),
                R.drawable.line_divider,
                5,
                5
            )
        )

        setupEvents()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boardViewModel.boardLiveData.observe(
            viewLifecycleOwner, Observer { board ->
                if (board?.name != null) {
                    board.let {
                        this.board = board
                        updateUI()
                    }
                } else {
                    val dialog = NotificationDialog(requireContext())
                    dialog.on()
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.off()
                        backStack()
                    }, 2000)

                }
            }
        )
    }

    private fun setupEvents() {

        binding.modifyReplyCancelButton.setOnClickListener {
            hideKeyboard()
            binding.replyLayout.visibility = View.GONE
        }

        binding.reportText.setOnClickListener {

            val dialog = ReportDialog(requireContext())

            dialog.setOnOKClickedListener { content ->
                val report = Report()
                report.boardID = board.id
                report.content = content
                report.userId = auth.currentUser?.uid
                boardViewModel.addReportedBoard(report)

                Toast.makeText(requireContext(), "신고가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            }
            dialog.start(requireContext())

        }

        binding.removeText.setOnClickListener {

            val dialog = RemoveDialog(requireContext())

            dialog.setOnOKClickedListener {
                if (board.imageUrlList.isNotEmpty()) {
                    boardViewModel.removeStorageBoardImage(board)
                }
                boardViewModel.removeBoard(board)

                Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                backStack()
            }
            dialog.start()

        }

        binding.modifyText.setOnClickListener {
            val bundle = bundleOf("boardID" to board.id)
            findNavController().popBackStack()
            if (!findNavController().popBackStack()) {
                findNavController().navigate(R.id.add_fragment, bundle)
            }
        }

        binding.replyRegisterText.setOnClickListener {

            if (binding.replyEdt.text.isNotEmpty()) {
                val mReply = Reply()
                mReply.userId = auth.currentUser!!.uid
                mReply.content = binding.replyEdt.text.toString()
                mReply.date = UIUtil.timeStampToString()
                mReply.name = ContextUtil.getUserName(requireContext())!!

                boardViewModel.addReply(board, mReply)
                board.replyCount += 1
                boardViewModel.updateBoard(board, "replyCount")
                board.replyList.add(mReply)

                updateUI()

                binding.replyEdt.setText("")

                hideKeyboard()

            }

        }

        binding.backText.setOnClickListener {
            backStack()
        }

        binding.boardDetailToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_block -> {
                    if (board.userId != auth.currentUser!!.uid) {
                        val dialog = UserBlockDialog(requireContext())
                        dialog.setOnOKClickedListener { onlyMessage, blockUid ->
                            val putUser = User()
                            putUser.uid = board.userId
                            putUser.name = board.name
                            chatViewModel.addBlock(putUser, onlyMessage)
                            Toast.makeText(
                                requireContext(),
                                "해당 사용자를 차단하였습니다. 차단한 사용자는 [마이페이지 - 내가 차단한 유저 관리]에서 확인할 수 있습니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        dialog.start(board.userId)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "해당 게시물은 본인 게시물입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    true
                }
                R.id.menu_chat -> {
                    val dialog = SendMessageDialog(requireContext())
                    dialog.setOnOKClickedListener { result ->
                        if (result) {
                            if (board.userId != auth.currentUser!!.uid) {
                                val chat = Chat()
                                val me = User()
                                val other = User()
                                me.uid = auth.currentUser!!.uid
                                other.uid = board.userId
                                chat.users.put(board.userId, other)
                                chat.users.put(auth.currentUser!!.uid, me)

                                var checked = false
                                var existedChatId: String? = null

                                chatViewModel.checkRoom().observe(viewLifecycleOwner, Observer { mList ->
                                    mList?.let {
                                        for (mChat in mList) {
                                            if (mChat.users.keys.size == 2 && mChat.users.containsKey(me.uid) && mChat.users.containsKey(other.uid)) {
                                                checked = true
                                                existedChatId = mChat.id
                                            }
                                        }
                                        if (checked) {
                                            val bundle = bundleOf("chatID" to existedChatId)
                                            findNavController().popBackStack()
                                            findNavController().navigate(R.id.chat_detail_fragment, bundle)
                                        } else {
                                            chatViewModel.addChatRoom(chat)
                                            chatViewModel.addChatRoomUser(chat.id, me)
                                            chatViewModel.addChatRoomUser(chat.id, other)
                                            val bundle = bundleOf("chatID" to chat.id)
                                            findNavController().popBackStack()
                                            findNavController().navigate(R.id.chat_detail_fragment, bundle)
                                        }
                                    }
                                })
                            } else {
                                Toast.makeText(requireContext(), "자신과는 채팅방을 만들 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    dialog.start(requireContext())


                    true
                }
                R.id.menu_favorite -> {
                    board.favorite = if (board.favorite) {
                        it.setIcon(R.drawable.ic_favorite)
                        boardViewModel.removeFavoriteBoard(board)
                        false
                    } else {
                        it.setIcon(R.drawable.ic_favorite_yellow)
                        boardViewModel.addFavoriteBoard(board)
                        true
                    }
                    true
                }

                else -> false
            }
        }



        binding.likeButton.setOnClickListener {

            if (board.isLikeDislikeClicked != null) {

                if (board.isLikeDislikeClicked!!) {
                    binding.likeButton.setImageResource(R.drawable.ic_like)
                    board.likeCount -= 1
                    board.isLikeDislikeClicked = null
                    binding.likeText.text = board.likeCount.toString()
                    boardViewModel.removeLikeDislikeBoard(board)
                } else {
                    board.isLikeDislikeClicked = true
                    binding.likeButton.setImageResource(R.drawable.ic_like_red)
                    binding.dislikeButton.setImageResource(R.drawable.ic_dislike)
                    board.dislikeCount += 1
                    board.likeCount += 1
                    board.isLikeDislikeClicked = true
                    binding.likeText.text = board.likeCount.toString()
                    binding.dislikeText.text = board.dislikeCount.toString()
                    boardViewModel.updateLikeDislikeBoard(board, true)
                }

            } else {
                binding.likeButton.setImageResource(R.drawable.ic_like_red)
                board.likeCount += 1
                board.isLikeDislikeClicked = true
                binding.likeText.text = board.likeCount.toString()
                boardViewModel.updateLikeDislikeBoard(board, true)
            }
            boardViewModel.updateBoard(board, "dislikeCount")
            boardViewModel.updateBoard(board, "likeCount")

        }

        binding.dislikeButton.setOnClickListener {

            if (board.isLikeDislikeClicked != null) {
                if (board.isLikeDislikeClicked!!) {
                    binding.dislikeButton.setImageResource(R.drawable.ic_dislike_blue)
                    binding.likeButton.setImageResource(R.drawable.ic_like)
                    board.likeCount -= 1
                    board.dislikeCount -= 1
                    board.isLikeDislikeClicked = false
                    binding.likeText.text = board.likeCount.toString()
                    binding.dislikeText.text = board.dislikeCount.toString()
                    boardViewModel.updateLikeDislikeBoard(board, false)
                } else {
                    binding.dislikeButton.setImageResource(R.drawable.ic_dislike)
                    board.isLikeDislikeClicked = null
                    board.dislikeCount += 1
                    binding.dislikeText.text = board.dislikeCount.toString()
                    boardViewModel.removeLikeDislikeBoard(board)
                }
            } else {
                binding.dislikeButton.setImageResource(R.drawable.ic_dislike_blue)
                board.dislikeCount -= 1
                board.isLikeDislikeClicked = false
                binding.dislikeText.text = board.dislikeCount.toString()
                boardViewModel.updateLikeDislikeBoard(board, false)
            }
            boardViewModel.updateBoard(board, "dislikeCount")
            boardViewModel.updateBoard(board, "likeCount")
        }


        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (binding.replyEdt.text.toString() != "") {
                    binding.replyRegisterText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                } else {
                    binding.replyRegisterText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.light_gray
                        )
                    )
                }
            }
        }

        binding.replyEdt.addTextChangedListener(textWatcher)

    }

    override fun onDestroy() {
        super.onDestroy()

        mainTab.visibility = View.VISIBLE
    }

    private fun backStack() {
        mainTab.visibility = View.VISIBLE
        findNavController().popBackStack()
    }

    private fun updateUI() {

        if (board.userId == auth.currentUser!!.uid) {
            binding.reportText.visibility = View.GONE
            binding.modifyText.visibility = View.VISIBLE
            binding.removeText.visibility = View.VISIBLE
        }

        val authUser = auth.currentUser!!.uid
        val adapter = ReplyAdapter(requireContext(), board.replyList, authUser)
        binding.boardDetailRecyclerView.adapter = adapter

        adapter.setOnModified(object : ReplyAdapter.OnModified {
            override fun onItemModified(reply: Reply) {
                binding.replyLayout.visibility = View.VISIBLE
                binding.modifyReplyEdt.setText(reply.content)

                binding.modifyReplyRegisterButton.setOnClickListener {
                    reply.content = binding.modifyReplyEdt.text.toString()
                    boardViewModel.addReply(board, reply)
                    Toast.makeText(requireContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show()
                    binding.replyLayout.visibility = View.GONE
                    hideKeyboard()
                    updateUI()
                }

            }
        })

        adapter.setOnRemoved(object : ReplyAdapter.OnRemoved {
            override fun onItemRemove(reply: Reply) {
                boardViewModel.removeReply(board, reply)
                board.replyList.remove(reply)
                board.replyCount -= 1
                updateUI()
            }
        })

        adapter.setOnReported(object : ReplyAdapter.OnReported {
            override fun onItemReported(report: Report) {
                report.userId = auth.currentUser!!.uid
                report.boardID = board.id
                boardViewModel.addReportedBoard(report)
                Toast.makeText(requireContext(), "신고가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        adapter.setOnSelected(object : ReplyAdapter.OnSelectedItem {
            override fun onSelectedItem(reply: Reply) {
                val dialog = SendMessageDialog(requireContext())
                dialog.setOnOKClickedListener { result ->
                    if (result) {
                        val chat = Chat()
                        val me = User()
                        val other = User()
                        me.uid = auth.currentUser!!.uid
                        other.uid = board.userId
                        chat.users.put(board.userId, other)
                        chat.users.put(auth.currentUser!!.uid, me)

                        var checked = false
                        var existedChatId: String? = null

                        chatViewModel.checkRoom().observe(viewLifecycleOwner, Observer { mList ->
                            mList?.let {
                                for (mChat in mList) {
                                    if (mChat.users.keys.size == 2 && mChat.users.containsKey(me.uid) && mChat.users.containsKey(other.uid)) {
                                        checked = true
                                        existedChatId = mChat.id
                                    }
                                }

                                if (checked) {
                                    val bundle = bundleOf("chatID" to existedChatId)
                                    findNavController().popBackStack()
                                    findNavController().navigate(R.id.chat_detail_fragment, bundle)
                                } else {
                                    chatViewModel.addChatRoom(chat)
                                    chatViewModel.addChatRoomUser(chat.id, me)
                                    chatViewModel.addChatRoomUser(chat.id, other)
                                    val bundle = bundleOf("chatID" to chat.id)
                                    findNavController().popBackStack()
                                    findNavController().navigate(R.id.chat_detail_fragment, bundle)
                                }
                            }
                        })
                    }
                }
                dialog.start(requireContext())
            }

        })

        if (board.imageUrlList.isNotEmpty()) {
            val imageAdapter = BoardImageAdapter(requireContext(), board.imageUrlList)
            binding.imageRecyclerView.adapter = imageAdapter
        } else {
            val imageAdapter = BoardImageAdapter(requireContext(), emptyList())
            binding.imageRecyclerView.adapter = imageAdapter
        }

        binding.titleText.text = board.title
        binding.contentText.text = board.content
        binding.nameText.text = board.name
        binding.likeText.text = board.likeCount.toString()
        binding.dislikeText.text = board.dislikeCount.toString()
        binding.replyText.text = board.replyCount.toString()

        if (board.category != null) {
            binding.categoryText.text = board.category
        }

        if (board.favorite) binding.boardDetailToolbar.menu[2].setIcon(R.drawable.ic_favorite_yellow)
        binding.dateText.text = UIUtil.stringTimeToStringDate(board.date)

        if (board.isLikeDislikeClicked != null) {
            if (board.isLikeDislikeClicked!!) {
                binding.likeButton.setImageResource(R.drawable.ic_like_red)
            } else {
                binding.dislikeButton.setImageResource(R.drawable.ic_dislike_blue)
            }
        }
    }


    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.replyEdt.windowToken, 0)
    }


}