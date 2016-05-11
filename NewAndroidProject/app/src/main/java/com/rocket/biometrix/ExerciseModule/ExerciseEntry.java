package com.rocket.biometrix.ExerciseModule;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rocket.biometrix.Common.DateTimeSelectorPopulateTextView;
import com.rocket.biometrix.Common.StringDateTimeConverter;
import com.rocket.biometrix.Database.AsyncResponse;
import com.rocket.biometrix.Database.JsonCVHelper;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.Sync;
import com.rocket.biometrix.Login.SettingsAndEntryHelper;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExerciseEntry.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExerciseEntry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExerciseEntry extends Fragment implements AsyncResponse{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TABLENAME_PARAM = "tablename";
    private static final String ROWID_PARAM = "uid";

    private String uid;
    private String tablename; //unused
    View onCreateView; //Saves inflated UI view inside onCreateView()

    public static TextView timeTV; //Used by the DateTimePopulateTextView in the onCreate event
    public static TextView dateTV;


    boolean Editing;

    Spinner typeSpinner;

    String minSelected; //string to save minutes exercised spinner result
    String typeSelected; //string to save type of exercise selected in the radio 'bubble' buttons

    Spinner minuteSpinner;
    boolean toasted = false; //Used to display encouraging messages ONCE in minuteSpinner.


    String lowestSpinnerValueThreshold = "5"; //5 minutes
    String lowSpinnerValueThreshold = "10"; //10 minutes (idea is to encourage user to exercise more but still celebrate their 'baby' gains)
    String lowSpinnerMessage = "Keep it up :)"; //The encouraging message
    String highSpinnerMessage = "Nice!"; //The BEST message users strive for

    String[] exerciseEntryData = {}; //String array that will store all user entered data, used in bundles and SQLite insert

    private OnFragmentInteractionListener mListener;

    public ExerciseEntry() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment ExerciseEntry.
     */
    public static ExerciseEntry newInstance(String tablename, String uid) {
        ExerciseEntry fragment = new ExerciseEntry();
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
        try {
            NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
            //Change the title of the action bar to reflect the current fragment
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_exercise_entry);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e) {
        }

    }

    //This is where the real inflater is, it inflate the actual UI layout of the 'entry'
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_exercise_entry, container, false);

        typeSpinner  = (Spinner) v.findViewById(R.id.ex_type);
        ArrayAdapter typeSpin = ArrayAdapter.createFromResource(
                getActivity(), R.array.ex_type_array, android.R.layout.simple_spinner_item);

        typeSpinner.setAdapter(typeSpin);

        //Linking contexts likes non-null variables.
        timeTV = (TextView) v.findViewById(R.id.ex_tv_time);
        dateTV = (TextView) v.findViewById(R.id.ex_tv_date);

        //Slick calls to fill date and time textviews.
        DateTimeSelectorPopulateTextView DTPOWAH = new DateTimeSelectorPopulateTextView(getActivity(), v, R.id.ex_tv_date, R.id.ex_tv_time);
        DTPOWAH.Populate(); //Change the text

        onCreateView = v; //This view (the inflated UI layout view ) is saved so onDoneClick() can use it.
        SettingsAndEntryHelper.makeDisabledEntryViewsInvisible(onCreateView, LocalStorageAccessExercise.TABLE_NAME);

        if (uid != null)
        {
            onCreateView.findViewById(R.id.ex_entry_done_button).setVisibility(View.GONE);
            SettingsAndEntryHelper.repopulateEntryPage(onCreateView, tablename, Integer.parseInt(uid));
        }
        else
        {
            onCreateView.findViewById(R.id.exercise_entry_delete_button).setVisibility(View.GONE);
            onCreateView.findViewById(R.id.exercise_entry_update_button).setVisibility(View.GONE);
        }
        return v;
    }


    //See NavigationDrawerActivity for documentation on why onDoneClick() exists as its own function

    /*
    * What is happening here?
    * I pull data from the UI,
    * Load that data into a ContentValues set (column, data in that column)
    * Error check the CV
    * Call LocalStorageAccessExercise' Insert method with the CV
    *
    * How did I pull data? Used the static helper methods in StringDateTimeConverter (YOU Could have the UI 'pulling' its own helper funtion and/or class if this is too messy)
    * Everything is a string, relying on SQLite's implicit conversion here.
    * I get the context and UI elements from the onCreateView, NOT the done button's view
    * Notice that things like dateTV are declared way up at the top of the class and set in onCreateView() before being used here
    * To 'error' check I just make sure the ContentValues I am passing the insertFromContentValues() has 100% correct column names
    *
    * That's it! I also make a bundle but do nothing with it, purely for future extensibility.
    *
     */
    public void onDoneClick(View v) {
        String dateString = dateTV.getText().toString();
        String timeText = timeTV.getText().toString();

        //Cleaning date and time strings with helper class
        dateString = StringDateTimeConverter.fixDate(dateString);
        //Deactivated for time since this module is storing date in a different manner than all
        //of the others, may need to go back to this later instead, but for all modules.
        //timeString = StringDateTimeConverter.fixTime(timeString);

        timeText = timeText.substring(timeText.indexOf(":") + 2).trim();

        //String[] exerciseEntryData = new String[]{null, username, null, titleString, typeSelected,
        // minSelected, Integer.toString(intensity), notes, dateString, timeString};
        //Has the affect of the comment above
        String[] exerciseEntryData = SettingsAndEntryHelper.prepareColumnArray(onCreateView,
                LocalStorageAccessExercise.TABLE_NAME, dateString, timeText);

        String[] cols = LocalStorageAccessExercise.getColumns();

        //Getting context for LSA constructor
        Context context = onCreateView.getContext();

        //Constructor for LSA Exercise
        LocalStorageAccessExercise dbEx = new LocalStorageAccessExercise(context);
        //Making sure I have data for each column (even if null or empty, note that this is NOT required, you can insert columns individually if you wish.) @see putNull
        //I don't believe the above comment is correct.
        if (cols.length == exerciseEntryData.length) {
            ContentValues rowToBeInserted = new ContentValues();
            int dataIndex = 0;
            for (String column : cols) {
                //Insert column name ripped from LSA child class, and the user's entry data we gathered above
                rowToBeInserted.put(column, exerciseEntryData[dataIndex]);
                dataIndex++;
            }
            //Call insert method
            dbEx.insertFromContentValues(rowToBeInserted, v.getContext());

            Sync sync = new Sync(v.getContext());
            sync.databaseInsertOrUpdateSyncTable(this, rowToBeInserted, LocalStorageAccessExercise.TABLE_NAME);
        }
    }

    /**
     * This method is called when a user is viewing an entry that was previously entered and then
     * clicks update.
     * @param v The current view
     */
    public void onUpdateClick(View v)
    {
        String dateString = dateTV.getText().toString();
        String timeText = timeTV.getText().toString();

        //Cleaning date and time strings with helper class
        dateString = StringDateTimeConverter.fixDate(dateString);
        //Deactivated for time since this module is storing date in a different manner than all
        //of the others, may need to go back to this later instead, but for all modules.
        //timeString = StringDateTimeConverter.fixTime(timeString);

        timeText = timeText.substring(timeText.indexOf(":") + 2).trim();

        //String[] exerciseEntryData = new String[]{null, username, null, titleString, typeSelected,
        // minSelected, Integer.toString(intensity), notes, dateString, timeString};
        //Has the affect of the comment above
        String[] exerciseEntryData = SettingsAndEntryHelper.prepareColumnArray(onCreateView,
                LocalStorageAccessExercise.TABLE_NAME, dateString, timeText);

        Integer webPrimarykey = LocalStorageAccessExercise.getWebKeyFromLocalKey(onCreateView.getContext(), Integer.parseInt(uid));
        //Changes the null of the web primary key to the expected value
        exerciseEntryData[2] = webPrimarykey.toString();
        exerciseEntryData[0] = uid;

        String[] cols = LocalStorageAccessExercise.getColumns();

        //Getting context for LSA constructor
        Context context = onCreateView.getContext();

        //Constructor for LSA Exercise
        LocalStorageAccessExercise dbEx = new LocalStorageAccessExercise(context);
        //Making sure I have data for each column (even if null or empty, note that this is NOT required, you can insert columns individually if you wish.) @see putNull
        //I don't believe the above comment is correct.
        if (cols.length == exerciseEntryData.length) {
            ContentValues rowToUpdate = new ContentValues();
            int dataIndex = 0;
            for (String column : cols) {
                //Insert column name ripped from LSA child class, and the user's entry data we gathered above
                if (exerciseEntryData[dataIndex] != null) {
                    rowToUpdate.put(column, exerciseEntryData[dataIndex]);
                }
                dataIndex++;
            }
            //Call insert method
            dbEx.updateFromContentValues(rowToUpdate, v.getContext(), Integer.parseInt(uid));

            Sync sync = new Sync(v.getContext());
            sync.databaseUpdateOrUpdateSyncTable(this, rowToUpdate, Integer.parseInt(uid), webPrimarykey, LocalStorageAccessExercise.TABLE_NAME);
        }

    }

    /**
     * Deletes the entry on click based on local ID and web ID
     * @param view
     */
    public void onDeleteClick(View view)
    {
        Integer webPrimarykey = LocalStorageAccessExercise.getWebKeyFromLocalKey(onCreateView.getContext(), Integer.parseInt(uid));
        LocalStorageAccessExercise.deleteByLocalKeyValue(onCreateView.getContext(), Integer.parseInt(uid));

        Sync sync = new Sync(onCreateView.getContext());
        sync.databaseDeleteOrUpdateSyncTable(this, Integer.parseInt(uid), webPrimarykey, LocalStorageAccessExercise.TABLE_NAME);
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
        Context context = onCreateView.getContext();

        JsonCVHelper.processServerJsonString(result, context, LocalStorageAccessExercise.TABLE_NAME);
    }
}
