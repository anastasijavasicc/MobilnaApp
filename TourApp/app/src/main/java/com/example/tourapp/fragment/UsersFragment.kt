package com.example.tourapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ListView
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.data.User
import com.example.tourapp.databinding.FragmentUsersBinding
import com.example.tourapp.model.UserListAdapter
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null


    private val binding get() = _binding!!
    private var ascending = 0
    var list: ArrayList<User> = ArrayList()

    private lateinit var database: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        usersReference = database.reference.child("Users")
        var switch: Switch = binding.switch1
        switch.setOnCheckedChangeListener { compoundButton, b ->
            if (b)
                ascending = 1
            else
                ascending = 0

            createList()
        }
        createList()

    }

    fun createList() {
        list.clear()
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val dataSnapshot = usersReference.orderByChild("addCount").get().await()

                for (snapshot in dataSnapshot.children) {
                    val data = snapshot.value as Map<*, *>?

                    if (data != null) {
                        val user = User(
                            data["username"].toString() ?: "",
                            data["password"].toString() ?: "",
                            data["firstName"].toString() ?: "",
                            data["lastName"].toString() ?: "",
                            data["phoneNumber"].toString() ?: "",
                            data["profilePhotoUrl"].toString() ?: "",
                            (data["addCount"] as? Long ?: 0.0).toDouble(),
                            (data["starsCount"] as? Long ?: 0.0).toDouble(),
                            (data["commentsCount"] as? Long ?: 0.0).toDouble(),
                            snapshot.key.toString()
                        )

                        list.add(user)
                    }
                    if (ascending != 1) {
                        list.reverse()
                    }
                }

                withContext(Dispatchers.Main) {
                    showList(requireView(), list)
                }
            } catch (e: Exception) {
                Log.e("TAGA", "Greška", e)
            }
        }

    }

    fun showList(view: View, arrayList: java.util.ArrayList<User>) {


        val listView: ListView = requireView().findViewById(R.id.listUsers)

        val arrayAdapter = UserListAdapter(
            view.context,
            arrayList
        )
        listView.adapter = arrayAdapter

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_new_place -> {
                this.findNavController().navigate(R.id.action_ListFragment_to_EditFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}