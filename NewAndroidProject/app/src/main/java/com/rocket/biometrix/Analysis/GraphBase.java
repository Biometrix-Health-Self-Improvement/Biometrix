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
import android.widget.TextView;

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
import java.util.GregorianCalendar;

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
        v = graphInflate(inflater, container);

        graph = getGraph(); //gets the correct graph from derived class

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
        if (year < Calendar.getInstance().get(Calendar.YEAR)
                || (year == Calendar.getInstance().get(Calendar.YEAR)
                    && month < Calendar.getInstance().get(Calendar.MONTH)+1)) {
            month++;
            if (month > 12) {
                year++;
                month = 1;
            }
        }
        populateGraph();
    }

    protected DefaultLabelFormatter setDateBounds(GraphView graph){
        int numDays = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH);

        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(numDays);
        graph.getViewport().setXAxisBoundsManual(true);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        DefaultLabelFormatter label = new DefaultLabelFormatter(nf,NumberFormat.getInstance());

        graph.getGridLabelRenderer().setLabelFormatter(label);
        return label ;

    }

    protected void setLegend(GraphView graph){
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setTextSize(30);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    protected boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    abstract protected void setMonthYearTitle();


    abstract public void populateGraph();

    abstract public ArrayList<DataPoint[]> getDataPointArray(int year, int month);
    abstract GraphView getGraph();
    abstract View graphInflate(LayoutInflater inflater, ViewGroup container);
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
