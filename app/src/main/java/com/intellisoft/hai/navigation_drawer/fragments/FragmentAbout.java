package com.intellisoft.hai.navigation_drawer.fragments;

import static org.apache.commons.lang3.SystemUtils.getUserName;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.intellisoft.hai.R;
import com.intellisoft.hai.helper_class.DbDataEntry;
import com.intellisoft.hai.helper_class.FormatterClass;
import com.intellisoft.hai.helper_class.Information;
import com.intellisoft.hai.navigation_drawer.fragments.child.FragmentAboutApp;
import com.intellisoft.hai.navigation_drawer.fragments.child.FragmentAboutPss;
import com.intellisoft.hai.room.Converters;
import com.intellisoft.hai.room.MainViewModel;

public class FragmentAbout extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    public FragmentAbout() {
    }

    private MainViewModel myViewModel;
    private FormatterClass formatterClass = new FormatterClass();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        myViewModel = new MainViewModel(((Application) requireContext().getApplicationContext()));
        viewPager = rootView.findViewById(R.id.viewPager);
        tabLayout = rootView.findViewById(R.id.tabLayout);
//        pagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
//        pagerAdapter.addFragment(new FragmentAboutPss(), "About PSS");
//        pagerAdapter.addFragment(new FragmentAboutApp(), "About App");
//        viewPager.setAdapter(pagerAdapter);
//        tabLayout.setupWithViewPager(viewPager);
//        tabLayout.setTabTextColors(requireContext().getResources().getColor(R.color.secondaryColor), requireContext().getResources().getColor(R.color.secondaryColor));
//        tabLayout.setSelectedTabIndicatorColor(requireContext().getResources().getColor(R.color.secondaryColor));

        return rootView;
    }



}