package com.intellisoft.hai.navigation_drawer.fragments;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.intellisoft.hai.helper_class.FormatterClass;
import com.intellisoft.hai.helper_class.NavigationValues;
import com.intellisoft.hai.R;
import com.intellisoft.hai.navigation_drawer.MainActivity;
import com.intellisoft.hai.room.MainViewModel;

public class FragmentHome extends Fragment {

    private CardView cardViewSubmission;
    private MainViewModel myViewModel;
    private FormatterClass formatterClass = new FormatterClass();


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
//        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sync_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_1:
                selectAllSubmittedSubmissions();
                return true;
            // Add more cases for other menu items as needed
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectAllSubmittedSubmissions() {


    }

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

    }
}