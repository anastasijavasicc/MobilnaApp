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
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.data.MyPlaces
import com.example.tourapp.data.User
import com.example.tourapp.data.UserObject
import com.example.tourapp.data.UserObject.commentsCount
import com.example.tourapp.databinding.FragmentRankBinding
import com.example.tourapp.model.MyPlacesViewModel
import com.google.android.play.core.integrity.e
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class RankFragment : Fragment() {

    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private var _binding: FragmentRankBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance()
    private val placesReference: DatabaseReference = database.getReference("places")
    private val usersReference: DatabaseReference = database.getReference("Users")
   // private lateinit var dbReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userName = getUserIdFromLocalStorage()!!
        Log.w("TAGA", "KORISNIK JE: $userName")

        val tvName: TextView = view.findViewById(R.id.RankFragmentName)
        tvName.text = myPlacesViewModel.selected?.name
        val rate: RatingBar = view.findViewById(R.id.RankFragmentRating)
        val kom: EditText = view.findViewById(R.id.RankFragmentKomentar)
        val confirmbtn: Button = view.findViewById(R.id.RankFragmentbtnConfirm)
        val cancelbtn: Button = view.findViewById(R.id.RankFragmentbtnCancel)

        CoroutineScope(Dispatchers.Main).launch {
            try{
               // val placeId = myPlacesViewModel.selected?.id

                val sharedPreferences = context?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
                val placeId = sharedPreferences?.getString("myPlaceId", null)

                var myPlaces: MyPlaces
                val databaseReference = placesReference.child(placeId.toString())

                val dataSnapshot: DataSnapshot = databaseReference.get().await()
                myPlaces = dataSnapshot.getValue(MyPlaces::class.java)!!
                myPlacesViewModel.selected = myPlaces!!

                /*val placeId = withContext(Dispatchers.IO) {
                    myPlacesViewModel.selected?.id?.let {
                        placesReference.child(it).get().await()
                    }
                }*/
                Log.w("TAGA", "PLACE ID: $placeId")
                Log.w("TAGA", "Selektovano mesto: ${myPlacesViewModel.selected!!.name}")
                if (placeId != null){
                    val dataSnapshot = placesReference.child(placeId).get().await()
                    val data = (dataSnapshot.value as? Map<String, Any>?)?.toMutableMap()
                    val hmGrades = data?.get("grades") as? Map<String, Double>
                    val hmComments = data?.get("comments") as? Map<String, String>

                    var flagGrade = 0
                    if (hmGrades?.containsKey(userName) == true) {
                        rate.rating = (hmGrades[userName] as Long).toFloat()
                        flagGrade = 1
                       // rate.rating = hmGrades[userName]?.toFloat() ?: 0f
                    }
                    var flagCom = 0
                    if (hmComments?.containsKey(userName) == true) {
                        kom.setText(hmComments[userName])
                        flagCom = 1
                    }

                    val oldRate = rate.rating
                    val oldKomm = kom.text.toString()

                    confirmbtn.setOnClickListener{
                        if (rate.rating != 0f) {
                            myPlacesViewModel.selected?.addGrade(userName, rate.rating.toDouble())
                           // val newGrade = hashMapOf(userName to rate.rating.toDouble())
                            //placesReference.child(placeId).child("grades").updateChildren(newGrade)
                        }
                        myPlacesViewModel.selected?.addComment(userName, kom.text.toString())





////////////////////////////////////////////////


                        if(data != null){
                            data["grades"] = myPlacesViewModel.selected?.grades!!
                            data["comments"] = myPlacesViewModel.selected?.comments!!

                            placesReference.child(placeId).setValue(data)

                        }

///////////////////////////////////////

                        var starsCount: Long = 0
                        if (rate.rating != 0f && oldRate == 0f)
                            starsCount = 1
                        var kommCount: Long = 0
                        if (kom.text.isNotEmpty() && oldKomm.isEmpty())
                            kommCount = 1
                        var tourC: Long = 0

                        usersReference.orderByChild("username").equalTo(userName).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (userSnapshot in dataSnapshot.children) {
                                    val documentRef = usersReference.child(userSnapshot.key!!)
                                    kommCount = userSnapshot.child("commentsCount").getValue(Long::class.java) ?: 0L
                                    starsCount = userSnapshot.child("starsCount").getValue(Long::class.java) ?: 0L
                                    Log.w("TAGA","stigli smo do negde")
                                    tourC = userSnapshot.child("tourCount").getValue(Long::class.java) ?: 0L
                                    Log.w("TAGA","stari tour count: $tourC")
                                    if(flagGrade==1 && flagCom == 1)
                                    {

                                    }
                                    else if(flagGrade == 1 && flagCom == 0)
                                    {
                                        kommCount+=1L
                                    }
                                    else if(flagGrade == 0 && flagCom == 0)
                                    {
                                        kommCount+=1L
                                        starsCount+=1L
                                        tourC+=1L
                                    }
                                    val noviPodaci = hashMapOf<String, Any>(
                                        "commentsCount" to (kommCount),
                                        "starsCount" to (starsCount),
                                        "tourCount" to (tourC)
                                    )

                                    documentRef.updateChildren(noviPodaci)
                                        .addOnSuccessListener {
                                            Toast.makeText(context,"Uspesno ste ocenili lokaciju!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.w("TAGA", "Error", exception)
                                        }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("TAGA", "Error", databaseError.toException())
                            }
                        })
                        /*
                        val userQuery = usersReference.orderByChild("username").equalTo(userName)
                        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                //var starsCount: Long = 0
                               // var commentsCount: Long = 0
                                Log.w("TAGA", "userQuery usao sa korisnikom $userName")
                                for (snapshot in dataSnapshot.children) {
                                   // val user = snapshot.getValue(User::class.java) // Pretpostavljamo da postoji klasa User koja sadrži potrebne informacije
                                   // val documentRef = database.reference.child("Users").

                                    if (rate.rating != 0f && oldRate == 0f) {
                                        starsCount += 1
                                    }

                                    if (kom.text.isNotEmpty() && oldKomm.isEmpty()) {
                                        kommCount++
                                    }




                                    val userUpdate = hashMapOf(
                                        "starsCount" to starsCount,
                                        "commentsCount" to kommCount
                                    )

                                    usersReference.child(snapshot.key!!).updateChildren(userUpdate as Map<String, Any>)
                                        .addOnSuccessListener {
                                            Log.w("TAGA", "Uspesno azuriran i korisnik")
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.w("TAGA", "Error updating user data", exception)
                                        }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("TAGA", "User query cancelled", databaseError.toException())
                            }
                        })*/




                        myPlacesViewModel.selected = null

                        findNavController().navigate(R.id.action_RankFragment_to_HomeFragment)


                    }
                }
            }catch (e: Exception) {
                Log.w("TAGA", "Greska", e)
            }
            cancelbtn.setOnClickListener {
                myPlacesViewModel.selected = null
                findNavController().navigate(R.id.action_RankFragment_to_HomeFragment)
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_RankFragment_to_HomeFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun getUserIdFromLocalStorage(): String? {
        val sharedPreferences = context?.getSharedPreferences("TourApp", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("userId", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

      //  myPlacesViewModel.selected = null
    }

}