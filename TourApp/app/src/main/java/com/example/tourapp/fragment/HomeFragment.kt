package com.example.tourapp.fragment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.*
import android.widget.Toast
import android.content.SharedPreferences
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.tourapp.databinding.FragmentHomeBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView



class HomeFragment : Fragment() /*, BottomNavigationView.OnNavigationItemSelectedListener*/
{
    var navigationView: BottomNavigationView? = null
    var firebaseDatabase: FirebaseDatabase? = null
    private lateinit var auth: FirebaseAuth
    var toolbar: MaterialToolbar? = null
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: MaterialToolbar = binding.toolbar
        val navigationView: BottomNavigationView = binding.bottomNav

        // Postavite opcije menija i osluškivače ovde

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    logoutClick()
                    true
                }
                R.id.menu_leaderboard -> {
                    findNavController().navigate(R.id.action_HomeFragment_to_UsersFragment)
                    true
                }
                R.id.action_new_place -> {
                    findNavController().navigate(R.id.action_HomeFragment_to_EditFragment)
                    true
                }
                else -> false
            }
        }

        // Postavite osluškivač za navigaciju na dnu ekrana
        navigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_new_place -> {
                    findNavController().navigate(R.id.action_HomeFragment_to_EditFragment)
                    true
                }
                R.id.map -> {
                    findNavController().navigate(R.id.action_HomeFragment_to_MapFragment)
                    true
                }
                R.id.menu_profile -> {
                    findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
                    true
                }
                R.id.menu_logout -> {
                    logoutClick()
                    true
                }
                R.id.menu_leaderboard -> {
                    findNavController().navigate(R.id.action_HomeFragment_to_UsersFragment)
                    true
                }
                else -> false
            }
        }
    }
    private fun logoutClick() {
        val sharedPreferences = requireActivity().getSharedPreferences("TourApp", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
        sharedPreferences.edit().remove("username").apply()
        sharedPreferences.edit().remove("password").apply()
        findNavController().navigate(R.id.action_HomeFragment_to_LoginFragment)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        // Toast poruka za proveru
        Toast.makeText(requireContext(), "Prikazano na HomeFragment-u", Toast.LENGTH_SHORT).show()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_place -> {
                findNavController().navigate(R.id.action_HomeFragment_to_EditFragment)
                return true
            }
            R.id.map -> {
                findNavController().navigate(R.id.action_HomeFragment_to_MapFragment)
                return true
            }
            R.id.menu_profile -> {
                findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
                return true
            }
            R.id.menu_logout -> {
                logoutClick()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
   /* override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        findViews()
        setListeners()
        setFirebase()
        auth = FirebaseAuth.getInstance()
    }
    private fun logoutClick() {
        closeRemember()
        auth.signOut()
        Navigation.findNavController(binding.root).navigate(R.id.action_HomeFragment_to_LoginFragment)
    }
    private fun findViews() {
        navigationView = view?.findViewById(R.id.bottom_nav)
        toolbar = view?.findViewById(R.id.toolbar)
    }
  /*  private fun setListeners() {
        navigationView?.setOnNavigationItemSelectedListener(this)
    }*/
    private fun closeRemember() {
        val sharedPreferences = requireActivity().getSharedPreferences("remember", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("remember", "false")
        editor.apply()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    logoutClick()
                    true
                }
                R.id.menu_leaderboard -> {
                    this.findNavController().navigate(R.id.action_HomeFragment_to_UsersFragment)
                    true
                }
                R.id.action_new_place -> {
                    this.findNavController().navigate(R.id.action_HomeFragment_to_EditFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main,menu)
        Toast.makeText(this.activity,"prebacio me na home", Toast.LENGTH_SHORT).show()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
             when (item.itemId) {
                /** R.id.action_users -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_ListFragment)
                true
                }*/
                R.id.action_new_place -> {
                    this.findNavController().navigate(R.id.action_HomeFragment_to_EditFragment)
                    return true
                }
                R.id.map -> {
                    this.findNavController().navigate(R.id.action_HomeFragment_to_MapFragment)
                    return true
                }
                R.id.menu_profile -> {
                    this.findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
                    return true
                }
                R.id.menu_logout -> {

                    //this.findNavController().navigate(R.id.action_HomeFragment_to_LoginFragment)
                    return true
                }
               /* R.id.menu_leaderboard -> {
                    this.findNavController().navigate(R.id.action_HomeFragment_to_UsersFragment)
                    return true
                }*/


             }
        return super.onOptionsItemSelected(item)
    }

   /* override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            /** R.id.action_users -> {
            this.findNavController().navigate(R.id.action_HomeFragment_to_ListFragment)
            true
            }*/
            R.id.action_new_place -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_EditFragment)
                return true
            }
            R.id.map -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_MapFragment)
                return true
            }
            R.id.menu_profile -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
                return true
            }
            R.id.menu_logout -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_LoginFragment)
                return true
            }
            R.id.menu_leaderboard -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_UsersFragment)
                return true
            }


        }
        return false
    }*/
    private fun setFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance("https://tourapp-f60cd-default-rtdb.firebaseio.com/")
    }
*/
}