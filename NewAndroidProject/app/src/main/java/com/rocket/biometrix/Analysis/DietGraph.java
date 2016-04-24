package com.rocket.biometrix.Analysis;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.rocket.biometrix.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;

/**

 */
public class DietGraph extends GraphBase {


    @Override
    public void populateGraph() {

    }

    @Override
    public ArrayList<DataPoint[]> getDataPointArray(int year, int month) {
        return null;
    }

    @Override
    GraphView getGraph() {
        return (GraphView) v.findViewById(R.id.graphDiet);
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
