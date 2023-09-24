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

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("places")
    private var userName: String = UserObject.username.toString()
    private lateinit var myUser: User
    private val locationViewModel: LocationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        getList()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_MyPlacesFragment_to_ProfileFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    verride fun onContextItemSelected(item: MenuItem): Boolean{

    }

}