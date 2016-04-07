package com.rocket.biometrix.Analysis;

import android.content.ContentValues;
import android.database.Cursor;

import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMedication;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;

import java.util.Collections;
import java.util.LinkedList;

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
            LocalStorageAccessMedication.WEB_MEDICATION_ID, LocalStorageAccessMedication.LOCAL_MEDICATION_ID,
            LocalStorageAccessSleep.UPDATED}; //Since the updated field is named the same everywhere, I am only listing it once

    //A list of strings that are added onto the end of the column names to be able to retrieve
    //the values from the contentvalues
    public static final String MIN = "Min";
    public static final String MAX = "Max";
    public static final String MEAN = "Mean";
    public static final String MEDIAN = "Median";
    public static final String COUNT = "Count";

    /**
     * Private constructor since this class should never be directly instantiated
     */
    private BiometrixAnalysis()
    { }

    /**
     * Performs basic analysis on the integer fields of the passed in cursor. Basic analysis
     * consists of operations such as the mean, median, min, max, and standard deviation
     * @param cursor A cursor containing all the rows that are to be analyzed
     * @return A ContentValues object that contains all of the analyzed rows along with the calculated
     * values.
     */
    public static ContentValues AnalyzeIntFieldsBasic(Cursor cursor)
    {
        ContentValues contentValues = new ContentValues();

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

                    //Check that the column actually has
                    boolean hasNonNull = false;

                    //Don't need to check if there is a non-null value if the field is not tracked
                    if (isTracked)
                    {
                        if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER)
                        {
                            hasNonNull = true;
                        } else
                        {
                            while (cursor.moveToNext() && !hasNonNull)
                            {
                                if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER)
                                {
                                    hasNonNull = true;
                                }
                            }
                        }
                    }

                    //Only care about columns that store integers for this method
                    //Only process the value if it is a tracked column
                    if (hasNonNull && isTracked)
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
                            median = (list.get((int) ((float)totalNum / 2)) - 1 );
                            median += list.get((int) (((float)totalNum / 2)));
                            median /= 2;
                        } //The median is the very middle element
                        else {
                            median = list.get((int) Math.ceil((float)totalNum / 2));
                        }

                        for (int j = 0; j < totalNum; ++j) {
                            totalValue += list.get(j);
                        }

                        mean = (float)totalValue / (float)totalNum;

                        String columnName = cursor.getColumnName(i);
                        contentValues.put(columnName + MIN, min);
                        contentValues.put(columnName + MAX, max);
                        contentValues.put(columnName + MEAN, mean);
                        contentValues.put(columnName + MEDIAN, median);
                        contentValues.put(columnName + COUNT, totalNum);
                    }
                }
            }
        }
        catch (Exception except)
        {
            except.getMessage();
        }

        return contentValues;
    }

}
