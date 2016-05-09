package com.rocket.biometrix;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.rocket.biometrix.Database.AsyncResponse;
import com.rocket.biometrix.Database.DatabaseConnect;
import com.rocket.biometrix.Database.DatabaseConnectionTypes;
import com.rocket.biometrix.Database.JsonCVHelper;
import com.rocket.biometrix.Login.LocalAccount;
import com.rocket.biometrix.Settings.ModuleSettings;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeScreen.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeScreen extends Fragment implements AsyncResponse {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View homeScreenView;
    private String username;

    private OnFragmentInteractionListener mListener;

    public HomeScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeScreen newInstance(String param1, String param2) {
        HomeScreen fragment = new HomeScreen();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        try{
            NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
            //Change the title of the action bar to reflect the current fragment
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_home_screen);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_home_screen, container, false);
        homeScreenView = v;
        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_help) {

        } else if(id == R.id.action_settings){
            Fragment frag = new ModuleSettings();
            return true;
        } else if(id == R.id.action_logout){

        }
        return super.onOptionsItemSelected(item);
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
     * Calls the database to check the login for the user
     * @param view
     */
    public void okayButtonClick(View view)
    {
        String password;

        EditText usernameEdit =  (EditText) homeScreenView.findViewById(R.id.HomeScreenUserNameEditText);
        username = usernameEdit.getText().toString();

        EditText passwordEdit = (EditText) homeScreenView.findViewById(R.id.HomeScreenPasswordEditText);
        password = passwordEdit.getText().toString();

        if (username.equals("") || password.equals("") )
        {
            Toast.makeText(homeScreenView.getContext(), "Username or password is blank", Toast.LENGTH_LONG).show();
        }
        else
        {
            new DatabaseConnect(this).execute(DatabaseConnectionTypes.LOGIN_CHECK,username, password);
        }
    }

    @Override
    /**
     * Retrieves the results of the call to the webserver
     */
    public void processFinish(String result)
    {
        String returnResult = result;

        JSONObject jsonObject;

        jsonObject = JsonCVHelper.processServerJsonString(returnResult, homeScreenView.getContext(), "Login Failed");

        if (jsonObject != null)
        {
            try
            {
                //If the json object passes back a token then it was a login
                if (jsonObject.has("Token"))
                {
                    Toast.makeText(homeScreenView.getContext(), "Login Successful!", Toast.LENGTH_LONG).show();

                    //Logs the user in with their login token.
                    LocalAccount.Login(username, jsonObject.getString("Token"));

                    NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
                    nav.returnToLoggedInHomePage();
                } else
                //Assume it was a password reset
                {
                    Toast.makeText(homeScreenView.getContext(), "Check your email (and your spam folder) for your reset link", Toast.LENGTH_LONG).show();
                }
            }
            catch (JSONException jsonExcept)
            {
                Toast.makeText(homeScreenView.getContext(), "Something went wrong with the server's return", Toast.LENGTH_LONG).show();
            }
        }

    }
}
