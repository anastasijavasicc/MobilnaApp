package com.example.tourapp.model

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.ViewModel
import com.example.tourapp.R
import com.example.tourapp.data.MyPlaces

class MyPlacesViewModel: ViewModel() {
    var myPlacesList: ArrayList<MyPlaces> = ArrayList<MyPlaces>()

    fun addPlace(place: MyPlaces) {
        myPlacesList.add(place);
    }

    var selected: MyPlaces? = null
}







