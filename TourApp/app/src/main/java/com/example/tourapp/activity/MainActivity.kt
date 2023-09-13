package com.example.tourapp.activity

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.tourapp.About
import com.example.tourapp.R
import com.example.tourapp.data.ILocationClient
import com.example.tourapp.data.LocationClient
import com.example.tourapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), ILocationClient {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        var iLocationClient: ILocationClient? = null
        var locationClient: LocationClient? = null
        var currentLocation: Location? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationClient = LocationClient(applicationContext, this)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


        binding.fab?.show()


        binding.fab?.setOnClickListener { view ->
            if (navController.currentDestination?.id == R.id.HomeFragment)
                navController.navigate(R.id.action_HomeFragment_to_EditFragment)
            else if (navController.currentDestination?.id == R.id.ListFragment)
                navController.navigate(R.id.action_ListFragment_to_EditFragment)
            else if (navController.currentDestination?.id == R.id.MapFragment)
                navController.navigate(R.id.action_MapFragment_to_EditFragment)

            // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //  .setAction("Action", null).show()

        }


    }



    //override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // menuInflater.inflate(R.menu.menu_my_places_list, menu)
    //  return true
    //  }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.HomeFragment) {
            navController.popBackStack(R.id.LoginFragment, false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.



        when (item.itemId) {
            R.id.menu_map -> {
                when (navController.currentDestination?.id) {
                    R.id.HomeFragment -> {
                        navController.navigate(R.id.action_HomeFragment_to_MapFragment)
                    }
                    R.id.ListFragment -> {
                        navController.navigate(R.id.action_ListFragment_to_MapFragment)
                    }
                    R.id.ProfileFragment -> {
                        navController.navigate(R.id.action_ProfileFragment_to_MapFragment)
                    }
                    R.id.UsersFragment -> {
                        navController.navigate(R.id.action_UsersFragment_to_MapFragment)
                    }
                }
                //Toast.makeText(this,"Show map!", Toast.LENGTH_SHORT).show()

            }
            R.id.menu_logout -> {
                logout()
            }
            R.id.menu_home -> {
                when (navController.currentDestination?.id) {
                    R.id.HomeFragment -> {
                        navController.navigate(R.id.action_HomeFragment_to_HomeFragment)
                    }
                    R.id.ListFragment -> {
                        navController.navigate(R.id.action_ListFragment_to_HomeFragment)
                    }
                    R.id.ProfileFragment -> {
                        navController.navigate(R.id.action_ProfileFragment_to_HomeFragment)
                    }
                    R.id.UsersFragment -> {
                        navController.navigate(R.id.action_UsersFragment_to_HomeFragment)
                    }
                }
            }
            R.id.menu_profile -> {
                when (navController.currentDestination?.id) {
                    R.id.HomeFragment -> {
                        navController.navigate(R.id.action_HomeFragment_to_ProfileFragment)
                    }
                    R.id.MapFragment -> {
                        navController.navigate(R.id.action_MapFragment_to_ProfileFragment)
                    }
                    R.id.ListFragment -> {
                        navController.navigate(R.id.action_ListFragment_to_ProfileFragment)
                    }
                    R.id.UsersFragment -> {
                        navController.navigate(R.id.action_UsersFragment_to_ProfileFragment)
                    }
                }
            }
            R.id.menu_leaderboard -> {
                when (navController.currentDestination?.id) {
                    R.id.HomeFragment -> {
                        navController.navigate(R.id.action_HomeFragment_to_UsersFragment)
                    }
                    R.id.MapFragment -> {
                        navController.navigate(R.id.action_MapFragment_to_UsersFragment)
                    }
                    R.id.ListFragment -> {
                        navController.navigate(R.id.action_ListFragment_to_UsersFragment)
                    }
                    R.id.ProfileFragment -> {
                        navController.navigate(R.id.action_ProfileFragment_to_UsersFragment)
                    }

                }
            }
            R.id.action_new_place -> Toast.makeText(this, "New Place!", Toast.LENGTH_SHORT).show()
            //  R.id.action_my_places_list -> {
            //  this.findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_HomeFragment_to_ListFragment)
            //}
            /** R.id.action_about -> {
            val i: Intent = Intent(this, About::class.java)
            startActivity(i)
            }*/

        }
        return super.onOptionsItemSelected(item)


    }
    private fun logout(){
        val sharedPreferences = this.getSharedPreferences("TourApp", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
        sharedPreferences.edit().remove("username").apply()
        sharedPreferences.edit().remove("password").apply()
        navController.navigate(R.id.action_HomeFragment_to_LoginFragment)
    }
    override fun onDestroy() {
        locationClient?.stop()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onNewLocation(location: Location) {
        currentLocation = location
        iLocationClient?.onNewLocation(location)
    }
}