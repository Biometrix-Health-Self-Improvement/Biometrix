package com.rocket.biometrix.EditPastEntries;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.rocket.biometrix.EditPastEntries.fragments.EditCalendar;
import com.rocket.biometrix.EditPastEntries.fragments.EntryCandiesFragment;
import com.rocket.biometrix.R;

import java.util.ArrayList;
import java.util.List;

public class EditPastActivity extends AppCompatActivity
        implements EditCalendar.OnFragmentInteractionListener, EntryCandiesFragment.OnListFragmentInteractionListener {

       List<CursorPair> mCursorList = new ArrayList<CursorPair>();
        boolean mNewDateTouched; //if true, cal fragment's focused date was changed and RV needs to be updated.
        EntryCandiesFragment ECF = new EntryCandiesFragment();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_past2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNewDateTouched = false;

//         Check that the activity is using the layout version with
//         the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            EditCalendar CalendarFragmenti = new EditCalendar();
            //Made below global mutable
            //EntryCandiesFragment ECF = new EntryCandiesFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            CalendarFragmenti.setArguments(getIntent().getExtras());
            ECF.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, CalendarFragmenti).commit();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, ECF).commit();

        }


        //TODO: USE this Activity as 'bridge'
        //TODO: try http://developer.android.com/training/basics/fragments/communicating.html
        //TODO: Get tests (can be hardcoded not true tests) running to demo current RV code
        //https://developer.android.com/training/material/lists-cards.html
        //TODO: FInally finish RV code and start adding tab code etc. in actually extensible way
        //TODO: PRAY TO SWEET BABY JESUS that changes to the LocalDB don't ruin my life.

    }




    //RV Fragment
    @Override
    public boolean isListCurrent() {
        return mNewDateTouched;
    }

    //RV Fragment
    @Override
    public List<CursorPair> getCursorList() {

        mNewDateTouched = false;
        return mCursorList;
    }

    //Cal Fragment
    @Override
    public int onFragDateSelect(String table, Cursor datesQuery) {
        int errno = 0;

        //TODO: Error Checking
        mCursorList.add(new CursorPair(table,datesQuery));
        mNewDateTouched = true;

        //TODO: DOES this acutually work???
        ECF.updateCandies();

        return errno;
    }
}
