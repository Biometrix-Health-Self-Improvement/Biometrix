package com.rocket.biometrix.Login;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.rocket.biometrix.NavigationDrawerActivity;

/**
 * Created by TJ on 1/24/2016.
 * This class is designed to hold the shared preferences for each user account. Since each user can
 * have different preferences, the file will be named after the username.
 *
 * Since only one user can be logged in at a time, this will be a singleton.
 */
public class LocalAccount {
    public static final String DEFAULT_NAME = "Default";

    //Reference variables for information needed by shared preferences
    private static final int PREFERENCE_PRIVATE_MODE = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferenceEditor;
    private Context currentContext;

    //The reference back to itself
    private static LocalAccount _instance;

    //The username string is used as the filename for the shared preferences
    private static String username;

    //The username that was most recently used for a preference file
    private String preferenceUsername;

    //A google account for the current sign in. This way it can be accessed from wherever it is needed
    private static GoogleSignInAccount googleAccount;

    //A token signed by the webserver to ensure the user is currently valid
    private String webServerToken;

    //A reference to the navigation drawer, used to call update
    private static NavigationDrawerActivity navDrawerRef;

    public static void setNavDrawerRef(NavigationDrawerActivity ref)
    {
        navDrawerRef = ref;
    }

    /**
     * Creates the LocalAccount. Private since this is a singleton
     *
     * @param jsonToken The token that was passed back by the webserver
     */
    private LocalAccount(String jsonToken)
    {
        webServerToken = jsonToken;
    }

    /**
     * Changes the login information to be the newly logged in user. If another user was logged in,
     * this changes to the new user. This should only be used for regular sign-in, not google sign in
     *
     * @param new_username The username of the newly logged in user.
     * @param jsonToken    The token returned by the server when the login was called.
     */
    public static LocalAccount Login(String new_username, String jsonToken)
    {
        //Overrides the current username
        username = new_username;

        //Since a username was specified, this is not a google account.
        googleAccount = null;

        _instance = new LocalAccount(jsonToken);

        navDrawerRef.UpdateMenuItems();

        return _instance;
    }

    /**
     * Returns whether a google account is currently signed in or not
     *
     * @return True if a google account is signed in, false if not.
     */
    public static boolean isGoogleAccountSignedIn()
    {
        if (googleAccount == null)
            return false;
        else
            return true;
    }

    /**
     * Returns whether a user is currently logged in or not
     *
     * @return True if there is a user logged in, false otherwise.
     */
    public static boolean isLoggedIn()
    {
        if (_instance == null) return false;

        if (username.equals(DEFAULT_NAME)) return false;

        return true;
    }

    /**
     * Logs the user in with their google account instead of with their Biometrix account
     *
     * @param googleSignInAccount A reference to the google account that will be held
     * @return A reference to the account that was logged in
     */
    public static LocalAccount Login(GoogleSignInAccount googleSignInAccount, String jsonToken)
    {
        //If there is no google account signed in, sign the user in. If the currently logged in account
        //is the same as the one being logged in, do nothing
        if (googleAccount == null || !(googleAccount.getIdToken().equals(googleSignInAccount.getIdToken())))
        {
            //Sets the static fields before creating the login
            googleAccount = googleSignInAccount;
            username = googleSignInAccount.getId();

            _instance = new LocalAccount(jsonToken);
        }


        navDrawerRef.UpdateMenuItems();
        return _instance;
    }

    /**
     * Retrieves a reference to the currently logged in local account
     *
     * @return A reference to the local account
     */
    public static LocalAccount GetInstance()
    {
        if (_instance == null)
        {
            _instance = new LocalAccount(null);
            username = DEFAULT_NAME;
        }

        return _instance;
    }

    /**
     * Logs the current user out of the system and sets the default user as logged in
     */
    public static void Logout()
    {
        googleAccount = null;
        _instance = null;
        _instance = GetInstance();

        navDrawerRef.UpdateMenuItems();
    }

    /**
     * Returns the username of the user who is currently logged in.
     *
     * @return
     */
    public String GetUsername()
    {
        return username;
    }

    /**
     * Returns the currently logged in user's token.
     *
     * @return A string containing the user's token
     */
    public String GetToken()
    {
        return webServerToken;
    }

    /**
     * Sets up the shared preferences for the current user and context to allow reading or writing
     *
     * @param context The current context
     */
    private void setupPreferences(Context context)
    {
        if (currentContext != context || !preferenceUsername.equals(username))
        {
            currentContext = context;
            sharedPreferences = currentContext.getSharedPreferences(username, PREFERENCE_PRIVATE_MODE);
            preferenceEditor = sharedPreferences.edit();
            preferenceUsername = username;
        }
    }

    /**
     * Retrieves a boolean value that was stored with the passed in key. If no value has been
     * stored yet, this returns the default
     *
     * @param context      The current context. Needed to pull user settings.
     * @param key          The key of the key value pair
     * @param defaultValue The value that is returned if no value has been stored yet
     * @return The stored value that corresponds to the key, or the defaultValue if no value is stored
     */
    public boolean getBoolean(Context context, String key, boolean defaultValue)
    {
        setupPreferences(context);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Sets a boolean value of the passed in key to the passed in value
     *
     * @param context The current context which is needed to get user data
     * @param key     The key to look up the entry for
     * @param value   The value to store
     */
    public void setBoolean(Context context, String key, boolean value)
    {
        setupPreferences(context);
        preferenceEditor.putBoolean(key, value).commit();
    }

    /**
     * Retrieves an int value that was stored with the passed in key. If no value has been
     * stored yet, this returns the default
     * @param context      The current context. Needed to pull user settings.
     * @param key          The key of the key value pair
     * @param defaultValue The value that is returned if no value has been stored yet
     * @return The stored value that corresponds to the key, or the defaultValue if no value is stored
     */
    public int getInt(Context context, String key, int defaultValue)
    {
        setupPreferences(context);
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Sets a boolean value of the passed in key to the passed in value
     * @param context The current context which is needed to get user data
     * @param key     The key to look up the entry for
     * @param value   The value to store
     */
    public void setInt(Context context, String key, int value)
    {
        setupPreferences(context);
        preferenceEditor.putInt(key, value).commit();
    }
}
