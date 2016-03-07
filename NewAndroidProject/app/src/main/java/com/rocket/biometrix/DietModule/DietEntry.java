package com.rocket.biometrix.DietModule;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.rocket.biometrix.Common.DateTimeSelectorPopulateTextView;
import com.rocket.biometrix.Common.StringDateTimeConverter;
import com.rocket.biometrix.Database.AsyncResponse;
import com.rocket.biometrix.Database.DatabaseConnect;
import com.rocket.biometrix.Database.DatabaseConnectionTypes;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.Login.LocalAccount;
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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View dietView;

    public DietEntry() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DietEntry.
     */
    public static DietEntry newInstance(String param1, String param2) {
        DietEntry fragment = new DietEntry();
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
        String foodName = ((TextView)dietView.findViewById(R.id.Food_Name)).getText().toString();
        String meal = ((Spinner)dietView.findViewById(R.id.Meal_Select)).getSelectedItem().toString();
        String servingSize = ((Spinner)dietView.findViewById(R.id.ServingSize_Select)).getSelectedItem().toString();
        String calories = ((TextView)dietView.findViewById(R.id.Calories_Amt)).getText().toString();
        String totalFat = ((TextView)dietView.findViewById(R.id.TotalFat_Amt)).getText().toString();
        String satFat = ((TextView)dietView.findViewById(R.id.SaturatedFat_Amt)).getText().toString();
        String transFat = ((TextView)dietView.findViewById(R.id.TransFat_Amt)).getText().toString();
        String chol = ((TextView)dietView.findViewById(R.id.Cholesterol_Amt)).getText().toString();
        String sodium = ((TextView)dietView.findViewById(R.id.Sodium_Amt)).getText().toString();
        String totalCarbs = ((TextView)dietView.findViewById(R.id.TotalCarb_Amt)).getText().toString();
        String fiber = ((TextView)dietView.findViewById(R.id.DietaryFiber_Amt)).getText().toString();
        String sugars = ((TextView)dietView.findViewById(R.id.Sugars_Amt)).getText().toString();
        String protein = ((TextView)dietView.findViewById(R.id.Protein_Amt)).getText().toString();
        String vitaminA = ((TextView)dietView.findViewById(R.id.VitaminA_Amt)).getText().toString();
        String vitaminB = ((TextView)dietView.findViewById(R.id.VitaminB_Amt)).getText().toString();
        String calcium = ((TextView)dietView.findViewById(R.id.Calcium_Amt)).getText().toString();
        String iron = ((TextView)dietView.findViewById(R.id.Iron_Amt)).getText().toString();
        String notes = ((TextView)dietView.findViewById(R.id.dietDetailsEditText)).getText().toString();

        String username = "default";

        if (LocalAccount.isLoggedIn() )
        {
            username = LocalAccount.GetInstance().GetUsername();
        }

        //Make string array for all of the above data
        String[] dietEntryData = {null, username, null, dateText, foodName, meal, servingSize,
                calories, totalFat, satFat, transFat, chol, sodium, totalCarbs, fiber, sugars,
                protein, vitaminA, vitaminB, calcium, iron, notes, "0"};


        //Create the object that will update the sleep table
        //LocalStorageAccessSleep sleepSQL = new LocalStorageAccessSleep(context);

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

            int id = LocalStorageAccessDiet.GetLastID(v.getContext());

            rowToBeInserted.put(LocalStorageAccessDiet.LOCAL_DIET_ID, id);
            rowToBeInserted.remove(LocalStorageAccessDiet.USER_NAME);

            String jsonToInsert = DatabaseConnect.convertToJSON(rowToBeInserted);

            //Trys to insert the user's data
            try
            {
                new DatabaseConnect(this).execute(DatabaseConnectionTypes.INSERT_TABLE_VALUES, jsonToInsert,
                        LocalAccount.GetInstance().GetToken(),
                        //"asdf",
                        DatabaseConnectionTypes.DIET_TABLE);
            }
            catch (NullPointerException except)
            {
                //TODO display error if user is not logged in.
            }


        }

    }

    public void processFinish(String result)
    {
        Log.i("", result);
    }
}
