package com.rocket.biometrix.EditPastEntries;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.rocket.biometrix.R;

public class EditPastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_past2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: Get Calendar Fragment and RV fragment in here, implement their on frag interactions somehow
        //TODO: USE this Activity as 'bridge'
        //TODO: try http://developer.android.com/training/basics/fragments/communicating.html
        //TODO: Get tests (can be hardcoded not true tests) running to demo current RV code
        //https://developer.android.com/training/material/lists-cards.html
        //TODO: FInally finish RV code and start adding tab code etc. in actually extensible way
        //TODO: PRAY TO SWEET BABY JESUS that changes to the LocalDB don't ruin my life.

    }

}
