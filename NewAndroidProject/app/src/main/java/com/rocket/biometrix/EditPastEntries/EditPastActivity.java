package com.rocket.biometrix.EditPastEntries;

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
    //Bool flip is unused NOW; but necessary for custom touch events later.
        boolean mNewDateTouched; //if true, cal fragment's focused date was changed and RV needs to be updated.
        EntryCandiesFragment ECF;


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
            ECF = new EntryCandiesFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            CalendarFragmenti.setArguments(getIntent().getExtras());
            ECF.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, CalendarFragmenti).commit();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, ECF).commit();


            //v.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        }

    }//end onCreate()


    //RV Fragment
    @Override
    public boolean isListCurrent() {
        return mNewDateTouched;
    }

    //RV Fragment
    @Override
    public List<CursorPair> getCursorList() {


        mNewDateTouched = false;

        List<CursorPair> stackCursorList = new ArrayList<CursorPair>(mCursorList);
        mCursorList.clear(); //Hack to remove stale cursors from last Cal fragment callback

        return stackCursorList;
    }

    //Cal Fragment
    //TODO: For multiple tables to work; this needs to be refactored to accept a list of CursorPairs genned from calendar
    @Override
    public int onFragDateSelect(List<CursorPair> injected) {
        int errno = 0;

        //TODO: Error Checking
        mCursorList = injected;
        //IE here do a quick count() check on the list of cursor pairs then mCursorList = CP parameter
        mNewDateTouched = true;

        //Callback to RV adapter
        ECF.updateCandies();


        return errno;
    }
}
