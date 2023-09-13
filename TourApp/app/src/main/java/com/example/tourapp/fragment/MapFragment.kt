package com.example.tourapp.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.activity.MainActivity
import com.example.tourapp.data.ILocationClient
import com.example.tourapp.data.MyPlaces
import com.example.tourapp.databinding.FragmentMapBinding
import com.example.tourapp.model.LocationViewModel
import com.example.tourapp.model.MyPlacesViewModel
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import kotlin.collections.HashMap


class MapFragment : Fragment(), ILocationClient {

    lateinit var map: MapView
    private val locationViewModel: LocationViewModel by activityViewModels()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private var db = FirebaseDatabase.getInstance().reference.child("places")
    private var searchType = "name"

    private lateinit var myMarker: Marker
    var lastCheckedRadioButton: RadioButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        //inflater.inflate(R.layout.fragment_map, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bottom_nav, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivity.iLocationClient = this
        var ctx: Context? = getActivity()?.getApplicationContext()
        var btn2: Button = binding.button3

        val radioGroup: RadioGroup = view.findViewById(R.id.rgTable)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbAutorTabela -> {
                    searchType = "autor"
                }
                R.id.rbCategory -> {
                    searchType = "category"
                }
                R.id.rbRadius -> {
                    searchType = "radius"
                }
                R.id.rbGrade -> {
                    searchType = "grade"
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
        btn2.setOnClickListener {
            radioGroup.clearCheck()

            resetMap()
        }

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences((ctx)))
        map=requireView().findViewById<MapView>(R.id.map)
        map.setMultiTouchControls(true)

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }else {
            //setMyLocationOverlay()
            //setOnMapClickOverlay()
            setupMap(myPlacesViewModel.myPlacesList)

        }
        //da ukazuje na Nis:
      //  map.controller.setZoom(15.0)
       // val startPoint = GeoPoint(43.3209, 21.8958)
       // map.controller.setCenter(startPoint)
    }
    private fun setupMap(list: ArrayList<MyPlaces>){
        removeAllMarkers(map)

        myMarker = Marker(map)


        val drawable =
            ResourcesCompat.getDrawable(resources, org.osmdroid.library.R.drawable.person, null)

        myMarker.apply {
            this.position = GeoPoint(0.0, 0.0)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setOnMarkerClickListener { _, _ ->
                false
            }
            drawable?.toBitmap()?.let { bitmap ->
                icon = BitmapDrawable(
                    resources,
                    Bitmap.createScaledBitmap(
                        bitmap,
                        ((50.0f * resources.displayMetrics.density).toInt()),
                        ((50.0f * resources.displayMetrics.density).toInt()),
                        true
                    )
                )
            }
        }

        val location = MainActivity.currentLocation
        var startPoint: GeoPoint = if (location == null)
            GeoPoint(43.32289, 21.8925)
        else
            GeoPoint(location.latitude, location.longitude)
        map.controller.setZoom(15.0)
        if (myPlacesViewModel.selected != null) {
            startPoint = GeoPoint(
                myPlacesViewModel.selected!!.latitude.toDouble(),
                myPlacesViewModel.selected!!.longitude.toDouble()
            )
        }


        map.controller.animateTo(startPoint)

        list.forEach { place ->
            val marker = Marker(map)
            marker.position = GeoPoint(place.latitude.toDouble(), place.longitude.toDouble())
            marker.title = place.name
            map.overlays.add(marker)
        }
        map.overlays.add(myMarker)
      /**  var startPoint:GeoPoint = GeoPoint(43.3289,21.8958)
        map.controller.setZoom(15.0)
        if(locationViewModel.setLocation){
            setOnMapClickOverlay()
        }
        else{
            if(myPlacesViewModel.selected !=null){
                startPoint = GeoPoint(myPlacesViewModel.selected!!.latitude.toDouble(),myPlacesViewModel.selected!!.longitude.toDouble())

            }else{
                setMyLocationOverlay()
            }
        }
        map.controller.animateTo(startPoint)
      */

    }
    fun createList(snapshot: DataSnapshot): kotlin.collections.ArrayList<MyPlaces> {

        var list: kotlin.collections.ArrayList<MyPlaces> = ArrayList()

        for (dataSnapshot in snapshot.children) {
            val data = dataSnapshot.value as Map<String, Any>
            var grades = HashMap<String, Double>()
            if (data["grades"] != null) {
                for (g in data["grades"] as HashMap<String, Double>)
                    grades[g.key] = g.value
            }
            var comments = HashMap<String, String>()
            if (data["comments"] != null) {
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
                    dataSnapshot.key.toString()

                )
            )

        }
        return list
    }

    fun resetMap() {
        myPlacesViewModel.myPlacesList.clear()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val snapshot = withContext(Dispatchers.IO) {
                    db.get().await()
                }

                myPlacesViewModel.myPlacesList.addAll(createList(snapshot))

                setupMap(myPlacesViewModel.myPlacesList)
            } catch (e: java.lang.Exception) {
                Log.w("TAGA", "Greska", e)
            }
        }
    }

    fun removeAllMarkers(map: MapView) {
        val markers = map.overlays
            .filterIsInstance<Marker>()
            .toMutableList()

        markers.forEach { map.overlays.remove(it) }
        map.invalidate()
    }

    private fun filterMap(field: String, value: String, type: Int) {
        if (value != "") {
            myPlacesViewModel.myPlacesList.clear()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    var snapshot: DataSnapshot
                    if (field == "radius") {
                        val location = MainActivity.currentLocation
                        var center: GeoLocation = if (location == null)
                            GeoLocation(43.32289, 21.8925)
                        else
                            GeoLocation(location.latitude, location.longitude)

                        val radius = value.toDouble()

                        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radius)
                        val tasks: MutableList<Task<DataSnapshot>> = ArrayList()
                        for (b in bounds) {
                            val q = db.orderByChild("geohash").startAt(b.startHash)
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

                                    setupMap(myPlacesViewModel.myPlacesList)
                                }
                            }

                    } else
                        if (field.equals("grade")) {
                            val snapshot = withContext(Dispatchers.IO) {
                                db.get().await()
                            }
                            for (dataSnapshot in snapshot.children) {
                                val data = dataSnapshot.value as Map<String, Any>
                                val gradesData = data["grades"] as Map<String, Double>?
                                val commentsData = data["comments"] as Map<String, String>?

                                val grades = gradesData?.toMutableMap() ?: mutableMapOf()
                                val comments = commentsData?.toMutableMap() ?: mutableMapOf()

                                var sum: Double = 0.0
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
                                                grades as HashMap<String, Double>,
                                                comments as HashMap<String, String>,
                                                url,
                                                data["category"].toString(),
                                                dataSnapshot.key.toString()

                                            )
                                        )

                            }

                            setupMap(myPlacesViewModel.myPlacesList)

                        } else {
                            if (type == 0) {
                                val query = db.orderByChild(field).equalTo(value)
                                snapshot = withContext(Dispatchers.IO) {
                                    query.get().await()
                                }

                            } else {
                                val query = db.orderByChild(field).startAt(value)
                                snapshot = withContext(Dispatchers.IO) {
                                    query.get().await()
                                }

                            }
                            myPlacesViewModel.myPlacesList.addAll(createList(snapshot))

                            setupMap(myPlacesViewModel.myPlacesList)
                        }
                } catch (e: java.lang.Exception) {
                    Log.w("TAGA", "Greska", e)
                }
            }


        }

    }


    private fun setMyLocationOverlay(){
        var myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(activity), map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add(myLocationOverlay)
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                resetMap()
            }
        }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_new_place -> {
                this.findNavController().navigate(R.id.action_MapFragment_to_EditFragment)
                true
            }
            else
                -> super.onOptionsItemSelected(item)
        }
    }

    private fun setOnMapClickOverlay(){
        var receive = object: MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                var lon = p?.longitude.toString()
                var lat = p?.latitude.toString()
                locationViewModel.setLocation(lon,lat)
                findNavController().popBackStack()
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        var overlayEvents = MapEventsOverlay(receive)
        map.overlays.add(overlayEvents)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        var item=menu.findItem(R.id.menu_map)
        if(item != null)
            item.isVisible = false;
    }

    override fun onResume() {
        super.onResume()
        map.onResume()

        val searchView: SearchView = binding.svTable
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {


                filterMap(searchType, query, 0)
                return true
            }


            override fun onQueryTextChange(newText: String): Boolean {


                filterMap(searchType, newText, 1)
                return true
            }
        })
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        MainActivity.iLocationClient = null
        super.onDestroyView()
        _binding=null
    }

    override fun onNewLocation(location: Location) {
        map.controller.animateTo(GeoPoint(location.latitude, location.longitude))
        myMarker.position = GeoPoint(location.latitude, location.longitude)
    }
}