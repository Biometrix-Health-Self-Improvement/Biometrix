package com.rocket.biometrix.Analysis;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by tannalynn on 4/9/2016.
 *
 */
public class SleepGraph extends GraphBase{
    @Override
    public void populateGraph() {
        graph.removeAllSeries();

        ArrayList<DataPoint[]> dp = getDataPointArray(year, month);
        LineGraphSeries<DataPoint> len = new LineGraphSeries<>(dp.get(0));
        len.setTitle("Duration of Sleep");

        LineGraphSeries<DataPoint> qual = new LineGraphSeries<>(dp.get(1));
        qual.setTitle("Quality of Sleep");
        qual.setColor(Color.RED);

        setDateBounds(graph);

        if(!len.isEmpty()) graph.addSeries(len);
        if(!qual.isEmpty()) graph.addSeries(qual);
        graph.setTitle("Sleep");

        setLegend(graph);
        setMonthYearTitle();
    }

    @Override
    public ArrayList<DataPoint[]> getDataPointArray(int year, int month) {
        Cursor cursor = LocalStorageAccessSleep.getMonthEntries(v.getContext(), year, month);
        ArrayList<DataPoint> len = new ArrayList<>();
        ArrayList<DataPoint> qual = new ArrayList<>();

        //date, time, dur, qual

        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()) {
                int day = Integer.parseInt(cursor.getString(0).substring(8));

                String hr = cursor.getString(2).substring(0, cursor.getString(2).indexOf(":"));
                String min= cursor.getString(2).substring(cursor.getString(2).indexOf(":")+1);

                double val = Integer.parseInt(hr) + (Integer.parseInt(min) / 60.0);
                len.add(new DataPoint(day, val));
                val = Integer.parseInt(cursor.getString(3));
                qual.add(new DataPoint(day, val));

                cursor.moveToNext();
            }
        }
        cursor.close();

        ArrayList<DataPoint[]> ret = new ArrayList<>();
        ret.add(new DataPoint[len.size()]);
        int i = 0;
        for (DataPoint n : len) {
            ret.get(0)[i++] = n;
        }
        ret.add(new DataPoint[qual.size()]);
        i = 0;
        for (DataPoint n : qual) {
            ret.get(1)[i++] = n;
        }

        return ret;
    }

    @Override
    GraphView getGraph() {
        return (GraphView) v.findViewById(R.id.graphSleep);
    }

    @Override
    View graphInflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_sleep_graph, container, false);
    }

    @Override
    protected void setMonthYearTitle() {
        ((TextView)v.findViewById(R.id.yearTextViewSleep)).setText(Integer.toString(year));
        TextView mnth = (TextView)v.findViewById(R.id.monthTextViewSleep);
        mnth.setText(new DateFormatSymbols().getMonths()[month-1]);
    }
}