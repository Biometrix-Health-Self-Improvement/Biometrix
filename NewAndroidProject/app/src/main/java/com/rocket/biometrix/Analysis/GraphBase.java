package com.rocket.biometrix.Analysis;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jjoe64.graphview.*;
import com.jjoe64.graphview.helper.*;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoodGraph.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MoodGraph#newInstance} factory method to
 * create an instance of this fragment.
 */
public abstract class GraphBase extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View v;
    GraphView graph;
    int year;
    int month;

    private OnFragmentInteractionListener mListener;

    public GraphBase() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_mood_graph, container, false);

        graph = (GraphView) v.findViewById(R.id.graphMood);

        year = Calendar.getInstance().get(Calendar.YEAR);
        month = Calendar.getInstance().get(Calendar.MONTH)+1;
        populateGraph();

        return v;
    }

    public void prevMonth(){
        month--;
        if (month < 1) {
            year--;
            month = 12;
        }
        populateGraph();
    }
    public void nextMonth(){
        if(year <= Calendar.getInstance().get(Calendar.YEAR) && month < Calendar.getInstance().get(Calendar.MONTH)+1) {
            month++;
            if (month > 12) {
                year++;
                month = 1;
            }
        }
        populateGraph();
    }

    abstract public void populateGraph();

    abstract public ArrayList<DataPoint[]> getDataPointArrayMoods(int year, int month);
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
