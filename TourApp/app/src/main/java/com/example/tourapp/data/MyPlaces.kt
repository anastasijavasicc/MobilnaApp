package com.example.tourapp.data

data class MyPlaces(var name:String,
                    var description:String,
                    var longitude:String,
                    var latitude:String,
                    var autor:String,
                    var grades:HashMap<String,Double>,
                    var comments:HashMap<String,String>,
                    var url:String,
                    @Transient var id:String){
    override fun toString(): String = name

    fun addGrade(username:String,grade:Double){

        if(grades==null)
            grades = java.util.HashMap<String, Double>()
        grades[username] = grade
    }
    fun  addComment(username: String,comm:String){
        if(comments == null)
            comments = java.util.HashMap<String, String>()
        comments[username] = comm
    }
}

