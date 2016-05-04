package com.rocket.biometrix.ExerciseModule;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rocket.biometrix.Common.DateTimeSelectorPopulateTextView;
import com.rocket.biometrix.Common.StringDateTimeConverter;
import com.rocket.biometrix.Database.AsyncResponse;
import com.rocket.biometrix.Database.DatabaseConnect;
import com.rocket.biometrix.Database.DatabaseConnectionTypes;
import com.rocket.biometrix.Database.JsonCVHelper;
import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Login.LocalAccount;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    View onCreateView; //Saves inflated UI view inside onCreateView()

    private String mParam1;
    private String mParam2;

    public static TextView timeTV; //Used by the DateTimePopulateTextView in the onCreate event
    public static TextView dateTV;

    String minSelected; //string to save minutes exercised spinner result
    String typeSelected; //string to save type of exercise selected in the radio 'bubble' buttons

    Spinner typeSpinner;
    int intensity=0;
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
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExerciseEntry.
     */
    public static ExerciseEntry newInstance(String param1, String param2) {
        ExerciseEntry fragment = new ExerciseEntry();
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
                getActivity(),R.array.ex_type_array,android.R.layout.simple_spinner_item);

        typeSpinner.setAdapter(typeSpin);

        SeekBar inty = (SeekBar) v.findViewById(R.id.ex_intensity_seekbar);
        inty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                intensity = progress;
                                            }

                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }

                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {

                                            }
                                        }

        );



        //Linking contexts likes non-null variables.
        timeTV = (TextView) v.findViewById(R.id.ex_tv_time);
        dateTV = (TextView) v.findViewById(R.id.ex_tv_date);

        //Slick calls to fill date and time textviews.
        DateTimeSelectorPopulateTextView DTPOWAH = new DateTimeSelectorPopulateTextView(getActivity(), v, R.id.ex_tv_date, R.id.ex_tv_time);
        DTPOWAH.Populate(); //Change the text

        onCreateView = v; //This view (the inflated UI layout view ) is saved so onDoneClick() can use it.
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
        //Keep in mind that the 'View' you reference here is only the 'View' for the actual done button
        //NOT the whole UI Layout you made.
        String titleString = StringDateTimeConverter.GetStringFromEditText(onCreateView.findViewById(R.id.ex_title));
        String notes = ((TextView)onCreateView.findViewById(R.id.exDetailsEditText)).getText().toString();
        typeSelected = ((Spinner) onCreateView.findViewById(R.id.ex_type)).getSelectedItem().toString();
        //Filling date and time strings for bundle's string array
        String dateString = dateTV.getText().toString();
        String timeString = timeTV.getText().toString();

        //Cleaning date and time strings with helper class
        dateString = StringDateTimeConverter.fixDate(dateString);
        timeString = StringDateTimeConverter.fixTime(timeString);

        String username = LocalAccount.DEFAULT_NAME;

        if (LocalAccount.isLoggedIn()) {
            username = LocalAccount.GetInstance().GetUsername();
        }

        exerciseEntryData = new String[]{null, username, null, titleString, typeSelected, minSelected, Integer.toString(intensity), notes, dateString, timeString};

        //https://developer.android.com/reference/android/os/Bundle.html
        //Put string array that has all the entries data points in it into a Bundle. This bundle is for future extensibility it is NOT for the parent class.
        Bundle exerciseEntryBundle = new Bundle();
        exerciseEntryBundle.putStringArray("exEntBundKey", exerciseEntryData);

        String[] cols = LocalStorageAccessExercise.getColumns();

        //Getting context for LSA constructor
        Context context = onCreateView.getContext();

        //Constructor for LSA Exercise
        LocalStorageAccessExercise dbEx = new LocalStorageAccessExercise(context);
        //Making sure I have data for each column (even if null or empty, note that this is NOT required, you can insert columns individually if you wish.) @see putNull
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

            if (LocalAccount.isLoggedIn())
            {
                int id = LocalStorageAccessExercise.GetLastID(v.getContext());

                //Adds the primary key of the field to the sync table along with the value marking it
                //needs to be added to the webdatabase
                LocalStorageAccess.getInstance(v.getContext()).insertOrUpdateSyncTable(v.getContext(),
                        LocalStorageAccessExercise.TABLE_NAME, id, -1, LocalStorageAccess.SYNC_NEEDS_ADDED);

                rowToBeInserted.put(LocalStorageAccessExercise.LOCAL_EXERCISE_ID, id);
                rowToBeInserted.remove(LocalStorageAccessExercise.USER_NAME);

                String jsonToInsert = JsonCVHelper.convertToJSON(rowToBeInserted);

                //Trys to insert the user's data
                new DatabaseConnect(this).execute(DatabaseConnectionTypes.INSERT_TABLE_VALUES, jsonToInsert,
                        LocalAccount.GetInstance().GetToken(),
                        DatabaseConnectionTypes.EXERCISE_TABLE);
            }



        }

