package com.example.tourapp.data

import android.location.Location

interface ILocationClient {

    fun onNewLocation(location:Location)


}