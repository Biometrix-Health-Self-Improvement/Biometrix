package com.rocket.biometrix.EditPastEntries.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.EditPastEntries.CandyItems;
import com.rocket.biometrix.EditPastEntries.EntryCandyViewHolder;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import java.util.List;

/**
 * Created by JP on 2/16/2016.
 */
public class MyEntryCandiesRecyclerViewAdapter extends RecyclerView.Adapter<EntryCandyViewHolder>
{

    private List<CandyItems> listItemsList;
    private Context mContext;

    private int focusedItem = 0;

    //Constructor
    public MyEntryCandiesRecyclerViewAdapter(Context context, List<CandyItems> candyItemslist) {
        this.mContext = context;
        this.listItemsList = candyItemslist;
    }

    @Override
    public EntryCandyViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {

        //Give layout of 'a' candy to the holder
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_entrycandies, null); //second param was null
        final EntryCandyViewHolder holder = new EntryCandyViewHolder(v);

            holder.recLayout.setOnClickListener(new View.OnClickListener() {

                @Override
            public void onClick(View v) {

                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(mContext, holder.recLayout);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.manage_popup, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            //Toast.makeText(mContext,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();

                            if (item.getTitle().toString().equals("Edit")) {
                                //Log.d("List Size", Integer.toString(getItemCount()))
                                //Toast.makeText(mContext, "EDITING ENTRY", Toast.LENGTH_LONG).show();

                                //rowEntryClicked(holder.type.toString(), holder._UID); //WAY too complicated to make 3 interfaces //HOW tf keep references to context/classes?
                                Intent intent = new Intent(mContext, NavigationDrawerActivity.class);
                                intent.putExtra("tablename", holder.type);
                                intent.putExtra("uid", holder._UID);
                                mContext.startActivity(intent);
                            }
                            else if (item.getTitle().toString().equals("Delete")){
                                LocalStorageAccess.safeDeleteRow(holder.type,holder._UID);
                            }


                            return true;
                        }
                    });

                    popup.show();//showing popup menu


                }
            });


        return holder;
    }

    @Override
    public void onBindViewHolder(EntryCandyViewHolder ECVholder, int position) {

        CandyItems listItem = listItemsList.get(position); //get an entry in the list of 'candies'
        ECVholder.itemView.setSelected(focusedItem == position);

        ECVholder.getLayoutPosition();

        //Fill entry candy UI with appropriate data
        ECVholder.type = (listItem.type);
        ECVholder.time.setText(listItem.time);
        ECVholder.title.setText(listItem.title);
        ECVholder._UID = (listItem._ID); //Have to map UI back to database somehow :)
        ECVholder.setBGC();

    }

    @Override
    public int getItemCount() {
        //ternary; if null 0; else size of list of candies
        return (null != listItemsList ? listItemsList.size() : 0);
    }

    public void clearAdapter(){
        listItemsList.clear();
        notifyDataSetChanged(); //calls onBindViewHolder
    }

}
