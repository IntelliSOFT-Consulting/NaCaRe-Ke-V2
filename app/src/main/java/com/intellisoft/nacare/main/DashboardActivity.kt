package com.intellisoft.nacare.main

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.gson.Gson
import com.intellisoft.nacare.auth.Login
import com.intellisoft.nacare.helper_class.EntityAttributes
import com.intellisoft.nacare.helper_class.EntityEnrollments
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.helper_class.PatientPayload
import com.intellisoft.nacare.helper_class.Person
import com.intellisoft.nacare.helper_class.ProgramStages
import com.intellisoft.nacare.main.registry.RegistryActivity
import com.intellisoft.nacare.models.Constants.PROGRAM_TRACKED_ENTITY_TYPE
import com.intellisoft.nacare.models.Constants.TRACKED_ENTITY_TYPE
import com.intellisoft.nacare.network_request.RetrofitCalls
import com.intellisoft.nacare.room.Converters
import com.intellisoft.nacare.room.MainViewModel
import com.intellisoft.nacare.util.AppUtils
import com.nacare.capture.R
import com.nacare.capture.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private var formatterClass = FormatterClass()
    private val retrofitCalls = RetrofitCalls()
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

        val receivedIntent = intent
        if (receivedIntent != null) {
            val data = receivedIntent.getStringExtra("searchPatient")
            if (data == "searchPatient") {
                openEventList()
                val event = viewModel.loadLatestEvent(this@DashboardActivity)
                if (event != null) {
                    formatterClass.saveSharedPref("date", event.date, this@DashboardActivity)
                    formatterClass.saveSharedPref("code", event.orgUnitCode, this@DashboardActivity)
                    formatterClass.saveSharedPref("name", event.orgUnitName, this@DashboardActivity)

                    val bundle = Bundle()
                    val converters = Converters().toJsonEvent(event)
                    bundle.putString("event", converters)
                    formatterClass.saveSharedPref("event", converters, this@DashboardActivity)

                    val intent = Intent(this@DashboardActivity, RegistryActivity::class.java)
                    intent.putExtra("data", bundle)
                    intent.putExtra("searchPatient", "searchPatient")
                    startActivity(intent)
                }
            }
        }
//        syncData()
        prepareEventData()
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.landingFragment,
                    R.id.nav_home,
                    R.id.nav_gallery,
                    R.id.nav_slideshow,
                    R.id.settingsFragment,
                    R.id.programsFragment,
                    R.id.facilityFragment,
                    R.id.eventsFragment
                ), drawerLayout
            )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.landingFragment -> {
                    // Handle Home item click
                    navController.navigate(R.id.landingFragment)
                }

                R.id.nav_home -> {
                    // Handle Home item click
                    navController.navigate(R.id.landingFragment)
                }

                R.id.nav_settings -> {
                    // Handle Settings item click
                    navController.navigate(R.id.settingsFragment)
                }

                R.id.nav_cases -> {
                    openEventList()
                }

                R.id.nav_set_pin -> {
                }

                R.id.nav_logout -> {
                    // Handle Logout item click
                    // Implement logout logic here
                    showLogoutConfirmationDialog()
                }

                R.id.nav_about -> {
                    // Handle About item click
//          navController.navigate(R.id.nav_about)
                    syncData()
                }

                R.id.nav_help -> {
                    viewModel.resetAllEvents(this@DashboardActivity)
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

        binding.appBarDashboard.apply {
            tvTitle.text = "The National Cancer Registry of Kenya"
        }

    }

    private fun prepareEventData() {

        // Prepare data for sync
        val data = viewModel.loadProgram(this, "notification")
        if (data != null) {
            val json = data.programStages
            val gson = Gson()
            val items = gson.fromJson(json, Array<ProgramStages>::class.java)
            items.forEach {
                Log.e("TAG", "Event Program Data ${it.id} name ${it.name}")
            }
        }
    }

    private fun syncData() {
        val userId = formatterClass.getSharedPref("username", this@DashboardActivity)
        if (userId != null) {
            val patients = viewModel.getAllPatientsData(this)
            if (patients != null) {
                val entity =
                    formatterClass.getSharedPref(TRACKED_ENTITY_TYPE, this@DashboardActivity)
                if (entity != null) {
                    val programCode =
                        formatterClass.getSharedPref(
                            PROGRAM_TRACKED_ENTITY_TYPE,
                            this@DashboardActivity
                        )
                    if (programCode != null) {
                        val groupedPatients = patients.groupBy { it.eventId }
                        groupedPatients.forEach { (eventId, patientsWithSameEventId) ->

                            val event = viewModel.loadCurrentEvent(this@DashboardActivity, eventId)
                            if (event != null) {
                                val attributes: MutableList<EntityAttributes> = mutableListOf()
                                val enrollments: MutableList<EntityEnrollments> = mutableListOf()
                                attributes.clear()
                                patientsWithSameEventId.forEach {
                                    val ent = EntityAttributes(
                                        displayName = "",
                                        attribute = it.indicatorId,
                                        value = it.value
                                    )
                                    attributes.add(ent)

                                }
                                enrollments.clear()
                                val enrol = EntityEnrollments(
                                    storedBy = userId,
                                    createdAtClient = formatterClass.getFormattedDate(),
                                    program = programCode
                                )
                                enrollments.add(enrol)
                                val payload = PatientPayload(
                                    trackedEntityType = entity,
                                    orgUnit = event.orgUnitCode,
                                    attributes = attributes,
                                    enrollments = enrollments
                                )
                                if (event.saved) {
                                    retrofitCalls.registerPatient(
                                        this@DashboardActivity,
                                        payload,
                                        event
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
                openEventList()
                true
            }
            // Add more cases for other menu items if needed
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openEventList() {
        formatterClass.deleteSharedPref("date", this@DashboardActivity)
        formatterClass.deleteSharedPref("code", this@DashboardActivity)
        formatterClass.deleteSharedPref("name", this@DashboardActivity)
        val hostNavController = findNavController(R.id.nav_host_fragment_content_dashboard)
        hostNavController.navigate(R.id.nav_gallery)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
