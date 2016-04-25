package com.rocket.biometrix.Analysis;

import android.content.Context;
import android.database.Cursor;

import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMedication;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

    private static JSONObject statJsonObject = null;

    /**
     * Private constructor since this class should never be directly instantiated
     */
    private BiometrixAnalysis()
    { }

    /**
     * Performs the basic analysis on all tracked modules and stores a static jsonobject that has
     * the analysis performed on it
     * @param context A context to give to the local storage methods in order to pull data
     */
    private static void AnalyzeAllModulesBasic(Context context)
    {
        JSONObject jsonObject = new JSONObject();

        JSONObject moodJson = AnalyzeIntFieldsBasic(LocalStorageAccessMood.selectAll(context, true));
        JSONObject dietJson = AnalyzeIntFieldsBasic(LocalStorageAccessDiet.selectAll(context, true));
        JSONObject exerciseJson = AnalyzeIntFieldsBasic(LocalStorageAccessExercise.selectAll(context, true));
        JSONObject sleepJson = AnalyzeIntFieldsBasic(LocalStorageAccessSleep.selectAll(context, true));
        JSONObject sleepJsonDuration = AnalyzeTimeField(LocalStorageAccessSleep.selectAll(context, true), LocalStorageAccessSleep.DURATION);

        try
        {
            if (sleepJsonDuration.has(LocalStorageAccessSleep.DURATION) )
            {
                sleepJson.put(LocalStorageAccessSleep.DURATION, sleepJsonDuration.getJSONObject(LocalStorageAccessSleep.DURATION));
            }

            jsonObject.put(LocalStorageAccessMood.TABLE_NAME, moodJson);
            jsonObject.put(LocalStorageAccessDiet.TABLE_NAME, dietJson);
            jsonObject.put(LocalStorageAccessExercise.TABLE_NAME, exerciseJson);
            jsonObject.put(LocalStorageAccessSleep.TABLE_NAME, sleepJson);
        }
        catch (JSONException except)
        {
            except.getMessage();
        }

        statJsonObject = jsonObject;
    }

    /**
     * Performs basic analysis on the integer fields of the passed in cursor. Basic analysis
     * consists of operations such as the mean, median, min, max, and standard deviation
     * @param cursor A cursor containing all the rows that are to be analyzed
     * @return A JSONObject that contains all of the analyzed rows along with the calculated
     * values.
     */
    protected static JSONObject AnalyzeIntFieldsBasic(Cursor cursor)
    {
        JSONObject jsonObject = new JSONObject();

        try {
            //Loop through each column
            for (int i = 0; i < cursor.getColumnCount(); ++i)
            {
                //Check if there are any rows in the set and move to the first one
                if (cursor.moveToFirst())
                {
                    boolean isTracked = true;

                    //Checks the column against the list of untracked columns to ensure it is not in there
                    for (String string : UNTRACKED_INT_COLS)
                    {

                        if (string.equals(cursor.getColumnName(i)))
                        {
                            isTracked = false;
                            break;
                        }
                    }

                    //Check that the column actually has the value type expected (i.e. not all nulls
                    //and not containing strings or dates etc.
                    boolean hasValidInt = false;

                    //Don't need to check if there is a non-null value if the field is not tracked
                    if (isTracked)
                    {
                        if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER)
                        {
                            hasValidInt = true;
                        }
                        else if(cursor.getType(i) == Cursor.FIELD_TYPE_NULL)
                        {
                            boolean invalidTypeFound = false;

                            while (cursor.moveToNext() && !hasValidInt && !invalidTypeFound)
                            {
                                if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER)
                                {
                                    hasValidInt = true;
                                }
                                else if (cursor.getType(i) != Cursor.FIELD_TYPE_NULL)
                                {
                                    invalidTypeFound = true;
                                }
                            }
                        }
                    }

                    //Only care about columns that store integers for this method
                    //Only process the value if it is a tracked column
                    if (hasValidInt && isTracked)
                    {
                        LinkedList<Integer> list = new LinkedList<Integer>();

                        list.add(cursor.getInt(i));

                        while (cursor.moveToNext())
                        {
                            //Since nulls have to be allowed, check that the current thing is
                            //actually an integer
                            if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER)
                            {
                                list.add(cursor.getInt(i));
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

                        String columnName = cursor.getColumnName(i);

                        JSONObject column = new JSONObject();

                        column.put(MIN, min);
                        column.put(MAX, max);
                        column.put(MEAN, mean);
                        column.put(MEDIAN, median);
                        column.put(COUNT, totalNum);

                        jsonObject.put(columnName, column);
                    }
                }
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
     * Performs the same basic analysis as above, but on one specific column that is declared as a
     * time variable in the SQLite database
     * @param cursor A cursor containing all the rows that are to be analyzed
     * @return A JSONObject that contains the analyzed row name as the key and then has the
     * statistical information encapsulated. Returns an empty JSONObject upon error.
     */
    protected static JSONObject AnalyzeTimeField(Cursor cursor, String columnName)
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
    protected static Integer ConvertTimeToMinutes(String timeString)
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
    public static JSONObject Analyze( Context context)
    {
        JSONObject jsonObject = new JSONObject();

        if (statJsonObject == null)
        {
            AnalyzeAllModulesBasic(context);
        }




        //Free up the memory of the basic analysis portion.
        statJsonObject = null;

        return jsonObject;
    }

    /**
     * Grabs a list of pairs of each value in the passed in column of the passed in table (if it is
     * not null)
     * @param tableName Name of the table to pull from
     * @param dateName Name of the date field on the table
     * @param columnName Name of the column to pull from on the table
     * @param context The context to use for database operations
     * @return A list containing pairs of integers. The integers are the value, and then the
     */
    private List<Pair<Integer, Integer>> getColumnDatesAndValues(String tableName, String dateName,
                                                                 String columnName, Context context)
    {
        List<Pair<Integer, Integer>> returnList = new LinkedList<>();

        Cursor cursor = LocalStorageAccess.selectAllEntries(context, LocalStorageAccessMood.TABLE_NAME,
                LocalStorageAccessMood.DATE + " DESC",
                new String[]{LocalStorageAccessMood.DEP, LocalStorageAccessMood.DATE}, true);

        return returnList;
    }
}
