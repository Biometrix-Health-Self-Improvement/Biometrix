package com.rocket.biometrix.Analysis;

import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.*;
import com.jjoe64.graphview.helper.*;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.R;
import java.util.ArrayList;


public class MoodGraph extends GraphBase {

    @Override
    public void populateGraph(){
        graph.removeAllSeries();
        ArrayList<DataPoint[]> dp = getDataPointArray(year, month);

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
        //staticLabelsFormatter.setHorizontalLabels(new String[]{"1", "10", "20", "31"});

        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        graph.addSeries(dep);
        graph.addSeries(elev);
        graph.addSeries(irr);
        graph.addSeries(anx);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setTextSize(25);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }


    @Override
    public ArrayList<DataPoint[]> getDataPointArray(int year, int month) {
        //gets it for the current month
        Cursor cursor = LocalStorageAccessMood.getMonthEntries(v.getContext(), year, month);
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
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8))-1;
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

    @Override
    GraphView getGraph() {
        return (GraphView) v.findViewById(R.id.graphMood);
    }

    @Override
    View graphInflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_mood_graph, container, false);
    }
}