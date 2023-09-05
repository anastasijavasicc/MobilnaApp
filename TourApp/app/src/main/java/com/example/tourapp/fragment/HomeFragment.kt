package com.example.tourapp.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.databinding.FragmentHomeBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment()
{
    var navigationView: BottomNavigationView? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var auth: FirebaseAuth? = null
    var toolbar: MaterialToolbar? = null
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main,menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            /** R.id.action_users -> {
            this.findNavController().navigate(R.id.action_HomeFragment_to_ListFragment)
            true
            }*/
            R.id.action_new_place -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_EditFragment)
                true
            }
            R.id.map -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_MapFragment)
                true
            }
            R.id.menu_profile -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
                true
            }
            R.id.menu_logout -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_LoginFragment)
                true
            }
            R.id.menu_leaderboard -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_UsersFragment)
                true
            }
            R.id.menu_home -> {
                this.findNavController().navigate(R.id.action_HomeFragment_to_HomeFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}