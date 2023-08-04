package com.intellisoft.hai.navigation_drawer.fragments;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intellisoft.hai.helper_class.DbDataEntry;
import com.intellisoft.hai.helper_class.PositionStatus;
import com.intellisoft.hai.helper_class.SubmissionQueue;
import com.intellisoft.hai.helper_class.SubmissionsStatus;
import com.intellisoft.hai.helper_class.FormatterClass;
import com.intellisoft.hai.helper_class.NavigationValues;
import com.intellisoft.hai.R;
import com.intellisoft.hai.navigation_drawer.MainActivity;
import com.intellisoft.hai.room.Converters;
import com.intellisoft.hai.room.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class FragmentSubmission extends Fragment {

    private Button btnAdd;
    private FormatterClass formatterClass = new FormatterClass();
    private MainViewModel myViewModel;
    private RecyclerView mRecyclerView;
//    ArrayList<Submissions> submissionArrayList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_submission, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

//        submissionArrayList = new ArrayList<>();
        myViewModel = new MainViewModel(((Application) requireContext().getApplicationContext()));
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnAdd = rootView.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(view -> {

            formatterClass.saveSharedPref(NavigationValues.NAVIGATION.name(),
                    NavigationValues.DATA_ENTRY.name(), requireContext());

            saveSubmission(SubmissionsStatus.DRAFT.name());

            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);

        });

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveSubmission(String status) {
        //Create a entity of the date it was pressed
        String userId = formatterClass.getSharedPref("username", requireContext());
        formatterClass.deleteSharedPref(PositionStatus.CURRENT.name(), requireContext());
        if (userId != null) {
            String date = formatterClass.getCurrentDate();
            String year = formatterClass.getYear();
//            Submissions submissions = new Submissions(
//                    date,
//                    "",
//                    "",
//                    status,
//                    userId,
//                    year,loadInitialData(),"",false
//            );
//            myViewModel.initiateSubmissions(submissions);
            formatterClass.saveSharedPref(SubmissionQueue.INITIATED.name(), "", requireContext());
        }
    }

//    private String loadInitialData() {
//        IndicatorsData indicatorsData = myViewModel.getAllMyData(requireContext());
//        if (indicatorsData != null) {
//            String jsonData = indicatorsData.getJsonData();
//            Converters converters = new Converters();
//            DbDataEntry dataEntry = converters.fromJson(jsonData);
//            String referenceSheet = dataEntry.getReferenceSheet();
//            formatterClass.saveSharedPref("referenceSheet", referenceSheet, requireContext());
//            return jsonData;
//        }
//        return null;
//    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                showCancelAlertDialog();
                formatterClass.saveSharedPref(NavigationValues.NAVIGATION.name(),
                        NavigationValues.HOME.name(), requireContext());
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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

    @Override
    public void onStart() {
        super.onStart();

        getSubmissions();
    }

    private void getSubmissions() {

//        List<Submissions> submissionList = myViewModel.getSubmissions(requireContext());
//
//        SubmissionAdapter dataEntryAdapter = new SubmissionAdapter((ArrayList<Submissions>) submissionList, requireContext());
//        mRecyclerView.setAdapter(dataEntryAdapter);
    }
}