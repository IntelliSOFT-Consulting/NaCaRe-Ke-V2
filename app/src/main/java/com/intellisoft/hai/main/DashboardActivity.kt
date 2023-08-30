package com.intellisoft.hai.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.intellisoft.hai.R
import com.intellisoft.hai.auth.Login
import com.intellisoft.hai.databinding.ActivityDashboardBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.main.workflows.RegistrationActivity
import com.intellisoft.hai.util.AppUtils

class DashboardActivity : AppCompatActivity() {

  private lateinit var appBarConfiguration: AppBarConfiguration
  private lateinit var binding: ActivityDashboardBinding
  private var formatterClass = FormatterClass()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityDashboardBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.appBarDashboard.toolbar)
    AppUtils.hideKeyboard(this@DashboardActivity)

    binding.appBarDashboard.fab.setOnClickListener { _ ->
      val intent = Intent(this@DashboardActivity, RegistrationActivity::class.java)
      startActivity(intent)
    }
    val drawerLayout: DrawerLayout = binding.drawerLayout
    val navView: NavigationView = binding.navView
    val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    appBarConfiguration =
        AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
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
          navController.navigate(R.id.nav_settings)
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

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }
}
