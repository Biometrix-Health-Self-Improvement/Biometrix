package com.rocket.biometrix.Analysis;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;

/**

 */
public class ExerciseGraph extends GraphBase {

    @Override
    public void populateGraph() {
        graph.removeAllSeries();
        ArrayList<DataPoint[]> dp = getDataPointArray(year, month);

        PointsGraphSeries<DataPoint> len = new PointsGraphSeries<DataPoint>(dp.get(0));
        len.setTitle("Minutes");

        setDateBounds();

        graph.setTitle("Exercise");
        if(!len.isEmpty()) graph.addSeries(len);


        setLegend();
        setMonthYearTitle();
    }

    @Override
    public ArrayList<DataPoint[]> getDataPointArray(int year, int month) {
        Cursor cursor = LocalStorageAccessExercise.getMonthEntries(v.getContext(), year, month);
        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

/*
DATE, TIME, TITLE, TYPE, MINUTES, REPS, LAPS, WEIGHT, INTY
*/
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                String s = cursor.getString(4);
                if(s != null) {
                    int val = Integer.parseInt(s);
                    dataPoints.add(new DataPoint(dayOfMonth, val));
                }

                cursor.moveToNext();
            }
        }
        cursor.close();

        ArrayList<DataPoint[]> ret = new ArrayList<DataPoint[]>();
        ret.add(new DataPoint[dataPoints.size()]);
        int i = 0;
        for (DataPoint n : dataPoints) {
            ret.get(0)[i++] = n;
        }
        return ret;
    }

    @Override
    GraphView getGraph() {
        return (GraphView) v.findViewById(R.id.graphExercise);
    }

    @Override
    View graphInflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_exercise_graph, container, false);
    }

    @Override
    protected void setMonthYearTitle() {
        ((TextView)v.findViewById(R.id.yearTextViewExercise)).setText(Integer.toString(year));
        TextView mnth = (TextView)v.findViewById(R.id.monthTextViewExercise);
        mnth.setText(new DateFormatSymbols().getMonths()[month-1]);
    }
}
