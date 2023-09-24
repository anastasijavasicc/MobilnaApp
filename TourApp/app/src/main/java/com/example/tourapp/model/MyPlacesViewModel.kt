package com.example.tourapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.data.MyPlaces
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MyPlacesViewModel: ViewModel() {
    var myPlacesList: ArrayList<MyPlaces> = ArrayList<MyPlaces>()

    fun addPlace(place: MyPlaces) {
        myPlacesList.add(place);
    }

    var selected: MyPlaces? = null
/////////////////////////////
    private val database= Firebase.database.reference
    private val storageRef = FirebaseStorage.getInstance().reference


    private val _place= MutableLiveData<MyPlaces?>(null)
    private val _places= MutableLiveData<List<MyPlaces>>(emptyList())
    private var placeList: List<MyPlaces> = emptyList()

    var place
        get() = _place.value
        set(value) { _place.value=value}

    val patrols: LiveData<List<MyPlaces>> get() = _places

    private fun getDistance(currentLat: Double, currentLon: Double, deviceLat: Double, deviceLon: Double): Double {
        val earthRadius = 6371000.0
        val currentLatRad = Math.toRadians(currentLat)
        val deviceLatRad = Math.toRadians(deviceLat)
        val deltaLat = Math.toRadians(deviceLat - currentLat)
        val deltaLon = Math.toRadians(deviceLon - currentLon)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(currentLatRad) * cos(deviceLatRad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun fetchPlaces() {
        val placesRef = database.child("places")

        placesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val placesList = mutableListOf<MyPlaces>()

                for (placeSnapshot in snapshot.children) {
                    val place = placeSnapshot.getValue(MyPlaces::class.java)
                    place?.let { placesList.add(it) }
                }

                placeList = placesList
                _places.value = placesList
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    fun filterPlaces(
        autorFilter: String?, categoryFilter: String?, gradesFilter: HashMap<String, Double>?,
        radiusFilter:Int?, userLat:Double?, userLon: Double?) {

        val filteredList = placeList.filter { place ->
            var includePlace = true
            if (autorFilter != null) {
                includePlace = includePlace && place.autor == autorFilter //.contains(autorFilter, ignoreCase = true)
            }
            if (categoryFilter != null) {
                includePlace = includePlace && place.category == categoryFilter
            }
            if (gradesFilter != null) {
                includePlace = includePlace && place.grades == gradesFilter
            }

            if(radiusFilter!= null)
            {
                includePlace=includePlace && getDistance(userLat!!,userLon!!,place.latitude!!.toDouble(),place.longitude!!.toDouble())<radiusFilter
            }
            includePlace
        }
        _places.value = filteredList
    }

}







