package com.rocket.biometrix.Analysis;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;


public class AllGraph extends GraphBase {


    @Override
    public void populateGraph() {
        populateGraphDep();
        populateGraphElev();
        populateGraphIrr();
        populateGraphAnx();
        setMonthYearTitle();
    }

    private void populateGraphAnx() {
        GraphView graph = (GraphView) v.findViewById(R.id.grapAllAnx) ;
        graph.removeAllSeries();
        ArrayList<DataPoint[]> dp = getDataPointArrayAnx(year, month);
        /*
            0 - Anx * 2
            1 - hrs slept
            2 - exercise / 5
         */
        LineGraphSeries<DataPoint> anx = new LineGraphSeries<DataPoint>(dp.get(0));
        anx.setTitle("Anxiety");

        LineGraphSeries<DataPoint> sleep = new LineGraphSeries<DataPoint>(dp.get(1));
        sleep.setTitle("Hours Slept");
        sleep.setColor(Color.MAGENTA);

        PointsGraphSeries<DataPoint> exercise = new PointsGraphSeries<DataPoint>(dp.get(2));
        exercise.setTitle("Exercise");
        exercise.setColor(Color.GREEN);

        setDateBounds(graph);

        graph.setTitle("Anxiety");

        if(!anx.isEmpty()) graph.addSeries(anx);
        if(!sleep.isEmpty()) graph.addSeries(sleep);
        if(!exercise.isEmpty()) graph.addSeries(exercise);

        setLegend(graph);
    }

    private void populateGraphIrr() {
        GraphView graph = (GraphView) v.findViewById(R.id.grapAllIrr) ;
        graph.removeAllSeries();
        ArrayList<DataPoint[]> dp = getDataPointArrayIrr(year, month);
        /*
            0 - Irr * 2
            1 - hrs slept
            2 - exercise / 5
         */
        LineGraphSeries<DataPoint> irr = new LineGraphSeries<DataPoint>(dp.get(0));
        irr.setTitle("Irritability");

        LineGraphSeries<DataPoint> sleep = new LineGraphSeries<DataPoint>(dp.get(1));
        sleep.setTitle("Hours Slept");
        sleep.setColor(Color.MAGENTA);

        PointsGraphSeries<DataPoint> exercise = new PointsGraphSeries<DataPoint>(dp.get(2));
        exercise.setTitle("Exercise");
        exercise.setColor(Color.GREEN);

        setDateBounds(graph);

        graph.setTitle("Irritability");

        if(!irr.isEmpty()) graph.addSeries(irr);
        if(!sleep.isEmpty()) graph.addSeries(sleep);
        if(!exercise.isEmpty()) graph.addSeries(exercise);

        setLegend(graph);
        
    }

    private void populateGraphElev() {
        GraphView graph = (GraphView) v.findViewById(R.id.grapAllElev) ;
        graph.removeAllSeries();
        ArrayList<DataPoint[]> dp = getDataPointArrayElev(year, month);
        /*
            0 - Elev * 2
            1 - hrs slept
            2 - exercise / 5
         */
        LineGraphSeries<DataPoint> elev = new LineGraphSeries<DataPoint>(dp.get(0));
        elev.setTitle("Elevation");

        LineGraphSeries<DataPoint> sleep = new LineGraphSeries<DataPoint>(dp.get(1));
        sleep.setTitle("Hours Slept");
        sleep.setColor(Color.MAGENTA);

        PointsGraphSeries<DataPoint> exercise = new PointsGraphSeries<DataPoint>(dp.get(2));
        exercise.setTitle("Exercise");
        exercise.setColor(Color.GREEN);

        setDateBounds(graph);

        graph.setTitle("Elevation");

        if(!elev.isEmpty()) graph.addSeries(elev);
        if(!sleep.isEmpty()) graph.addSeries(sleep);
        if(!exercise.isEmpty()) graph.addSeries(exercise);

        setLegend(graph);
    }

    private void populateGraphDep() {
        GraphView graph = (GraphView) v.findViewById(R.id.grapAllDep) ;
        graph.removeAllSeries();
        ArrayList<DataPoint[]> dp = getDataPointArrayDep(year, month);
        /*
            0 - Dep * 2
            1 - hrs slept
            2 - exercise / 5
         */
        LineGraphSeries<DataPoint> dep = new LineGraphSeries<DataPoint>(dp.get(0));
        dep.setTitle("Depression");

        LineGraphSeries<DataPoint> sleep = new LineGraphSeries<DataPoint>(dp.get(1));
        sleep.setTitle("Hours Slept");
        sleep.setColor(Color.MAGENTA);

        PointsGraphSeries<DataPoint> exercise = new PointsGraphSeries<DataPoint>(dp.get(2));
        exercise.setTitle("Exercise");
        exercise.setColor(Color.GREEN);

        setDateBounds(graph);

        graph.setTitle("Depression");

        if(!dep.isEmpty()) graph.addSeries(dep);
        if(!sleep.isEmpty()) graph.addSeries(sleep);
        if(!exercise.isEmpty()) graph.addSeries(exercise);

        setLegend(graph);
    }


