package com.nacare.capture.data.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;

import java.util.List;

public class FacilityTask  extends AsyncTask<Void, Void, String> {
    private Context context;
    String eventUid, itemUid,itemValue;
    public FacilityTask(Context context, String eventUid, String itemUid, String itemValue) {
        this.context = context;
        this.eventUid = eventUid;
        this.itemUid = itemUid;
        this.itemValue = itemValue;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try { // Assuming you want events for the first enrollment
            Log.e("TAG", "Data Here ****: " + eventUid);
            Log.e("TAG", "Data Here ****: " + itemUid);
            Log.e("TAG", "Data Here ****: " + itemValue);
            TrackedEntityDataValueObjectRepository valueRepository =
                    Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .value(eventUid, itemUid);
            valueRepository.blockingSet(itemValue);

            Log.e("TAG", "Data Here ****: Record saved successfully" );
        } catch (Exception e) {
            Log.e("TAG", "Data Here **** Error: " + e.getMessage());
        }

        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.e("TAG", "Data Here **** Results " + result);
        }
    }
}
