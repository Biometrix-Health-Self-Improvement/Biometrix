package com.rocket.biometrix.EditPastEntries;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.rocket.biometrix.EditPastEntries.fragments.EditCalendar;
import com.rocket.biometrix.EditPastEntries.fragments.EntryCandiesFragment;
import com.rocket.biometrix.R;

import java.util.Dictionary;

public class EditPastActivity extends AppCompatActivity
        implements EditCalendar.OnFragmentInteractionListener, EntryCandiesFragment.OnListFragmentInteractionListener {

    //???Should this be matrix cursor? custom Map? List of custom class??? list of templated Pairs?
    //Dictionary for storing database cursors that calendar injects and RV reads
    Dictionary<String,Cursor> CursorDictionary;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_past2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: Get Calendar Fragment and RV fragment in here, implement their on frag interactions somehow

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            EditCalendar CalendarFragmenti = new EditCalendar();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            CalendarFragmenti.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, CalendarFragmenti).commit();
        }


        //TODO: USE this Activity as 'bridge'
        //TODO: try http://developer.android.com/training/basics/fragments/communicating.html
        //TODO: Get tests (can be hardcoded not true tests) running to demo current RV code
        //https://developer.android.com/training/material/lists-cards.html
        //TODO: FInally finish RV code and start adding tab code etc. in actually extensible way
        //TODO: PRAY TO SWEET BABY JESUS that changes to the LocalDB don't ruin my life.

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(CandyItems item) {

    }
}
