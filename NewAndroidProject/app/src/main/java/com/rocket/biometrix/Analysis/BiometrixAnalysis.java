package com.rocket.biometrix.Analysis;

import android.content.Context;
import android.database.Cursor;

import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.Login.LocalAccount;
import com.rocket.biometrix.Login.SettingsHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import android.util.Pair;

/**
 * Created by TJ on 4/6/2016.
 * A class with methods to perform analysis on all of the modules
 */
public class BiometrixAnalysis
{
    /**
     * Empty constructor
     */
    public BiometrixAnalysis()
    { }

    /**
     * Retrieves the basic column statistics for the passed in column on the passed in table
     * @param context The current context, used for database operations
     * @param columnName The name of the column that is being queried
     * @param tableName The name of the table that the operations should be performed on
     * @return A ColumnStatistics object containing the requested information.
     */
    public ColumnStatistics getColumnStats(Context context, String columnName, String tableName)
    {
        ColumnList colList;
        boolean average = false;
        String dateColumnName = "";

        boolean sleepDuration = false;

        switch (tableName)
        {
            case LocalStorageAccessDiet.TABLE_NAME:
                dateColumnName = LocalStorageAccessDiet.DATE;
                break;
            case LocalStorageAccessExercise.TABLE_NAME:
                dateColumnName = LocalStorageAccessExercise.DATE;
                break;
            case LocalStorageAccessMood.TABLE_NAME:
                dateColumnName = LocalStorageAccessMood.DATE;
                average = true;
                break;
            case LocalStorageAccessSleep.TABLE_NAME:
                dateColumnName = LocalStorageAccessSleep.DATE;
                if (columnName.equals(LocalStorageAccessSleep.QUALITY))
                {
                    average = true;
                }
                else if (columnName.equals(LocalStorageAccessSleep.DURATION))
                {
                    sleepDuration = true;
                }
                break;
        }

        if (sleepDuration) {
            colList = new ColumnList(getSleepDuration(context), columnName, tableName, true);
        }
        else
        {
            colList = new ColumnList(getColumnDatesAndValues(tableName, dateColumnName, columnName, context, average),
                    columnName, tableName, true);
        }


        ColumnStatistics columnStatistics = new ColumnStatistics(colList.totalCount, colList.median, colList.mean,
                colList.min, colList.tableName, colList.columnName, colList.max);

        return columnStatistics;
    }

    /**
     * Retrieves the basic column statistics for the passed in column on the passed in table
     * @param context The current context, used for database operations
     * @param firstColumnName The name of the column that is being queried
     * @param firstTableName The name of the table that the operations should be performed on
     * @param secondColumnName The name of the column that is being queried
     * @param secondTableName The name of the table that the operations should be performed on
     * @param daysApart The number of days before that the first element is from the second element,
     *                  e.g. 2 days before, or -2 days before for 2 days after
     * @return A ColumnStatistics object containing the requested information.
     */
    public ColumnCorrelation getColumnCorrelations(Context context, String firstColumnName, String firstTableName,
                                                  String secondColumnName, String secondTableName, int daysApart)
    {
        ColumnList colList = null;
        ColumnList colList2 = null;
        boolean average = false;
        boolean isDuration = false;
        String dateColumnName = "";

        switch (firstTableName)
        {
            case LocalStorageAccessDiet.TABLE_NAME:
                dateColumnName = LocalStorageAccessDiet.DATE;
                break;
            case LocalStorageAccessExercise.TABLE_NAME:
                dateColumnName = LocalStorageAccessExercise.DATE;
                break;
            case LocalStorageAccessMood.TABLE_NAME:
                dateColumnName = LocalStorageAccessMood.DATE;
                average = true;
                break;
            case LocalStorageAccessSleep.TABLE_NAME:
                dateColumnName = LocalStorageAccessSleep.DATE;

                if (firstColumnName.equals(LocalStorageAccessSleep.DURATION) )
                {
                    colList = new ColumnList(getSleepDuration(context), firstColumnName, firstTableName, false);
                    isDuration = true;
                }
                break;
        }

        if (!isDuration) {
            colList = new ColumnList(getColumnDatesAndValues(firstTableName, dateColumnName, firstColumnName, context, average),
                    firstColumnName, firstTableName, false);
        }
        isDuration = false;

        switch (secondTableName)
        {
            case LocalStorageAccessDiet.TABLE_NAME:
                dateColumnName = LocalStorageAccessDiet.DATE;
                break;
            case LocalStorageAccessExercise.TABLE_NAME:
                dateColumnName = LocalStorageAccessExercise.DATE;
                break;
            case LocalStorageAccessMood.TABLE_NAME:
                dateColumnName = LocalStorageAccessMood.DATE;
                average = true;
                break;
            case LocalStorageAccessSleep.TABLE_NAME:
                dateColumnName = LocalStorageAccessSleep.DATE;

                if (secondColumnName.equals(LocalStorageAccessSleep.DURATION) )
                {
                    colList2 = new ColumnList(getSleepDuration(context), secondColumnName, secondTableName, false);
                    isDuration = true;
                }
                break;
        }

        if (!isDuration) {
            colList2 = new ColumnList(getColumnDatesAndValues(secondTableName, dateColumnName, secondColumnName, context, average),
                    secondColumnName, secondTableName, false);
        }


        return getCorrelation(colList, colList2, daysApart);
    }

