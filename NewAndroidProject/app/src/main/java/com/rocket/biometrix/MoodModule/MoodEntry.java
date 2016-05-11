package com.rocket.biometrix.MoodModule;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rocket.biometrix.Common.DateTimeSelectorPopulateTextView;
import com.rocket.biometrix.Database.AsyncResponse;
import com.rocket.biometrix.Database.JsonCVHelper;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.Sync;
import com.rocket.biometrix.Login.SettingsAndEntryHelper;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoodEntry.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MoodEntry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoodEntry extends Fragment implements AsyncResponse {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TABLENAME_PARAM = "tablename";
    private static final String ROWID_PARAM = "uid";

    private String uid;
    private String tablename; //unused

    View view;

    private OnFragmentInteractionListener mListener;

    public MoodEntry() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MoodEntry.
     */
    public static MoodEntry newInstance(String tablename, String uid) {
        MoodEntry fragment = new MoodEntry();
        Bundle args = new Bundle();
        args.putString(TABLENAME_PARAM, tablename);
        args.putString(ROWID_PARAM, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tablename = getArguments().getString(TABLENAME_PARAM);
            uid = getArguments().getString(ROWID_PARAM);
        }
        else
        {
            uid = null;
        }

        try{
            NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
            //Change the title of the action bar to reflect the current fragment
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_mood_entry);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mood_entry, container, false);

        //setRatingBarListener(view);

        DateTimeSelectorPopulateTextView popDateTime = new DateTimeSelectorPopulateTextView
                (getActivity(), view, R.id.moodCreateEntryDateSelect, R.id.moodCreateEntryTimeSelect);
        popDateTime.Populate();

        SettingsAndEntryHelper.makeDisabledEntryViewsInvisible(view, LocalStorageAccessMood.TABLE_NAME);
        if (uid != null)
        {
            view.findViewById(R.id.mood_entry_done_button).setVisibility(View.GONE);
            SettingsAndEntryHelper.repopulateEntryPage(view, tablename, Integer.parseInt(uid));
        }
        else
        {
            view.findViewById(R.id.mood_entry_update_button).setVisibility(View.GONE);
            view.findViewById(R.id.mood_entry_delete_button).setVisibility(View.GONE);
        }
        return view;
    }




    public void onDoneClick(View v) {
        //get date, and time
        String datetmp = ((TextView) view.findViewById(R.id.moodCreateEntryDateSelect)).getText().toString().substring(11);

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(datetmp);
        } catch (Exception e) {
        }
        format = new SimpleDateFormat("yyyy-MM-dd");
        String dateShort = format.format(date);
        String time = ((TextView) view.findViewById(R.id.moodCreateEntryTimeSelect)).getText().toString().substring(6);

        //String[] data = new String[]{null, username, null, dateShort, time, dep, elev, irr, anx, notes};
        //The below has the affect of the above comment
        String[] data = SettingsAndEntryHelper.prepareColumnArray(view, LocalStorageAccessMood.TABLE_NAME,
                dateShort, time);

        String[] cols = LocalStorageAccessMood.getColumns();

        ContentValues row = new ContentValues();
        int dataIndex = 0;
        for (String col : cols) {
            row.put(col, data[dataIndex++]);
        }
        LocalStorageAccessMood.AddEntry(row, v.getContext());

        Sync sync = new Sync(v.getContext());
        sync.databaseInsertOrUpdateSyncTable(this, row, LocalStorageAccessMood.TABLE_NAME);
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

    /**
     * Called asynchronously when the call to the webserver is done. This method updates the webID
     * reference that is stored on the local database
     * @param result The json encoded regular string that contains the WebID and localID of the
     *               updated row
     */
    public void processFinish(String result)
    {
        //Getting context for LSA constructor
        Context context = view.getContext();

        JsonCVHelper.processServerJsonString(result, context, LocalStorageAccessMood.TABLE_NAME);
    }

}
