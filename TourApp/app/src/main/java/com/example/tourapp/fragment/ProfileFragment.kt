package com.example.tourapp.fragment

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tourapp.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.example.tourapp.R.layout.profile_info_dialog
import com.example.tourapp.data.User
import com.example.tourapp.data.UserObject
import com.example.tourapp.data.UserObject.profilePhotoUrl
import com.example.tourapp.databinding.FragmentHomeBinding
import com.example.tourapp.databinding.FragmentProfileBinding
import com.example.tourapp.databinding.FragmentUsersBinding
import com.example.tourapp.model.UserListAdapter
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
  //  private lateinit var databaseReference: DatabaseReference
    private lateinit var userReference: DatabaseReference
    private lateinit var binding: FragmentProfileBinding
    private var auth = FirebaseAuth.getInstance()
    //val currentUser = FirebaseAuth.getInstance().currentUser
    var list: ArrayList<User> = ArrayList()
    var storage: FirebaseStorage? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }
    private fun getUserIdFromLocalStorage(): String? {
        val sharedPreferences = context?.getSharedPreferences("TourApp", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("userId", null)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            var userId = getUserIdFromLocalStorage()
            //val userId = auth.currentUser!!
        if(userId == null)
            userId = "105" //ovo je da proverim ja za sebe
        database = FirebaseDatabase.getInstance()
        userReference = database.reference.child("Users")

        createUser()
    }
    private fun createUser(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataSnapshot = userReference.orderByChild("addCount").get().await()

                for (snapshot in dataSnapshot.children) {
                    val data = snapshot.value as Map<*, *>?

                    if (data != null) {
                        val user = User(
                            data["username"].toString() ?: "",
                            data["password"].toString() ?: "",
                            data["firstName"].toString() ?: "",
                            data["lastName"].toString() ?: "",
                            data["phoneNumber"].toString() ?: "",
                            data["profilePhotoUrl"].toString() ?: "",
                            (data["addCount"] as? Double) ?: 0.0,
                            (data["starsCount"] as? Double) ?: 0.0,
                            (data["commentsCount"] as? Double) ?: 0.0,
                            snapshot.key.toString()
                        )
                        val dd = snapshot.key
                        Log.e("TAGA", "user je ${user.firstName} sa ${dd}")
                        if(dd == getUserIdFromLocalStorage()) {
                            list.add(user)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    showList(requireView(), list)
                }
            }
            catch (e: Exception) {
                Log.e("TAGA", "Greška", e)
            }
        }

    }
    fun showList(view: View, arrayList: java.util.ArrayList<User>) {

        if (arrayList.isNotEmpty()) {
            val user = arrayList[0] // Prvi korisnik u listi
            val nameDisplayTextView: TextView = requireView().findViewById(R.id.name_display)
            val surnameDisplayTextView: TextView = requireView().findViewById(R.id.lastname_display)
            val pointss:TextView = requireView().findViewById(R.id.point_display)
            val profileImageImageView: ImageView = requireView().findViewById(R.id.profile_image)
            val phone: TextView = requireView().findViewById(R.id.phone_display)

            nameDisplayTextView.text = user.firstName
            surnameDisplayTextView.text = user.lastName
            pointss.text = user.addCount.toString()
            phone.text = user.phoneNumber
            Glide.with(view.context)
                .load(user.profilePhotoUrl)
                .into(profileImageImageView)

        }

    }

}
