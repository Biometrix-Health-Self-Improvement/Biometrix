package com.rocket.biometrix.Common;

import android.content.Context;
import android.view.View;
import android.widget.Button;

/**
 * Created by TJ on 5/8/2016.
 */
public class PastEntryButton extends Button{

    private int referencedID = -1;

    public PastEntryButton(Context context) {
        super(context);
    }

    public void setID(int id)
    {
        referencedID = id;
    }

}
