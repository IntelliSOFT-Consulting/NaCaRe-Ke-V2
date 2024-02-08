package com.nacare.capture.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.nacare.capture.R;
import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.service.ActivityStarter;

import org.apache.commons.jexl2.Main;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.domain.aggregated.data.AggregatedD2Progress;
import org.hisp.dhis.android.core.tracker.exporter.TrackerD2Progress;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SyncActivity extends AppCompatActivity {
    private CompositeDisposable compositeDisposable;
    private boolean isSyncing = false;

    public static Intent getIntent(Context context) {
        return new Intent(context, SyncActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        compositeDisposable = new CompositeDisposable();
        setSyncing();
        syncMetadata();
        downloadData();
    }

    private Observable<D2Progress> downloadMetadata() {
        return Sdk.d2().metadataModule().download();
    }

    private Observable<TrackerD2Progress> downloadTrackedEntityInstances() {
        return Sdk.d2().trackedEntityModule().trackedEntityInstanceDownloader()
                .limitByOrgunit(false).limitByProgram(false).download();
    }

    private Observable<TrackerD2Progress> downloadSingleEvents() {
        return Sdk.d2().eventModule().eventDownloader()
                .limitByOrgunit(false).limitByProgram(false).download();
    }

    private Observable<AggregatedD2Progress> downloadAggregatedData() {
        return Sdk.d2().aggregatedModule().data().download();
    }

    private void downloadData() {
        try {
            compositeDisposable.add(
                    Observable.merge(
                                    downloadTrackedEntityInstances(),
                                    downloadSingleEvents(),
                                    downloadAggregatedData()
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete(() -> {
                                ActivityStarter.startActivity(this, MainActivity.getMainActivityIntent(this), true);
                            })
                            .doOnError(error -> {
                                Toast.makeText(SyncActivity.this, "Please try again later...", Toast.LENGTH_SHORT).show();
                            })
                            .subscribe());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncMetadata() {
        compositeDisposable.add(downloadMetadata()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(error -> {
                    Toast.makeText(SyncActivity.this, "Please try again later...", Toast.LENGTH_SHORT).show();
                })
                .doOnComplete(this::downloadData)
                .subscribe());
    }

    private void setSyncing() {
        isSyncing = true;
    }

    private void setSyncingFinished() {
        isSyncing = false;
    }
}