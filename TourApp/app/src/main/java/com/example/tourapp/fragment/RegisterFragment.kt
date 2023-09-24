package com.example.tourapp.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.example.tourapp.R
import com.example.tourapp.data.User
import com.example.tourapp.databinding.ActivityRegisterBinding
import java.io.FileNotFoundException
import java.io.InputStream
import java.security.MessageDigest
import kotlin.random.Random


class RegisterFragment : Fragment() {

    private val random = Random(System.currentTimeMillis())
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private var databaseUser:DatabaseReference?=null
    private var downloadUrl:String=""
    private var CAMERA_REQUEST_CODE = 0
    private var GALLERY_REQUEST_CODE = 0
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Registrovanje: 0 %")
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.progress = 0
        progressDialog.max = 100
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ActivityRegisterBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.regbtnGalerija.setOnClickListener{
            otvoriGaleriju()
        }
        binding.regbtnKamera.setOnClickListener{
            otvoriKameru()
        }
        binding.regbtnReg.setOnClickListener{
            register() }
        binding.regbtnclose.setOnClickListener{
            Navigation.findNavController(binding.root).navigate(R.id.action_RegisterFragment_to_LoginFragment)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView: ImageView = requireView().findViewById<ImageView>(R.id.imgUser)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            try
            {
                val imageStream: InputStream? = requireActivity().contentResolver.openInputStream(selectedImageUri!!)
                val selectedImageBitmap = BitmapFactory.decodeStream(imageStream)
                imageView.setImageBitmap(selectedImageBitmap)

            }
            catch(e: FileNotFoundException)
            {
                e.printStackTrace();
            }
        }
       /* else if (requestCode == CAMERA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val image: Bitmap? = data.extras?.get("data") as Bitmap
            imageView.setImageBitmap(image)
            CAMERA_REQUEST_CODE = 0
        }*/
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
    private fun register() {
        var editName =  requireView().findViewById<EditText>(R.id.regetname)
        var editSurname =  requireView().findViewById<EditText>(R.id.regetprezime)
        var editUserName =  requireView().findViewById<EditText>(R.id.regetusername)
        var editPassword =  requireView().findViewById<EditText>(R.id.regetpassword)
        var editPhone =  requireView().findViewById<EditText>(R.id.regetphone)


        val firstName = editName.text.toString()
        val lastName = editSurname.text.toString()
        val username = editUserName.text.toString()
        val password = hashPassword(editPassword.text.toString())
        val phoneNumber = editPhone.text.toString()

        if (firstName != "" && lastName != "" && username != "" && password != "" && phoneNumber != "") {
            databaseUser =
                FirebaseDatabase.getInstance("https://tourapp-f60cd-default-rtdb.firebaseio.com/")
                    .getReference("Users")

            val storageRef= FirebaseStorage.getInstance().getReference();
            val stringBuilder = StringBuilder()
            for (i in 1..50) {
                val randomDigit = random.nextInt(10)
                stringBuilder.append(randomDigit)
            }
            if (selectedImageUri!=null) {
                progressDialog.show()
                val fileRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
                fileRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {

                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val activityObj: Activity? = this.activity
                            val user = User(username,password,firstName, lastName,phoneNumber ,uri.toString(),0.0,0.0,0.0,0.0,id.toString())
                            if (user.username != null) {
                                val databaseUser = FirebaseDatabase.getInstance("https://tourapp-f60cd-default-rtdb.firebaseio.com/").getReference("Users")
                                databaseUser.child(username).get().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val dataSnapshot = task.result
                                        if (dataSnapshot.exists()) {
                                            Toast.makeText(activityObj, "Vec postoji nalog sa tim usernameom", Toast.LENGTH_LONG).show()
                                        }
                                        else
                                        {
                                            databaseUser?.child(user.username)?.setValue(user)
                                                ?.addOnSuccessListener {
                                                    Navigation.findNavController(binding.root).navigate(R.id.action_RegisterFragment_to_LoginFragment)
                                                    Toast.makeText(activityObj, "Uspesno registrovan korisnik", Toast.LENGTH_LONG).show()
                                                }
                                                ?.addOnFailureListener {
                                                    Toast.makeText(activityObj, "Bezuspesno registrovanje", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }
                                }
                            } else {
                                val activityObj: Activity? = this.activity
                                Toast.makeText(activityObj, "Unesite sve podatke", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val percent = ((100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                        progressDialog.progress = percent
                        progressDialog.setMessage("Registrovanje: $percent %")
                    }
                    .addOnFailureListener {
                        val activityObj: Activity? = this.activity
                        Toast.makeText(activityObj, "Doslo je do greske prilikom uploadovanja slike", Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                    }
            }
            else {
                val activityObj: Activity? = this.activity
                Toast.makeText(activityObj, "Problem sa slikom", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun otvoriGaleriju()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)

    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
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

    private fun otvoriKameru() {
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

    /* private fun otvoriKameru()
    {
            val cameraPermission = android.Manifest.permission.CAMERA
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
            CAMERA_REQUEST_CODE = 1
            if (!hasCameraPermission) {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            } else {
                startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE)
            }


    }
    private val requestPermissionLauncher =
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
        }*/
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v.ushr(4)]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }


    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}