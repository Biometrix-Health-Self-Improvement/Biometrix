package com.rocket.biometrix.EditPastEntries.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.EditPastEntries.CandyItems;
import com.rocket.biometrix.EditPastEntries.CursorHelper;
import com.rocket.biometrix.EditPastEntries.CursorPair;
import com.rocket.biometrix.EditPastEntries.adapters.MyEntryCandiesRecyclerViewAdapter;
import com.rocket.biometrix.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class EntryCandiesFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private List<CandyItems> candyItemslist = new ArrayList<>();
    private OnListFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private MyEntryCandiesRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EntryCandiesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static EntryCandiesFragment newInstance(int columnCount) {
        EntryCandiesFragment fragment = new EntryCandiesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_entrycandies_list, container, false);

        //Initialize recycler view
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.RVlist);

        // Set the adapter
        if (mRecyclerView instanceof RecyclerView) {
            Context context = mRecyclerView.getContext();

            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            updateCandies();

            //TODO: Lp HOOK in item deco {mRecyclerView.addItemDecoration()}

            mRecyclerView.setAdapter(new MyEntryCandiesRecyclerViewAdapter(context,candyItemslist));
        }
        return rootView;
    }


    //onAttach enables referencing the Listener interface below
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateCandies(){

        //declare the adapter and attach it to the recyclerview
        adapter = new MyEntryCandiesRecyclerViewAdapter(getActivity(),candyItemslist);
        mRecyclerView.setAdapter(adapter);

        //Clear the adapter because new Cal data
        adapter.clearAdapter();

        //get cursor list
        List<CursorPair> Queries = mListener.getCursorList();

        List<CursorHelper> allCH = new ArrayList<>();

        //Populate all Cursor Helper list
        for (CursorPair taybell : Queries) {
            //System.out.println();
            if (taybell.getTableName() == "exercise") {
                CursorHelper exerciseCH = new CursorHelper(taybell, LocalStorageAccessExercise.TITLE, LocalStorageAccessExercise.TIME);
                allCH.add(exerciseCH);
            }

        }

        for(CursorHelper CurseHel : allCH){
            for (int j=0; j < CurseHel.mRows; j++) {
                CandyItems item = new CandyItems();

                item.title = CurseHel.mTitleStrings[j];
                item.time = CurseHel.mTimeStrings[j];

                candyItemslist.add(item);
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {

        boolean isListCurrent();

        List<CursorPair> getCursorList();
    }



}
