package com.example.tourapp.fragment

import androidx.fragment.app.Fragment
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.activity.MainActivity
import com.example.tourapp.data.ILocationClient
import com.example.tourapp.data.MyPlaces
import com.example.tourapp.data.User
import com.example.tourapp.data.UserObject
import com.example.tourapp.databinding.FragmentListBinding
import com.example.tourapp.databinding.FragmentMyPlacesBinding
import com.example.tourapp.model.LocationViewModel
import com.example.tourapp.model.MyPlacesListAdapter
import com.example.tourapp.model.MyPlacesViewModel
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.*


class MyPlacesFragment : Fragment() {

    private var _binding: FragmentMyPlacesBinding? = null
    private val binding get() = _binding!!
    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("places")
    //private var userName: String = UserObject.username.toString()
    private lateinit var myUser: User
    private val locationViewModel: LocationViewModel by activityViewModels()

    var userId: String? = null //= getUserIdFromLocalStorage()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPlacesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        userId = getUserIdFromLocalStorage()
        getList()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_MyPlacesFragment_to_ProfileFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }



    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        myPlacesViewModel.selected = myPlacesViewModel.myPlacesList[info.position]
        this.findNavController().navigate(R.id.action_MyPlacesFragment_to_EditFragment)
        return super.onContextItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    fun getList() {

        myPlacesViewModel.myPlacesList.clear()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                myPlacesViewModel.myPlacesList.addAll(listCreating(snapshot))
                //showList(requireView(), myPlacesViewModel.myPlacesList)
                val fragmentView = view
                if (fragmentView != null) {
                    showList(fragmentView, myPlacesViewModel.myPlacesList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAGA", "Greska", error.toException())
            }
        })


    }

    fun listCreating(snapshot: DataSnapshot): kotlin.collections.ArrayList<MyPlaces> {

        var list : kotlin.collections.ArrayList<MyPlaces> = ArrayList()

        for (placeSnapshot in snapshot.children) {
            //var place = placeSnapshot.getValue(MyPlaces::class.java)
            // place?.let { list.add(it) }
            val data = placeSnapshot.value as Map<String, Any>
            var grades = HashMap<String, Double>()
            if (data?.get("grades") != null) {
                for (g in data["grades"] as HashMap<String, Double>)
                    grades[g.key] = g.value
            }
            var comments = HashMap<String, String>()
            if (data?.get("comments") != null) {
                for (c in data["comments"] as HashMap<String, String>)
                    comments[c.key] = c.value
            }
            val autor = data["autor"] as? String
            Log.w("TAGA","Userr je $autor")
            Log.w("TAGA","Username koji se poklapa je $userId")
            if (autor == userId) {
                Log.w("TAGA","USLI SMO")
                list.add(
                    MyPlaces(
                        data["name"].toString(),
                        data["description"].toString(),
                        data["latitude"].toString(),
                        data["longitude"].toString(),
                        data["autor"].toString(),
                        grades,
                        comments,
                        data["url"].toString(),
                        data["category"].toString(),
                        placeSnapshot.key.toString()

                    )
                )
            }

        }
        return list
    }
    fun showList(view: View, arrayList: ArrayList<MyPlaces>) {

        val listView: ListView = requireView().findViewById(R.id.my_places_l)

        val arrayAdapter = MyPlacesListAdapter(
            view.context,
            arrayList
        )
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var myPlace: MyPlaces = p0?.adapter?.getItem(p2) as MyPlaces
                myPlacesViewModel.selected = myPlace

                val sharedPreferences = context?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences?.edit()
                editor?.putString("myPlaceId", myPlace.id)
                editor?.apply()

                findNavController().navigate(R.id.action_MyPlacesFragment_to_EditFragment)

            }
        })


        listView.setOnItemLongClickListener { parent, view, position, id ->
            var myPlace: MyPlaces = parent?.adapter?.getItem(position) as MyPlaces
            myPlacesViewModel.selected = myPlace

            val sharedPreferences = context?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.putString("myPlaceId", myPlace.id)
            editor?.apply()

            //showPopupMenu(view, position)
            true
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUserIdFromLocalStorage(): String? {
        val sharedPreferences = context?.getSharedPreferences("TourApp", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("userId", null)
    }

}