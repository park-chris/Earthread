package com.crystal.worldchat.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.crystal.worldchat.R
import com.crystal.worldchat.adapters.BoardAdapter
import com.crystal.worldchat.adapters.TopicAdapter
import com.crystal.worldchat.adapters.TopicSpinnerAdapter
import com.crystal.worldchat.boards.BoardViewModel
import com.crystal.worldchat.databinding.FragmentBoardBinding
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.utils.ContextUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class BoardFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentBoardBinding
    private lateinit var adapter: BoardAdapter
    private lateinit var topicAdapter: TopicAdapter

    private var putTopics = arrayListOf<String>()

    private var keyword: String? = null
    private var listKeyword: ArrayList<String>? = null
    private var sortKeyword: Int? = null

    private val boardViewModel by lazy {
        ViewModelProvider(this).get(BoardViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_board, container, false)

        setValues()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (auth.currentUser?.uid != null) {
            putTopics = ContextUtil.getFilterTopic(requireContext())
            if (putTopics.isNotEmpty()) {
                if (putTopics.contains("전체")) {
                    listKeyword = null
                } else {
                    listKeyword = putTopics
                }
            } else {
                listKeyword = null
            }
            boardViewModel.getBoards(auth.currentUser!!.uid, keyword, listKeyword, sortKeyword)
                .observe(
                    viewLifecycleOwner, Observer { boards ->
                        boards?.let {
                            if (boards.size == 0) {
                                binding.notificationText.visibility = View.VISIBLE
                            } else {
                                binding.notificationText.visibility = View.GONE

                            }
                            updateUI(boards)
                        }
                    }
                )
        }



        setupEvents()
    }

    private fun setValues() {

        auth = Firebase.auth

        binding.boardRecyclerView.layoutManager = LinearLayoutManager(context)

        adapter = BoardAdapter(requireContext(), emptyList(), boardViewModel)
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.boardToolbar.inflateMenu(R.menu.board_toolbar_menu)

        binding.topicRecyclerView.layoutManager =
            StaggeredGridLayoutManager(5, LinearLayout.HORIZONTAL)

        binding.sortSpinner.adapter =
            TopicSpinnerAdapter(requireContext(), resources.getStringArray(R.array.sort_array))

        val topicList = resources.getStringArray(R.array.category_array).toMutableList()
        topicList.removeAt(0)

        putTopics = ContextUtil.getFilterTopic(requireContext())

        topicAdapter = TopicAdapter(requireContext(), topicList, putTopics)
        binding.topicRecyclerView.adapter = topicAdapter
        topicAdapter.setOnSaved(object : TopicAdapter.OnSavedTopic {
            override fun onSavedPref(topics: ArrayList<String>) {
                putTopics = topics
                binding.topicRecyclerView.adapter = topicAdapter
            }
        })

    }

    private fun setupEvents() {

        binding.searchImgButton.setOnClickListener {

            hideKeyboard()

            if (binding.searchEditText.text.isNotEmpty()) {

                if (binding.searchCheckBox.isChecked) {

                    if (putTopics.isNotEmpty()) {
                        if (putTopics.contains("전체")) {
                            listKeyword = null
                        } else {
                            listKeyword = putTopics
                        }
                        ContextUtil.setFilterTopic(requireContext(), putTopics)
                    } else {
                        listKeyword = null
                    }

                    keyword = binding.searchEditText.text.toString()

                    sortKeyword =
                        binding.sortSpinner.selectedItemPosition

                } else {

                    keyword = binding.searchEditText.text.toString()
                    listKeyword = null
                    sortKeyword = null

                }

                boardViewModel.getBoards(
                    auth.currentUser!!.uid,
                    keyword,
                    listKeyword,
                    sortKeyword
                ).observe(viewLifecycleOwner, Observer { filteredBoard ->
                    filteredBoard?.let {
                        if (filteredBoard.size == 0) {
                            binding.notificationText.visibility = View.VISIBLE
                        } else {
                            binding.notificationText.visibility = View.GONE
                        }
                        updateUI(filteredBoard)
                    }
                })

                binding.searchEditText.text.clear()
                binding.drawer.closeDrawer(Gravity.RIGHT)

            } else {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.search_hint),
                    Toast.LENGTH_SHORT
                ).show()
            }


        }

        binding.filterButton.setOnClickListener {
            if (putTopics.isNotEmpty()) {
                if (putTopics.contains("전체")) {
                    listKeyword = null
                } else {
                    listKeyword = putTopics
                }
                ContextUtil.setFilterTopic(requireContext(), putTopics)
            } else {
                listKeyword = null
            }

            keyword = null

            sortKeyword =
                binding.sortSpinner.selectedItemPosition

            boardViewModel.getBoards(
                auth.currentUser!!.uid,
                keyword,
                listKeyword,
                sortKeyword
            ).observe(viewLifecycleOwner, Observer { filteredBoard ->
                filteredBoard?.let {
                    if (filteredBoard.size == 0) {
                        binding.notificationText.visibility = View.VISIBLE
                    } else {
                        binding.notificationText.visibility = View.GONE
                    }
                    updateUI(filteredBoard)
                }
            })

            binding.drawer.closeDrawer(Gravity.RIGHT)

        }

        binding.boardToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_add -> {
                    findNavController().navigate(R.id.action_board_to_add)
                    true
                }

                R.id.menu_search -> {
                    if (!binding.drawer.isDrawerOpen(Gravity.RIGHT)) {
                        binding.drawer.openDrawer(Gravity.RIGHT)
                    }
                    true
                }

                else -> false
            }
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
                findNavController().navigate(R.id.action_board_to_detail, bundle)
            }
        })


    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }


}