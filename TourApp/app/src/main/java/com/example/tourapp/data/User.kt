package com.example.tourapp.data

data class User(
    val username:String,
    val password:String,
    val firstName:String,
    val lastName: String,
    val phoneNumber: String,
    val profilePhotoUrl:String,
    val addCount: Double,
    val startCount:Double,
    val commentsCount:Double,
    var tourCount: Double,
    @Transient val id:String):java.io.Serializable {

}