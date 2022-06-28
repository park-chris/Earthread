package com.crystal.worldchat.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.crystal.worldchat.adapters.MyBlockAdapter
import com.crystal.worldchat.databinding.FragmentMyBlockBinding
import com.crystal.worldchat.databinding.FragmentMyBoardBinding
import com.crystal.worldchat.datas.Block
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.datas.User
import com.crystal.worldchat.users.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyBlockFragment : Fragment() {

    private lateinit var binding: FragmentMyBlockBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var blocks: MutableList<Block>
    private lateinit var adapter: MyBlockAdapter


    private val userViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_block, container, false)
        binding.blockUserRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        auth = Firebase.auth

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = "차단한 유저 관리"

        userViewModel.getMyBlock().observe(viewLifecycleOwner, Observer { blocks ->
            blocks?.let {
                this.blocks = blocks
                updateUI(blocks)
            }
            if (blocks.size <= 0) {
                binding.notificationText.visibility = View.VISIBLE
            } else {
                binding.notificationText.visibility = View.GONE
            }
        })

        adapter = MyBlockAdapter(requireContext(), mutableListOf())
        binding.blockUserRecyclerView.adapter = adapter

        setupEvents()

    }

    private fun setupEvents() {
        binding.backImageButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun updateUI(blocks: MutableList<Block>) {
        adapter = MyBlockAdapter(requireContext(), blocks)
        binding.blockUserRecyclerView.adapter = adapter

        adapter.onRemoveBlock(object : MyBlockAdapter.OnRemoveBlock {
            override fun onSelectedBoard(string: String) {
                userViewModel.removeBlock(string)
                var removed: Block? = null
                for (i in blocks) {
                    if (i.uid == string) {
                        removed = i
                    }
                }
                Toast.makeText(requireContext(), "차단이 해제되었습니다.", Toast.LENGTH_SHORT).show()
            }

        })
    }


}