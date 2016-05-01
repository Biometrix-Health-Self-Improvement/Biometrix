package com.rocket.biometrix.Analysis;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;

import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMedication;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.ExerciseModule.ExerciseEntry;
import com.rocket.biometrix.Login.LocalAccount;
import com.rocket.biometrix.Login.SettingsHelper;

import org.json.JSONException;
import org.json.JSONObject;

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
 * A class with static methods to perform analysis on all of the modules
 */
public class BiometrixAnalysis
{
    //A list of all untracked integer type columns. This mainly consists of primary keys as the user
    //should not care about those
    private static final String[] UNTRACKED_INT_COLS = {LocalStorageAccessDiet.WEB_DIET_ID, LocalStorageAccessDiet.LOCAL_DIET_ID,
            LocalStorageAccessMood.WEB_MOOD_ID, LocalStorageAccessMood.LOCAL_MOOD_ID,
            LocalStorageAccessExercise.WEB_EXERCISE_ID, LocalStorageAccessExercise.LOCAL_EXERCISE_ID,
            LocalStorageAccessSleep.WEB_SLEEP_ID, LocalStorageAccessSleep.LOCAL_SLEEP_ID,
            LocalStorageAccessMedication.WEB_MEDICATION_ID, LocalStorageAccessMedication.LOCAL_MEDICATION_ID};

    //A list of strings that are added onto the end of the column names to be able to retrieve
    //the values from the contentvalues
    public static final String MIN = "Min";
    public static final String MAX = "Max";
    public static final String MEAN = "Mean";
    public static final String MEDIAN = "Median";
    public static final String COUNT = "Count";

    /**
     * Empty constructor
     */
    public BiometrixAnalysis()
    { }

    /**
     * Performs the same basic analysis as above, but on one specific column that is declared as a
     * time variable in the SQLite database
     * @param cursor A cursor containing all the rows that are to be analyzed
     * @return A JSONObject that contains the analyzed row name as the key and then has the
     * statistical information encapsulated. Returns an empty JSONObject upon error.
     */
    protected JSONObject AnalyzeTimeField(Cursor cursor, String columnName)
    {
        JSONObject jsonObject = new JSONObject();

        try
        {
            //Check if there are any rows in the set and move to the first one
            if (cursor.moveToFirst())
            {

                LinkedList<Integer> list = new LinkedList<Integer>();
                int columnIndex = cursor.getColumnIndex(columnName);

                String timeString = cursor.getString(columnIndex);
                Integer timeInMinutes = ConvertTimeToMinutes(timeString);

                if ( timeInMinutes != -1)
                {
                    list.add(timeInMinutes);
                }

                while (cursor.moveToNext())
                {
                    if (cursor.getType(columnIndex) != Cursor.FIELD_TYPE_NULL)
                    {
                        timeString = cursor.getString(cursor.getColumnIndex(columnName));

                        timeInMinutes = ConvertTimeToMinutes(timeString);

                        if ( timeInMinutes != -1)
                        {
                            list.add(timeInMinutes);
                        }
                    }
                }

                Collections.sort(list);

                int totalValue = 0;
                float mean = 0;
                int min = list.getFirst();
                int max = list.getLast();
                float median = 0;
                int totalNum = list.size();

                //If there are an even number of elements, then the median is an average of
                //the two middle elements
                if (totalNum % 2 == 0) {
                    median = (list.get((int) ((float)totalNum / 2) - 1)  );
                    median += list.get((int) (((float)totalNum / 2)));
                    median /= 2;
                } //The median is the very middle element
                else {
                    median = list.get((int) Math.floor((float) totalNum / 2));
                }

                for (int j = 0; j < totalNum; ++j) {
                    totalValue += list.get(j);
                }

                mean = (float)totalValue / (float)totalNum;


                JSONObject column = new JSONObject();

                column.put(MIN, min);
                column.put(MAX, max);
                column.put(MEAN, mean);
                column.put(MEDIAN, median);
                column.put(COUNT, totalNum);

                jsonObject.put(columnName, column);

            }
        }
        catch (JSONException except)
        {
            except.getMessage();
        }
        catch (Exception except)
        {
            except.getMessage();
        }

        cursor.close();

        return jsonObject;
    }

