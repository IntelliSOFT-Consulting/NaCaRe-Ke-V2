package com.nacare.capture.data.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.ui.main.custom.TrackedEntityInstanceActivity;

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.program.ProgramStage;

public class EnrollmentTask extends AsyncTask<Void, Void, String> {
    private AppCompatActivity context;
    private String trackedUid, selectedProgram, selectedOrganization;

    private ProgressDialog progressDialog;
    private String eventUid, enrollmentUid;


    public EnrollmentTask(AppCompatActivity context, String trackedUid, String selectedProgram, String selectedOrganization) {
        this.context = context;
        this.trackedUid = trackedUid;
        this.selectedProgram = selectedProgram;
        this.selectedOrganization = selectedOrganization;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            EnrollmentCollectionRepository enrollmentCollectionRepository = Sdk.d2().enrollmentModule().enrollments();
            Enrollment enrollment = enrollmentCollectionRepository
                    .byProgram().eq(selectedProgram)
                    .byOrganisationUnit().eq(selectedOrganization)
                    .byTrackedEntityInstance().eq(trackedUid)
                    .one()
                    .blockingGet();

            Log.e("TAG", " Current Enrollment **** " + enrollment);
            if (enrollment == null) {
                enrollmentUid = Sdk.d2().enrollmentModule().enrollments().blockingAdd(
                        EnrollmentCreateProjection.builder()
                                .organisationUnit(selectedOrganization)
                                .program(selectedProgram)
                                .trackedEntityInstance(trackedUid)
                                .build()
                );
                EnrollmentObjectRepository enrollmentRepository = Sdk.d2().enrollmentModule().enrollments().uid(enrollmentUid);
                enrollmentRepository.setEnrollmentDate(new FormatterClass().getNowWithoutTime());
                enrollmentRepository.setIncidentDate(new FormatterClass().getNowWithoutTime());
            } else {
                enrollmentUid = enrollment.uid();
            }
            ProgramStage stage = Sdk.d2().programModule().programStages()
                    .byProgramUid().eq(selectedProgram)
                    .byName().like("ALL")
                    .one()
                    .blockingGet();

            if (stage != null) {

                EventCollectionRepository eventCollectionRepository = Sdk.d2().eventModule().events().withTrackedEntityDataValues();
                Event event = eventCollectionRepository
                        .byEnrollmentUid().eq(enrollmentUid)
                        .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                        .one()
                        .blockingGet();

                if (event != null) {
                    eventUid = event.uid();
                } else {
                    eventUid = Sdk.d2().eventModule().events()
                            .blockingAdd(
                                    EventCreateProjection.builder()
                                            .organisationUnit(selectedOrganization)
                                            .program(selectedProgram)
                                            .programStage(stage.uid())
                                            .enrollment(enrollment.uid())
                                            .build()
                            );

                    Log.e("TAG", " Current Enrollment **** Event Created" + eventUid);
                }

                new FormatterClass().saveSharedPref("tracked_event", eventUid, context);

            } else {
                Toast.makeText(context, "Please select a program stage to proceed", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", " Current Enrollment **** Error " + e.getMessage());
        }
        return eventUid;
    }

    @Override
    protected void onPostExecute(String results) {
        super.onPostExecute(results);
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        if (results != null) {
            ActivityStarter.startActivity(context,
                    TrackedEntityInstanceActivity.getIntent(context, results, trackedUid, selectedProgram, selectedOrganization, false), true);

        }
    }
}
