package com.example.tourapp.fragment

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
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

class ListFragment : Fragment(), ILocationClient {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("places")
    private var searchType: String = "name"
    private var userName: String = UserObject.username.toString()
    private lateinit var myUser: User
    var lastCheckedRadioButton: RadioButton? = null
    private val locationViewModel: LocationViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivity.iLocationClient = this
        getList()
        val radioGroup: RadioGroup = view.findViewById(R.id.rgTable)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbAutorTabela -> {
                    searchType = "autor"

                }
                R.id.rbTip -> {
                    searchType = "category"
                }
                R.id.rbGrade -> {
                    searchType = "grade"

                }
                R.id.rbRadius -> {
                    searchType = "radius"
                }

            }


        }


        for (i in 0 until radioGroup.childCount) {
            val radioButton: RadioButton = radioGroup.getChildAt(i) as RadioButton
            radioButton.setOnClickListener {
                if (lastCheckedRadioButton == radioButton) {
                    radioGroup.clearCheck()
                    lastCheckedRadioButton = null
                } else {
                    lastCheckedRadioButton = radioButton
                    radioButton.isChecked = true
                }
            }
        }

        var btn2: Button = binding.button3

        btn2.setOnClickListener {
            radioGroup.clearCheck()
            getList()
        }
        binding.btnOk.setOnClickListener {
            onResume()
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_ListFragment_to_HomeFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        if (item.itemId === 1){
            myPlacesViewModel.selected = myPlacesViewModel.myPlacesList[info.position]
            this.findNavController().navigate(R.id.action_ListFragment_to_ViewFragment)
        } else if(item.itemId === 2)
        {
            myPlacesViewModel.selected = myPlacesViewModel.myPlacesList[info.position]
            this.findNavController().navigate(R.id.action_ListFragment_to_EditFragment)
        } else if (item.itemId ===3){
            myPlacesViewModel.myPlacesList.removeAt(info.position)
            val myPlacesList: ListView = requireView().findViewById<ListView>(R.id.my_places_list)
            myPlacesList.adapter = this@ListFragment.context?.let { ArrayAdapter<MyPlaces>(it, android.R.layout.simple_list_item_1,myPlacesViewModel.myPlacesList)}
        }else if(item.itemId === 4){
            myPlacesViewModel.selected = myPlacesViewModel.myPlacesList[info.position]
            this.findNavController().navigate(R.id.action_ListFragment_to_MapFragment)
        }
        return super.onContextItemSelected(item)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_new_place -> {
                this.findNavController().navigate(R.id.action_ListFragment_to_EditFragment)
                true
            }
            R.id.menu_leaderboard -> {
                this.findNavController().navigate(R.id.action_ListFragment_to_UsersFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

     override fun onResume() {
        super.onResume()
        val searchView: SearchView = binding.svTable
       // searchType = "name"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
               // if (searchType.equals("grade") && query.toIntOrNull() == null)
               //     Toast.makeText(requireContext(), "Unesite broj", Toast.LENGTH_SHORT).show()
              //  else {
                    filter(searchType, query, 0)
                   // getAndShowFiltredList(searchType, query, 0)
               // }
                return true
            }


            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty())
                  //  if (searchType.equals("grade") && newText.toIntOrNull() == null) {

                     //   Toast.makeText(requireContext(), "Unesite broj", Toast.LENGTH_SHORT).show()
                  //  } else
                        filter(searchType, newText, 0)
                        //getAndShowFiltredList(searchType, newText, 1)
                else
                    getList()
                return true
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
        return list
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

    fun showList(view: View, arrayList: ArrayList<MyPlaces>) {

        val listView: ListView = requireView().findViewById(R.id.my_places_list)

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

                findNavController().navigate(R.id.action_ListFragment_to_ViewFragment)

            }
        })


        listView.setOnItemLongClickListener { parent, view, position, id ->
            var myPlace: MyPlaces = parent?.adapter?.getItem(position) as MyPlaces
            myPlacesViewModel.selected = myPlace

            val sharedPreferences = context?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.putString("myPlaceId", myPlace.id)
            editor?.apply()

            showPopupMenu(view, position)
            true
        }


    }

    private fun filter(field: String, value: String, type: Int){
        if (value != "") {
            myPlacesViewModel.myPlacesList.clear()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    var snapshot: DataSnapshot
                    if(field == "radius"){
                        val location = MainActivity.currentLocation
                        var center: GeoLocation = if (location == null)
                            GeoLocation(43.32289, 21.8925)
                        else
                            GeoLocation(location.latitude, location.longitude)

                        val radius = value.toDouble()

                        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radius)
                        val tasks: MutableList<Task<DataSnapshot>> = ArrayList()
                        for (b in bounds) {
                            val q = database.orderByChild("geohash").startAt(b.startHash)
                                .endAt(b.endHash)
                            val task = q.get()
                            tasks.add(task)
                        }

                        Tasks.whenAllComplete(tasks)
                            .addOnCompleteListener { taskList ->
                                val matchingDocuments: MutableList<DataSnapshot> = ArrayList()
                                for (task in tasks) {
                                    if (task.isSuccessful) {
                                        val snapshot = task.result
                                        for (childSnapshot in snapshot!!.children) {
                                            val lat = childSnapshot.child("latitude").getValue(String::class.java) ?: ""
                                            val lng = childSnapshot.child("longitude").getValue(String::class.java) ?: ""
                                            val docLocation = GeoLocation(lat.toDouble(), lng.toDouble())
                                            val distance = GeoFireUtils.getDistanceBetween(docLocation, center)
                                            if (distance <= radius)
                                                matchingDocuments.add(childSnapshot)
                                        }
                                    } else {
                                        // Handle the error here
                                        val exception = task.exception
                                        Log.e("FirebaseError", "Error getting data: $exception")
                                    }

                                    for (document in matchingDocuments) {
                                        var data = document.value as Map<String, Any>
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

                                        var url: String =
                                            "places/${data["name"]}${data["latitude"]}${data["longitude"]}.jpg"
                                        myPlacesViewModel
                                            .addPlace(
                                                MyPlaces(
                                                    data["name"].toString(),
                                                    data["description"].toString(),
                                                    data["latitude"].toString(),
                                                    data["longitude"].toString(),
                                                    data["autor"].toString(),
                                                    grades,
                                                    comments,
                                                    url,
                                                    data["category"].toString(),
                                                    ""
                                                )
                                            )

                                    }

                                    showList(requireView(),myPlacesViewModel.myPlacesList)
                                }
                            }

                    }
                    else
                        if (field.equals("grade")) {
                        val snapshot = withContext(Dispatchers.IO) {
                            database.get().await()
                        }
                        for (dataSnapshot in snapshot.children) {
                            val data = dataSnapshot.value as Map<String, Any>
                            var grades = java.util.HashMap<String, Double>()
                            var sum: Double = 0.0
                            if (data["grades"] != null) {
                                for (g in data["grades"] as java.util.HashMap<String, Double>) {
                                    grades[g.key] = g.value
                                    sum += g.value
                                }
                                sum /= grades.size

                            }
                            var comments = kotlin.collections.HashMap<String, String>()
                            if (data["comments"] != null) {
                                for (c in data["comments"] as kotlin.collections.HashMap<String, String>)
                                    comments[c.key] = c.value
                            }
                            var url: String =
                                "places/${data["name"]}${data["latitude"]}${data["longitude"]}.jpg"
                            if (sum >= value.toDouble())
                                myPlacesViewModel
                                    .addPlace(
                                        MyPlaces(
                                            data["name"].toString(),
                                            data["description"].toString(),
                                            data["latitude"].toString(),
                                            data["longitude"].toString(),
                                            data["autor"].toString(),
                                            grades,
                                            comments,
                                            url,
                                            data["category"].toString(),
                                            dataSnapshot.key.toString()

                                        )
                                    )

                        }
                        showList(requireView(),myPlacesViewModel.myPlacesList)
                    }
                    else{
                        if (type == 0) {
                            val query = database.orderByChild(field).equalTo(value)
                            snapshot = withContext(Dispatchers.IO) {
                                query.get().await()
                            }

                        } else {
                            val query = database.orderByChild(field).startAt(value)
                            snapshot = withContext(Dispatchers.IO) {
                                query.get().await()
                            }

                        }
                        myPlacesViewModel.myPlacesList.addAll(listCreating(snapshot))

                        showList(requireView(),myPlacesViewModel.myPlacesList)
                    }
                }
                catch (e: java.lang.Exception) {
                    Log.w("TAGA", "Greska", e)
                }
            }
        }
    }

    private fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        if (!myPlacesViewModel.selected!!.autor.equals(userName)) {
            val menu = popupMenu.menu.findItem(R.id.editPlace)
            menu.isVisible = false

        }

        popupMenu.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.viewPlace -> {

                    findNavController().navigate(R.id.action_ListFragment_to_ViewFragment)
                    true
                }

                R.id.editPlace -> {

                    findNavController().navigate(R.id.action_ListFragment_to_EditFragment)

                    true
                }

                R.id.showOnMap -> {

                    this.findNavController().navigate(R.id.action_ListFragment_to_MapFragment)

                    true
                }
                R.id.rankPlace -> {

                    this.findNavController().navigate(R.id.action_ListFragment_to_RankFragment)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }
    private fun saveLoginState(userId: String) {
        val sharedPreferences = requireContext().getSharedPreferences("TourApp", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("userId",userId ).apply()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNewLocation(location: Location) {

    }
}