package com.rocket.biometrix.DietModule;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rocket.biometrix.Common.DateTimeSelectorPopulateTextView;
import com.rocket.biometrix.Common.StringDateTimeConverter;
import com.rocket.biometrix.Database.AsyncResponse;
import com.rocket.biometrix.Database.JsonCVHelper;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.Sync;
import com.rocket.biometrix.Login.SettingsAndEntryHelper;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DietEntry.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DietEntry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DietEntry extends Fragment implements AsyncResponse {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TABLENAME_PARAM = "tablename";
    private static final String ROWID_PARAM = "uid";

    private String uid;
    private String tablename; //unused

    private OnFragmentInteractionListener mListener;
    private View dietView;

    public DietEntry() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DietEntry.
     */
    public static DietEntry newInstance(String tablename, String uid) {
        DietEntry fragment = new DietEntry();
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
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_diet_entry);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diet_entry, container, false);

        DateTimeSelectorPopulateTextView popDateTime = new DateTimeSelectorPopulateTextView
                (getActivity(), view, R.id.DietStartDateTextView, R.id.DietStarTimeTextView);
        popDateTime.Populate();

        dietView = view;
        SettingsAndEntryHelper.makeDisabledEntryViewsInvisible(dietView, LocalStorageAccessDiet.TABLE_NAME);

        if (uid != null)
        {
            dietView.findViewById(R.id.diet_entry_done_button).setVisibility(View.GONE);
            SettingsAndEntryHelper.repopulateEntryPage(dietView, tablename, Integer.parseInt(uid));
        }
        else
        {
            dietView.findViewById(R.id.diet_entry_update_button).setVisibility(View.GONE);
            dietView.findViewById(R.id.diet_entry_delete_button).setVisibility(View.GONE);
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


    public void onDoneClick(View v)
    {
        String dateText = ((TextView)dietView.findViewById(R.id.DietStartDateTextView)).getText().toString();
        dateText = StringDateTimeConverter.fixDate(dateText);

        //String[] dietEntryData = {null, username, null, dateText, foodName, meal, servingSize,
        //        calories, totalFat, satFat, transFat, chol, sodium, totalCarbs, fiber, sugars,
        //        protein, vitaminA, vitaminB, calcium, iron, notes};
        //The below has essentially the affect of the comment above
        String[] dietEntryData = SettingsAndEntryHelper.prepareColumnArray(dietView,
                LocalStorageAccessDiet.TABLE_NAME, dateText, null);

        //Retrieves column names from the class
        String[] columnNames = LocalStorageAccessDiet.getColumns();


        if (columnNames.length == dietEntryData.length)
        {
            ContentValues rowToBeInserted = new ContentValues();
            int dataIndex = 0;

            for (String column : columnNames)
            {
                rowToBeInserted.put(column, dietEntryData[dataIndex]);
                dataIndex++;
            }

            //Call insert method
            LocalStorageAccessDiet.insertFromContentValues(rowToBeInserted, dietView.getContext());

            Sync sync = new Sync(v.getContext());
            sync.databaseInsertOrUpdateSyncTable(this, rowToBeInserted, LocalStorageAccessDiet.TABLE_NAME);
        }

    }

    /**
     * This method is called when a user is viewing an entry that was previously entered and then
     * clicks update.
     * @param v The current view
     */
    public void onUpdateClick(View v)
    {
        String dateText = ((TextView)dietView.findViewById(R.id.DietStartDateTextView)).getText().toString();
        dateText = StringDateTimeConverter.fixDate(dateText);

        //String[] dietEntryData = {null, username, null, dateText, foodName, meal, servingSize,
        //        calories, totalFat, satFat, transFat, chol, sodium, totalCarbs, fiber, sugars,
        //        protein, vitaminA, vitaminB, calcium, iron, notes};
        //The below has essentially the affect of the comment above
        String[] dietEntryData = SettingsAndEntryHelper.prepareColumnArray(dietView,
                LocalStorageAccessDiet.TABLE_NAME, dateText, null);

        Integer webPrimarykey = LocalStorageAccessDiet.getWebKeyFromLocalKey(dietView.getContext(), Integer.parseInt(uid));
        //Changes the null of the web primary key to the expected value
        dietEntryData[2] = webPrimarykey.toString();
        dietEntryData[0] = uid;

        //Retrieves column names from the class
        String[] columnNames = LocalStorageAccessDiet.getColumns();


        if (columnNames.length == dietEntryData.length)
        {
            ContentValues rowToUpdate = new ContentValues();
            int dataIndex = 0;

            for (String column : columnNames)
            {
                //Don't need to insert nulls
                if (dietEntryData[dataIndex] != null) {
                    rowToUpdate.put(column, dietEntryData[dataIndex]);
                }
                dataIndex++;
            }

            //Call update method
            LocalStorageAccessDiet.updateFromContentValues(rowToUpdate, dietView.getContext(), Integer.parseInt(uid));

            Sync sync = new Sync(v.getContext());
            sync.databaseUpdateOrUpdateSyncTable(this, rowToUpdate, Integer.parseInt(uid), webPrimarykey, LocalStorageAccessDiet.TABLE_NAME);
        }

    }

    /**
     * Deletes the entry on click based on local ID and web ID
     * @param view
     */
    public void onDeleteClick(View view)
    {
        Integer webPrimarykey = LocalStorageAccessDiet.getWebKeyFromLocalKey(dietView.getContext(), Integer.parseInt(uid));
        LocalStorageAccessDiet.deleteByLocalKeyValue(dietView.getContext(), Integer.parseInt(uid));

        Sync sync = new Sync(dietView.getContext());
        sync.databaseDeleteOrUpdateSyncTable(this, Integer.parseInt(uid), webPrimarykey, LocalStorageAccessDiet.TABLE_NAME);
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
        Context context = dietView.getContext();

        JsonCVHelper.processServerJsonString(result, context, LocalStorageAccessDiet.TABLE_NAME);
    }
}
