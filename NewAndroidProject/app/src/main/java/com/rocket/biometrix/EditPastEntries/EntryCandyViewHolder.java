package com.rocket.biometrix.EditPastEntries;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rocket.biometrix.R;

/**
 * Created by JP on 2/16/2016.
 * https://developer.android.com/training/material/lists-cards.html
 *
 * Child ViewHolder needed by recycle view Linear layout manager
 */
public class EntryCandyViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView time;
    public TextView misc;

    public String _UID;
    public String type;

    public GridLayout recLayout;

    public ImageView eyeCon;

    public EntryCandyViewHolder(View itemView) {
        super(itemView);

        //this.type = (TextView)(itemView.findViewById(R.id.candy_type));
        this.title = (TextView)(itemView.findViewById(R.id.candy_title));
        this.misc = (TextView)(itemView.findViewById(R.id.candy_misc));
        this.time = (TextView)(itemView.findViewById(R.id.candy_time));

        this.recLayout = (GridLayout) (itemView.findViewById(R.id.candy_linearl));

        itemView.setClickable(true);

    }

    public void setBGC(){
        if (this.type != null){
            switch(this.type){
                case "exercise":
                    this.recLayout.setBackgroundColor(this.recLayout.getResources().getColor(R.color.background_exercise_color));
                    break;
                case "sleep":
                    this.recLayout.setBackgroundColor(this.recLayout.getResources().getColor(R.color.background_sleep_color));
                    break;
                case "diet":
                    this.recLayout.setBackgroundColor(this.recLayout.getResources().getColor(R.color.background_diet_color));
                    break;
                case "medication":
                    this.recLayout.setBackgroundColor(this.recLayout.getResources().getColor(R.color.background_medication_color));
                    break;
                case "mood":
                    this.recLayout.setBackgroundColor(this.recLayout.getResources().getColor(R.color.background_mood_color));
                    break;
                default:
                    break;
            }


        }

    }
}
