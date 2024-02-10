package com.imeja.nacare_live

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import com.imeja.nacare_live.auth.LoginActivity
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.databinding.ActivityMainBinding
import com.imeja.nacare_live.network.RetrofitCalls

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val retrofitCalls = RetrofitCalls()
    private val formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.programsFragment,
                R.id.patientListFragment,
                R.id.facilityListFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        // Assuming navView is your NavigationView
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navExit -> {
                    // Handle click for additionalMenuItem1
                    formatterClass.deleteSharedPref("isLoggedIn", this)
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    true
                }

                R.id.nav_home -> {
                    // Handle click for additionalMenuItem2
                    drawerLayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.nav_home)
                    true
                }

                R.id.nav_gallery -> {
                    // Handle click for additionalMenuItem2
                    drawerLayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.nav_gallery)
                    true
                }

                R.id.navSyncData -> {
                    // Handle click for additionalMenuItem2
                    true
                }
                // Add more cases for other menu items not in the setOf
                else -> false
            }
        }


        loadPrograms()
    }

    private fun loadPrograms() {
        retrofitCalls.loadProgram(this, "notification")
        retrofitCalls.loadProgram(this, "facility")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}