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
import com.crystal.worldchat.adapters.BoardAdapter
import com.crystal.worldchat.boards.BoardViewModel
import com.crystal.worldchat.databinding.FragmentMyBoardBinding
import com.crystal.worldchat.datas.Board
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyFavoriteFragment : Fragment() {

    private lateinit var binding: FragmentMyBoardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: BoardAdapter

    private val boardViewModel by lazy {
        ViewModelProvider(this).get(BoardViewModel::class.java)
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

        binding.titleText.text = "북마크"
        adapter = BoardAdapter(requireContext(), emptyList(), boardViewModel)
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        boardViewModel.getMyFavoriteBoard().observe(
            viewLifecycleOwner, Observer { boards ->
                boards?.let {
                    if (boards.size == 0) {
                        binding.notificationText.visibility = View.VISIBLE
                        binding.notificationText.text = "북마크한 게시물이 없습니다."
                    }
                    updateUI(boards)
                }

            }
        )

        setupEvents()

    }

    private fun setupEvents() {
        binding.backImageButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun updateUI(boards: List<Board>) {


        adapter = BoardAdapter(requireContext(), boards, boardViewModel)
        binding.boardRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BoardAdapter.OnItemClickListener {
            override fun onItemClick(view: View, board: Board) {
                board.watcher += 1
                boardViewModel.updateBoard(board, "watcher")

                val bundle = bundleOf("boardID" to board.id)

                findNavController().navigate(R.id.board_detail_fragment, bundle)
            }
        })

    }




}