package com.rocket.biometrix;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItem;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import com.rocket.biometrix.Analysis.AllGraph;
import com.rocket.biometrix.Analysis.AnalysisFragment;
import com.rocket.biometrix.Analysis.DietGraph;
import com.rocket.biometrix.Analysis.ExerciseGraph;
import com.rocket.biometrix.Analysis.GraphBase;
import com.rocket.biometrix.Analysis.MoodGraph;
import com.rocket.biometrix.Analysis.SleepGraph;
import com.rocket.biometrix.Database.Sync;
import com.rocket.biometrix.DietModule.DietEntry;
import com.rocket.biometrix.DietModule.DietParent;
import com.rocket.biometrix.ExerciseModule.ExerciseEntry;
import com.rocket.biometrix.ExerciseModule.ExerciseParent;
import com.rocket.biometrix.Login.CreateLogin;
import com.rocket.biometrix.Login.GetLogin;
import com.rocket.biometrix.Login.GoogleLogin;
import com.rocket.biometrix.Login.LocalAccount;
import com.rocket.biometrix.Login.SettingsAndEntryHelper;
import com.rocket.biometrix.MedicationModule.MedicationEntry;
import com.rocket.biometrix.MedicationModule.MedicationParent;
import com.rocket.biometrix.MoodModule.MoodEntry;
import com.rocket.biometrix.MoodModule.MoodParent;
import com.rocket.biometrix.Settings.DietSettings;
import com.rocket.biometrix.Settings.ExerciseSettings;
import com.rocket.biometrix.Settings.MedicationSettings;
import com.rocket.biometrix.Settings.ModuleSettings;
import com.rocket.biometrix.Settings.MoodSettings;
import com.rocket.biometrix.Settings.SleepSettings;
import com.rocket.biometrix.SleepModule.SleepEntry;

import com.rocket.biometrix.SleepModule.SleepParent;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //keeps track of the currently active fragment
    public Fragment activeFragment = null;

    //A reference to the navigation view
    protected NavigationView navView;

    public String mTblSignal;
    public String mUidSignal;
    Bundle mEditEntryB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigatoin_drawer_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Local account/settings setup
        navView = navigationView;
        LocalAccount.setNavDrawerRef(this);
        UpdateMenuItems();

        Fragment frag;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        mEditEntryB = getIntent().getExtras();

        frag = new HomeScreen();

        //TODO: Get first loaded page to be correct color
