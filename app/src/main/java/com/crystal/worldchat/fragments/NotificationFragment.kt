package com.crystal.worldchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R
import com.crystal.worldchat.adapters.NotificationAdapter
import com.crystal.worldchat.databinding.FragmentHelpBinding
import com.crystal.worldchat.datas.Notification
import com.crystal.worldchat.notifications.NotificationViewModel
import com.crystal.worldchat.utils.DividerDecoration

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentHelpBinding
    private lateinit var adapter: NotificationAdapter

    private val notificationViewModel by lazy {
        ViewModelProvider(this).get(NotificationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_help, container, false)
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerView.addItemDecoration(
            DividerDecoration(
                requireContext(),
                R.drawable.line_divider,
                5,
                5
            )
        )

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = "공지사항"
        adapter = NotificationAdapter(requireContext(), arrayListOf())
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


        notificationViewModel.getNotification().observe(
            viewLifecycleOwner, Observer { notifications ->
                if (notifications.isEmpty()) {
                    binding.notificationText.visibility = View.VISIBLE
                } else {
                updateUI(notifications)
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

    private fun updateUI(notifications: ArrayList<Notification>) {

        adapter = NotificationAdapter(requireContext(), notifications)
        binding.notificationRecyclerView.adapter = adapter

    }




}