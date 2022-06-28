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
import com.crystal.worldchat.adapters.MyAdapter
import com.crystal.worldchat.databinding.FragmentMyBoardBinding
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.datas.User
import com.crystal.worldchat.users.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyBoardFragment : Fragment() {

    private lateinit var binding: FragmentMyBoardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var user: User
    private lateinit var adapter: MyAdapter


    private val userViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_board, container, false)
        binding.boardRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        auth = Firebase.auth

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = "내가 작성한 글"

        userViewModel.getUserResponse(auth.currentUser!!.uid).observe(
            viewLifecycleOwner, Observer { user ->
                user?.let {
                    this.user = user
                    updateUI(user)
                }
            }
        )
        adapter = MyAdapter(requireContext(), mutableListOf())
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        setupEvents()

    }

    private fun setupEvents() {
        binding.backImageButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun updateUI(user: User) {
        
        val boardList = mutableListOf<Board>()
        
        if (!user.myBoard.isNullOrEmpty()) {
            for (board in user.myBoard!!) {
                board.isLikeDislikeClicked = true
                boardList.add(board)
            }
        }

        if (!user.myReply.isNullOrEmpty()) {
            for (board in user.myReply!!) {
                board.isLikeDislikeClicked = false
                boardList.add(board)
            }
        }

        boardList.sortBy { it.date }
        boardList.reverse()

        if (boardList.isNotEmpty()) {

            adapter = MyAdapter(requireContext(), boardList)
            binding.boardRecyclerView.adapter = adapter
            adapter.selectedBoard(object : MyAdapter.OnSelectedBoard {
                override fun onSelectedBoard(string: String) {
                    val bundle = bundleOf("boardID" to string)
                    findNavController().navigate(R.id.board_detail_fragment, bundle)
                }

            })

        } else {
            binding.notificationText.visibility = View.VISIBLE
        }

    }


}