package com.example.tourapp.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tourapp.R
import com.example.tourapp.data.User
import com.example.tourapp.databinding.FragmentProfileBinding
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

    var list: ArrayList<User> = ArrayList()


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

        if(userId == null)
            userId = "105" //ovo je da proverim ja za sebe
        database = FirebaseDatabase.getInstance()
        userReference = database.reference.child("Users")

        createUser()
        binding.myPlacesBtn.setOnClickListener{
            findNavController().navigate(R.id.action_ProfileFragment_to_MyPlacesFragment)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_ProfileFragment_to_HomeFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
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
                            (data["addCount"] as? Long ?: 0.0).toDouble(),
                            (data["starsCount"] as? Long ?: 0.0).toDouble(),
                            (data["commentsCount"] as? Long ?: 0.0).toDouble(),
                            (data["tourCount"] as? Long ?: 0.0).toDouble(),
                            snapshot.key.toString()
                        )
                        val dd = snapshot.key
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
            val poeniUkupno = user.addCount+user.commentsCount + user.startCount + user.tourCount!!
            nameDisplayTextView.text = user.firstName
            surnameDisplayTextView.text = user.lastName
            pointss.text = (user.addCount + user.commentsCount + user.startCount + user.tourCount).toString()
            phone.text = user.phoneNumber
            Glide.with(view.context)
                .load(user.profilePhotoUrl)
                .into(profileImageImageView)

        }

    }

}
