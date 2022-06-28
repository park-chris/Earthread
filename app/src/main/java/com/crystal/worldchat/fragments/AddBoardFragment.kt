package com.crystal.worldchat.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crystal.worldchat.R
import com.crystal.worldchat.adapters.ImageEditAdapter
import com.crystal.worldchat.boards.BoardViewModel
import com.crystal.worldchat.databinding.FragmentAddBoardBinding
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.datas.BoardImage
import com.crystal.worldchat.datas.FirebaseCallback
import com.crystal.worldchat.utils.ContextUtil
import com.crystal.worldchat.utils.ProgressDialog
import com.crystal.worldchat.utils.UIUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.Observer
import com.crystal.worldchat.adapters.TopicSpinnerAdapter
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.lang.reflect.Field
import kotlin.collections.ArrayList

class AddBoardFragment : Fragment() {

    private lateinit var binding: FragmentAddBoardBinding
    private var mBoard: Board? = Board()
    private lateinit var mainNavi: BottomNavigationView
    private var boardId: String? = null
    private var imageIndex = 0
    private var category: String? = null
    private lateinit var itemList: Array<String>
    private val boardImageList: ArrayList<BoardImage> = arrayListOf()
    private val boardViewModel: BoardViewModel by lazy {
        ViewModelProvider(this).get(BoardViewModel::class.java)
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data?.data != null) {

                val clipData = result.data?.clipData
                val clipDataSize = clipData?.itemCount

                if (clipData == null) {
                    val selectedImageUri = result.data?.data!!

                    val boardImage = BoardImage()

                    boardImage.uri = selectedImageUri.toString()
                    boardImage.id = boardImageList.size

                    boardImageList.add(boardImage)

                } else {
                    clipData.let { data ->
                        for (i in 0 until clipDataSize!!) {
                            val boardImage = BoardImage()

                            boardImage.uri = data.getItemAt(clipDataSize - 1 - i).uri.toString()
                            boardImage.id = imageIndex

                            boardImageList.add(boardImage)
                            imageIndex += 1

                        }
                    }
                }

                updateUI(boardImageList)

            } else {
                Toast.makeText(requireContext(), "이미지를 불러오질 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        boardId = arguments?.getString("boardID")

        if (boardId != null) boardViewModel.loadBoard(boardId!!)

        mainNavi =  requireActivity().findViewById(R.id.main_navi) as BottomNavigationView
        mainNavi.visibility = View.GONE
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_board, container, false)
        binding.addBoardRecyclerView.layoutManager = LinearLayoutManager(context)

        itemList = resources.getStringArray(R.array.category_array)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (boardId != null) setValues()

        setupEvents()
    }

    private fun setValues() {
        boardViewModel.boardLiveData.observe(
            viewLifecycleOwner, Observer { getBoard ->
                mBoard = getBoard

                getBoard?.let {

                    binding.titleEditText.setText(getBoard.title)
                    binding.contentEditText.setText(getBoard.content)
                    if (getBoard.imageUrlList.isNotEmpty()) {
                        imageIndex = getBoard.imageUrlList.size
                        for (i in 0 until getBoard.imageUrlList.size) {
                            boardImageList.add(getBoard.imageUrlList[i])
                        }
                    }

                    if (mBoard?.category != null) {

                        for (i in itemList.indices) {
                            if (itemList[i] == mBoard?.category)
                                binding.topicSpinner.setSelection(i)
                        }

                    }

                    updateUI(boardImageList)
                }
            }
        )
    }

    private fun setupEvents() {

        binding.topicSpinner.adapter = TopicSpinnerAdapter(requireContext(), itemList)

        try {
            val popup: Field = binding.topicSpinner.javaClass.getDeclaredField("mPopup")
            popup.isAccessible = true

            val popupWindow: ListPopupWindow = popup.get(binding.topicSpinner) as ListPopupWindow

            popupWindow.height = 800
        } catch (e: NoClassDefFoundError) {
        }




        binding.topicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(requireContext(), itemList[position], Toast.LENGTH_SHORT).show()
                if (position != 0 ) {
                    category = itemList[position]
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

        }

        binding.addImageButton.setOnClickListener {

            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                    val intentImage = Intent(Intent.ACTION_PICK)
                    intentImage.type = "image/*"
                    intentImage.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    getContent.launch(intentImage)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(requireContext(), "저장소 접근 권한을 거부하셨습니다. 이미지를 업로드하기 위해서는 해당 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                }

            }

            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check()

        }


        binding.cancelText.setOnClickListener {
            backStack(false)
        }

        binding.registerText.setOnClickListener {

            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()

            if (title != "" || content != "") {

                if (boardId != null) {
                    mBoard!!.title = title
                    mBoard!!.content = content
                    mBoard!!.category = category
                    mBoard!!.imageUrlList = boardImageList

                } else {
                    mBoard = Board()

                    val userId = ContextUtil.getUserId(requireContext())
                    val name = ContextUtil.getUserName(requireContext())

                    mBoard!!.title = title
                    mBoard!!.content = content
                    mBoard!!.userId = userId!!
                    mBoard!!.category = category
                    mBoard!!.name = name
                    mBoard!!.date = UIUtil.timeStampToString()
                    mBoard!!.imageUrlList = boardImageList

                }

                if (mBoard!!.imageUrlList.isEmpty()) {
                    boardViewModel.addBoard(mBoard!!)
                    backStack(false)
                } else {
                    val dialog = ProgressDialog(requireContext())
                    dialog.on(requireContext())
                    boardViewModel.updateStorageBoardImage(mBoard!!, object : FirebaseCallback {
                        override fun onSuccess(result: Boolean) {
                            if (result) {
                                boardViewModel.addBoard(mBoard!!)

                                dialog.off()
                                backStack(false)
                            }
                        }
                    })
                }

            }


        }


        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (binding.titleEditText.text.toString() != "" && binding.contentEditText.text.toString() != "") {
                    binding.registerText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                } else {
                    binding.registerText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.light_gray
                        )
                    )
                }
            }
        }

        binding.contentEditText.addTextChangedListener(textWatcher)
        binding.titleEditText.addTextChangedListener(textWatcher)


    }

    private fun backStack(isBoard: Boolean) {

        mainNavi = requireActivity().findViewById(R.id.main_navi)
        mainNavi.visibility = View.VISIBLE

        findNavController().popBackStack()
        if (!findNavController().popBackStack()) {
            if (isBoard) {
                val bundle = bundleOf("boardID" to mBoard!!.id)
                findNavController().navigate(R.id.action_board_to_detail, bundle)
            } else {
                findNavController().navigate(R.id.board_fragment)
            }

        }


    }


    private fun updateUI(boardImageList: MutableList<BoardImage>) {
        val adapter = ImageEditAdapter(requireContext(), boardImageList)
        binding.addBoardRecyclerView.adapter = adapter
        adapter.setOnRemoved(object : ImageEditAdapter.OnRemoved {
            override fun onItemRemove(index: Int) {
                boardImageList.removeAt(index)
                updateUI(boardImageList)
            }

        })
    }


}