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

import java.util.List;

public class EventTask extends AsyncTask<Void, Void, String> {
    private Context context;
    String selectedProgram, selectedOrgUnit;
    Enrollment enrollment;

    public EventTask(Context context, Enrollment enrollment, String selectedProgram, String selectedOrgUnit) {
        this.context = context;
        this.enrollment = enrollment;
        this.selectedProgram = selectedProgram;
        this.selectedOrgUnit = selectedOrgUnit;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try { // Assuming you want events for the first enrollment
            EventCollectionRepository eventCollectionRepository = Sdk.d2().eventModule().events().withTrackedEntityDataValues();
            List<Event> events = eventCollectionRepository
                    .byEnrollmentUid().eq(enrollment.uid())
                    .blockingGet();

            if (events.isEmpty()) {
                String stage = Sdk.d2().programModule().programStages()
                        .byProgramUid().eq(selectedProgram)
                        .one().blockingGet().uid();

                String eventUid = Sdk.d2().eventModule().events()
                        .blockingAdd(
                                EventCreateProjection.builder()
                                        .organisationUnit(selectedOrgUnit)
                                        .program(selectedProgram)
                                        .enrollment(enrollment.uid())
                                        .programStage(stage)
                                        .build()
                        );
                Log.e("TAG", "Data Here **** New event created for enrollment: " + enrollment.uid());
                Log.e("TAG", "Data Here **** New event created for enrollment with event: " + eventUid);
                new FormatterClass().saveSharedPref("tracked_event",eventUid,context);
            } else {
                // Now you have a list of events related to the specified enrollment
                for (Event event : events) {
                    // Do something with each event
                    Log.e("TAG", "Data Here **** Event UID: " + event.uid());
                    Log.e("TAG", "Data Here **** Event Lists " + event.trackedEntityDataValues());
                    new FormatterClass().saveSharedPref("tracked_event", event.uid(), context);
                }
            }
            String eventUid=new FormatterClass().getSharedPref("tracked_event", context);
            try {
                EventObjectRepository ev = Sdk.d2().eventModule().events().uid(eventUid);
                Event event = Sdk.d2().eventModule().events().uid(eventUid).blockingGet();
                if (event != null) {

                    Log.e("TAG", "Data Here **** Event Event Date Original: " + event.eventDate());
                    if (event.eventDate() == null) {
                        ev.setEventDate(new FormatterClass().getNowWithoutTime());
                        Log.e("TAG", "Data Here **** Event Event Date Updated: " + event.eventDate());
                    }

                }
            } catch (Exception e) {
                Log.e("TAG", "Error Updating Event Date ***" + e.getMessage());
            }
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