    /**
     * Returns the correlation information for the passed in lists
     * @param columnList1 The first column list that contains value, date, and statistical information
     * @param columnList2 The second column list that contains value, date, and statistical information
     * @param daysApart The number of days the first list is before the second
     * @return A column correlation that has the statistics for both lists as well as the correlation
     * value
     */
    private ColumnCorrelation getCorrelation(ColumnList columnList1, ColumnList columnList2, int daysApart)
    {
        double correlation = 0;

        float productTotal = 0;
        float sumFirst = 0;
        float sumFirstSquared = 0;
        float sumSecond = 0;
        float sumSecondSquared = 0;

        Iterator<Pair<Float, Integer>> firstListIter =  columnList1.list.iterator();
        Iterator<Pair<Float, Integer>> secondListIter =  columnList2.list.iterator();

        Pair<Float, Integer> list1Element = null;
        Pair<Float, Integer> list2Element = null;
        int adjustedDate = -1;

        boolean isDone = false;
        int numEntries = 0;

        //Priming read to grab the first elements in the list
        if (firstListIter.hasNext() && secondListIter.hasNext() )
        {
            list1Element = firstListIter.next();
            list2Element = secondListIter.next();

            adjustedDate = list1Element.second + daysApart;
        }
        else
        {
            isDone = true;
        }


        while (!isDone )
        {
            //If the days are equal according to the adjusted date value, then that means they
            //need to be compared and both lists should move forward
            if (adjustedDate == list2Element.second)
            {
                productTotal += list1Element.first * list2Element.first;
                sumFirst += list1Element.first;
                sumFirstSquared += list1Element.first * list1Element.first;
                sumSecond += list2Element.first;
                sumSecondSquared += list2Element.first * list2Element.first;

                ++numEntries;

                if (firstListIter.hasNext() && secondListIter.hasNext() ) {
                    list1Element = firstListIter.next();
                    list2Element = secondListIter.next();
                    adjustedDate = list1Element.second + daysApart;
                }
                else
                {
                    isDone = true;
                }
            }
            //Since the lists are in descending order, if the adjusted date from the first list is
            //greater than the second list's date, move the first list forward
            else if (adjustedDate > list2Element.second)
            {
                if (firstListIter.hasNext() ) {
                    list1Element = firstListIter.next();
                    adjustedDate = list1Element.second + daysApart;
                }
                else
                {
                    isDone = true;
                }
            }
            //Otherwise move the second list forward
            else
            {
                if (secondListIter.hasNext() ) {
                    list2Element = secondListIter.next();
                }
                else
                {
                    isDone = true;
                }
            }

        }

        //Avoids division by zero error
        if (numEntries == 0 || sumFirst == 0 || sumSecond == 0)
        {
            correlation = 0;
        }
        else
        {
            double numerator = numEntries * productTotal - sumFirst * sumSecond;
            double denominator = numEntries * sumFirstSquared - sumFirst * sumFirst;
            denominator = Math.sqrt(denominator);

            double rightDenominator = numEntries * sumSecondSquared - sumSecond * sumSecond;
            rightDenominator = Math.sqrt(rightDenominator);

            denominator = denominator * rightDenominator;

            if (denominator == 0)
            {
                correlation = 0;
            }
            else
            {
                correlation = numerator / denominator;
            }
        }

        return new ColumnCorrelation(daysApart, correlation, numEntries);
    }

