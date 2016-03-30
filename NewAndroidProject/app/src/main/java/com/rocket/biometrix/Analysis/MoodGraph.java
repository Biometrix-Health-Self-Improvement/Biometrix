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
public class MoodGraph extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
View v;

    private OnFragmentInteractionListener mListener;

    public MoodGraph() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_mood_graph, container, false);

        GraphView graph = (GraphView) v.findViewById(R.id.graphMood);

        ArrayList<DataPoint[]> dp = getDataPointArrayMoods();

        LineGraphSeries<DataPoint> dep = new LineGraphSeries<DataPoint>(dp.get(0));
        dep.setTitle("Depression");

        LineGraphSeries<DataPoint> elev = new LineGraphSeries<DataPoint>(dp.get(1));
        elev.setTitle("Elevation");
        elev.setColor(Color.MAGENTA);

        LineGraphSeries<DataPoint> irr = new LineGraphSeries<DataPoint>(dp.get(2));
        irr.setTitle("Irritability");
        irr.setColor(Color.RED);

        LineGraphSeries<DataPoint> anx = new LineGraphSeries<DataPoint>(dp.get(3));
        anx.setTitle("Anxiety");
        anx.setColor(Color.GREEN);


        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setVerticalLabels(new String[]{"None", "Mild", "Moderate", "Severe", "Very Severe"});


        //will set x axis as dates
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        graph.addSeries(dep);
        graph.addSeries(elev);
        graph.addSeries(irr);
        graph.addSeries(anx);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setTextSize(25);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        return v;
    }


    ArrayList<DataPoint[]> getDataPointArrayMoods() {
        //gets it for the current month
        Cursor cursor = LocalStorageAccessMood.getCurrentMonthEntries(v.getContext());
        ArrayList<DataPoint> dep = new ArrayList<DataPoint>();
        ArrayList<DataPoint> elev = new ArrayList<DataPoint>();
        ArrayList<DataPoint> irr = new ArrayList<DataPoint>();
        ArrayList<DataPoint> anx = new ArrayList<DataPoint>();


/*        String query = "Select " + DATE + ", " + TIME + ", " +
                DEP + ", " + ELEV + ", " + IRR + ", " + ANX + ", " + NOTE +
                " FROM " + TABLE_NAME + " WHERE " + DATE+ " BETWEEN (date('now', 'start of month')) AND (date('now')) Order By " + DATE;
*/
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                int val = Integer.parseInt(cursor.getString(2));
                dep.add(new DataPoint(dayOfMonth, val));
                val = Integer.parseInt(cursor.getString(3));
                elev.add(new DataPoint(dayOfMonth, val));
                val = Integer.parseInt(cursor.getString(4));
                irr.add(new DataPoint(dayOfMonth, val));
                val = Integer.parseInt(cursor.getString(5));
                anx.add(new DataPoint(dayOfMonth, val));

                cursor.moveToNext();
            }
        }
        cursor.close();

        ArrayList<DataPoint[]> ret = new ArrayList<DataPoint[]>();
        ret.add(new DataPoint[dep.size()]);
        int i = 0;
        for (DataPoint n : dep) {
            ret.get(0)[i++] = n;
        }
        ret.add(new DataPoint[elev.size()]);
        i = 0;
        for (DataPoint n : elev) {
            ret.get(1)[i++] = n;
        }
        ret.add(new DataPoint[irr.size()]);
        i = 0;
        for (DataPoint n : irr) {
            ret.get(2)[i++] = n;
        }
        ret.add(new DataPoint[anx.size()]);
        i = 0;
        for (DataPoint n : anx) {
            ret.get(3)[i++] = n;
        }

        return ret;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
