package com.example.tourapp.fragment

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tourapp.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.example.tourapp.R.layout.profile_info_dialog

class ProfileFragment: Fragment() {
    val PHONE_DB_NAME = "PhoneNumbers"
    val PROFILE_IMG_STORAGE = "ProfileImages"
    val PICK_IMAGE = 2
    var points = 0

    var addPhoneBtn: MaterialButton? = null
    var auth: FirebaseAuth? = null

    var firebaseDatabase: FirebaseDatabase? = null

    var storage: FirebaseStorage? = null

    var nameTxt: TextView? = null
    var emailTxt: TextView? = null
    var phoneTxt: TextView? = null
    var pointsTxt: TextView? = null
    var profileIMG: ImageView? = null
    var AddPhotoBtn: MaterialButton? = null
    var imageURI: Uri? = null
    var infoButton: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)
        setFirebase()
        val user = auth!!.currentUser
        findViews(view)
        if (user != null) {
            setUI(user)
        }

        AddPhotoBtn!!.setOnClickListener { view: View? ->
            if (view != null) {
                this.addPhoto(
                    view
                )
            }
        }
        return view
    }

    private fun setUI(user: FirebaseUser) {
        val name = user.displayName
        val mail = user.email
        val phone = user.phoneNumber
        val storage_reference =
            storage!!.reference.child(this.PROFILE_IMG_STORAGE).child(
                "$name.jpg"
            )
        if (activity == null) {
            return
        }
        storage_reference.downloadUrl.addOnSuccessListener(OnSuccessListener<Uri?> { uri ->
            if (activity == null) {
                return@OnSuccessListener
            }
            Glide.with(this@ProfileFragment)
                .load(uri)
                .error(R.drawable.ic_launcher_background)
                .circleCrop()
                .into(profileIMG!!)
            nameTxt!!.text = name
            emailTxt!!.text = mail
            phoneTxt!!.text = phone
        })
        infoButton!!.setOnClickListener {
            val dialog = Dialog(context!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.profile_info_dialog)
            dialog.window!!.attributes.width = ActionBar.LayoutParams.FILL_PARENT
            val cancel = dialog.findViewById<Button>(R.id.infoDialogCancel)
            cancel.setOnClickListener { dialog.cancel() }
            dialog.show()
        }
        val reference = firebaseDatabase!!.reference
        reference.child("Points")
            .get().addOnSuccessListener { dataSnapshot ->
                val user = FirebaseAuth.getInstance().currentUser!!.displayName
                for (snapshot in dataSnapshot.children) {
                    val users = snapshot.children
                    for (snapshot1 in users) {
                        if (user == snapshot1.key) {
                            for (sn in snapshot1.children) {
                                points += 20
                            }
                        }
                    }
                }
                pointsTxt!!.text = points.toString()
               /** if (points > 100) {
                    profileIMG.setBorderColor(Color.parseColor("#6AFF00"))
                } else if (points > 200) {
                    profileIMG.setBorderColor(Color.parseColor("#00BCD4"))
                } else if (points > 300) {
                    profileIMG.setBorderColor(Color.parseColor("#FF5722"))
                } else if (points > 400) {
                    profileIMG.setBorderColor(Color.parseColor("#9C27B0"))
                }*/
                reference.child("Leaderboard")
                    .child(FirebaseAuth.getInstance().currentUser!!.displayName!!)
                    .setValue(points)
            }
    }

    private fun findViews(view: View) {
        addPhoneBtn = view.findViewById(R.id.add_number_btn)
        nameTxt = view.findViewById(R.id.name_display)
        emailTxt = view.findViewById(R.id.email_display)
        phoneTxt = view.findViewById(R.id.phone_display)
        pointsTxt = view.findViewById(R.id.point_display)
        profileIMG = view.findViewById(R.id.profile_image)
        AddPhotoBtn = view.findViewById(R.id.add_photo_btn)
        infoButton = view.findViewById(R.id.infoButton)
    }

    private fun addPhoto(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            this.PICK_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == this.PICK_IMAGE) {
            imageURI = data!!.data
            if (imageURI == null) {
                Toast.makeText(activity, "Error adding photo", Toast.LENGTH_SHORT).show()
                return
            }
            profileIMG!!.setImageURI(imageURI)
            storage!!.reference
                .child(this.PROFILE_IMG_STORAGE)
                .child(auth!!.currentUser!!.displayName + ".jpg")
                .putFile(imageURI!!).addOnCompleteListener {
                    Toast.makeText(
                        activity,
                        "Photo added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
/**
    private fun showPhoneDialog(view: View) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.phone_dialog)
        dialog.window!!.attributes.width = ActionBar.LayoutParams.FILL_PARENT
        val phoneTxt = dialog.findViewById<TextInputEditText>(R.id.phone_input)
        val applyBtn = dialog.findViewById<MaterialButton>(R.id.dialog_apply)
        val cancelBtn = dialog.findViewById<MaterialButton>(R.id.dialog_cancel)
        applyBtn.setOnClickListener {
            val phone = phoneTxt.text.toString()
            val user = auth!!.currentUser
            val name = user!!.displayName
            val id = user.uid
            val newPhone = PhoneNumbers(phone, id)
            val reference = firebaseDatabase!!.reference
            reference.child(ProfileFragment.PHONE_DB_NAME).child(name!!).setValue(newPhone)
            Toast.makeText(context, "Phone added", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
*/

    private fun setFirebase() {
        firebaseDatabase =
            FirebaseDatabase.getInstance("https://checkup-f6ce4-default-rtdb.europe-west1.firebasedatabase.app/")
        //firebaseDatabase = FirebaseDatabase.getInstance("https://checkup-f6ce4-default-rtdb.europe-west1.firebasedatabase.app");
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
    }


}