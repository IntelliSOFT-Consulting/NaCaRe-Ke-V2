package com.intellisoft.hai.navigation_drawer.fragments.child;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intellisoft.hai.R;
import com.intellisoft.hai.helper_class.FormatterClass;
import com.intellisoft.hai.helper_class.Information;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentAboutPss#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentAboutPss extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentAboutPss() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentAboutPss.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentAboutPss newInstance(String param1, String param2) {
        FragmentAboutPss fragment = new FragmentAboutPss();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private FormatterClass formatterClass;
    private String about;
    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about_pss, container, false);
        formatterClass = new FormatterClass();
        textView = view.findViewById(R.id.tv_about);
        about = getAbout();
        Spanned spanned = Html.fromHtml(about);
        textView.setText(spanned);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    private String getAbout() {
        String data = "";
        String about = formatterClass.getSharedPref(Information.ABOUT.name(), requireContext());
        if (about != null) {
            data = about;
        }
        return data;
     /*   return "App Version: v2<br>" +
                "SDK Version: 1.1.1<br><br>" +
                "More about this app: read <a href='#'>what's supported</a> and what's not supported</a><br><br>" +
                "Connected to: " + getServerUrl() + "<br>" +
                "Current user: " + getUsername() + "<br><br>" +
                "<a href='#'>Click here</a> to check our Privacy Policy<br><br>" +
                "This and previous versions of this application, as well as the source code are available at <a href='#'>Github</a>.<br>" +
                "PSS V2 Android Capture is Licensed under the terms of the <a href='https://www.gnu.org/licenses/gpl-3.0.en.html'>GNU General Public License</a> as published by the Free Software Foundation<br><br><br>" +
                "Developed and maintained by the IntelliSOFT team.<br><br>" +
                "For more information or questions about this application, please write to <a href='mailto:apps@intellisoftkenya.com'>apps@intellisoftkenya.com</a>";
   */
    }

    private String getUsername() {
        return formatterClass.getSharedPref("username", requireContext());
    }

    private String getServerUrl() {
        return formatterClass.getSharedPref("serverUrl1", requireContext());
    }
}