package com.rocket.biometrix.Analysis;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.Login.SettingsAndEntryHelper;
import com.rocket.biometrix.R;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;


public class AllGraph extends GraphBase {

    private View graphView;
    private String leftTable = "";
    private String rightTable = "";
    private String leftColumn = "";
    private String rightColumn = "";
    private ColumnStatistics leftColumnStatistics;
    private ColumnStatistics rightColumnStatistics;

    //The values that the graphs are scaled to
    private final static int MAX = 10;
    private final static int MIN = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                         Bundle savedInstanceState) {
        graphView = super.onCreateView(inflater, container, savedInstanceState);

        List<String> stringList = SettingsAndEntryHelper.getEnabledModuleNames(graphView.getContext());
        String[] moduleNames = stringList.toArray(new String[stringList.size()]);
        ArrayAdapter<String> spinnerArrayAdapterTable = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, moduleNames);
        ((Spinner) graphView.findViewById(R.id.graphTableSpinner1)).setAdapter(spinnerArrayAdapterTable);
        ((Spinner) graphView.findViewById(R.id.graphTableSpinner2)).setAdapter(spinnerArrayAdapterTable);

        updateLeftColumnSpinner();
        updateRightColumnSpinner();

        //Update table spinners when selected
        ((Spinner) graphView.findViewById(R.id.graphTableSpinner1)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateLeftColumnSpinner();
                leftTable = ((Spinner) graphView.findViewById(R.id.graphTableSpinner1)).getSelectedItem().toString();
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                leftTable = "";
            }
        });

        ((Spinner) graphView.findViewById(R.id.graphTableSpinner2)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRightColumnSpinner();
                rightTable = ((Spinner) graphView.findViewById(R.id.graphTableSpinner2)).getSelectedItem().toString();
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                rightTable = "";
            }
        });

        ((Spinner) graphView.findViewById(R.id.graphColumnSpinner1)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                leftColumn = ((Spinner) graphView.findViewById(R.id.graphColumnSpinner1)).getSelectedItem().toString();
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                leftColumn = "";
            }
        });

        ((Spinner) graphView.findViewById(R.id.graphColumnSpinner2)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rightColumn = ((Spinner) graphView.findViewById(R.id.graphColumnSpinner2)).getSelectedItem().toString();
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                rightColumn = "";
            }
        });

        return graphView;
    }

    /**
     * Updates the spinners for the columns depending on which module is in the table
     */
    private void updateLeftColumnSpinner()
    {
        String tableName1 = ((Spinner) graphView.findViewById(R.id.graphTableSpinner1)).getSelectedItem().toString();

        List<String> stringList = SettingsAndEntryHelper.getEnabledAnalysisColumns(graphView.getContext(), tableName1, true);
        String[] columnNames1 = stringList.toArray(new String[stringList.size()]);

        ArrayAdapter<String> spinnerArrayAdapterColumn1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, columnNames1);
        ((Spinner) graphView.findViewById(R.id.graphColumnSpinner1)).setAdapter(spinnerArrayAdapterColumn1);

        if (((Spinner) graphView.findViewById(R.id.graphColumnSpinner1)).getSelectedItem() == null)
        {
            leftColumn = "";
        }
        else {
            leftColumn = ((Spinner) graphView.findViewById(R.id.graphColumnSpinner1)).getSelectedItem().toString();
        }
    }

    /**
     * Updates the spinners for the columns depending on which module is in the table
     */
    private void updateRightColumnSpinner()
    {
        String tableName2 = ((Spinner) graphView.findViewById(R.id.graphTableSpinner2)).getSelectedItem().toString();

        List<String> stringList = SettingsAndEntryHelper.getEnabledAnalysisColumns(graphView.getContext(), tableName2, true);
        String[] columnNames2 = stringList.toArray(new String[stringList.size()]);

        ArrayAdapter<String> spinnerArrayAdapterColumn1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, columnNames2);
        ((Spinner) graphView.findViewById(R.id.graphColumnSpinner2)).setAdapter(spinnerArrayAdapterColumn1);

        if (((Spinner) graphView.findViewById(R.id.graphColumnSpinner2)).getSelectedItem() == null)
        {
            rightColumn = "";
        }
        else {
            rightColumn = ((Spinner) graphView.findViewById(R.id.graphColumnSpinner2)).getSelectedItem().toString();
        }
    }

    /**
     * Updates the text view so that it shows which columns are being compared
     */
    private void updateTextView() {
        StringBuilder operationDisplay = new StringBuilder();
        operationDisplay.append("Graphing ");
        if (((Spinner) graphView.findViewById(R.id.graphColumnSpinner1)).getSelectedItem() != null) {
            operationDisplay.append(((Spinner) graphView.findViewById(R.id.graphColumnSpinner1)).getSelectedItem().toString());
        } else {
            operationDisplay.append("Nothing");
        }

        operationDisplay.append(" and ");
        if (((Spinner) graphView.findViewById(R.id.graphColumnSpinner2)).getSelectedItem() != null) {
            operationDisplay.append(((Spinner) graphView.findViewById(R.id.graphColumnSpinner2)).getSelectedItem().toString());
        } else {
            operationDisplay.append("Nothing");
        }

        ((TextView)graphView.findViewById(R.id.graphOperationTextView)).setText(operationDisplay);
    }

    @Override
    public void populateGraph() {
        try {
            populateGraphData();
        } catch (Exception ex) { }
        setMonthYearTitle();
    }

    private void populateGraphData() {
        GraphView graph = (GraphView) v.findViewById(R.id.graphAll);
        graph.removeAllSeries();
        clearStatisticsTextViews();

        //Don't try to process if one of the columns is not an actual column
        if (!leftColumn.equals("") && !rightColumn.equals("") ) {
            try {
                BiometrixAnalysis analysis = new BiometrixAnalysis();
                leftColumnStatistics = analysis.getColumnStats(v.getContext(), leftColumn, leftTable);
                rightColumnStatistics = analysis.getColumnStats(v.getContext(), rightColumn, rightTable);

                ArrayList<DataPoint[]> dp = getDataPointArrayFromColumns(year, month);

                LineGraphSeries<DataPoint> first = new LineGraphSeries<>(dp.get(0));
                first.setTitle(leftColumn);

                LineGraphSeries<DataPoint> second = new LineGraphSeries<>(dp.get(1));
                second.setTitle(rightColumn);
                second.setColor(Color.MAGENTA);

                graph.setTitle(leftColumn + " and " + rightColumn);

                if (!first.isEmpty()) graph.addSeries(first);
                if (!second.isEmpty()) graph.addSeries(second);

                graph.getViewport().setMinY(MIN);
                graph.getViewport().setMaxY(MAX);

                updateStatisticsTextViews();
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
        setDateBounds(graph);
        setLegend(graph);
    }


    /**
     * Retrieves a data point array based on the columns and tables that are currently selected
     * @param year Integer year that is being queried
     * @param month Integer month that is being queried
     * @return An array list of data points that
     */
    private ArrayList<DataPoint[]> getDataPointArrayFromColumns(int year, int month) {
        ArrayList<DataPoint> first = getScaledDataPointArray(year, month, leftTable, leftColumn, leftColumnStatistics);
        ArrayList<DataPoint> secondList = getScaledDataPointArray(year, month, rightTable, rightColumn, rightColumnStatistics);

        ArrayList<DataPoint[]> ret = new ArrayList<>();

        ret.add(new DataPoint[first.size()]);
        int i = 0;
        for (DataPoint n : first) {
            ret.get(0)[i++] = n;
        }
        ret.add(new DataPoint[secondList.size()]);
        i = 0;
        for (DataPoint n : secondList) {
            ret.get(1)[i++] = n;
        }

        return ret;
    }

    /**
     * Retrieves a scaled data point array where the numbers go from 1 to 10 based on the column's
     * minimums and maximums
     * @param year The integer year to query
     * @param month The integer month to query
     * @param tableName The name of the table to retrieve values from
     * @param columnName The name of the column to retrieve values from
     * @param statistics The statistics calculated for this column used for scaling the values
     * @return The array list of datapoints that have the scaled information
     */
    private ArrayList<DataPoint> getScaledDataPointArray(int year, int month, String tableName, String columnName,
                                                         ColumnStatistics statistics)
    {
        ArrayList<DataPoint> returnList = new ArrayList<DataPoint>();

        Cursor cursor;

        switch (tableName)
        {
            case LocalStorageAccessExercise.TABLE_NAME:
                cursor = LocalStorageAccessExercise.getMonthEntriesForColumn(v.getContext(), year, month, columnName);
                break;
            case LocalStorageAccessMood.TABLE_NAME:
                cursor = LocalStorageAccessMood.getMonthEntriesForColumn(v.getContext(), year, month, columnName);
                break;
            case LocalStorageAccessSleep.TABLE_NAME:
                cursor = LocalStorageAccessSleep.getMonthEntriesForColumn(v.getContext(), year, month, columnName);
                break;
            case LocalStorageAccessDiet.TABLE_NAME:
                cursor = LocalStorageAccessDiet.getMonthEntriesForColumn(v.getContext(), year, month, columnName);
                break;
            default:
                throw new InvalidParameterException("Unrecognized table name " + tableName);
        }

        float min = statistics.getMin();
        float max = statistics.getMax();
        float range = max - min;

        if (columnName.equals(LocalStorageAccessSleep.DURATION) ) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    if (tryParseInt(cursor.getString(0).substring(8))) {
                        int day = Integer.parseInt(cursor.getString(0).substring(8));
                        String hr = cursor.getString(1).substring(0, cursor.getString(1).indexOf(":"));
                        String minute = cursor.getString(1).substring(cursor.getString(1).indexOf(":") + 1);
                        if (tryParseInt(hr) && tryParseInt(minute)) {
                            float val;

                            //If the range is not 0, scale the value down to the MIN to MAX interval
                            //based on minimum and maximum values reported for the column
                            //If the range is 0, that means that all the values are the same. If
                            //all the values are the same, then just set the value to the middle of
                            //the interval
                            if (range != 0) {
                                val = (float)(Integer.parseInt(hr) * 60 + Integer.parseInt(minute));

                                //Moves the values down to the minimum
                                val = val - (min - MIN);

                                //Scales the values to fill up 1 through 10
                                val = val * MAX / range;
                            }
                            else
                            {
                                val = (MIN + MAX) / 2;
                            }

                            if (val > MAX)
                            {
                                val = MAX;
                            }

                            returnList.add(new DataPoint(day, val));
                        }
                    }
                    cursor.moveToNext();
                }
            }
        }
        else
        {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    if (tryParseInt(cursor.getString(0).substring(8))) {
                        int day = Integer.parseInt(cursor.getString(0).substring(8));

                        if (tryParseInt(cursor.getString(1))) {
                            float val;

                            //If the range is not 0, scale the value down to the MIN to MAX interval
                            //based on minimum and maximum values reported for the column
                            //If the range is 0, that means that all the values are the same. If
                            //all the values are the same, then just set the value to the middle of
                            //the interval
                            if (range != 0) {
                                val = (float)Integer.parseInt(cursor.getString(1));

                                //Moves the values down to the minimum
                                val = val - (min - MIN);

                                //Scales the values to fill up 1 through 10
                                val = val * MAX / range;
                            }
                            else
                            {
                                val = (MIN + MAX) / 2;
                            }

                            if (val > MAX)
                            {
                                val = MAX;
                            }

                            returnList.add(new DataPoint(day, val));
                        }
                    }
                    cursor.moveToNext();
                }
            }
        }
        cursor.close();

        return returnList;
    }


    @Override
    public ArrayList<DataPoint[]> getDataPointArray(int year, int month) {
        return null;
    }

    @Override
    GraphView getGraph() {
        return (GraphView) v.findViewById(R.id.graphAll);
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

    /**
     * Updates the text views that display statistics
     */
    private void updateStatisticsTextViews()
    {
        ((TextView) v.findViewById(R.id.graphColumnNameTextView1)).setText(leftColumn);

        if (!leftColumn.equals(LocalStorageAccessSleep.DURATION)) {
            ((TextView) v.findViewById(R.id.graphMinTextView1)).setText("Minimum value: " + Float.toString(leftColumnStatistics.getMin()));
            ((TextView) v.findViewById(R.id.graphMaxTextView1)).setText("Maximum value: " + Float.toString(leftColumnStatistics.getMax()));
            ((TextView) v.findViewById(R.id.graphMeanTextView1)).setText("Mean value: " + Float.toString(leftColumnStatistics.getMean()));
            ((TextView) v.findViewById(R.id.graphMedianTextView1)).setText("Median value: " + Float.toString(leftColumnStatistics.getMedian()));
        }
        else
        {
            ((TextView) v.findViewById(R.id.graphMinTextView1)).setText("Minimum value: " + getTimeFormattedStatistics(leftColumnStatistics.getMin()));
            ((TextView) v.findViewById(R.id.graphMaxTextView1)).setText("Maximum value: " + getTimeFormattedStatistics(leftColumnStatistics.getMax()));
            ((TextView) v.findViewById(R.id.graphMeanTextView1)).setText("Mean value: " + getTimeFormattedStatistics(leftColumnStatistics.getMean()));
            ((TextView) v.findViewById(R.id.graphMedianTextView1)).setText("Median value: " + getTimeFormattedStatistics(leftColumnStatistics.getMedian()));
        }

        ((TextView) v.findViewById(R.id.graphColumnNameTextView2)).setText(rightColumn);

        if (!rightColumn.equals(LocalStorageAccessSleep.DURATION)) {
            ((TextView) v.findViewById(R.id.graphMinTextView2)).setText("Minimum value: " + Float.toString(rightColumnStatistics.getMin()));
            ((TextView) v.findViewById(R.id.graphMaxTextView2)).setText("Maximum value: " + Float.toString(rightColumnStatistics.getMax()));
            ((TextView) v.findViewById(R.id.graphMeanTextView2)).setText("Mean value: " + Float.toString(rightColumnStatistics.getMean()));
            ((TextView) v.findViewById(R.id.graphMedianTextView2)).setText("Median value: " + Float.toString(rightColumnStatistics.getMedian()));
        }
        else
        {
            ((TextView) v.findViewById(R.id.graphMinTextView2)).setText("Minimum value: " + getTimeFormattedStatistics(rightColumnStatistics.getMin()));
            ((TextView) v.findViewById(R.id.graphMaxTextView2)).setText("Maximum value: " + getTimeFormattedStatistics(rightColumnStatistics.getMax()));
            ((TextView) v.findViewById(R.id.graphMeanTextView2)).setText("Mean value: " + getTimeFormattedStatistics(rightColumnStatistics.getMean()));
            ((TextView) v.findViewById(R.id.graphMedianTextView2)).setText("Median value: " + getTimeFormattedStatistics(rightColumnStatistics.getMedian()));
        }
    }

    /**
     * Formats a duration value as a string and returns it
     * @param value The integer value corresponding to the number of minutes
     * @return A formatted string that has the value in terms of hours and minutes
     */
    private String getTimeFormattedStatistics(float value)
    {
        double minutes = value;
        double hours = Math.floor(minutes/60);
        minutes = minutes % 60;

        return Double.toString(hours) + " hours and " + Double.toString(minutes) + " minutes";
    }

    /**
     * Clears the statistics text views of their values
     */
    private void clearStatisticsTextViews()
    {
        ((TextView)v.findViewById(R.id.graphColumnNameTextView1)).setText("");
        ((TextView)v.findViewById(R.id.graphColumnNameTextView2)).setText("");

        ((TextView)v.findViewById(R.id.graphMinTextView1)).setText("");
        ((TextView)v.findViewById(R.id.graphMinTextView2)).setText("");

        ((TextView)v.findViewById(R.id.graphMaxTextView1)).setText("");
        ((TextView)v.findViewById(R.id.graphMaxTextView2)).setText("");

        ((TextView)v.findViewById(R.id.graphMeanTextView1)).setText("");
        ((TextView)v.findViewById(R.id.graphMeanTextView2)).setText("");

        ((TextView)v.findViewById(R.id.graphMedianTextView1)).setText("");
        ((TextView)v.findViewById(R.id.graphMedianTextView2)).setText("");
    }
}
