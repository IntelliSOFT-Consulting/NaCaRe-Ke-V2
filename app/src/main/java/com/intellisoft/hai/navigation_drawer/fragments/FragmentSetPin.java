package com.intellisoft.hai.navigation_drawer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.intellisoft.hai.R;
import com.intellisoft.hai.helper_class.FormatterClass;
import com.intellisoft.hai.helper_class.PinLockStatus;
import com.intellisoft.hai.pinlockview.IndicatorDots;
import com.intellisoft.hai.pinlockview.PinLockListener;
import com.intellisoft.hai.pinlockview.PinLockView;

public class FragmentSetPin extends Fragment {
    public static final String TAG = "PinLockView";

    private FormatterClass formatterClass = new FormatterClass();
    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView profile_name;

    public FragmentSetPin() {
    }

    private PinLockListener mPinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {
            Log.d(TAG, "Pin complete: " + pin);
            setNavigateBackHome(pin);
        }

        @Override
        public void onEmpty() {
            Log.d(TAG, "Pin empty");
        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
            Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
        }
    };

    private void setNavigateBackHome(String pin) {
        formatterClass.saveSharedPref(PinLockStatus.INITIAL.name(), "true", requireContext());
        formatterClass.saveSharedPref(PinLockStatus.LOCK.name(), pin, requireContext());
        String completed = formatterClass.getSharedPref(PinLockStatus.CONFIRMED.name(), requireContext());
        if (completed == null) {
            formatterClass.saveSharedPref(PinLockStatus.CONFIRMED.name(), "true", requireContext());
            profile_name.setText("Confirm your PIN");
            mPinLockView.resetPinLockView();
        } else {
            Toast.makeText(requireContext(), "Pin Set Successfully", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_set_pin, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        mPinLockView = rootView.findViewById(R.id.pin_lock_view);
        mIndicatorDots = rootView.findViewById(R.id.indicator_dots);
        profile_name = rootView.findViewById(R.id.profile_name);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);
        mPinLockView.setPinLength(4);
        mPinLockView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
        return rootView;
    }

}