/*
        //Filling a string that holds title
        String titleString = StringDateTimeConverter.GetStringFromEditText(onCreateView.findViewById(R.id.ex_title));

        //Filling date and time strings for bundle's string array
        String dateString = dateTV.getText().toString();
        String timeString = timeTV.getText().toString();

        //Cleaning date and time strings with helper class
        dateString = StringDateTimeConverter.fixDate(dateString);
        timeString = StringDateTimeConverter.fixTime(timeString);

        //Filling reps/laps string
        String repsString = StringDateTimeConverter.GetStringFromEditText(onCreateView.findViewById(R.id.ex_et_reps));

        //Filling weight/intensity string from its editText found @content_exercise_entry.xml
        String weightString = StringDateTimeConverter.GetStringFromEditText(onCreateView.findViewById(R.id.ex_et_weight));

        //Filling notes string
        String notesString = StringDateTimeConverter.GetStringFromEditText(onCreateView.findViewById(R.id.ex_notes));

        String username = LocalAccount.DEFAULT_NAME;

        if (LocalAccount.isLoggedIn() )
        {
            username = LocalAccount.GetInstance().GetUsername();
        }

        //Make string array to hold all the strings extracted from the user's input on this entry activity
        //{LOCALEXERCISEID, USERNAME, WEBEXERCISEID, TITLE, TYPE, MINUTES, REPS, LAPS, WEIGHT, INTY, NOTES, DATE, TIME, UPDATED}; //No distinction between reps and laps, weight and intensity.
        exerciseEntryData = new String[]{null, username, null, titleString, typeSelected, minSelected, repsString, repsString, weightString, weightString, notesString, dateString, timeString};

        //https://developer.android.com/reference/android/os/Bundle.html
        //Put string array that has all the entries data points in it into a Bundle. This bundle is for future extensibility it is NOT for the parent class.
        Bundle exerciseEntryBundle = new Bundle();
        exerciseEntryBundle.putStringArray("exEntBundKey", exerciseEntryData);


        //Getting context for LSA constructor
        Context context = onCreateView.getContext();

        //Constructor for LSA Exercise
        LocalStorageAccessExercise dbEx = new LocalStorageAccessExercise(context);

        //You don't have to keep strings in the same order across classes, I just did to make the code easier.
        //{TITLE, TYPE, MINUTES, REPS, LAPS, WEIGHT, INTY, NOTES, DATE, TIME};
        String[] columnNames = dbEx.getColumns();//Pull keys from LSA Exercise

        //Making sure I have data for each column (even if null or empty, note that this is NOT required, you can insert columns individually if you wish.) @see putNull
        if (columnNames.length == exerciseEntryData.length) {
            ContentValues rowToBeInserted = new ContentValues();
            int dataIndex = 0;
            for (String column : columnNames) {
                //Insert column name ripped from LSA child class, and the user's entry data we gathered above
                rowToBeInserted.put(column, exerciseEntryData[dataIndex]);
                dataIndex++;
            }
            //Call insert method
            dbEx.insertFromContentValues(rowToBeInserted, v.getContext());

            if (LocalAccount.isLoggedIn())
            {
                int id = LocalStorageAccessExercise.GetLastID(v.getContext());

                //Adds the primary key of the field to the sync table along with the value marking it
                //needs to be added to the webdatabase
                LocalStorageAccess.getInstance(v.getContext()).insertOrUpdateSyncTable(v.getContext(),
                        LocalStorageAccessExercise.TABLE_NAME, id, -1, LocalStorageAccess.SYNC_NEEDS_ADDED);

                rowToBeInserted.put(LocalStorageAccessExercise.LOCAL_EXERCISE_ID, id);
                rowToBeInserted.remove(LocalStorageAccessExercise.USER_NAME);

                String jsonToInsert = JsonCVHelper.convertToJSON(rowToBeInserted);

                //Trys to insert the user's data
                new DatabaseConnect(this).execute(DatabaseConnectionTypes.INSERT_TABLE_VALUES, jsonToInsert,
                        LocalAccount.GetInstance().GetToken(),
                        DatabaseConnectionTypes.EXERCISE_TABLE);
            }



        }
*/
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

        JSONObject jsonObject;
        jsonObject = JsonCVHelper.processServerJsonString(result, context, "Could not create exercise entry on web database");

        if (jsonObject != null)
        {
            int[] tableIDs = new int[2];
            JsonCVHelper.getIDColumns(tableIDs, jsonObject);

            if (tableIDs[0] != -1 && tableIDs[1] != -1)
            {
                LocalStorageAccessExercise.updateWebIDReference(tableIDs[0], tableIDs[1], context, true);
            }
            else
            {
                Toast.makeText(context, "There was an error processing information from the webserver", Toast.LENGTH_LONG).show();
            }

        }

    }
}
