package com.intellisoft.hai.navigation_drawer.fragments;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.intellisoft.hai.helper_class.DbDataEntry;
import com.intellisoft.hai.helper_class.DbDataEntryDetails;
import com.intellisoft.hai.helper_class.DbDataEntryForm;
import com.intellisoft.hai.helper_class.DbDataEntrySubmit;
import com.intellisoft.hai.helper_class.DbIndicators;
import com.intellisoft.hai.helper_class.DbIndicatorsDetails;
import com.intellisoft.hai.helper_class.DbSaveDataEntry;
import com.intellisoft.hai.helper_class.NavigationValues;
import com.intellisoft.hai.helper_class.FormatterClass;
import com.intellisoft.hai.R;
import com.intellisoft.hai.helper_class.PositionStatus;
import com.intellisoft.hai.helper_class.SubmissionQueue;
import com.intellisoft.hai.helper_class.SubmissionsStatus;
import com.intellisoft.hai.navigation_drawer.MainActivity;
import com.intellisoft.hai.network_request.RetrofitCalls;
import com.intellisoft.hai.room.Converters;
import com.intellisoft.hai.room.MainViewModel;
import com.intellisoft.hai.viewmodels.StatusViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FragmentDataEntry extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 102;
    private MainViewModel myViewModel;
    private static final int REQUEST_IMAGE_PICKER = 1001;
    private FormatterClass formatterClass = new FormatterClass();
    private RecyclerView mRecyclerView;

    private MaterialButton saveDraft, submitSurvey, btnCancel, btnNext;
    private AutoCompleteTextView etPeriod;
    private TextView progressLabel, progressText;
    private RetrofitCalls retrofitCalls = new RetrofitCalls();
    private LinearLayoutManager linearLayoutManager;

    private Map<String, String> stringMap;
    private String organizationsCode;

    private List<String> stringList;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter adapter;
    private String submissionId = "";
    private ProgressBar progressBar;
    private String orgCode = "";
    private int mCurrentActiveItemPosition = 0;
    private GestureDetectorCompat mGestureDetector;

    private StatusViewModel statusViewModel;

    @RequiresApi(api = Build.VERSION_CODES.O)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_data_entry, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        mGestureDetector = new GestureDetectorCompat(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
//        mRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        myViewModel = new MainViewModel(((Application) requireContext().getApplicationContext()));

        saveDraft = rootView.findViewById(R.id.saveDraft);
        submitSurvey = rootView.findViewById(R.id.submitSurvey);
        btnCancel = rootView.findViewById(R.id.btn_cancel);
        btnNext = rootView.findViewById(R.id.btn_next);
        progressLabel = rootView.findViewById(R.id.progress_label);
        progressBar = rootView.findViewById(R.id.progressBar);
        progressText = rootView.findViewById(R.id.progressText);

        etPeriod = rootView.findViewById(R.id.etPeriod);
        autoCompleteTextView = rootView.findViewById(R.id.act_organization);

        saveDraft.setOnClickListener(view -> {
            String period = etPeriod.getText().toString();
            if (TextUtils.isEmpty(period)) {
                etPeriod.setError("Field cannot be empty..");
                etPeriod.requestFocus();
                return;
            }
            String organizationsCode = autoCompleteTextView.getText().toString();
            if (TextUtils.isEmpty(organizationsCode)) {
                autoCompleteTextView.setError("Field cannot be empty..");
                autoCompleteTextView.requestFocus();
                return;
            }
            orgCode = getCodeFromHash(organizationsCode);
            if (TextUtils.isEmpty(orgCode)) {
                autoCompleteTextView.setError("Select organization");
                autoCompleteTextView.requestFocus();
                return;
            }
            formatterClass.saveSharedPref(PositionStatus.CURRENT.name(), "0", requireContext());

            saveSubmission(SubmissionsStatus.DRAFT.name(), period, organizationsCode, orgCode);
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);

        });
        submitSurvey.setOnClickListener(view -> {
            //Create a entity of the date it was pressed
            String period = etPeriod.getText().toString();
            if (TextUtils.isEmpty(period)) {
                etPeriod.setError("Field cannot be empty..");
                etPeriod.requestFocus();
                return;
            }
            String organizationsCode = autoCompleteTextView.getText().toString();
            if (TextUtils.isEmpty(period)) {
                autoCompleteTextView.setError("Field cannot be empty..");
                autoCompleteTextView.requestFocus();
                return;
            }
            orgCode = getCodeFromHash(organizationsCode);
            if (TextUtils.isEmpty(orgCode)) {
                autoCompleteTextView.setError("Select organization");
                autoCompleteTextView.requestFocus();
                return;
            }
            formatterClass.saveSharedPref(PositionStatus.CURRENT.name(), "0", requireContext());
            saveSubmission(SubmissionsStatus.SUBMITTED.name(), period, organizationsCode, orgCode);
//            DbSaveDataEntry dataEntry = myViewModel.getSubmitData(requireContext());
//            if (dataEntry != null) {
//                submissionDialog();
//            } else {
//                Toast.makeText(requireContext(), "Please try again.", Toast.LENGTH_SHORT).show();
//            }

        });
        loadYears();
        loadInitial();

        return rootView;
    }


    private String getCodeFromHash(String organizationsCode) {
//        organizationsList = myViewModel.getOrganizations(requireContext());
//        stringMap = new HashMap<>();
//        for (Organizations organization : organizationsList) {
//            stringMap.put(organization.getIdcode(), organization.getDisplayName());
//            if (organization.getDisplayName().equalsIgnoreCase(organizationsCode)) {
//                orgCode = organization.getIdcode();
//            }
//        }
        return orgCode;
    }

    private void loadYears() {
        ArrayList<String> stringList = new ArrayList<>(generateYears());
        adapter = new ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, stringList);
        etPeriod.setAdapter(adapter);
        if (adapter.getCount() > 0) {
            etPeriod.setText(adapter.getItem(0).toString(), false);
            etPeriod.setSelection(0);
        }

    }

    private Collection<String> generateYears() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i <= 20; i++) {
            years.add(Integer.toString(currentYear - i));
        }
        return years;
    }

    private void submissionDialog() {

        //show this dialog 3 seconds then dismiss plus have the button as weel
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        TextView titleTextView = dialogView.findViewById(R.id.tv_title);
        ImageView okButton = dialogView.findViewById(R.id.dialog_cancel_image);

        okButton.setOnClickListener(v -> {

            navigateToSubmissions();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

// Dismiss the dialog after 3 seconds
        new Handler().postDelayed(() -> {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            navigateToSubmissions();
        }, 2000);

    }

    private void navigateToSubmissions() {

        formatterClass.saveSharedPref(NavigationValues.NAVIGATION.name(),
                NavigationValues.SUBMISSION.name(), requireContext());
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadInitial() {
//        String id = formatterClass.getSharedPref(SubmissionQueue.INITIATED.name(), requireContext());
//
//        if (id != null) {
//            Submissions submission = myViewModel.getSubmission(id, requireContext());
//            if (submission != null) {
//                autoCompleteTextView.setText(submission.getOrganization());
//                etPeriod.setText(submission.getPeriod());
//                submissionId = id;
//                orgCode = submission.getOrgCode();
//            } else {
//                Submissions submission1 = myViewModel.getLatestSubmission(requireContext());
//                if (submission1 != null) {
//                    submissionId = submission1.getId().toString();
//                    orgCode = submission1.getOrgCode();
//                    formatterClass.saveSharedPref(SubmissionQueue.INITIATED.name(), submissionId, requireContext());
//                }
//            }
//            updateProgress();
//        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                handleButtonClicks();
            }
        });


        controlSubmission();
        btnCancel.setOnClickListener(v -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            int lastVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            int totalItemCount = mRecyclerView.getAdapter().getItemCount();
            if (lastVisibleItemPosition < totalItemCount) {
                lastVisibleItemPosition--;
                mRecyclerView.smoothScrollToPosition(lastVisibleItemPosition);
                formatterClass.saveSharedPref(PositionStatus.CURRENT.name(), "" + lastVisibleItemPosition, requireContext());
            }

            handleButtonClicks();
        });
        btnNext.setOnClickListener(v -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            int totalItemCount = mRecyclerView.getAdapter().getItemCount();
            if (lastVisibleItemPosition < totalItemCount) {
                lastVisibleItemPosition++;
                mRecyclerView.smoothScrollToPosition(lastVisibleItemPosition);
                formatterClass.saveSharedPref(PositionStatus.CURRENT.name(), "" + lastVisibleItemPosition, requireContext());
            }
            handleButtonClicks();
        });

        loadOrganizations();

        if (checkIfSubmitted()) {
            autoCompleteTextView.setAdapter(null);
            etPeriod.setAdapter(null);
            autoCompleteTextView.setEnabled(false);
            etPeriod.setEnabled(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showCancelAlertDialog();
            }
        });
    }


    private void showCancelAlertDialog() {
        // Implement your custom logic here
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to cancel?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Handle "Yes" button action
            // ...
            dialog.dismiss();
            formatterClass.saveSharedPref(NavigationValues.NAVIGATION.name(),
                    NavigationValues.HOME.name(), requireContext());
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Handle "Cancel" button action or do nothing
            // ...
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void controlSubmission() {

        String userId = formatterClass.getSharedPref("username", requireContext());
        if (userId != null) {
//            Submissions submission = myViewModel.getSubmissionsById(requireContext(), userId, submissionId);
//            if (submission != null) {
//                if (submission.isSynced()) {
//                    saveDraft.setVisibility(View.INVISIBLE);
//                    submitSurvey.setVisibility(View.GONE);
//                }
//                if (submission.getStatus().equalsIgnoreCase(SubmissionsStatus.SUBMITTED.name())) {
//                    saveDraft.setVisibility(View.INVISIBLE);
//                    submitSurvey.setVisibility(View.GONE);
//                }
//            }
        }
    }

    private void loadOrganizations() {

//        organizationsList = myViewModel.getOrganizations(requireContext());
//        stringMap = new HashMap<>();
//        stringList = new ArrayList<>();
//        for (Organizations organization : organizationsList) {
//            stringList.add(organization.getDisplayName());
//            stringMap.put(organization.getIdcode(), organization.getDisplayName());
//        }
        adapter = new ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, stringList);

        autoCompleteTextView.setAdapter(adapter);
        if (adapter.getCount() > 0) {
            autoCompleteTextView.setText(adapter.getItem(0).toString(), false);
            autoCompleteTextView.setSelection(0);
        }
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            organizationsCode = getOrganizationsCode(autoCompleteTextView.getText().toString());
        });
    }

    private String getOrganizationsCode(String toString) {
        return stringMap.get(toString);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveSubmission(String status, String period, String organizationsCode, String orgCode) {

        //Create a entity of the date it was pressed
        String userId = formatterClass.getSharedPref("username", requireContext());
        if (userId != null) {
            String date = formatterClass.getCurrentDate();
//            Submissions submissions = new Submissions(
//                    date,
//                    organizationsCode,
//                    orgCode,
//                    status,
//                    userId,
//                    period,
//                    retrieveInitialData(), confirmServerId(), false
//            );
            if (submissionId == null) {
                Toast.makeText(requireContext(), "Please try again", Toast.LENGTH_SHORT).show();
            } else {
//                myViewModel.updateSubmissions(submissions, submissionId);
            }
        }
        formatterClass.saveSharedPref(NavigationValues.NAVIGATION.name(),
                NavigationValues.SUBMISSION.name(), requireContext());

    }

    private String retrieveInitialData() {
//        Submissions submission = myViewModel.getSubmission(submissionId, requireContext());
//        if (submission != null) {
//            if (submission.getJsonData().isEmpty()) {
//
//                return updateAndRefresh(submission);
//            } else {
//                return submission.getJsonData();
//            }
//        }
        return "";
    }

//    private String updateAndRefresh(Submissions ab) {
//        Submissions sub = new Submissions(
//                ab.getDate(),
//                ab.getOrganization(),
//                ab.getOrgCode(),
//                ab.getStatus(),
//                ab.getUserId(),
//                ab.getPeriod(),
//                quickData(),
//                ab.getServerId(), ab.isSynced()
//        );
//        myViewModel.updateSubmissions(sub, submissionId);
//        return sub.getJsonData();
//
//    }

//    private String quickData() {
//        IndicatorsData indicatorsData = myViewModel.getAllMyData(requireContext());
//        if (indicatorsData != null) {
//            String jsonData = indicatorsData.getJsonData();
//            Converters converters = new Converters();
//            DbDataEntry dataEntry = converters.fromJson(jsonData);
//            String referenceSheet = dataEntry.getReferenceSheet();
//            formatterClass.saveSharedPref("referenceSheet", referenceSheet, requireContext());
//            return jsonData;
//        }
//        return "";
//    }

//    private String confirmServerId() {
//        Submissions submission = myViewModel.getSubmission(submissionId, requireContext());
//        if (submission != null) {
//            return submission.getServerId();
//        }
//        return null;
//    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("Submission ID ", "Submission ID::::" + submissionId);
        if (submissionId == null) {

        } else {
//            Submissions submission = myViewModel.getSubmission(submissionId, requireContext());
//            if (submission != null) {
//                loadRetrievedIndicators(submission);
//            }
        }
    }

