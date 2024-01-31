package com.nacare.capture.data.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;


public class StringValueTask extends AsyncTask<Void, Void, String> {
    private String value, itemuid;
    private Context context;
    private boolean isEntity;

    public StringValueTask(Context context, String uid, String value, boolean isEntity) {
        this.context = context;
        this.itemuid = uid;
        this.value = value;
        this.isEntity = isEntity;
    }

    @Override
    protected String doInBackground(Void... params) {
        String message = "Data Saved";
        Log.e("TAG", "Details Here *** Saved successfully Is Entity " + isEntity);
        if (isEntity) {
            try {
                String selectedEntity = new FormatterClass().getSharedPref("selectedTei", context);
                Log.e("TAG", "Details Here *** TEI Tracked " + selectedEntity);
                TrackedEntityAttributeValueObjectRepository valueRepository = Sdk.d2().trackedEntityModule().
                        trackedEntityAttributeValues().value(itemuid, selectedEntity);

                valueRepository.blockingSet(value);
                message = "Response Saved Successfully";
                String currentValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : "";
                if (currentValue == null) currentValue = "";

                Log.e("TAG", "Details Here *** Saved successfully " + currentValue);

            } catch (Exception d2Error) {
                d2Error.printStackTrace();
                message = "Response Saved Successfully with Error " + d2Error.getMessage();
                Log.e("TAG", "Response Saved Successfully with Error " + d2Error.getMessage());
            }
        }
        else {
            String eventUid = new FormatterClass().getSharedPref("tracked_event", context);
            Log.e("TAG", "Details Here *** Event Tracked " + eventUid);

            if (eventUid != null) {

                try {
                    TrackedEntityDataValueObjectRepository valueRepository =
                            Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                                    .value(eventUid, itemuid);

                    valueRepository.blockingSet(value);
                    String currentValue = valueRepository.blockingExists() ?
                            valueRepository.blockingGet().value() : "";

                    Log.e("TAG", "Details Here *** Saved successfully " + currentValue);

                } catch (Exception d2Error) {
                    message = "Response Saved Successfully with Error " + d2Error.getMessage();
                    d2Error.printStackTrace();
                    Log.e("TAG", "Details Here *** Response Saved Successfully with Error " + d2Error.getMessage());
                }
            } else {
                Log.e("TAG", "Details Here *** Response Saved Successfully No tracked Entity");
                message = "Response Saved Successfully No tracked Entity";
            }
        }
        return message;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.e("TAG", "Post Results Data **** " + result);
        }
    }
}