package com.rocket.biometrix.EditPastEntries.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rocket.biometrix.EditPastEntries.CandyItems;
import com.rocket.biometrix.EditPastEntries.EntryCandyViewHolder;
import com.rocket.biometrix.R;

import java.util.List;

/**
 * Created by JP on 2/16/2016.
 */
public class MyEntryCandiesRecyclerViewAdapter extends RecyclerView.Adapter<EntryCandyViewHolder> {

    private List<CandyItems> listItemsList;
    private Context mContext;

    private int focusedItem = 0;

    //Constructor
    public MyEntryCandiesRecyclerViewAdapter(Context context, List<CandyItems> candyItemslist) {
        this.mContext = context;
        this.listItemsList = candyItemslist;
    }

    @Override
    public EntryCandyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        //Give layout of 'a' candy to the holder
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_entrycandies, null);
        EntryCandyViewHolder holder = new EntryCandyViewHolder(v);

            holder.recLayout.setOnClickListener(new View.OnClickListener() {

                @Override
            public void onClick(View v) {

                    Log.d("List Size", Integer.toString(getItemCount()));

                    //TODO:Open up edit entry with proper info for candy touched

                    Toast.makeText(mContext, "EDITING ENTRY", Toast.LENGTH_LONG).show();
                }
            });


        return holder;
    }

    @Override
    public void onBindViewHolder(EntryCandyViewHolder ECVholder, int position) {
        CandyItems listItems = listItemsList.get(position); //get an entry in the list of 'candies'
        ECVholder.itemView.setSelected(focusedItem == position);

        ECVholder.getLayoutPosition();
        //Fill all entry candies with appropriate data

        //TODO: FILL UI ELEMENTS HERE from cursor genned by calendar

        //TODO: WRITE CURSOR PARSING CLASS == CursorHelper

    }

    @Override
    public int getItemCount() {
        //ternary; if null 0; else size of list of candies
        return (null != listItemsList ? listItemsList.size() : 0);
    }

    public void clearAdapter(){
        listItemsList.clear();
        notifyDataSetChanged();
    }
}
