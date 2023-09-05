package com.example.tourapp.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Insets.add
import com.google.android.gms.tasks.OnFailureListener
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.activity.MainActivity
import com.example.tourapp.data.MyPlaces
import com.example.tourapp.data.User
import com.example.tourapp.data.UserObject
import com.example.tourapp.databinding.FragmentEditBinding
import com.example.tourapp.model.LocationViewModel
import com.example.tourapp.model.MyPlacesViewModel
import com.example.tourapp.viewmodels.LoggedUserViewModel
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()

    // Ostalo
    private var CAMERA_REQUEST_CODE: Int = 0
    private var GALLERY_REQUEST_CODE: Int = 0
    private val db = FirebaseDatabase.getInstance("https://tourapp-f60cd-default-rtdb.firebaseio.com/")
    private val storage = Firebase.storage
    private var storageRef = storage.reference
    private lateinit var databasePlace: DatabaseReference

    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ovo sam dodala
        val user = loggedUserViewModel.user
        var userName: String = ""
        if (user != null) {
            userName = user.username
        }

        var addButton: Button = requireView().findViewById(R.id.editmyplace_finished_button)

        val editName: EditText = requireView().findViewById(R.id.editmyplace_name_edit)
        val editDesc: EditText = requireView().findViewById(R.id.editmyplace_desc_edit)
        val editLongitude: EditText = requireView().findViewById(R.id.editmyplace_longitude_label)
        val editCategory: EditText = requireView().findViewById(R.id.editmyplace_category_edit)
        val lonObserver = Observer<String> { newValue ->
            editLongitude.setText(newValue.toString())
        }
        locationViewModel.longitude.observe(viewLifecycleOwner, lonObserver)

        val editLatitude: EditText = requireView().findViewById(R.id.editmyplace_latitude_label)

        val latObserver = Observer<String> { newValue ->
            editLatitude.setText(newValue.toString())
        }
        locationViewModel.latitude.observe(viewLifecycleOwner, latObserver)

        // Dodato
        val editAutor: EditText = requireView().findViewById<EditText>(R.id.editmyplace_autor_edit)
        val ratGrade: RatingBar = requireView().findViewById(R.id.ratingBar)
        //

        if (myPlacesViewModel.selected != null) {
            addButton.isEnabled = true
            editName.setText(myPlacesViewModel.selected?.name)
            editDesc.setText(myPlacesViewModel.selected?.description)
            editLongitude.setText(myPlacesViewModel.selected?.longitude.toString())
            editLatitude.setText(myPlacesViewModel.selected?.latitude.toString())
            editCategory.setText(myPlacesViewModel.selected?.category)
            editAutor.setText(myPlacesViewModel.selected?.autor)
            var sum = 0.0
            for (g in myPlacesViewModel.selected?.grades!!)
                sum += g.value
            if (myPlacesViewModel.selected?.grades!!.size != 0)
                sum /= myPlacesViewModel.selected?.grades!!.size

            ratGrade.rating = sum.toFloat()

            addButton.setText(R.string.edit_fragment_save_label)
            editAutor.isEnabled = false
            ratGrade.isEnabled = false
        } else {

            addButton.isEnabled = false
            val location = MainActivity.currentLocation
            if (location == null) {
                editLongitude.setText("21.8925")
                editLatitude.setText("43.32289")
            } else {
                editLongitude.setText(location.longitude.toString())
                editLatitude.setText(location.latitude.toString())
            }
            //  if(myPlacesViewModel.selected != null)
            //     addButton.setText(R.string.editmyplace_name_label)
            editName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addButton.isEnabled = (editName.text.length > 0)
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            if (user != null) {
                editAutor.setText(userName)
            }
        }
        addButton.setOnClickListener {
            val name: String = editName.text.toString()
            val desc: String = editDesc.text.toString()
            val longitude: String = editLongitude.text.toString()
            val latitude: String = editLatitude.text.toString()
            val autor: String = editAutor.text.toString()
            var starsCount = 0L
            var kommCount = 0L
            val grades = HashMap<String, Double>()
            val category = editCategory.text.toString()
            if (ratGrade.rating.toDouble() != 0.0 && user != null) {
                grades[userName] = ratGrade.rating.toDouble()
                starsCount += 2
            }

            if (myPlacesViewModel.selected != null) {
                myPlacesViewModel.selected?.name = name
                myPlacesViewModel.selected?.description = desc
                myPlacesViewModel.selected?.longitude = longitude
                myPlacesViewModel.selected?.latitude = latitude

                val documentRef = db.getReference("places").child(myPlacesViewModel.selected!!.id)

                val place = hashMapOf(
                    "name" to name,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "description" to desc,
                    "category" to category
                )
                var fragmentContext = requireContext()
                documentRef.updateChildren(place as Map<String, Any>)
                    .addOnSuccessListener {
                        Navigation.findNavController(binding.root).navigate(R.id.action_EditFragment_to_HomeFragment)
                        Toast.makeText(
                            fragmentContext,
                            "Uspesno izmenjeni podaci o mestu",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(fragmentContext, "Greška pri ažuriranju dokumenta", Toast.LENGTH_LONG).show()

                    }
            } else {
                var Place =
                    MyPlaces(
                        name,
                        "",
                        longitude,
                        latitude,
                        autor,
                        grades,
                        HashMap(),
                        desc,
                        category,
                        ""
                    )


                myPlacesViewModel.addPlace(Place)
                val hash = GeoFireUtils.getGeoHashForLocation(
                    GeoLocation(
                        Place.latitude.toDouble(),
                        Place.longitude.toDouble()
                    )
                )

                val place = hashMapOf(
                    "name" to name,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "autor" to autor,
                    "description" to desc,
                    "url" to "",
                    "grades" to grades,
                    "geohash" to hash,
                    "category" to category
                )

                var imageView: ImageView =
                    this@EditFragment.requireView().findViewById(R.id.EditFragmentImg)
                var url = ""

                var context: Context = requireContext()
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            val dataSnapshot = db.getReference("places")
                                .orderByChild("longitude")
                                .equalTo(longitude)
                                .get()
                                .await()

                            val resultList = mutableListOf<MyPlaces>()

                            dataSnapshot.children.forEach { data ->
                                val place = data.getValue(MyPlaces::class.java)
                                if (place != null && place.latitude == latitude) {
                                    resultList.add(place)
                                }
                            }

                            resultList
                        }

                        if (result.isEmpty()) {
                            val newPlaceRef = db.getReference("places").push()
                            newPlaceRef.setValue(place)
                            Place.id = newPlaceRef.key.toString()

                            Place.url = "places/" + Place.id + ".jpg"
                            url = "places/" + Place.id + ".jpg"
                            var imageRef: StorageReference? =
                                storageRef.child("images/" + url)
                            var PlaceRef = storageRef.child(url)

                            imageView.isDrawingCacheEnabled = true
                            imageView.buildDrawingCache()

                            if (imageView.drawable is BitmapDrawable) {
                                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                                val baos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val data = baos.toByteArray()
                                var uploadTask = PlaceRef.putBytes(data)
                                uploadTask.addOnFailureListener { e ->
                                    Log.w("TAGA", "Greska", e)
                                }
                            }
                            val documentRef = db.getReference("places").child(Place.id)
                            val placeUrl = hashMapOf(
                                "url" to "places/" + Place.id + ".jpg"
                            )

                            documentRef.updateChildren(placeUrl as Map<String, Any>)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener {

                                }
                        } else
                            Toast.makeText(
                                context,
                                "Nije uspelo dodavanje jer na lokaciji postoji objekat",
                                Toast.LENGTH_SHORT
                            ).show()

                    } catch (e: Exception) {
                        Log.w("TAGA", "Greska krajnja", e)
                    }
                }
            }
        }
        val cancelButton: Button = requireView().findViewById<Button>(R.id.editmyplace_cancel_button)
        cancelButton.setOnClickListener {
            myPlacesViewModel.selected = null
            findNavController().popBackStack()
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    if (CAMERA_REQUEST_CODE == 1) {
                        startActivityForResult(
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                            CAMERA_REQUEST_CODE
                        )
                    } else if (GALLERY_REQUEST_CODE == 1) {
                        startActivityForResult(
                            Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            ), GALLERY_REQUEST_CODE
                        )
                    }
                }
            }

        val galleryButton: Button = requireView().findViewById(R.id.btnGalerija)
        galleryButton.setOnClickListener {
            val galleryPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            val hasGalleryPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                galleryPermission
            ) == PackageManager.PERMISSION_GRANTED
            GALLERY_REQUEST_CODE = 1
            if (!hasGalleryPermission) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                startActivityForResult(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ), GALLERY_REQUEST_CODE
                )
            }
        }

        val cameraButton: Button = requireView().findViewById(R.id.btnCamera)
        cameraButton.setOnClickListener {
            val cameraPermission = android.Manifest.permission.CAMERA
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
            CAMERA_REQUEST_CODE = 1
            if (!hasCameraPermission) {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            } else {
                startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView: ImageView = requireView().findViewById<ImageView>(R.id.EditFragmentImg)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val image: Bitmap? = data.extras?.get("data") as Bitmap
            imageView.setImageBitmap(image)
            CAMERA_REQUEST_CODE = 0
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            imageView.setImageURI(selectedImage)
            GALLERY_REQUEST_CODE = 0
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        myPlacesViewModel.selected = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