    /**
     * Converts a time string (as we are storing them in the local DB) into an integer that contains
     * the number of minutes represented by that time if it is valid
     * @param timeString A String formatted as a time. (e.g. 10:01 for 10 hours and 1 minute)
     * @return The number of minutes in the passed in time string. Returns -1 if the format is
     * not as expected
     */
    private Integer ConvertTimeToMinutes(String timeString)
    {
        Integer retVal = -1;

        //Impossible to have this format with less than 3 characters (e.g. 1:2) or more than 5 (e.g
        //10:15)
        if (timeString.length() > 2 && timeString.length() < 6)
        {
            String hourString = timeString.substring(0, timeString.indexOf(":"));
            String minuteString = timeString.substring(timeString.indexOf(":") + 1);

            Integer hourPart;
            Integer minPart;

            try
            {
                hourPart = Integer.parseInt(hourString);
                minPart = Integer.parseInt(minuteString);
                retVal = hourPart * 60 + minPart;
            }
            catch (Exception except)
            {
                except.getMessage();
            }
        }

        return retVal;
    }

    /**
     * Grabs a list of pairs of each value in the passed in column of the passed in table (if it is
     * not null). This reutrns a list of integer pairs where integers on the ame day are averaged
     * @param tableName Name of the table to pull from
     * @param dateName Name of the date field on the table
     * @param columnName Name of the column to pull from on the table
     * @param context The context to use for database operations
     * @param average A boolean value. If true, the day's values are averaged. If false, they are summed
     * @return A list containing pairs of integers. The integers are the value, and then the
     */
    private List<Pair<Float, Integer>> getColumnDatesAndValues(String tableName, String dateName,
                                                                 String columnName, Context context, boolean average)
    {
        List<Pair<Float, Integer>> returnList = new LinkedList<>();

        Cursor cursor = LocalStorageAccess.selectAllEntries(context, tableName, dateName + " DESC",
                    new String[]{columnName, dateName}, true);

        int prevDate, curDate, dayTotal, numEntriesInDay;

        //Set to an invalid date for first compare
        curDate = -1;

        boolean firstDay = true;
        numEntriesInDay = 0;
        dayTotal = 0;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (cursor.moveToFirst() )
        {
            while (!cursor.isAfterLast() )
            {
                String dateString = cursor.getString(1);

                try {
                    Date enteredDate = dateFormat.parse(dateString);

                    prevDate = curDate;
                    curDate = getDateIntValue(enteredDate);

                    //If still on the same day, add the entries together
                    if(curDate == prevDate)
                    {
                        ++numEntriesInDay;
                        dayTotal += cursor.getInt(0);
                    }
                    else
                    //If not on the same day, add the last day's average as an entry for that day
                    {
                        if (!firstDay )
                        {
                            float entry;

                            if (average)
                            {
                                entry = (float) dayTotal / (float) numEntriesInDay;
                            }
                            else
                            {
                                entry = (float) dayTotal;
                            }

                            returnList.add(new Pair<>(entry, prevDate));
                        }
                        else
                        {
                            firstDay = false;
                        }

                        numEntriesInDay = 1;
                        dayTotal = cursor.getInt(0);
                    }

                    cursor.moveToNext();

                    //Duplicate of above code except with current date
                    if (cursor.isAfterLast() )
                    {
                        float entry;

                        if (average)
                        {
                            entry = (float) dayTotal / (float) numEntriesInDay;
                        }
                        else
                        {
                            entry = (float) dayTotal;
                        }

                        returnList.add(new Pair<>(entry, curDate));
                    }
                } catch (Exception except) {
                    Log.i("AnalysisParse", "Could not change string " + dateString);
                }
            }
        }

        cursor.close();
        return returnList;
    }

