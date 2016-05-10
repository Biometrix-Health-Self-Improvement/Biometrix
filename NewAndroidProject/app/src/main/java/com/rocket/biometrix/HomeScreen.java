package com.rocket.biometrix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
public class HomeScreen extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        AsyncResponse {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View homeScreenView;
    private String username;

    //A reference to the Google API
    private GoogleApiClient googleApiClient;
    //A loading symbol
    private ProgressDialog progressDialog;
    //A tag that is used in debug logging
    private static final String TAG = "GoogleLoginActivity";
    //The number that corresponds to the google API "sign-in" activity
    private static final int RC_SIGN_IN = 9001;
    //A reference to the user's google account
    private GoogleSignInAccount acct;

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
    public void onStop()
    {
        super.onStop();

        googleApiClient.disconnect();
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

        //Creates the google sign in object with requests for email
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("212262655567-0na15tnrbol7g1ukjhqh98vit1je64a4.apps.googleusercontent.com")
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(v.getContext())
                //.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();

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

    /**
     * Starts the google authentication activity from a button click.
     */
    public void googleSignIn(View v)
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Retrieves the result of the sign-in intent
     * @param requestCode The type of google API call used
     * @param resultCode A result code
     * @param data The data from the sign-in packaged in an intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * This function is called when the google sign in activity is finished (see resultcallback in
     * on start)
     * @param result The result of the attempt to sign in using Google APIs
     */
    private void handleSignInResult(GoogleSignInResult result)
    {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());

        if (result.isSuccess())
        {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));

            String token = acct.getIdToken();

            new DatabaseConnect(this).execute(DatabaseConnectionTypes.GOOGLE_TOKEN, token);
            showBiometrixProgressDialog();

        }
    }

    /**
     * Creates a progress dialog if it does not exist, and then shows it
     */
    private void showProgressDialog()
    {
        if (progressDialog == null)
        {
            progressDialog = new ProgressDialog(homeScreenView.getContext());
            progressDialog.setMessage(getString(R.string.login_loading));
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    /**
     * Creates a progress dialog if it does not exist, and then shows it
     */
    private void showBiometrixProgressDialog()
    {
        if (progressDialog == null)
        {
            progressDialog = new ProgressDialog(homeScreenView.getContext());
            progressDialog.setMessage(getString(R.string.login_biometrix_loading));
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    /**
     * Hides the progress dialog if it currently exists
     */
    private void hideProgressDialog()
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            progressDialog.hide();
        }
    }

    /**
     * A call to this function means that the connection to Google APIs failed, and so Google Account
     * cannot be used
     * @param connectionResult This paramater is ignored
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(homeScreenView.getContext(), "Unable to access Google Services", Toast.LENGTH_LONG).show();
    }

    @Override
    /**
     * Retrieves the results of the call to the webserver
     */
    public void processFinish(String result)
    {
        hideProgressDialog();
        String returnResult = result;

        JSONObject jsonObject;

        jsonObject = JsonCVHelper.processServerJsonString(returnResult, homeScreenView.getContext(), "Login Failed");

        if (jsonObject != null)
        {
            try
            {
                //If the json object passes back a token then it was a login
                if (jsonObject.has("Google") )
                {
                    //Logins the google account user with their id as their "username"
                    LocalAccount.Login(acct, jsonObject.getString("Token"));

                    Toast.makeText(homeScreenView.getContext(), "Google sign in succeeded!", Toast.LENGTH_LONG).show();
                    NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
                    nav.returnToLoggedInHomePage();
                }
                else if (jsonObject.has("Token"))
                {
                    Toast.makeText(homeScreenView.getContext(), "Login Successful!", Toast.LENGTH_LONG).show();

                    //Logs the user in with their login token.
                    LocalAccount.Login(username, jsonObject.getString("Token"));

                    NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
                    nav.returnToLoggedInHomePage();
                }
            }
            catch (JSONException jsonExcept)
            {
                Toast.makeText(homeScreenView.getContext(), "Something went wrong with the server's return", Toast.LENGTH_LONG).show();
            }
        }

    }
}