//    private void loadRetrievedIndicators(Submissions data) {
//        if (data != null) {
//            try {
//                int indicatorSize = 0;
//                ArrayList<DbDataEntryForm> dbDataEntryFormList = new ArrayList<>();
//                Converters converters = new Converters();
//                DbDataEntrySubmit dataEntry = converters.fromResubmitJson(data.getJsonData());
//                List<DbDataEntryDetails> detailsList = dataEntry.getDetails();
//                if (!detailsList.isEmpty()) {
//                    for (int j = 0; j < detailsList.size(); j++) {
//
//                        List<DbIndicatorsDetails> indicators = detailsList.get(j).getIndicators();
//
//                        for (int i = 0; i < indicators.size(); i++) {
//                            String categoryId = indicators.get(i).getCategoryId();
//                            String categoryCode = indicators.get(i).getCategoryCode();
//                            String categoryName = indicators.get(i).getCategoryName();
//                            String indicatorName = indicators.get(i).getIndicatorName();
//                            String description = indicators.get(i).getDescription();
//
//                            ArrayList<DbIndicators> indicatorsList = (ArrayList<DbIndicators>) indicators.get(i).getIndicatorDataValue();
//                            indicatorSize = indicatorSize + indicatorsList.size();
//
//                            DbDataEntryForm dbDataEntryForm = new DbDataEntryForm(
//                                    categoryCode, categoryName, indicatorName, categoryId, indicatorsList, description);
//                            dbDataEntryFormList.add(dbDataEntryForm);
//                        }
//                    }
//                    String status = SubmissionsStatus.DRAFT.name();
//                    Submissions submission1 = myViewModel.getSubmission(submissionId, requireContext());
//                    if (submission1 != null) {
//                        status = submission1.getStatus();
//                    }
//                    dataEntryAdapter = new DataEntryAdapter(dbDataEntryFormList, requireContext(), submissionId, status, FragmentDataEntry.this, statusViewModel);
//                    mRecyclerView.setAdapter(dataEntryAdapter);
//
//                    formatterClass.saveSharedPref("indicatorSize",
//                            String.valueOf(indicatorSize), requireContext());
//
//                    controlPagination(indicatorSize);
//                    checkUniqueIndicators(dbDataEntryFormList);
//                } else {
//                    Toast.makeText(requireContext(), "There are no published indicators. Please try again later!!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(requireContext(), MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                loadPublishedIndicators();
//            }
//        }
//    }
//
//    private void loadPublishedIndicators() {
//        IndicatorsData indicatorsData = myViewModel.getAllMyData(requireContext());
//        if (indicatorsData != null) {
//
//            int indicatorSize = 0;
//            ArrayList<DbDataEntryForm> dbDataEntryFormList = new ArrayList<>();
//            String jsonData = indicatorsData.getJsonData();
//
//            Converters converters = new Converters();
//            DbDataEntry dataEntry = converters.fromJson(jsonData);
//            List<DbDataEntryDetails> detailsList = dataEntry.getDetails();
//            String referenceSheet = dataEntry.getReferenceSheet();
//            formatterClass.saveSharedPref("referenceSheet", referenceSheet, requireContext());
//            if (!detailsList.isEmpty()) {
//                for (int j = 0; j < detailsList.size(); j++) {
//
//                    List<DbIndicatorsDetails> indicators = detailsList.get(j).getIndicators();
//
//                    for (int i = 0; i < indicators.size(); i++) {
//                        String categoryId = indicators.get(i).getCategoryId();
//                        String categoryCode = indicators.get(i).getCategoryCode();
//                        String categoryName = indicators.get(i).getCategoryName();
//                        String indicatorName = indicators.get(i).getIndicatorName();
//                        String description = indicators.get(i).getDescription();
//
//                        ArrayList<DbIndicators> indicatorsList = (ArrayList<DbIndicators>) indicators.get(i).getIndicatorDataValue();
//                        indicatorSize = indicatorSize + indicatorsList.size();
//
//                        DbDataEntryForm dbDataEntryForm = new DbDataEntryForm(
//                                categoryCode, categoryName, indicatorName, categoryId, indicatorsList, description);
//                        dbDataEntryFormList.add(dbDataEntryForm);
//                    }
//                }
//                String status = SubmissionsStatus.DRAFT.name();
//                Submissions submission1 = myViewModel.getSubmission(submissionId, requireContext());
//                if (submission1 != null) {
//                    status = submission1.getStatus();
//                }
//                dataEntryAdapter = new DataEntryAdapter(dbDataEntryFormList, requireContext(), submissionId, status, FragmentDataEntry.this, statusViewModel);
//                mRecyclerView.setAdapter(dataEntryAdapter);
//
//                formatterClass.saveSharedPref("indicatorSize",
//                        String.valueOf(indicatorSize), requireContext());
//                Log.e("Data", "Data::: Launch" + indicatorSize);
//                controlPagination(indicatorSize);
//
//                checkUniqueIndicators(dbDataEntryFormList);
//            } else {
//                Toast.makeText(requireContext(), "There are no published indicators. Please try again later!!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(requireContext(), MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//            }
//        }
//    }

    private void checkUniqueIndicators(ArrayList<DbDataEntryForm> dbDataEntryFormList) {
        int count = 0;
        List<String> codeList = new ArrayList<>();
        for (DbDataEntryForm def : dbDataEntryFormList) {
            List<DbIndicators> forms = def.getForms();
            String parent = def.getCategoryName();
//            Generate a list of string codes
            for (DbIndicators form : forms) {
                String code = form.getCode();
                if (!parent.equalsIgnoreCase(code)) {
                    count++;
                    codeList.add(code);
                }
            }
        }

        // Create a Set to store unique items
        Set<String> uniqueCodes = new HashSet<>(codeList);
        // Convert the Set back to a List, if needed
        List<String> uniqueCodeList = new ArrayList<>(uniqueCodes);
        formatterClass.saveSharedPref("indicatorSize",
                String.valueOf(uniqueCodeList.size()), requireContext());
    }

    private void controlPagination(int count) {
        if (count == 0) {
            btnCancel.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.GONE);
        }
        if (count == 1) {
            btnCancel.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.GONE);
            saveDraft.setVisibility(View.VISIBLE);
            submitSurvey.setVisibility(View.VISIBLE);
        } else {
            String pos = formatterClass.getSharedPref(PositionStatus.CURRENT.name(), requireContext());

            if (pos != null) {
                try {
                    int intPos = Integer.parseInt(pos);
                    // Navigate to positions
                    LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                    layoutManager.scrollToPosition(intPos);
//                    dataEntryAdapter.notifyDataSetChanged();
                    String activeItemText = "Page " + intPos + 1 + " / " + count;
                    progressLabel.setText(activeItemText);
                    btnCancel.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    handleButtonClicks();
                }
            }
            handleButtonClicks();
        }
    }

