package com.rocket.biometrix.EditPastEntries;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rocket.biometrix.R;

/**
 * Created by JP on 2/16/2016.
 * https://developer.android.com/training/material/lists-cards.html
 *
 * Child ViewHolder needed by recycle view Linear layout manager
 */
public class EntryCandyViewHolder extends RecyclerView.ViewHolder {
    public TextView type; //should be protected
    public TextView title;
    public TextView time;
    public TextView misc;

    public String _UID;

    public LinearLayout recLayout;

    public EntryCandyViewHolder(View itemView) {
        super(itemView);

        this.type = (TextView)(itemView.findViewById(R.id.candy_type));
        this.title = (TextView)(itemView.findViewById(R.id.candy_title));
        this.misc = (TextView)(itemView.findViewById(R.id.candy_misc));
        this.time = (TextView)(itemView.findViewById(R.id.candy_time));
        this.recLayout = (LinearLayout) (itemView.findViewById(R.id.candy_linearl));

        itemView.setClickable(true);
    }
}
