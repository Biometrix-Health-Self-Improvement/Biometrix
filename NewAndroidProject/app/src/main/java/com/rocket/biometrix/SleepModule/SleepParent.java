package com.rocket.biometrix.SleepModule;

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SleepParent.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SleepParent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SleepParent extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private LinearLayout displayEntriesLayout;


    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SleepParent() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SleepParent.
     */
    public static SleepParent newInstance(String param1, String param2) {
        SleepParent fragment = new SleepParent();
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
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_sleep_parent);

            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sleep_parent, container, false);

        displayEntriesLayout = (LinearLayout) v.findViewById(R.id.sleepDisplayEntriesLinearLayout);
        UpdatePreviousEntries(v);

        return v;
    }

    /**
     * Updates the scroll view with the information contained in the database for sleep.
     */
    private void UpdatePreviousEntries(View v)
    {
        Cursor sleepCursor = LocalStorageAccessSleep.selectAll(v.getContext());

        displayEntriesLayout.removeAllViews();

        while (sleepCursor.moveToNext())
        {

            TextView textView = new TextView(v.getContext());

            //Creates the string that will be displayed.
            StringBuilder dispString = new StringBuilder();


            dispString.append(sleepCursor.getString(sleepCursor.getColumnIndex(LocalStorageAccessSleep.DATE)));
            dispString.append(" for ");
            dispString.append(sleepCursor.getString(sleepCursor.getColumnIndex(LocalStorageAccessSleep.DURATION)));
            dispString.append(". Quality: ");
            dispString.append(sleepCursor.getString(sleepCursor.getColumnIndex(LocalStorageAccessSleep.QUALITY)));

            textView.setText(dispString);
            displayEntriesLayout.addView(textView);
        }

        sleepCursor.close();

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
}
