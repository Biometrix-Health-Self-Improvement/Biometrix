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
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;

/**
    @author tannalynn
 */
public class DietGraph extends GraphBase {


    @Override
    public void populateGraph() {
        GraphView graph1 =  (GraphView) v.findViewById(R.id.graphDiet1);
        GraphView graph2 =  (GraphView) v.findViewById(R.id.graphDiet2);

        graph1.removeAllSeries();
        graph2.removeAllSeries();

        ArrayList<DataPoint[]> dp = getDataPointArray(year, month);

        //graph 1
        LineGraphSeries<DataPoint> cal = new LineGraphSeries<>(dp.get(0));
        cal.setTitle("Calories");
        cal.setColor(Color.BLUE);

        setDateBounds(graph1);
        graph1.setTitle("Calories");
        if(!cal.isEmpty())
            graph1.addSeries(cal);


        //Graph 2
        setDateBounds(graph2);
        graph2.setTitle("Nutritional Info");

        LineGraphSeries<DataPoint> fat = new LineGraphSeries<>(dp.get(1));
        fat.setTitle("Fat");
        fat.setColor(Color.MAGENTA);
        LineGraphSeries<DataPoint> car = new LineGraphSeries<>(dp.get(2));
        car.setTitle("Carbs");
        car.setColor(Color.RED);
        LineGraphSeries<DataPoint> fib = new LineGraphSeries<>(dp.get(3));
        fib.setTitle("Fiber");
        fib.setColor(Color.GREEN);

        if(!fat.isEmpty())
            graph2.addSeries(fat);
        if(!car.isEmpty())
            graph2.addSeries(car);
        if(!fib.isEmpty())
            graph2.addSeries(fib);


        setLegend(graph2);
        setMonthYearTitle();

    }

    @Override
    public ArrayList<DataPoint[]> getDataPointArray(int year, int month) {
        Cursor cursor = LocalStorageAccessDiet.getMonthEntries(v.getContext(), year, month);
        ArrayList<DataPoint> cal = new ArrayList<>();
        ArrayList<DataPoint> fat = new ArrayList<>();
        ArrayList<DataPoint> carb = new ArrayList<>();
        ArrayList<DataPoint> fib = new ArrayList<>();
        ArrayList<DataPoint> pro = new ArrayList<>();
        ArrayList<DataPoint> cnt = new ArrayList<>();

/* ORDER
DATE,
"SUM("+CALORIES+")",
"SUM("+TOTALFAT+")",
"SUM("+TOTALCARBS+")",
"SUM("+FIBER+")",
"SUM("+PROTEIN+")",
"count(*)"}
 */
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8));
                int val;
                if(tryParseInt(cursor.getString(1))) {
                    val = Integer.parseInt(cursor.getString(1));
                    cal.add(new DataPoint(dayOfMonth, val));
                }
                if(tryParseInt(cursor.getString(2))) {
                    val = Integer.parseInt(cursor.getString(2));
                    fat.add(new DataPoint(dayOfMonth, val));
                }
                if(tryParseInt(cursor.getString(3))) {
                    val = Integer.parseInt(cursor.getString(3));
                    carb.add(new DataPoint(dayOfMonth, val));
                }
                if(tryParseInt(cursor.getString(4))) {
                    val = Integer.parseInt(cursor.getString(4));
                    fib.add(new DataPoint(dayOfMonth, val));
                }
                if(tryParseInt(cursor.getString(5))) {
                    val = Integer.parseInt(cursor.getString(5));
                    pro.add(new DataPoint(dayOfMonth, val));
                }
                if(tryParseInt(cursor.getString(6))) {
                    val = Integer.parseInt(cursor.getString(6));
                    cnt.add(new DataPoint(dayOfMonth, val));
                }

                cursor.moveToNext();
            }
        }
        cursor.close();

        ArrayList<DataPoint[]> ret = new ArrayList<DataPoint[]>();
        ret.add(new DataPoint[cal.size()]);
        int i = 0;
        for (DataPoint n : cal) {
            ret.get(0)[i++] = n;
        }
        ret.add(new DataPoint[fat.size()]);
        i = 0;
        for (DataPoint n : fat) {
            ret.get(1)[i++] = n;
        }
        ret.add(new DataPoint[carb.size()]);
        i = 0;
        for (DataPoint n : carb) {
            ret.get(2)[i++] = n;
        }
        ret.add(new DataPoint[fib.size()]);
        i = 0;
        for (DataPoint n : fib) {
            ret.get(3)[i++] = n;
        }
        ret.add(new DataPoint[pro.size()]);
        i = 0;
        for (DataPoint n : pro) {
            ret.get(2)[i++] = n;
        }
        ret.add(new DataPoint[cnt.size()]);
        i = 0;
        for (DataPoint n : cnt) {
            ret.get(3)[i++] = n;
        }

        return ret;
    }

    @Override
    GraphView getGraph() {
        return (GraphView) v.findViewById(R.id.graphDiet1);
    }

    @Override
    View graphInflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_diet_graph, container, false);    }

    @Override
    protected void setMonthYearTitle() {
        ((TextView)v.findViewById(R.id.yearTextViewDiet)).setText(Integer.toString(year));
        TextView mnth = (TextView)v.findViewById(R.id.monthTextViewDiet);
        mnth.setText(new DateFormatSymbols().getMonths()[month-1]);
    }
}
