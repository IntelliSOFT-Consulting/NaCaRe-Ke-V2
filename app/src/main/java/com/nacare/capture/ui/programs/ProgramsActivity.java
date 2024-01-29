package com.nacare.capture.ui.programs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.google.android.material.button.MaterialButton;
import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.FormatterClass;
import com.nacare.capture.data.service.ActivityStarter;
import com.nacare.capture.ui.base.ListActivity;
import com.nacare.capture.ui.events.EventsActivity;
import com.nacare.capture.ui.tracked_entity_instances.TrackedEntityInstancesActivity;

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ProgramsActivity extends ListActivity implements OnProgramSelectionListener {

    private Disposable disposable;
    private MaterialButton nextButton;

    public static Intent getProgramActivityIntent(Context context) {
        return new Intent(context, ProgramsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp(R.layout.activity_programs, R.id.programsToolbar, R.id.programsRecyclerView);
        observePrograms();
        nextButton = findViewById(R.id.btn_proceed);
        nextButton.setOnClickListener(v -> onBackPressed());

    }

    private void observePrograms() {
        ProgramsAdapter adapter = new ProgramsAdapter(this);
        recyclerView.setAdapter(adapter);

      try {
          disposable = Sdk.d2().organisationUnitModule().organisationUnits().getUids()
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .map(this::getPrograms)
                  .subscribe(programs -> programs.observe(this, programPagedList -> {
                      adapter.submitList(programPagedList);
                      findViewById(R.id.programsNotificator).setVisibility(
                              programPagedList.isEmpty() ? View.VISIBLE : View.GONE);
                  }));
      }catch (Exception e){
          e.printStackTrace();
      }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private LiveData<PagedList<Program>> getPrograms(List<String> organisationUnitUids) {
        return Sdk.d2().programModule().programs()
                .byOrganisationUnitList(organisationUnitUids)
//                .byProgramType().eq(ProgramType.WITH_REGISTRATION)
                .orderByName(RepositoryScope.OrderByDirection.ASC)
                .getPaged(20);
    }

    @Override
    public void onProgramSelected(String programUid, ProgramType programType, String type) {

        new FormatterClass().saveSharedPref("program", type, this);
        new FormatterClass().saveSharedPref("programUid", programUid, this);
        if (programType == ProgramType.WITH_REGISTRATION)
            ActivityStarter.startActivity(this,
                    TrackedEntityInstancesActivity
                            .getTrackedEntityInstancesActivityIntent(this, programUid),
                    false);
        else
            ActivityStarter.startActivity(this,
                    EventsActivity.getIntent(this,
                            programUid),
                    false);
    }
}