//    public void updateProgress() {
//        String indicatorSize = formatterClass.getSharedPref("indicatorSize", requireContext());
//        Integer answers = myViewModel.getSubmissionResponses(requireContext(), submissionId);
//        try {
//            double total = Double.parseDouble(indicatorSize);
//            double answered = answers;
//
//            double percent = (answered / total) * 100;
//            int percentInt = (int) percent;
//            if (percentInt > 100) {
//                percentInt = 100;
//            }
//            if (percentInt < 0) {
//                percentInt = 0;
//            }
//            progressBar.setProgress(percentInt);
//            progressText.setText(percentInt + "% done");
//
////            Toast.makeText(requireContext(), "Percentage" + percentInt, Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
////            Toast.makeText(requireContext(), "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }


    private void handleButtonClicks() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        int totalItemCount = mRecyclerView.getAdapter().getItemCount();

        int center = mRecyclerView.getWidth() / 2;
        int closestToCenter = Integer.MAX_VALUE;
        int activeItemPosition = -1;
        for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
            View itemView = layoutManager.findViewByPosition(i);
            if (itemView != null) {
                int itemCenter = (itemView.getLeft() + itemView.getRight()) / 2;
                int distance = Math.abs(center - itemCenter);
                if (distance < closestToCenter) {
                    closestToCenter = distance;
                    activeItemPosition = i + 1;
                }
            }
        }
        if (activeItemPosition == -1) {
            activeItemPosition = 1;
        }
        String activeItemText = "Page " + activeItemPosition + " / " + totalItemCount;
        progressLabel.setText(activeItemText);
        if (activeItemPosition == totalItemCount) {
            btnNext.setVisibility(View.GONE);

            boolean isSynced = checkIfSynced();
            boolean isSubmitted = checkIfSubmitted();
            if (isSynced || isSubmitted) {
                submitSurvey.setVisibility(View.GONE);
            } else {
                submitSurvey.setVisibility(View.VISIBLE);
            }
        } else {
            btnNext.setVisibility(View.VISIBLE);
            submitSurvey.setVisibility(View.GONE);
        }

        if (activeItemPosition > 1 && activeItemPosition <= totalItemCount) {
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            btnCancel.setVisibility(View.INVISIBLE);
        }
    }

    private boolean checkIfSubmitted() {
        boolean show = false;
        String userId = formatterClass.getSharedPref("username", requireContext());
        if (userId != null) {
//            Submissions submission = myViewModel.getSubmissionsById(requireContext(), userId, submissionId);
//            if (submission != null) {
//                if (submission.getStatus().equalsIgnoreCase(SubmissionsStatus.SUBMITTED.name())) {
//                    show = true;
//                }
//            }
        }
        return show;
    }

    private Boolean checkIfSynced() {
        boolean show = false;
        String userId = formatterClass.getSharedPref("username", requireContext());
        if (userId != null) {
//            Submissions submission = myViewModel.getSubmissionsById(requireContext(), userId, submissionId);
//            if (submission != null) {
//                if (submission.isSynced()) {
//                    show = true;
//                }
//            }
        }
        return show;
    }


    public void uploadImage(@NotNull String userId, @NotNull String
            indicatorId, @NotNull String submissionId) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.selectImage(userId, indicatorId, submissionId);

    }


    public void considerParentIgnoreChildren(boolean removeParent, String code) {
//        Load the refined indicators
        int count = 0;
        String userId = formatterClass.getSharedPref("username", requireContext());
        if (userId != null) {
//            Submissions submission = myViewModel.getSubmissionsById(requireContext(), userId, submissionId);
//            if (submission != null) {
//                try {
//                    Converters converters = new Converters();
//                    DbDataEntrySubmit dataEntry = converters.fromResubmitJson(submission.getJsonData());
//                    List<DbDataEntryDetails> detailsList = dataEntry.getDetails();
//                    if (!detailsList.isEmpty()) {
//                        for (int j = 0; j < detailsList.size(); j++) {
//                            List<DbIndicatorsDetails> inner = detailsList.get(j).getIndicators();
//                            for (DbIndicatorsDetails dbi : inner) {
//                                String codeCat = dbi.getCategoryName();
//                                if (codeCat.equalsIgnoreCase(code)) {
//                                    count = dbi.getIndicatorDataValue().size() - 1;
//                                }
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
        Log.e("Json", "Indicators:::: " + count);

        String indicatorSize = formatterClass.getSharedPref("indicatorSize", requireContext());
        if (removeParent) {
            // Remove Parent (1) add count
            try {
                int current = Integer.parseInt(indicatorSize);
                int lessParent = current - 1;
                int addChildren = lessParent + count;
                formatterClass.saveSharedPref("indicatorSize", String.valueOf(addChildren),requireContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Remove count add parent (1)
            try {
                int current = Integer.parseInt(indicatorSize);
                int addParent = current + 1;
                int lessChildren = addParent -count;
                formatterClass.saveSharedPref("indicatorSize", String.valueOf(lessChildren),requireContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
//        updateProgress();
    }

//    private void recountIndicators(Submissions data) {
//        if (data != null) {
//            try {
//                int indicatorSize = 0;
//                Converters converters = new Converters();
//                DbDataEntrySubmit dataEntry = converters.fromResubmitJson(data.getJsonData());
//                List<DbDataEntryDetails> detailsList = dataEntry.getDetails();
//                if (!detailsList.isEmpty()) {
//                    for (int j = 0; j < detailsList.size(); j++) {
//
//                        List<DbIndicatorsDetails> indicators = detailsList.get(j).getIndicators();
//
//                        for (int i = 0; i < indicators.size(); i++) {
//                            ArrayList<DbIndicators> indicatorsList = (ArrayList<DbIndicators>) indicators.get(i).getIndicatorDataValue();
//                            indicatorSize = indicatorSize + indicatorsList.size();
//                        }
//                    }
//                    formatterClass.saveSharedPref("indicatorSize",
//                            String.valueOf(indicatorSize), requireContext());
//
//                } else {
//                    Toast.makeText(requireContext(), "There are no published indicators. Please try again later!!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(requireContext(), MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(requireContext(), "There are no published indicators. Please try again later!!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(requireContext(), MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//            }
//        }
//    }
}