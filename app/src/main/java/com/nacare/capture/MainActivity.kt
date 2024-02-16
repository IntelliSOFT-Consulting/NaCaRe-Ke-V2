package com.nacare.capture

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.nacare.capture.auth.LoginActivity
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.databinding.ActivityMainBinding
import com.nacare.capture.model.EnrollmentEventUploadData
import com.nacare.capture.model.EnrollmentPostData
import com.nacare.capture.model.EventUploadData
import com.nacare.capture.model.TrackedEntityInstancePostData
import com.nacare.capture.network.RetrofitCalls
import com.nacare.capture.room.Converters
import com.nacare.capture.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val retrofitCalls = RetrofitCalls()
    private val formatter = FormatterClass()
    private val trackedEntityInstances = ArrayList<TrackedEntityInstancePostData>()
    private val enrollments = ArrayList<EnrollmentPostData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        viewModel = MainViewModel(this.applicationContext as Application)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_gallery,
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
                    formatter.deleteSharedPref("isLoggedIn", this)
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
                    try {
                        if (formatter.isNetworkAvailable(this@MainActivity)) {
                            handleDataSync()
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(10000) // Delay for 3 seconds
                                handleFacilityUploads()
                                uploadTrackedEvents()
                            }
                        } else {
                            formatter.showInternetConnectionRequiredDialog(this@MainActivity)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // Add more cases for other menu items not in the setOf
                else -> false
            }
        }

// Access the header view
        val headerView = navView.getHeaderView(0)
        // Set data to the header view
        headerView.findViewById<TextView>(R.id.tv_name).text = getUserData("surname")
        headerView.findViewById<TextView>(R.id.tv_email).text = getUserData("email")
        loadPrograms()
    }

    private fun handleFacilityUploads() {
        val data = viewModel.getAllFacilityData(this, false)
        if (data != null) {
            if (data.isNotEmpty()) {
                data.forEach {
                    val attributes = Converters().fromJsonDataAttribute(it.dataValues)
                    if (attributes.isNotEmpty()) {
                        val payload = EventUploadData(
                            eventDate = it.eventDate,
                            orgUnit = it.orgUnit,
                            program = it.program,
                            status = it.status,
                            dataValues = attributes
                        )
                        retrofitCalls.uploadFacilityData(
                            this@MainActivity,
                            payload,
                            "${it.id}", it.isServerSide,it.uid
                        )
                    }


                }
            }
        }
    }


    private fun uploadTrackedEvents() {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = viewModel.getTrackedEvents(this@MainActivity, false)
                if (data != null) {
                    if (data.isNotEmpty()) {
                        data.forEach {
                            val trackedEntity = viewModel.loadTrackedEntity(it.trackedEntity)
                            if (trackedEntity != null) {
                                if (it.dataValues.isNotEmpty()) {
                                    val attributes =
                                        Converters().fromJsonDataAttribute(it.dataValues)
                                    if (attributes.isNotEmpty()) {
                                        val payload = EnrollmentEventUploadData(
                                            eventDate = it.eventDate,
                                            orgUnit = it.orgUnit,
                                            program = it.program,
                                            programStage = formatter.getSharedPref(
                                                "programStage",
                                                this@MainActivity
                                            ).toString(),
                                            enrollment = trackedEntity.enrollment,
                                            trackedEntityInstance = trackedEntity.trackedEntity,
                                            status = it.status,
                                            dataValues = attributes
                                        )

                                        Log.e("TAG", "Tracked Events Here **** $payload")
                                        retrofitCalls.uploadEnrollmentData(
                                            this@MainActivity,
                                            payload, "${it.id}", it.initialUpload, it.eventUid
                                        )


                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    private fun getUserData(s: String): String {
        var data = ""
        val userData = formatter.getSharedPref("user_data", this)
        if (userData != null) {
            val converters = Converters().fromJsonUser(userData)
            if (s == "surname") {
                data = converters.surname
            }
            if (s == "email") {
                data = converters.email
            }
        }
        return data
    }

    private fun handleDataSync() {
        val tei = viewModel.loadTrackedEntities(this, false)
        if (tei != null) {
            trackedEntityInstances.clear()
            tei.forEach {
                val attributes = Converters().fromJsonAttribute(it.attributes)
                val trackedEntityType = formatter.getSharedPref(
                    "trackedEntity", this
                )
                val programUid = formatter.getSharedPref(
                    "programUid", this
                )
                enrollments.clear()
                enrollments.add(
                    EnrollmentPostData(
                        enrollment = formatter.generateUUID(11),
                        orgUnit = it.orgUnit,
                        program = programUid.toString(),
                        enrollmentDate = it.enrollDate,
                        incidentDate = it.enrollDate,
                    )
                )
                if (it.isSynced) {

                }
                val server = it.trackedEntity//
                val inst = TrackedEntityInstancePostData(
                    orgUnit = it.orgUnit,
                    trackedEntity = server,//it.trackedEntity,
                    attributes = attributes,
                    trackedEntityType = trackedEntityType.toString(),
                    enrollments = enrollments

                )
                Log.e("TAG", "Upload Data Here Enrolled **** $inst")
                trackedEntityInstances.add(inst)
                CoroutineScope(Dispatchers.IO).launch {
                    retrofitCalls.uploadSingleTrackedEntity(this@MainActivity, inst, server)
                }
            }

        }
    }

    private fun loadPrograms() {
        try {
            if (formatter.isNetworkAvailable(this@MainActivity)) {
                CoroutineScope(Dispatchers.IO).launch {
                    retrofitCalls.loadOrganization(this@MainActivity)
                    retrofitCalls.loadProgram(this@MainActivity, "notification")
                    retrofitCalls.loadProgram(this@MainActivity, "facility")
                    retrofitCalls.loadAllSites(this@MainActivity)
                    retrofitCalls.loadAllCategories(this@MainActivity)
                    retrofitCalls.loadAllEvents(this@MainActivity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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