/*        Class fragHome = activeFragment.getClass();
        if(fragHome.equals(HomeScreen.class)) {
            this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                    getResources().getColor(R.color.background_home_color));
            setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_home_color));
        }*/

        transaction.replace(R.id.navigation_drawer_fragment_content, frag, "home");
        transaction.addToBackStack(null);
        transaction.commit();
        this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                getResources().getColor(R.color.background_home_color));
        setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_home_color));

        if (mEditEntryB != null) {
            //yourDataObject = getIntent().getStringExtra(KEY_EXTRA);
            mTblSignal = mEditEntryB.getString("tablename"); //See MECR ViewAdapter, ViewHolder's list item onClick listener
            mUidSignal = mEditEntryB.getString("uid");

            if (mTblSignal != null){

                FragmentTransaction transactionEE = getFragmentManager().beginTransaction();
            frag = PopulateEntryIntercept(mTblSignal);

            frag.setArguments(getIntent().getExtras());

            transactionEE.replace(R.id.navigation_drawer_fragment_content, frag, mTblSignal);
            transactionEE.addToBackStack(mTblSignal);
            transactionEE.commit();
            mTblSignal = null;}

        }
        
        //Local account/settings setup
        navView = navigationView;
        MenuItem dietItem = navView.getMenu().findItem(R.id.nav_diet_module);

        LocalAccount.setNavDrawerRef(this);
        UpdateMenuItems();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer_activity, menu);

        if (LocalAccount.GetInstance().isLoggedIn())
        {
            menu.findItem(R.id.action_logout).setVisible(true);
        }
        else
        {
            menu.findItem(R.id.action_logout).setVisible(false);
        }
        return true;
    }

    /**
     * The method called when one of the items on the help side of the nav drawer is called
     * @param item The item that was clicked on in order to call this event
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment frag;

        switch(item.getItemId()) {
            case R.id.action_help:

                return true;
            case R.id.action_settings:
                Class fragClass = activeFragment.getClass();

                if(fragClass.equals(SleepParent.class) || fragClass.equals(SleepGraph.class)
                        || fragClass.equals(SleepEntry.class))
                {
                    frag = new SleepSettings();
                }
                else if(fragClass.equals(MoodParent.class) || fragClass.equals(MoodGraph.class)
                        || fragClass.equals(MoodEntry.class))
                {
                    frag = new MoodSettings();
                }
                else if(fragClass.equals(DietParent.class)  || fragClass.equals(DietGraph.class)
                        || fragClass.equals(DietEntry.class))
                {
                    frag = new DietSettings();
                }
                else if(fragClass.equals(MedicationParent.class)  //TODO: || fragClass.equals(MedicationGraph.class)
                        || fragClass.equals(MedicationEntry.class))
                {
                    frag = new MedicationSettings();
                }
                else if(fragClass.equals(ExerciseParent.class)  || fragClass.equals(ExerciseGraph.class)
                        || fragClass.equals(ExerciseEntry.class))
                {
                    frag = new ExerciseSettings();
                }
                else
                {
                    frag = new ModuleSettings();
                }

                replaceFragment(frag);
                return true;

            case R.id.action_logout:
                LogoutUser();
                frag = new HomeScreen();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment frag = new HomeScreen(); //intialize to homescreen in case something goes wrong it will not crash and just go back to home

        if(id == R.id.nav_home_logged_in) {
            frag = new HomeScreen_Logged_In();
        } else if (id == R.id.nav_home){
            frag = new HomeScreen();
        } else if (id == R.id.nav_mood_module) {
            frag = new MoodParent();
        } else if (id == R.id.nav_sleep_module) {
            frag = new SleepParent();
        } else if (id == R.id.nav_exercise_module) {
            frag = new ExerciseParent();
        } else if (id == R.id.nav_diet_module) {
            frag = new DietParent();
        } else if (id == R.id.nav_medication_module) {
            frag = new MedicationParent();
        } else if (id == R.id.nav_analytics) { //TODO: menu open analytics fragment
            frag = new AnalysisFragment();
        } else if (id == R.id.nav_create_account){
            frag = new CreateLogin();
        } else if (id == R.id.nav_google_login){
            frag = new GoogleLogin();
        } else if(id == R.id.nav_sync)
        {
            Sync sync = new Sync(getApplicationContext());
            sync.syncDatabases();
        }
        replaceFragment(frag);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**************************************************************************
     * Changes the action bar text to the string passed in
     * @param title The title the fragment wants the Action Bar to be set to
     **************************************************************************/
    public void setActionBarTitleFromFragment(int title){
        try {
            getSupportActionBar().setTitle(getResources().getString(title));
        } catch (Exception e){}
    }
    public void setActionBarColorFromFragment(int color){
        try {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
        } catch (Exception e){}
    }
    /**************************************************************************
     * Replaces the current active fragment with the fragment passed in
     * @param frag a new fragment that has been created before the function is called
     **************************************************************************/
    public void replaceFragment(Fragment frag){ //TODO:addToBackStack tags
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.navigation_drawer_fragment_content, frag);
        transaction.addToBackStack(null);
        transaction.commit();

        if (activeFragment.getClass() == HomeScreen_Logged_In.class ||
                frag.getClass() == HomeScreen_Logged_In.class)
        {
            supportInvalidateOptionsMenu();
        }

        Class fragClass = frag.getClass();

        if (fragClass == HomeScreen.class || fragClass == HomeScreen_Logged_In.class) //TODO ad other reset/create login pages
        {
            this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                    getResources().getColor(R.color.background_home_color));
            setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_home_color));
        }
        else if (fragClass == MoodParent.class || fragClass == MoodEntry.class)
        {
            this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                    getResources().getColor(R.color.background_mood_color));
            setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_mood_color));
        }
        else if (fragClass == SleepParent.class || fragClass == SleepEntry.class)
        {
            this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                    getResources().getColor(R.color.background_sleep_color));
            setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_sleep_color));
        }
        else if (fragClass == ExerciseParent.class || fragClass == ExerciseEntry.class)
        {
            this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                    getResources().getColor(R.color.background_exercise_color));
            setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_exercise_color));
        }
        else if(fragClass == DietParent.class || fragClass == DietEntry.class)
        {
            this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                    getResources().getColor(R.color.background_diet_color));
            setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_diet_color));
        }
        else if(fragClass == MedicationParent.class || fragClass == MedicationEntry.class)
        {
            this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                    getResources().getColor(R.color.background_medication_color));
            setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_medication_color));
        }
        else if(fragClass == AnalysisFragment.class)
        {
            this.findViewById(R.id.navigation_drawer_fragment_content).setBackgroundColor(
                    getResources().getColor(R.color.background_analysis_color));
            setActionBarColorFromFragment(getResources().getColor(R.color.ActionTopBar_analysis_color));
        }
    }

    /**************************************************************************
     *  Manages onClick events for each modules CreateEntry button.
     *  The current fragment is retrieved and identified and replaces with its create entry fragment
     * @param v
     **************************************************************************/
    public void CreateEntryOnClick(View v) {
        //Initialize to home screen in case the fragment active is not found in the following, it will not crash and just go back to home
        Fragment newFragment = new HomeScreen();

        //if fragment exists
        if (activeFragment != null && activeFragment.isVisible()) {

            //Determines which module parent activity is active and then replaces it with its child Entry fragment
            if(activeFragment.getClass() == MoodParent.class) {
                newFragment = new MoodEntry();
            } else if (activeFragment.getClass() == SleepParent.class){
                newFragment = new SleepEntry();
            } else if (activeFragment.getClass() == ExerciseParent.class){
                newFragment = new ExerciseEntry();
            } else if (activeFragment.getClass() == DietParent.class) {
                newFragment = new DietEntry();
            } else if (activeFragment.getClass() == MedicationParent.class){
                newFragment = new MedicationEntry();
            }

            //replaces the current fragment with the entry fragment
            replaceFragment(newFragment);
        }
    }

    /**************************************************************************
     *  Manages onClick events for each modules CreateEntry button.
     *  The current fragment is retrieved and identified and replaces with its create entry fragment
     *  Duplicate of above code with a set arguments call with the bundle
     * @param v
     * @param bundle A bundle of arguments to pass to the next fragment
     **************************************************************************/
    public void CreateEntryOnClickWithBundle(View v, Bundle bundle) {
        //Initialize to home screen in case the fragment active is not found in the following, it will not crash and just go back to home
        Fragment newFragment = new HomeScreen();

        //if fragment exists
        if (activeFragment != null && activeFragment.isVisible()) {

            //Determines which module parent activity is active and then replaces it with its child Entry fragment
            if(activeFragment.getClass() == MoodParent.class) {
                newFragment = new MoodEntry();
            } else if (activeFragment.getClass() == SleepParent.class){
                newFragment = new SleepEntry();
            } else if (activeFragment.getClass() == ExerciseParent.class){
                newFragment = new ExerciseEntry();
            } else if (activeFragment.getClass() == DietParent.class){
                newFragment = new DietEntry();
            } else if (activeFragment.getClass() == MedicationParent.class){
                newFragment = new MedicationEntry();
            }

            newFragment.setArguments(bundle);

            //replaces the current fragment with the entry fragment
            replaceFragment(newFragment);
        }
    }

    /**************************************************************************
     *  Manages onClick events for each modules Entry done button.
     *  The current fragment is retrieved and identified and replaces with its parent fragment
     * @param v
     **************************************************************************/
    public void EntryDoneOnClick(View v) {
        //Initialize to home screen in case the fragment active is not found in the following, it will not crash and just go back to home
        Fragment newFragment = new HomeScreen();

        //if fragment exists
        if (activeFragment != null && activeFragment.isVisible()) {
            //Determines which module entry activity is active and then replaces it with its parent fragment
            if(activeFragment.getClass() == MoodEntry.class) {
                ((MoodEntry) activeFragment).onDoneClick(v);
                newFragment = new MoodParent();
            } else if (activeFragment.getClass() == SleepEntry.class){
                ((SleepEntry) activeFragment).onDoneClick(v);
                newFragment = new SleepParent();
            } else if (activeFragment.getClass() == ExerciseEntry.class){
                ((ExerciseEntry) activeFragment).onDoneClick(v);
                newFragment = new ExerciseParent();
            } else if (activeFragment.getClass() == DietEntry.class){
                ((DietEntry) activeFragment).onDoneClick(v);
                newFragment = new DietParent();
            } else if (activeFragment.getClass() == MedicationEntry.class){
                ((MedicationEntry) activeFragment).onDoneClick(v);
                newFragment = new MedicationParent();
            }
            //If the active fragment is analysis, the default will be called and user will be taken
            //back to the home page.

            //replaces the current fragment with the parent fragment
            replaceFragment(newFragment);

            //Try to hide keyboard if open?
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public void EntryAcceptOnClick(View v) {
        //Initialize to home screen in case the fragment active is not found in the following, it will not crash and just go back to home
        Fragment newFragment = new HomeScreen();

        //if fragment exists
        if (activeFragment != null && activeFragment.isVisible()) {
            //Determines which module entry activity is active and then replaces it with its parent fragment
            if(activeFragment.getClass() == MoodSettings.class) {
                ((MoodSettings) activeFragment).onAcceptClick(v);
                newFragment = new MoodEntry();
            } else if (activeFragment.getClass() == SleepSettings.class){
                ((SleepSettings) activeFragment).onAcceptClick(v);
                newFragment = new SleepEntry();
            } else if (activeFragment.getClass() == ExerciseSettings.class){
                ((ExerciseSettings) activeFragment).onAcceptClick(v);
                newFragment = new ExerciseEntry();
            } else if (activeFragment.getClass() == DietSettings.class){
                ((DietSettings) activeFragment).onAcceptClick(v);
                newFragment = new DietEntry();
            } else if (activeFragment.getClass() == MedicationSettings.class){
                ((MedicationSettings) activeFragment).onAcceptClick(v);
                newFragment = new MedicationEntry();
            } else if (activeFragment.getClass() == ModuleSettings.class )
            {
                ((ModuleSettings)activeFragment).onAcceptClick(v);
                //Updates menu items if the module settings were changed
                UpdateMenuItems();
            }

            //replaces the current fragment with the parent fragment
            replaceFragment(newFragment);
        }
    }

    public void resetPasswordButtonClick(View v){
        ((com.rocket.biometrix.Login.GetLogin)activeFragment).resetPasswordClick();
    }
    public  void passwordSignIn(View v){
        ((com.rocket.biometrix.HomeScreen)activeFragment).okayButtonClick(v);
    }

    public void onRunButtonClick(View v)
    {
        ((com.rocket.biometrix.Analysis.AnalysisFragment)activeFragment).onRunButtonClick(v);
    }

    public void cancelButton(View v){
        replaceFragment(new HomeScreen());
    }

    public void createAccountButtonClick(View v){
        ((CreateLogin)activeFragment).createAccount();
    }

    public void returnToLoggedInHomePage()
    {
        replaceFragment(new HomeScreen_Logged_In());
    }

    /**
     * If the current user is logged in with Biometrix, sign them out. If they are logged in via
     * google this directs them to that page instead.
     */
    private void LogoutUser()
    {
        if (LocalAccount.isLoggedIn())
        {
            LocalAccount.Logout();
            Toast.makeText(getApplicationContext(), "Account logged out", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Shows or hides items in the navdrawer based on whether or not the user's account has them listed
     */
    public void UpdateMenuItems()
    {
        Menu navMenu = navView.getMenu();



        SetItemVisibility(navMenu, R.id.nav_mood_module, SettingsAndEntryHelper.MOOD_MODULE);
        SetItemVisibility(navMenu, R.id.nav_sleep_module, SettingsAndEntryHelper.SLEEP_MODULE);
        SetItemVisibility(navMenu, R.id.nav_exercise_module, SettingsAndEntryHelper.EXERCISE_MODULE);
        SetItemVisibility(navMenu, R.id.nav_diet_module, SettingsAndEntryHelper.DIET_MODULE);
        SetItemVisibility(navMenu, R.id.nav_medication_module, SettingsAndEntryHelper.MEDICATION_MODULE);

        //Makes a few options invisible if the user is not logged in.
        if(!LocalAccount.isLoggedIn() )
        {
            navMenu.findItem(R.id.nav_sync).setVisible(false);
            navMenu.findItem(R.id.nav_logout).setVisible(false);
            navMenu.findItem(R.id.nav_analytics).setVisible(false);
        }
        else
        {
            navMenu.findItem(R.id.nav_sync).setVisible(true);
            navMenu.findItem(R.id.nav_logout).setVisible(true);
            navMenu.findItem(R.id.nav_analytics).setVisible(true);
        }

    }

    /**
     * For use in the UpdateMenuItems above, set's the item's visibility based on settings
     * @param navMenu A reference to the menu
     * @param itemID The R.id. value of the item
     * @param key The key for the setting
     */
    private void SetItemVisibility(Menu navMenu, int itemID, String key)
    {
        if (!LocalAccount.GetInstance().getBoolean(getApplicationContext(), key, true)) {
            navMenu.findItem(itemID).setVisible(false);
        }
        else
        {
            navMenu.findItem(itemID).setVisible(true);
        }
    }

    public void MoodGraph(View v) {
        activeFragment = new MoodGraph();
        replaceFragment(activeFragment);
    }
    public void graphNext(View v) {
        ((GraphBase)activeFragment).nextMonth();
    }
    public void graphPrev(View v) {
        ((GraphBase)activeFragment).prevMonth();
    }

    public void SleepGraph(View v){
        activeFragment = new SleepGraph();
        replaceFragment(activeFragment);
    }
    public  void ExerciseGraph(View v){
        activeFragment = new ExerciseGraph();
        replaceFragment(activeFragment);
    }
    public  void DietGraph(View v){
        activeFragment = new DietGraph();
        replaceFragment(activeFragment);
    }
    public  void AllGraph(View v){
        activeFragment = new AllGraph();
        replaceFragment(activeFragment);
    }


    public Fragment PopulateEntryIntercept(String tableKey) {
        Fragment EntryFrag;

        switch (tableKey) {
            case "exercise":
                EntryFrag = new ExerciseEntry();
                break;
            case "sleep":
                EntryFrag = new SleepEntry();
                break;
            case "diet":
                EntryFrag = new DietEntry();
                break;
            case "mood":
                EntryFrag = new MoodEntry();
                break;
            case "medication":
                EntryFrag = new MedicationEntry();
                break;

            default:
                throw new IllegalArgumentException(" " + tableKey);
        }


        return EntryFrag;
    }
}