    /**
     * Converts a time string (as we are storing them in the local DB) into an integer that contains
     * the number of minutes represented by that time if it is valid
     * @param timeString A String formatted as a time. (e.g. 10:01 for 10 hours and 1 minute)
     * @return The number of minutes in the passed in time string. Returns -1 if the format is
     * not as expected
     */
    protected Integer ConvertTimeToMinutes(String timeString)
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
     * The only public method in this class, makes the calls to perform the needed analysis
     * @param context The current context, needed for database calls
     * @return A JSONObject containing all of the information that was gathered.
     */
    public JSONObject Analyze( Context context)
    {
        JSONObject jsonObject = new JSONObject();

        List<TableList> tableLists = new LinkedList<>();

        if (LocalAccount.GetInstance().getBoolean(context, SettingsHelper.DIET_MODULE, true))
        {
            TableList tableList = new TableList(LocalStorageAccessDiet.TABLE_NAME);

            for ( String string : SettingsHelper.getEnabledColumns(context, SettingsHelper.getAnalysisDietKeysAndColumns(), true) )
            {
                ColumnList colList = new ColumnList(getColumnDatesAndValues(
                        tableList.tableName, LocalStorageAccessDiet.DATE, string, context, true),
                        string);

                tableList.addColumnList(colList);
            }

            tableLists.add(tableList);
        }

        if (LocalAccount.GetInstance().getBoolean(context, SettingsHelper.EXERCISE_MODULE, true))
        {
            TableList tableList = new TableList(LocalStorageAccessExercise.TABLE_NAME);

            for ( String string : SettingsHelper.getEnabledColumns(context, SettingsHelper.getAnalysisExerciseKeysAndColumns(), true) )
            {
                ColumnList colList = new ColumnList(getColumnDatesAndValues(
                        tableList.tableName, LocalStorageAccessExercise.DATE, string, context, true),
                        string);

                tableList.addColumnList(colList);
            }

            tableLists.add(tableList);
        }

        if (LocalAccount.GetInstance().getBoolean(context, SettingsHelper.MOOD_MODULE, true))
        {
            TableList tableList = new TableList(LocalStorageAccessMood.TABLE_NAME);

            for ( String string : SettingsHelper.getEnabledColumns(context, SettingsHelper.getAnalysisMoodKeysAndColumns(), true) )
            {
                ColumnList colList = new ColumnList(getColumnDatesAndValues(
                        tableList.tableName, LocalStorageAccessMood.DATE, string, context, true),
                        string);

                tableList.addColumnList(colList);
            }

            tableLists.add(tableList);
        }

        if (LocalAccount.GetInstance().getBoolean(context, SettingsHelper.SLEEP_MODULE, true))
        {
            TableList tableList = new TableList(LocalStorageAccessSleep.TABLE_NAME);

            if (SettingsHelper.isSleepDurationEnabled(context) )
            {

                if (SettingsHelper.isSleepQualityEnabled(context) )
                {

                }
                else
                {

                }
            }
            else if (SettingsHelper.isSleepQualityEnabled(context) )
            {

            }
        }

        return jsonObject;
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

        Cursor cursor = null;
        try {
            cursor = LocalStorageAccess.selectAllEntries(context, tableName, dateName + " DESC",
                    new String[]{columnName, dateName}, true);
        }
        catch (Exception e)
        {
            e.getMessage();
        }

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

                        returnList.add(new Pair<>(entry, prevDate));
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
     * A class to hold all of the data for a single table. This is primarily the table's name and then
     * a list of columnLists
     */
    class TableList
    {
        private String tableName;
        private List<ColumnList> columnLists;

        private TableList(String table)
        {
            tableName = table;
            columnLists = new LinkedList<>();
        }

        private void addColumnList(ColumnList newList)
        {
            columnLists.add(newList);
        }
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
        private List<Pair<Float, Integer>> list;

        private ColumnList(List<Pair<Float, Integer>> newList, String column)
        {
            list = newList;
            columnName = column;
            totalCount = list.size();

            statAnalysis();
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


