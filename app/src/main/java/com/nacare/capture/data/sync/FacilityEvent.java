package com.nacare.capture.data.sync;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.ui.main.custom.FacilityDetailsActivity;

import org.hisp.dhis.android.core.event.EventCreateProjection;

public class FacilityEvent extends AsyncTask<Void, Void, String> {
    private AppCompatActivity context;
    String selectedProgram, orgCode;
    private ProgressDialog progressDialog;

    public FacilityEvent(AppCompatActivity context, String selectedProgram, String orgCode) {
        this.context = context;
        this.selectedProgram = selectedProgram;
        this.orgCode = orgCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String eventUid = null;
        try { // Assuming you want events for the first enrollment

            String stage = Sdk.d2().programModule().programStages()
                    .byProgramUid().eq(selectedProgram)
                    .one()
                    .blockingGet()
                    .uid();

            eventUid = Sdk.d2().eventModule().events()
                    .blockingAdd(
                            EventCreateProjection.builder()
                                    .organisationUnit(orgCode)
                                    .program(selectedProgram)
                                    .programStage(stage)
                                    .build()
                    );

            Log.e("TAG", "Facility Details Capture **** " + selectedProgram);
            Log.e("TAG", "Facility Details Capture **** " + orgCode);
            Log.e("TAG", "Facility Details Capture **** " + eventUid);

        } catch (Exception e) {
            Log.e("TAG", "Facility Details Capture **** " + e.getMessage());

        }

        return eventUid;
    }


    @Override
    protected void onPostExecute(String eventUid) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        if (eventUid != null) {
            Log.e("TAG", "Data Here **** Results " + eventUid);
            Intent activityIntent = FacilityDetailsActivity.getIntent(context,
                    eventUid,
                    selectedProgram, orgCode, FacilityDetailsActivity.FormType.CREATE);
//
            ActivityStarter.startActivity(
                    context, activityIntent, false);
        }
    }
}


