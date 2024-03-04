package com.capture.app.sync

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.*
import com.capture.app.data.FormatterClass
import com.capture.app.model.EnrollmentPostData
import com.capture.app.model.EventUploadData
import com.capture.app.model.TrackedEntityInstancePostData
import com.capture.app.network.RetrofitCalls
import com.capture.app.room.Converters
import com.capture.app.room.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    private val formatter = FormatterClass()
    private val retrofitCalls = RetrofitCalls()
    val viewModel = MainViewModel(context.applicationContext as Application)

    override fun doWork(): Result {
        // Do the background work here
        // This method is called on a background thread
        if (formatter.isNetworkAvailable(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(3000) // Delay for 3 seconds
                handleFacilityUploads()
                uploadTrackedEvents()
            }
        }
        return Result.success()
    }

    private fun uploadTrackedEvents() {

        val trackedEntityInstances = ArrayList<TrackedEntityInstancePostData>()
        val enrollments = ArrayList<EnrollmentPostData>()
        val tei = viewModel.loadTrackedEntities(context, false)
        if (tei != null) {
            trackedEntityInstances.clear()
            tei.forEach {
                val attributes = Converters().fromJsonAttribute(it.attributes)
                val trackedEntityType = formatter.getSharedPref(
                    "trackedEntity", context
                )
                val programUid = formatter.getSharedPref(
                    "programUid", context
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
                trackedEntityInstances.add(inst)
                CoroutineScope(Dispatchers.IO).launch {
                    retrofitCalls.uploadSingleTrackedEntity(context, inst, server)
                }
            }

        }
    }

    private fun handleFacilityUploads() {
        val data = viewModel.getAllFacilityData(context, false)
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
                            context,
                            payload,
                            "${it.id}", it.isServerSide, it.uid
                        )
                    }


                }
            }
        }
    }


    companion object {
        private const val SYNC_WORK_TAG = "sync_work"

        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    SYNC_WORK_TAG,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    syncWorkRequest
                )
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(SYNC_WORK_TAG)
        }
    }
}