    /**
     * Returns a list of pairs of floats and integers that correspond to the total sleep duration for
     * the day and an intger representation of the day's date
     * @param context The context to use to grab database information
     * @return A list containing the needed information
     */
    private List<Pair<Float, Integer>> getSleepDuration(Context context)
    {
        List<Pair<Float, Integer>> durationPairList = new LinkedList<>();

        //Grabs a cursor with the duration, quality, date and time in that order
        //Time is needed because
        Cursor cursor = null;
        try {
            cursor = LocalStorageAccess.selectAllEntries(context, LocalStorageAccessSleep.getTableName(),
                    LocalStorageAccessSleep.DATE + " DESC",
                    new String[]{LocalStorageAccessSleep.DURATION, LocalStorageAccessSleep.DATE, LocalStorageAccessSleep.TIME}, true);
        }
        catch (Exception e)
        {
            e.getMessage();
        }

        int prevDate, curDate, dayTotalDuration;

        //Set to an invalid date for first compare
        curDate = -1;

        boolean firstDay = true;
        dayTotalDuration = 0;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


        if (cursor.moveToFirst() )
        {
            while (!cursor.isAfterLast() )
            {
                String dateString = cursor.getString(1);

                try {
                    Date enteredDate = dateFormat.parse(dateString);

                    String timeString = cursor.getString(2);
                    String durationString = cursor.getString(0);

                    int timeMinutes = ConvertTimeToMinutes(timeString);
                    int durationMinutes = ConvertTimeToMinutes(durationString);

                    prevDate = curDate;
                    curDate = getDateIntValue(enteredDate);

                    //If the time of an entry starts before the user's cutoff hour (default is 8am)
                    //the entry belongs with the previous day
                    if (timeMinutes * 60 <
                            LocalAccount.GetInstance().getInt(context, SettingsHelper.SLEEP_INT_CUTOFF_HOUR, 8) )
                    {
                        curDate = curDate -1;
                    }

                    //If still on the same day, add the entries together
                    if(curDate == prevDate)
                    {
                        dayTotalDuration += durationMinutes;
                    }
                    else
                    //If not on the same day, add the last day's average as an entry for that day
                    //Except on the first day
                    {
                        if (!firstDay )
                        {
                            float entryDuration;
                            entryDuration = (float) dayTotalDuration;
                            dayTotalDuration = 0;

                            durationPairList.add(new Pair<>(entryDuration, prevDate));
                        }
                        else
                        {
                            firstDay = false;
                        }

                        dayTotalDuration += durationMinutes;
                    }

                    cursor.moveToNext();

                    if (cursor.isAfterLast() )
                    {
                        float entryDuration;
                        entryDuration = (float) dayTotalDuration;

                        durationPairList.add(new Pair<>(entryDuration, curDate));
                    }
                } catch (Exception except) {
                    Log.i("AnalysisParse", "Could not change string " + dateString);
                }
            }
        }

        cursor.close();


        return durationPairList;
    }

    /**
     * Retrieves an integer value unique to each day
     * @param date The date to grab from
     * @return An integer that corresponds to the number of days since Jan 1. 1970
     */
    private int getDateIntValue(Date date)
    {
        int day_num = -1;
        day_num = (int) (date.getTime() /86400000L);

        return day_num;
    }

    /**
     * A class to hold the data for one column list. This includes information like the statistical
     * data for the column as well as the table and column name.
     */
    class ColumnList
    {
        private int totalCount;
        private float median;
        private float mean;
        private float min;
        private float max;

        private String columnName;
        private String tableName;
        private List<Pair<Float, Integer>> list;

        /**
         * A secondary option if just the list is needed.
         * @param newList List of float integer pairs to add
         * @param column Name of the column
         * @param table Name of the table
         * @param calculateStats If false, statistical information will not be calculated
         */
        private ColumnList(List<Pair<Float, Integer>> newList, String column, String table, boolean calculateStats)
        {
            list = newList;
            columnName = column;
            tableName = table;

            if (calculateStats) {
                totalCount = list.size();

                statAnalysis();
            }
            else
            {
                totalCount = 0;
                mean = 0;
                median = 0;
                min = 0;
                max = 0;
            }
        }

        /**
         * Performs basic statistical analysis on the current list
         */
        private void statAnalysis()
        {
            if (totalCount > 0)
            {
                List<Float> floatList = new ArrayList<>(totalCount);

                for (Pair<Float, Integer> pair : list) {
                    floatList.add(pair.first);
                }

                Collections.sort(floatList);

                float totalValue = 0;
                min = floatList.get(0);
                max = floatList.get(floatList.size() - 1);

                //If there are an even number of elements, then the median is an average of
                //the two middle elements
                if (totalCount % 2 == 0) {
                    median = (floatList.get((int) ((float) totalCount / 2) - 1));
                    median += floatList.get((int) (((float) totalCount / 2)));
                    median /= 2;
                } //The median is the very middle element
                else {
                    median = floatList.get((int) Math.floor((float) totalCount / 2));
                }

                for (int j = 0; j < totalCount; ++j) {
                    totalValue += floatList.get(j);
                }

                mean = (float) totalValue / (float) totalCount;
            }
        }
    }
}


