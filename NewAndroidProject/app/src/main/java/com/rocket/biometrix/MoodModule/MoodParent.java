package com.rocket.biometrix.MoodModule;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.rocket.biometrix.Database.LocalStorageAccessMedication;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoodParent.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MoodParent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoodParent extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private LinearLayout displayEntriesLayout;

    private OnFragmentInteractionListener mListener;

    public MoodParent() { }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MoodParent.
     */
    public static MoodParent newInstance(String param1, String param2) {
        MoodParent fragment = new MoodParent();
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
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_mood_parent);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mood_parent, container, false);
        displayEntriesLayout = (LinearLayout) v.findViewById(R.id.pastMoodEntries);
        UpdatePreviousEntries(v);
        return v;
    }


    private void UpdatePreviousEntries(View v)
    {
        Cursor moodCursor = LocalStorageAccessMood.selectAll(v.getContext(), true);

        displayEntriesLayout.removeAllViews();

        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
                Bundle bundle = new Bundle();
                bundle.putString("uid", v.getTag().toString());
                bundle.putString("tablename", LocalStorageAccessMood.TABLE_NAME);
                nav.CreateEntryOnClickWithBundle(v, bundle);
            }
        };

        while (moodCursor.moveToNext())
        {
            Button button = new Button(v.getContext());

            //Creates the string that will be displayed.
            StringBuilder dispString = new StringBuilder();

            dispString.append(moodCursor.getString(moodCursor.getColumnIndex(LocalStorageAccessMood.DATE)));
            dispString.append(" ");
            dispString.append(moodCursor.getString(moodCursor.getColumnIndex(LocalStorageAccessMood.TIME)));
            dispString.append(" Depr: ");
            dispString.append(moodCursor.getString(moodCursor.getColumnIndex(LocalStorageAccessMood.DEP)));
            dispString.append(" Elev: ");
            dispString.append(moodCursor.getString(moodCursor.getColumnIndex(LocalStorageAccessMood.ELEV)));
            dispString.append(" Anx: ");
            dispString.append(moodCursor.getString(moodCursor.getColumnIndex(LocalStorageAccessMood.ANX)));
            dispString.append(" Irr: ");
            dispString.append(moodCursor.getString(moodCursor.getColumnIndex(LocalStorageAccessMood.IRR)));

            button.setText(dispString);
            button.setTransformationMethod(null);

            button.setOnClickListener(buttonListener);
            button.setTag(moodCursor.getInt(moodCursor.getColumnIndex(LocalStorageAccessMood.LOCAL_MOOD_ID)));
            button.setBackground(getResources().getDrawable(R.drawable.mood_past_entry_button));
            displayEntriesLayout.addView(button);

            Space space = new Space(v.getContext());
            space.setMinimumHeight(7);
            displayEntriesLayout.addView(space );
        }

        moodCursor.close();

    }

    String getDescString(String intStr){
        if (intStr == null) return "error";

        switch (intStr) { //get string based on rating
            case "0": //none
                return getResources().getString(R.string.mood_rating_none);
            case "1": //mild
                return getResources().getString(R.string.mood_rating_mild);
            case "2": //moderate
                return getResources().getString(R.string.mood_rating_mod);
            case "3": //severe
                return getResources().getString(R.string.mood_rating_sev);
            case "4": //very severe
                return getResources().getString(R.string.mood_rating_vsev);
            default:
                return "error";
        }
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
