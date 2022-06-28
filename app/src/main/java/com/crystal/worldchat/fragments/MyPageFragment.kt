package com.crystal.worldchat.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.crystal.worldchat.LoginActivity
import com.crystal.worldchat.R
import com.crystal.worldchat.adapters.MenuAdapter
import com.crystal.worldchat.boards.BoardViewModel
import com.crystal.worldchat.databinding.FragmentMypageBinding
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.datas.User
import com.crystal.worldchat.users.UserViewModel
import com.crystal.worldchat.utils.EditNicknameDialog
import com.crystal.worldchat.utils.LogoutDialog
import com.crystal.worldchat.utils.SelectImageDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

class MyPageFragment: Fragment() {

    private lateinit var binding: FragmentMypageBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var user: User
    private lateinit var boards: MutableList<Board>
    private val existedNames = arrayListOf<String>()
    private lateinit var adapter: MenuAdapter
    private var imageUri: Uri? = null

    private val userViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }
    private val boardViewModel by lazy {
        ViewModelProvider(this).get(BoardViewModel::class.java)
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                imageUri = result.data?.data    // 이미지 경로 원본
                Glide.with(this).load(imageUri).circleCrop().into(binding.profileImg)
                if (imageUri != null) {
                    userViewModel.updateStorageUserImage(imageUri!!)
                }
            } else {
                Toast.makeText(requireContext(), "이미지를 불러오질 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mypage, container, false)

        auth = Firebase.auth

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUserResponse(auth.currentUser!!.uid).observe(
            viewLifecycleOwner, Observer { user ->
                user?.let {
                    this.user = user
                    setValues(user)
                }
            }
        )

        boardViewModel.getMyBoardsResponse().observe(
            viewLifecycleOwner, Observer { boards ->
                boards?.let {
                    this.boards = boards
                    updateUI(boards)
                }
            }
        )

        userViewModel.getUsers().observe(
            viewLifecycleOwner, Observer { users ->
                users?.let {
                    for (user in users) {
                        if (user.name != null) {
                        existedNames.add(user.name!!)
                        }
                    }
                }
            }
        )



        setMenu()
        setupEvents()

    }

    private fun setMenu() {

        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter =  MenuAdapter(requireContext(), resources.getStringArray(R.array.menu_array))
        binding.menuRecyclerView.adapter = adapter
        adapter.selectedMenu(object : MenuAdapter.OnSelectedMenu {
            override fun onSelectedMenu(string: String) {
                when (string) {
                    "내가 작성한 글" -> {
                        findNavController().navigate(R.id.my_board_fragment)
                    }
                    "북마크" -> {
                        findNavController().navigate(R.id.my_favorite_fragment)
                    }
                    "차단한 유저 관리" -> {
                        findNavController().navigate(R.id.my_block_fragment)
                    }
                    "이용약관" -> {
                        findNavController().navigate(R.id.terms_of_service_fragment)
                    }
                    "개인정보취급방침" -> {
                        findNavController().navigate(R.id.privacy_information_fragment)
                    }
                    "도움말" -> {
                        findNavController().navigate(R.id.help_fragment)
                    }
                    "공지사항" -> {
                        findNavController().navigate(R.id.notification_fragment)
                    }
                    else -> {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }


    private fun setupEvents() {

        binding.logoutImageButton.setOnClickListener {
            val dialog = LogoutDialog(requireContext())
            dialog.setOnOKClickedListener {
                userViewModel.removeToken()
                auth.signOut()
                findNavController().popBackStack()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
            dialog.start()

        }

        binding.nicknameText.setOnClickListener {
            val dialog = EditNicknameDialog(requireContext(), existedNames)
            dialog.setOnOKClickedListener { string ->
                userViewModel.updateUser("name", string)
                user.name = string
                setValues(user)
            }
            dialog.start(requireContext())
        }

        binding.profileImg.setOnClickListener {

            val dialog = SelectImageDialog(requireContext())
            dialog.setOnOKClickedListener { string ->
                if (string == "basic") {
                    Glide.with(this).load(R.drawable.user).circleCrop().into(binding.profileImg)
                    userViewModel.removeUserImageProfile()
                }
                if (string == "select") {
                    val permissionListener = object : PermissionListener {
                        override fun onPermissionGranted() {
                            val intentImage = Intent(Intent.ACTION_PICK)
                            intentImage.type = "image/*"
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
            }
            dialog.start()
        }

    }


    private fun setValues(user: User) {
        binding.nicknameText.text = user.name
        if (user.profileImageUrl != null) {
            Glide.with(requireContext()).load(user.profileImageUrl).circleCrop().into(binding.profileImg)
        } else {
            Glide.with(requireContext()).load(R.drawable.user).circleCrop().into(binding.profileImg)
        }
    }

    private fun updateUI(boards: MutableList<Board>) {

        binding.boardCountText.text = boards.size.toString()

        if (boards.size == 0) {
            binding.boardCountText.text = "0"
        }

        var likeCount = 0
        for (board in boards) {
            likeCount += board.likeCount
        }

        binding.likeCountText.text = likeCount.toString()
    }

}