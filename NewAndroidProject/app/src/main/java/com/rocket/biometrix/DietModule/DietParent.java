package com.rocket.biometrix.DietModule;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DietParent.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DietParent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DietParent extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private LinearLayout displayEntriesLayout;

    public DietParent() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DietParent.
     */
    public static DietParent newInstance(String param1, String param2) {
        DietParent fragment = new DietParent();
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

        try{
            NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
            //Change the title of the action bar to reflect the current fragment
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_diet_parent);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_diet_parent, container, false);
        displayEntriesLayout = (LinearLayout)v.findViewById(R.id.pastDietEntries);
        UpdatePreviousEntries(v);
        return v;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void UpdatePreviousEntries(View v)
    {
        try {
            //LocalStorageAccessMood fileAccess = new LocalStorageAccessMood(v.getContext(),null,null,1);

            List<String[]> dietData = LocalStorageAccessDiet.getEntries(v.getContext());

            displayEntriesLayout.removeAllViews();

            for (String[] data : dietData) {
                TextView textView = new TextView(v.getContext());

                //Creates the string that will be displayed.
                StringBuilder str = new StringBuilder();
                str.append(data[0]);
                str.append(" - ");
                str.append(data[1]);
                str.append(" - Calories: ");
                str.append(data[2]);

                textView.setText(str);
                displayEntriesLayout.addView(textView);
            }
        } catch (Exception e)
        {
            Log.i("", e.toString() );
        }
    }
}
