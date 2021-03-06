package com.rocket.biometrix.MedicationModule;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rocket.biometrix.Common.DateTimeSelectorPopulateTextView;
import com.rocket.biometrix.Common.StringDateTimeConverter;
import com.rocket.biometrix.Database.AsyncResponse;
import com.rocket.biometrix.Database.JsonCVHelper;
import com.rocket.biometrix.Database.LocalStorageAccessMedication;
import com.rocket.biometrix.Database.Sync;
import com.rocket.biometrix.Login.SettingsAndEntryHelper;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MedicationEntry.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MedicationEntry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicationEntry extends Fragment implements AsyncResponse{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TABLENAME_PARAM = "tablename";
    private static final String ROWID_PARAM = "uid";

    private String uid;
    private String tablename; //unused

    private View entryView;

    private OnFragmentInteractionListener mListener;

    public MedicationEntry() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MedicationEntry.
     */
    public static MedicationEntry newInstance(String tablename, String uid) {
        MedicationEntry fragment = new MedicationEntry();
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
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_medication_entry);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_medication_entry, container, false);

        DateTimeSelectorPopulateTextView popDateTime = new DateTimeSelectorPopulateTextView
                (getActivity(), view, R.id.MedicationStartDateTextView, R.id.MedicationStartTimeTextView);
        popDateTime.Populate();

        entryView = view;

        SettingsAndEntryHelper.makeDisabledEntryViewsInvisible(entryView, LocalStorageAccessMedication.TABLE_NAME);
        if (uid != null)
        {
            entryView.findViewById(R.id.medicaiton_entry_done_button).setVisibility(View.GONE);
            SettingsAndEntryHelper.repopulateEntryPage(entryView, tablename, Integer.parseInt(uid));
        }
        else
        {
            entryView.findViewById(R.id.medication_entry_delete_button).setVisibility(View.GONE);
            entryView.findViewById(R.id.medication_entry_update_button).setVisibility(View.GONE);
        }
        return view;
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

    /*
   * Stores the users data when the done button is clicked
   *
    */
    public void onDoneClick(View v) {
        //Filling date and time strings for bundle's string array
        String dateString = ((TextView)entryView.findViewById(R.id.MedicationStartDateTextView)).getText().toString();
        String timeString = ((TextView)entryView.findViewById(R.id.MedicationStartTimeTextView)).getText().toString();

        //Cleaning date and time strings with helper class
        dateString = StringDateTimeConverter.fixDate(dateString);
        //Deactivated for time since this module is storing date in a different manner than all
        //of the others, may need to go back to this later instead, but for all modules.
        //timeString = StringDateTimeConverter.fixTime(timeString);
        timeString = timeString.substring(timeString.indexOf(":") + 2).trim();

        //String[] medEntryData = {null, username, null, dateString, timeString, brandString,
        //        prescriberString, dosetring, instructionsString, warningsString, notes};
        //Has the affect of the comment above
        String[] medEntryData = SettingsAndEntryHelper.prepareColumnArray(entryView,
                LocalStorageAccessMedication.TABLE_NAME, dateString, timeString);


        //Retrieves column names from the class
        String[] columnNames = LocalStorageAccessMedication.getColumns();


        if (columnNames.length == medEntryData.length) {
            ContentValues rowToBeInserted = new ContentValues();
            int dataIndex = 0;

            for (String column : columnNames) {
                //Insert column name ripped from LSA child class, and the user's entry data we gathered above
                if (column != LocalStorageAccessMedication.LOCAL_MEDICATION_ID) {
                    rowToBeInserted.put(column, medEntryData[dataIndex]);
                }
                dataIndex++;
            }

            //Call insert method
            LocalStorageAccessMedication.insertFromContentValues(rowToBeInserted, v.getContext());

            Sync sync = new Sync(v.getContext());
            sync.databaseInsertOrUpdateSyncTable(this, rowToBeInserted, LocalStorageAccessMedication.TABLE_NAME);
        }

    }

    /**
     * This method is called when a user is viewing an entry that was previously entered and then
     * clicks update.
     * @param v The current view
     */
    public void onUpdateClick(View v)
    {
        //Filling date and time strings for bundle's string array
        String dateString = ((TextView)entryView.findViewById(R.id.MedicationStartDateTextView)).getText().toString();
        String timeString = ((TextView)entryView.findViewById(R.id.MedicationStartTimeTextView)).getText().toString();

        //Cleaning date and time strings with helper class
        dateString = StringDateTimeConverter.fixDate(dateString);
        //Deactivated for time since this module is storing date in a different manner than all
        //of the others, may need to go back to this later instead, but for all modules.
        //timeString = StringDateTimeConverter.fixTime(timeString);
        timeString = timeString.substring(timeString.indexOf(":") + 2).trim();

        //String[] medEntryData = {null, username, null, dateString, timeString, brandString,
        //        prescriberString, dosetring, instructionsString, warningsString, notes};
        //Has the affect of the comment above
        String[] medEntryData = SettingsAndEntryHelper.prepareColumnArray(entryView,
                LocalStorageAccessMedication.TABLE_NAME, dateString, timeString);

        Integer webPrimarykey = LocalStorageAccessMedication.getWebKeyFromLocalKey(entryView.getContext(), Integer.parseInt(uid));
        //Changes the null of the web primary key to the expected value
        medEntryData[2] = webPrimarykey.toString();
        medEntryData[0] = uid;

        //Retrieves column names from the class
        String[] columnNames = LocalStorageAccessMedication.getColumns();


        if (columnNames.length == medEntryData.length)
        {
            ContentValues rowToUpdate = new ContentValues();
            int dataIndex = 0;

            for (String column : columnNames)
            {
                //Don't need to insert nulls
                if (medEntryData[dataIndex] != null) {
                    rowToUpdate.put(column, medEntryData[dataIndex]);
                }
                dataIndex++;
            }

            //Call update method
            LocalStorageAccessMedication.updateFromContentValues(rowToUpdate, entryView.getContext(), Integer.parseInt(uid));

            Sync sync = new Sync(v.getContext());
            sync.databaseUpdateOrUpdateSyncTable(this, rowToUpdate, Integer.parseInt(uid), webPrimarykey, LocalStorageAccessMedication.TABLE_NAME);
        }

    }

    /**
     * Deletes the entry on click based on local ID and web ID
     * @param view
     */
    public void onDeleteClick(View view)
    {
        Integer webPrimarykey = LocalStorageAccessMedication.getWebKeyFromLocalKey(entryView.getContext(), Integer.parseInt(uid));
        LocalStorageAccessMedication.deleteByLocalKeyValue(entryView.getContext(), Integer.parseInt(uid));

        Sync sync = new Sync(entryView.getContext());
        sync.databaseDeleteOrUpdateSyncTable(this, Integer.parseInt(uid), webPrimarykey, LocalStorageAccessMedication.TABLE_NAME);
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

        JsonCVHelper.processServerJsonString(result, context, LocalStorageAccessMedication.TABLE_NAME);
    }
}
