package com.nacare.capture.data.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nacare.capture.data.Sdk;
import com.nacare.capture.data.model.OrgTreeNode;

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository;

import java.util.ArrayList;
import java.util.List;

public class OrganizationTask extends AsyncTask<Void, Void, ArrayList<OrgTreeNode>> {
    private Context context;
    private OnTaskCompletedListener listener;

    private String parentUid;


    public OrganizationTask(Context context, String parentUid, OnTaskCompletedListener listener) {
        this.context = context;
        this.parentUid = parentUid;
        this.listener = listener;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null) {
            listener.startStopProgress(true);
        }

    }

    @Override
    protected ArrayList<OrgTreeNode> doInBackground(Void... voids) {
        ArrayList<OrgTreeNode> orgTreeNodes = new ArrayList<>();
        try {
            OrganisationUnitCollectionRepository repository = Sdk.d2().organisationUnitModule()
                    .organisationUnits();
            OrganisationUnit org = repository
                    .uid(parentUid)
                    .blockingGet();
            ArrayList<OrgTreeNode> children = new ArrayList<>();
            children = generateChildren(parentUid);
            OrgTreeNode otn = new OrgTreeNode(org.displayName(), org.uid(), org.level().toString(), children, false);
            orgTreeNodes.add(otn);

        } catch (
                Exception e) {
            Log.e("TAG", "Experienced Problems **** " + e.getMessage());
        }
        return orgTreeNodes;
    }

    private ArrayList<OrgTreeNode> generateChildren(String uid) {
        ArrayList<OrgTreeNode> orgTreeNodes = new ArrayList<>();
        try {
            OrganisationUnitCollectionRepository repository = Sdk.d2().organisationUnitModule()
                    .organisationUnits();
            List<OrganisationUnit> organisationUnitList = repository
                    .byParentUid().eq(uid)
                    .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet();
            if (!organisationUnitList.isEmpty()) {
                for (OrganisationUnit org : organisationUnitList) {
                    ArrayList<OrgTreeNode> children = new ArrayList<>();

                    if (org.level() <= 5) {
                        children = generateChildren(org.uid());
                    }
                    OrgTreeNode otn = new OrgTreeNode(org.displayName(), org.uid(), org.level().toString(), children, false);
                    orgTreeNodes.add(otn);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orgTreeNodes;
    }

    @Override
    protected void onPostExecute(ArrayList<OrgTreeNode> orgTreeNodes) {
        super.onPostExecute(orgTreeNodes);

        if (orgTreeNodes != null) {
            Log.e("TAG", "Data Retrieved **** " + orgTreeNodes.size());
            if (listener != null) {
                listener.startStopProgress(false);
                listener.onTaskCompleted(orgTreeNodes);
            }
        }
    }

    public interface OnTaskCompletedListener {
        void onTaskCompleted(ArrayList<OrgTreeNode> orgTreeNodes);

        void startStopProgress(boolean show);

    }
}
