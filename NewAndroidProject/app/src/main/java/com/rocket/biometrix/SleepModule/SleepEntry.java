package com.rocket.biometrix.SleepModule;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rocket.biometrix.Common.DateTimeSelectorPopulateTextView;
import com.rocket.biometrix.Database.AsyncResponse;
import com.rocket.biometrix.Database.JsonCVHelper;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.Database.Sync;
import com.rocket.biometrix.Login.SettingsAndEntryHelper;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SleepEntry.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SleepEntry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SleepEntry extends Fragment implements AsyncResponse {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TABLENAME_PARAM = "tablename";
    private static final String ROWID_PARAM = "uid";

    private String uid;
    private String tablename; //unused

    private TextView endDateTextView; //Reference for the end date textview so it only is grabbed once
    private SeekBar hourSeekBar;      //Reference for the hour seek bar so it only is grabbed once
    private SeekBar minuteSeekBar;    //Reference for the minute seek bar so it only is grabbed once

    private TextView startDateTextView; //Reference so it only is grabbed once
    private TextView startTimeTextView; //''
    private TextView sleptTimeTextView; //''

    private SeekBar qualitySeekBar;     //''
    private TextView qualityNumberTextView; //''

    private View entryView;

    private OnFragmentInteractionListener mListener;

    //Sets the minimum and the maximum for sleep quality.
    private static final int minQuality = 1;
    private static final int maxQuality = 10;

    public SleepEntry() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SleepEntry.
     */
    public static SleepEntry newInstance(String tablename, String uid) {
        SleepEntry fragment = new SleepEntry();
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
            uid = getArguments().getString(TABLENAME_PARAM);
            tablename = getArguments().getString(ROWID_PARAM);
        }
        else
        {
            uid = null;
        }

        try {
            NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
            //Change the title of the action bar to reflect the current fragment
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_sleep_parent);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e) {
        }


    }

    protected void GetViewReferences(View v) {
        endDateTextView = (TextView) v.findViewById(R.id.sleepEndTimeTextView);
        hourSeekBar = (SeekBar) v.findViewById(R.id.sleepHoursSeekBar);
        minuteSeekBar = (SeekBar) v.findViewById(R.id.sleepMinutesSeekBar);
        startDateTextView = (TextView) v.findViewById(R.id.sleepStartDateTextView);
        startTimeTextView = (TextView) v.findViewById(R.id.sleepStartTimeTextView);
        sleptTimeTextView = (TextView) v.findViewById(R.id.sleepTimeSleptTextView);
        qualitySeekBar = (SeekBar) v.findViewById(R.id.sleepQualitySeekBar);
        qualityNumberTextView = (TextView) v.findViewById(R.id.sleepQualityNumberTextView);
    }


    /**
     * Sets up the listeners for the view objects
     */
    protected void SetupListeners(View v) {
        //Sets up the date and time fields to work with the activity that grabs them.
        DateTimeSelectorPopulateTextView timeSelectSetup = new DateTimeSelectorPopulateTextView(getActivity(), v, R.id.sleepStartDateTextView, R.id.sleepStartTimeTextView);
        timeSelectSetup.Populate();

        //Sets up the listeners so the seek bars call updates when changed.
        hourSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                UpdateEndTimes();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Call updates when minute seekbar is changed.
        minuteSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                UpdateEndTimes();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Updates the text box to be the same as the seekbar
        qualitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Ensures that a quality too low cannot be given
                if (progress < minQuality) {
                    qualitySeekBar.setProgress(minQuality);
                }

                qualityNumberTextView.setText(Integer.toString(qualitySeekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Set's progress to a default of half for the quality of sleep.
        qualitySeekBar.setProgress(5);

        //Makes sure the times are updated if the original date is updated.
        startDateTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                UpdateEndTimes();
            }
        });

        //Makes sure the times are updated if the original time is updated.
        startTimeTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                UpdateEndTimes();
            }
        });

        //Following code was adapted from JP's exercise entry code..
        //Array adapter from sleep string resources
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.sleep_gen_health_array, android.R.layout.simple_spinner_item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sleep_entry, container, false);

        //Moves this to another function to clean up onCreate
        GetViewReferences(v);

        //Sets a default of 7 hours as this should be fairly average.
        hourSeekBar.setProgress(7);
        minuteSeekBar.setProgress(0);

        //Moves listener setups to another function to avoid clutter
        SetupListeners(v);

        qualitySeekBar.setMax(maxQuality);

        UpdateEndTimes();

        entryView = v;

        SettingsAndEntryHelper.makeDisabledEntryViewsInvisible(entryView, LocalStorageAccessSleep.TABLE_NAME);
        if (uid != null)
        {
            SettingsAndEntryHelper.repopulateEntryPage(entryView, tablename, Integer.parseInt(uid));
        }
        return v;
    }

    /**
     * Updates the times to match the addition of the current time and entered time, as well as updates
     * the time spent.
     */
    public void UpdateEndTimes() {
        //Grabs initial time and date
        String dateText = startDateTextView.getText().toString();
        String timeText = startTimeTextView.getText().toString();

        //Grabs seekbar input
        Integer enteredHours = hourSeekBar.getProgress();
        Integer enteredMinutes = minuteSeekBar.getProgress();

        //Builds the display string for the time slept
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getResources().getString(R.string.sleep_time_slept_base));
        stringBuilder.append(enteredHours.toString());
        stringBuilder.append(":");

        if (enteredMinutes < 10) {
            stringBuilder.append("0");
        }
        stringBuilder.append(enteredMinutes.toString());
        sleptTimeTextView.setText(stringBuilder);

        //Determines AM or PM
        boolean pmTime = timeText.contains("PM");

        //Pulls out the month, day, and year information of the string into an array
        dateText = dateText.substring(dateText.indexOf(",") + 1);
        String[] splitDate;
        splitDate = dateText.split("/");

        //Pulls out the hour and minute information
        timeText = timeText.substring(timeText.indexOf(":") + 2);
        String[] splitTime;
        splitTime = timeText.split(" ")[0].split(":");

        //Does not continue of there were an incorrect number of elements parsed.
        if ((splitDate.length == 3) && (splitTime.length == 2)) {
            //Parses date
            Integer month = Integer.parseInt(splitDate[0].trim());
            Integer day = Integer.parseInt(splitDate[1].trim());
            Integer year = Integer.parseInt(splitDate[2].trim());

            //Parses time
            Integer hour = Integer.parseInt(splitTime[0].trim());
            Integer minute = Integer.parseInt(splitTime[1].trim());

            //12 extra hours for PM selected
            if (pmTime) {
                hour = hour + 12;
            }

            //Creates a calendar and then adds time to it based on the sliders
            Calendar endCalendar = new GregorianCalendar(year, month, day, hour, minute);
            endCalendar.add(Calendar.HOUR, enteredHours);
            endCalendar.add(Calendar.MINUTE, enteredMinutes);

            //Creates a format for date and time together
            SimpleDateFormat endTimeFormat = new SimpleDateFormat(DateTimeSelectorPopulateTextView._timeFormat + " " + DateTimeSelectorPopulateTextView._dateFormat);


            String sleepEndString = getResources().getString(R.string.sleep_end_time_base) + endTimeFormat.format(endCalendar.getTime());
            endDateTextView.setText(sleepEndString);
        }
    }


    /*
   * Stores the users data when the done button is clicked
   *
    */
    public void onDoneClick(View v) {
        String dateText = startDateTextView.getText().toString();
        String timeText = startTimeTextView.getText().toString();
        dateText = dateText.substring(dateText.indexOf(",") + 1).trim();
        timeText = timeText.substring(timeText.indexOf(":") + 2).trim();
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        Date date = null;
        try{ date = format.parse(dateText); } catch (Exception e) { }
        format = new SimpleDateFormat("yyyy-MM-dd");
        dateText = format.format(date);

        //Calls a helper method to grab most of the data for this.
        //Effectively grabs,
        //String[] sleepEntryData = {null, username, null, dateText, timeText, duration, quality, notes};
        String[] sleepEntryData = SettingsAndEntryHelper.prepareColumnArray(entryView, LocalStorageAccessSleep.TABLE_NAME, dateText, timeText);

        //Retrieves column names from the class
        String[] columnNames = LocalStorageAccessSleep.getColumns();

        ContentValues rowToBeInserted = new ContentValues();
        int dataIndex = 0;

        for (String column : columnNames) {
            //Insert column name ripped from LSA child class, and the user's entry data we gathered above
            rowToBeInserted.put(column, sleepEntryData[dataIndex]);
            dataIndex++;
        }

        //Call insert method
        LocalStorageAccessSleep.insertFromContentValues(rowToBeInserted, v.getContext());

        Sync sync = new Sync(v.getContext());
        sync.databaseInsertOrUpdateSyncTable(this, rowToBeInserted, LocalStorageAccessExercise.TABLE_NAME);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Called asynchronously when the call to the webserver is done. This method updates the webID
     * reference that is stored on the local database
     *
     * @param result The json encoded regular string that contains the WebID and localID of the
     *               updated row
     */
    public void processFinish(String result) {
        //Getting context for LSA constructor
        Context context = entryView.getContext();

        JSONObject jsonObject;
        jsonObject = JsonCVHelper.processServerJsonString(result, context, "Could not create sleep entry on web database");

        if (jsonObject != null)
        {
            int[] tableIDs = new int[2];
            JsonCVHelper.getIDColumns(tableIDs, jsonObject);

            if (tableIDs[0] != -1 && tableIDs[1] != -1)
            {
                LocalStorageAccessSleep.updateWebIDReference(tableIDs[0], tableIDs[1], context, true);
            } else
            {
                Toast.makeText(context, "There was an error processing information from the webserver", Toast.LENGTH_LONG).show();
            }
        }
    }
}