    public ArrayList<DataPoint[]> getDataPointArrayDep(int year, int month) {
        ArrayList<DataPoint> mood = new ArrayList<DataPoint>();
        ArrayList<DataPoint> sleep = new ArrayList<DataPoint>();
        ArrayList<DataPoint> exer = new ArrayList<DataPoint>();

        Cursor cursor = LocalStorageAccessMood.getMonthEntries(v.getContext(), year, month);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                int val = Integer.parseInt(cursor.getString(2));
                mood.add(new DataPoint(dayOfMonth, val*2));

                cursor.moveToNext();
            }
        }
        cursor.close();

        cursor = LocalStorageAccessSleep.getMonthEntries(v.getContext(), year, month);
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()) {
                int day = Integer.parseInt(cursor.getString(0).substring(8));
                String hr = cursor.getString(2).substring(0, cursor.getString(2).indexOf(":"));
                String min= cursor.getString(2).substring(cursor.getString(2).indexOf(":")+1);

                double val = Integer.parseInt(hr) + (Integer.parseInt(min) / 60.0);
                sleep.add(new DataPoint(day, val));
                cursor.moveToNext();
            }
        }
        cursor.close();

        cursor = LocalStorageAccessExercise.getMonthEntries(v.getContext(), year, month);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                String s = cursor.getString(4);
                if(s != null) {
                    int val = Integer.parseInt(s);
                    exer.add(new DataPoint(dayOfMonth, val/5));
                }
                cursor.moveToNext();
            }
        }
        cursor.close();


        ArrayList<DataPoint[]> ret = new ArrayList<DataPoint[]>();
        ret.add(new DataPoint[mood.size()]);
        int i = 0;
        for (DataPoint n : mood) {
            ret.get(0)[i++] = n;
        }
        ret.add(new DataPoint[sleep.size()]);
        i = 0;
        for (DataPoint n : sleep) {
            ret.get(1)[i++] = n;
        }
        ret.add(new DataPoint[exer.size()]);
        i = 0;
        for (DataPoint n : exer) {
            ret.get(2)[i++] = n;
        }
        return ret;
    }

    public ArrayList<DataPoint[]> getDataPointArrayElev(int year, int month) {
        ArrayList<DataPoint> mood = new ArrayList<DataPoint>();
        ArrayList<DataPoint> sleep = new ArrayList<DataPoint>();
        ArrayList<DataPoint> exer = new ArrayList<DataPoint>();

        Cursor cursor = LocalStorageAccessMood.getMonthEntries(v.getContext(), year, month);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                int val = Integer.parseInt(cursor.getString(3));
                mood.add(new DataPoint(dayOfMonth, val*2));

                cursor.moveToNext();
            }
        }
        cursor.close();

        cursor = LocalStorageAccessSleep.getMonthEntries(v.getContext(), year, month);
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()) {
                int day = Integer.parseInt(cursor.getString(0).substring(8));
                String hr = cursor.getString(2).substring(0, cursor.getString(2).indexOf(":"));
                String min= cursor.getString(2).substring(cursor.getString(2).indexOf(":")+1);

                double val = Integer.parseInt(hr) + (Integer.parseInt(min) / 60.0);
                sleep.add(new DataPoint(day, val));
                cursor.moveToNext();
            }
        }
        cursor.close();

        cursor = LocalStorageAccessExercise.getMonthEntries(v.getContext(), year, month);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                String s = cursor.getString(4);
                if(s != null) {
                    int val = Integer.parseInt(s);
                    exer.add(new DataPoint(dayOfMonth, val/5));
                }
                cursor.moveToNext();
            }
        }
        cursor.close();


        ArrayList<DataPoint[]> ret = new ArrayList<DataPoint[]>();
        ret.add(new DataPoint[mood.size()]);
        int i = 0;
        for (DataPoint n : mood) {
            ret.get(0)[i++] = n;
        }
        ret.add(new DataPoint[sleep.size()]);
        i = 0;
        for (DataPoint n : sleep) {
            ret.get(1)[i++] = n;
        }
        ret.add(new DataPoint[exer.size()]);
        i = 0;
        for (DataPoint n : exer) {
            ret.get(2)[i++] = n;
        }
        return ret;
    }

    public ArrayList<DataPoint[]> getDataPointArrayIrr(int year, int month) {
        ArrayList<DataPoint> mood = new ArrayList<DataPoint>();
        ArrayList<DataPoint> sleep = new ArrayList<DataPoint>();
        ArrayList<DataPoint> exer = new ArrayList<DataPoint>();

        Cursor cursor = LocalStorageAccessMood.getMonthEntries(v.getContext(), year, month);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                int val = Integer.parseInt(cursor.getString(4));
                mood.add(new DataPoint(dayOfMonth, val*2));

                cursor.moveToNext();
            }
        }
        cursor.close();

        cursor = LocalStorageAccessSleep.getMonthEntries(v.getContext(), year, month);
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()) {
                int day = Integer.parseInt(cursor.getString(0).substring(8));
                String hr = cursor.getString(2).substring(0, cursor.getString(2).indexOf(":"));
                String min= cursor.getString(2).substring(cursor.getString(2).indexOf(":")+1);

                double val = Integer.parseInt(hr) + (Integer.parseInt(min) / 60.0);
                sleep.add(new DataPoint(day, val));
                cursor.moveToNext();
            }
        }
        cursor.close();

        cursor = LocalStorageAccessExercise.getMonthEntries(v.getContext(), year, month);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                String s = cursor.getString(4);
                if(s != null) {
                    int val = Integer.parseInt(s);
                    exer.add(new DataPoint(dayOfMonth, val/5));
                }
                cursor.moveToNext();
            }
        }
        cursor.close();


        ArrayList<DataPoint[]> ret = new ArrayList<DataPoint[]>();
        ret.add(new DataPoint[mood.size()]);
        int i = 0;
        for (DataPoint n : mood) {
            ret.get(0)[i++] = n;
        }
        ret.add(new DataPoint[sleep.size()]);
        i = 0;
        for (DataPoint n : sleep) {
            ret.get(1)[i++] = n;
        }
        ret.add(new DataPoint[exer.size()]);
        i = 0;
        for (DataPoint n : exer) {
            ret.get(2)[i++] = n;
        }
        return ret;
    }

    public ArrayList<DataPoint[]> getDataPointArrayAnx(int year, int month) {
        ArrayList<DataPoint> mood = new ArrayList<DataPoint>();
        ArrayList<DataPoint> sleep = new ArrayList<DataPoint>();
        ArrayList<DataPoint> exer = new ArrayList<DataPoint>();

        Cursor cursor = LocalStorageAccessMood.getMonthEntries(v.getContext(), year, month);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                int val = Integer.parseInt(cursor.getString(5));
                mood.add(new DataPoint(dayOfMonth, val*2));

                cursor.moveToNext();
            }
        }
        cursor.close();

        cursor = LocalStorageAccessSleep.getMonthEntries(v.getContext(), year, month);
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()) {
                int day = Integer.parseInt(cursor.getString(0).substring(8));
                String hr = cursor.getString(2).substring(0, cursor.getString(2).indexOf(":"));
                String min= cursor.getString(2).substring(cursor.getString(2).indexOf(":")+1);

                double val = Integer.parseInt(hr) + (Integer.parseInt(min) / 60.0);
                sleep.add(new DataPoint(day, val));
                cursor.moveToNext();
            }
        }
        cursor.close();

        cursor = LocalStorageAccessExercise.getMonthEntries(v.getContext(), year, month);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                String s = cursor.getString(4);
                if(s != null) {
                    int val = Integer.parseInt(s);
                    exer.add(new DataPoint(dayOfMonth, val/5));
                }
                cursor.moveToNext();
            }
        }
        cursor.close();


        ArrayList<DataPoint[]> ret = new ArrayList<DataPoint[]>();
        ret.add(new DataPoint[mood.size()]);
        int i = 0;
        for (DataPoint n : mood) {
            ret.get(0)[i++] = n;
        }
        ret.add(new DataPoint[sleep.size()]);
        i = 0;
        for (DataPoint n : sleep) {
            ret.get(1)[i++] = n;
        }
        ret.add(new DataPoint[exer.size()]);
        i = 0;
        for (DataPoint n : exer) {
            ret.get(2)[i++] = n;
        }
        return ret;
    }





    @Override
    public ArrayList<DataPoint[]> getDataPointArray(int year, int month) {
        return null;
    }

    @Override
    GraphView getGraph() {
        return (GraphView) v.findViewById(R.id.grapAllDep);
    }

    @Override
    View graphInflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_all_graph, container, false);
    }


    @Override
    protected void setMonthYearTitle() {
        ((TextView)v.findViewById(R.id.yearTextViewAll)).setText(Integer.toString(year));
        TextView mnth = (TextView)v.findViewById(R.id.monthTextViewAll);
        mnth.setText(new DateFormatSymbols().getMonths()[month-1]);
    }

}
