package com.example.tourapp.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.data.UserObject
import com.example.tourapp.databinding.FragmentRankBinding
import com.example.tourapp.model.MyPlacesViewModel
import com.google.android.play.core.integrity.e
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class RankFragment : Fragment() {

    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private var _binding: FragmentRankBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbReference: DatabaseReference




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRankBinding.inflate(inflater, container, false)
        dbReference = FirebaseDatabase.getInstance().reference
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var userName = getUserIdFromLocalStorage()!!



        var tvName: TextView = view.findViewById(R.id.RankFragmentName)
        tvName.setText(myPlacesViewModel.selected?.name)
        var rate: RatingBar = view.findViewById(R.id.RankFragmentRating)
        var kom: EditText = view.findViewById(R.id.RankFragmentKomentar)
        var confirmbtn: Button = view.findViewById(R.id.RankFragmentbtnConfirm)
        var cancelbtn: Button = view.findViewById(R.id.RankFragmentbtnCancel)



        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    myPlacesViewModel.selected?.id?.let {
                        dbReference.child("places").child(it).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val data = snapshot.value as Map<String, Any>?
                                    val hmGrades = data?.get("grades") as? HashMap<String, Double>?
                                    val hmComments = data?.get("comments") as? HashMap<String, String>?

                                    if (hmGrades?.get(userName) != null) {
                                        rate.rating = hmGrades[userName]?.toFloat() ?: 0f
                                    }

                                    if (hmComments?.get(userName) != null) {
                                        kom.setText(hmComments[userName] ?: "")
                                    }

                                }

                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.w("TAGA", "Greška: $error")
                            }
                        })
                    }
                }
                val oldRate = rate.rating
                val oldKomm = kom.text.toString()

                confirmbtn.setOnClickListener {
                    val newRate = rate.rating.toDouble()
                    val newComment = kom.text.toString()
                    if (rate.rating != 0f)
                        myPlacesViewModel.selected?.addGrade(userName, rate.rating.toDouble())
                    myPlacesViewModel.selected?.addComment(userName, kom.text.toString())
                    val placeId = myPlacesViewModel.selected?.id
                    if (placeId != null) {
                        val placeReference = dbReference.child("places").child(placeId)
                        placeReference.child("grades").setValue(myPlacesViewModel.selected?.grades!!)
                        placeReference.child("comments").setValue(myPlacesViewModel.selected?.comments!!)
                        Toast.makeText(context,"OKEJ",Toast.LENGTH_LONG).show()
                    }

                    val userReference = dbReference.child("users").child(userName)
                      //  .equalTo("username",userName)
                    var starsCount: Long = 0
                    if (rate.rating != 0f && oldRate == 0f)
                        starsCount = 1
                    var kommCount: Long = 0
                    if (kom.text.isNotEmpty() && oldKomm.isEmpty())
                        kommCount = 1
                    userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val userData = snapshot.value as Map<String, Any>?
                                starsCount += userData?.get("starsCount") as Long
                                var commentsCount = userData["commentsCount"] as Long

                                if (newRate > 0 && rate.rating == 0f) {
                                    starsCount += 1
                                }
                                if (newComment.isNotEmpty() && oldKomm.isEmpty()) {
                                    commentsCount += 1
                                }
                                val noviPodaci = hashMapOf<String, Any>(
                                    "starsCount" to starsCount,
                                    "commentsCount" to kommCount
                                )
                                userReference.updateChildren(noviPodaci)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Podaci su azurirani za korisnika", Toast.LENGTH_LONG).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w("TAGA", "Error", exception)
                                    }
                                //userReference.child("starsCount").setValue(starsCount)
                                //userReference.child("commentsCount").setValue(commentsCount)

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w("TAGA", "Greška: $error")
                        }
                    })
                    myPlacesViewModel.selected = null
                    findNavController().navigate(R.id.action_RankFragment_to_ListFragment)

                }

            } catch (e: java.lang.Exception) {
                Log.w("TAGA", "Greska", e)
            }
            cancelbtn.setOnClickListener {
                myPlacesViewModel.selected = null
                findNavController().popBackStack()
                //findNavController().navigate(R.id.action_RankFragment_to_ListFragment)
            }
        }


    }
    private fun getUserIdFromLocalStorage(): String? {
        val sharedPreferences = context?.getSharedPreferences("TourApp", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("userId", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        myPlacesViewModel.selected = null
    }

}