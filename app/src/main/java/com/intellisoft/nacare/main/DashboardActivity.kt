package com.intellisoft.nacare.main

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.intellisoft.nacare.auth.Login
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.util.AppUtils
import com.nacare.ke.capture.R
import com.nacare.ke.capture.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private var formatterClass = FormatterClass()
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = MainViewModel((this.applicationContext as Application))
        setSupportActionBar(binding.appBarDashboard.toolbar)
        AppUtils.hideKeyboard(this@DashboardActivity)

        binding.appBarDashboard.fab.setOnClickListener { _ ->

        }
        syncData()
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.nav_home,
                    R.id.nav_gallery,
                    R.id.nav_slideshow,
                    R.id.settingsFragment,
//                    R.id.periFragment,
//                    R.id.patientPreparationFragment,
//                    R.id.skinPreparationFragment,
//                    R.id.handPreparationFragment, R.id.caseSummaryFragment,
//                    R.id.preFragment, R.id.postSummaryFragment,
//                    R.id.postDateFragment, R.id.postFragment, R.id.infectionFragment

                ), drawerLayout
            )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Handle Home item click
                    navController.navigate(R.id.nav_home)
                }

                R.id.nav_settings -> {
                    // Handle Settings item click
                    navController.navigate(R.id.settingsFragment)
                }

                R.id.nav_set_pin -> {
                    // Handle Set PIN item click
//          navController.navigate(R.id.nav_set_pin)
                }

                R.id.nav_logout -> {
                    // Handle Logout item click
                    // Implement logout logic here
                    showLogoutConfirmationDialog()
                }

                R.id.nav_about -> {
                    // Handle About item click
//          navController.navigate(R.id.nav_about)
                }

                R.id.nav_help -> {
                    // Handle Help Desk item click
//          navController.navigate(R.id.nav_help)
                }

                R.id.nav_sync -> {
                    // Handle Sync item click
                    // Implement sync logic here
                }
            }
            // Close the drawer after item selection
            drawerLayout.closeDrawers()
            true
        }

    }

    private fun syncData() {

    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                // Handle Logout item click
                // Implement logout logic here

                formatterClass.saveSharedPref("isLoggedIn", "false", this@DashboardActivity)
                // Add code to navigate to your login screen or perform other necessary actions
                val intent = Intent(this@DashboardActivity, Login::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_settings -> {
                // Handle the first menu item click
                // You can put your code here for the action you want to perform
                true
            }

            R.id.action_clear -> {
                // Handle the second menu item click
//                viewModel.clearEntireDatabase()
                true
            }
            // Add more cases for other menu items if needed
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
