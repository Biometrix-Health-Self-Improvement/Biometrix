package com.rocket.biometrix.Login;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMedication;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by TJ on 4/18/2016.
 * The key names for the settings used by local account
 *
 * Modified by TJ on 4/30/2016
 * Changed to SettingsHelper class. Now contains key names as well as methods to set or retrieve
 * settings based on keys, or retrieve a list of keys that correspond to resource IDs
 *
 * 5/5/2016- Changed to SettingsAndEntryHelper class now that this contains more functionality for
 * preparing entries
 */
public class SettingsAndEntryHelper {

    /**
     * Sets the status of all switches for the passed in 2D string array based on the default value
     * and the user's settings
     * @param view The view that holds the switch that needs to be modified
     * @param keysAndIDs A 2D array containing all keys and their ID, should be retrieved from
     *                   SettingsAndEntryHelper as well
     * @param defaultValue Default to true or false based on this param
     */
    public static void setupSwitches(View view, String[][] keysAndIDs, boolean defaultValue)
    {
        LocalAccount localAccount = LocalAccount.GetInstance();

        for (String[] stringArray : keysAndIDs) {
            boolean setBool = localAccount.getBoolean(view.getContext(), stringArray[0], defaultValue);
            ((Switch) view.findViewById(Integer.parseInt(stringArray[1]))).setChecked(setBool);
        }
    }

    /**
     * Sets the status of all settings for the passed in 2D string array based on the value given
     * by the switches
     * @param view The view that holds the switch that needs to be modified
     * @param keysAndIDs A 2D array containing all keys and their ID, should be retrieved from
     *                   SettingsAndEntryHelper as well
     */
    public static void storeSwitchValues(View view, String[][] keysAndIDs)
    {
        LocalAccount localAccount = LocalAccount.GetInstance();

        for (String[] stringArray : keysAndIDs) {
            boolean isChecked = ((Switch) view.findViewById(Integer.parseInt(stringArray[1]))).isChecked();
            localAccount.setBoolean(view.getContext(), stringArray[0], isChecked);
        }
    }

    /**
     * Returns the columns that are available for analysis and are enabled by the user's settings
     * @param context The current context. Used for determining user settings
     * @param tableName The name of the table being referred to
     * @param defaultValue Default to columns being enabled or disabled by default if no setting
     *                     entry is found
     * @return Returns a list of strings that are the enabled analysis columns
     */
    public static List<String> getEnabledAnalysisColumns(Context context, String tableName, boolean defaultValue)
    {
        switch (tableName)
        {
            case LocalStorageAccessMood.TABLE_NAME:
                return getEnabledColumns(context, getAnalysisMoodKeysAndColumns(), defaultValue);
            case LocalStorageAccessDiet.TABLE_NAME:
                return getEnabledColumns(context, getAnalysisDietKeysAndColumns(), defaultValue);
            case LocalStorageAccessSleep.TABLE_NAME:
                List<String> arrayList = new ArrayList<>();
                if (LocalAccount.GetInstance().getBoolean(context, SLEEP_QUALITY, true))
                {
                    arrayList.add(LocalStorageAccessSleep.QUALITY);
                }

                if (LocalAccount.GetInstance().getBoolean(context, SLEEP_HOURS, true) ||
                        LocalAccount.GetInstance().getBoolean(context, SLEEP_MINUTES, true))
                {
                    arrayList.add(LocalStorageAccessSleep.DURATION);
                }

                return arrayList;
            case LocalStorageAccessExercise.TABLE_NAME:
                return getEnabledColumns(context, getAnalysisExerciseKeysAndColumns(), defaultValue);
            default:
                return new ArrayList<>();
        }
    }

    /**
     * Retrieves a list of columns that are enabled based on user settings
     * @param context The current context used to grab user settings
     * @param keysAndColumns A 2D array of the keys and their associated columns
     * @param defaultValue Whether to default to true or not if the setting is not found
     * @return A list of strings that contain the names of the enabled columns
     */
    private static List<String> getEnabledColumns(Context context, String[][] keysAndColumns, boolean defaultValue)
    {
        List<String> returnStringList = new LinkedList<>();

        LocalAccount localAccount = LocalAccount.GetInstance();

        for (String[] stringArray : keysAndColumns) {
            boolean isSet = localAccount.getBoolean(context, stringArray[0], defaultValue);

            if (isSet) {
                returnStringList.add(stringArray[1]);
            }
        }

        return returnStringList;
    }

    /**
     * Makes the views that are disabled by user settings invisible
     * @param view The parent view that has the disabled views in it
     */
    public static void makeDisabledEntryViewsInvisible(View view, String tableName)
    {
        List<Pair<String[], Integer[]>> pairList = null;

        switch (tableName)
        {
            case LocalStorageAccessSleep.TABLE_NAME:
                pairList = getSleepInvisibilityDependencies();
                break;
            case LocalStorageAccessMood.TABLE_NAME:
                pairList = getMoodInvisibilityDependencies();
                break;
            case LocalStorageAccessExercise.TABLE_NAME:
                pairList = getExerciseInvisibilityDependencies();
                break;
            case LocalStorageAccessDiet.TABLE_NAME:
                pairList = getDietInvisibilityDependencies();
                break;
            case LocalStorageAccessMedication.TABLE_NAME:
                pairList = getMedicationInvisibilityDependencies();
                break;
            default:
                pairList = null;
                break;
        }

        if (pairList != null)
        {
            LocalAccount localAccount = LocalAccount.GetInstance();

            for(Pair<String[], Integer[]> pair : pairList)
            {
                boolean shouldDisable = true;

                //If any of the dependencies are enabled, this means do not disable the views
                for(String string : pair.first)
                {
                    if (localAccount.getBoolean(view.getContext(), string, true) )
                    {
                        shouldDisable = false;
                    }
                }

                if (shouldDisable)
                {
                    for(Integer id : pair.second)
                    {
                        view.findViewById(id).setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    /**
     * Prepares the array from the text views and other views on the table based on whether they
     * are visible or not. If not visible it sets nulls, if visible this sets the value from the
     * associated view
     * @param view The parent view that has the other views on it
     * @param tableName The name of the table
     * @param dateString The string that is used for the date element in the array
     * @param timeString The string that is used for the time element in the array. Can be null (due
     *                   to diet table..) and is nothing is done with this if the call is from the
     *                   diet table
     * @return A string array with an element for each column
     */
    public static String[] prepareColumnArray(View view, String tableName, String dateString, String timeString)
    {
        String columnArray[] = null;
        int dateIndex;
        int timeIndex = -1;

        switch (tableName)
        {
            case LocalStorageAccessSleep.TABLE_NAME:
                columnArray = new String[LocalStorageAccessSleep.getColumns().length];
                dateIndex = 3;
                timeIndex = 4;
                break;
            case LocalStorageAccessMood.TABLE_NAME:
                columnArray = new String[LocalStorageAccessMood.getColumns().length];
                dateIndex = 3;
                timeIndex = 4;
                break;
            case LocalStorageAccessMedication.TABLE_NAME:
                columnArray = new String[LocalStorageAccessMedication.getColumns().length];
                dateIndex = 3;
                timeIndex = 4;
                break;
            case LocalStorageAccessDiet.TABLE_NAME:
                columnArray = new String[LocalStorageAccessDiet.getColumns().length];
                dateIndex = 3;
                break;
            case LocalStorageAccessExercise.TABLE_NAME:
                columnArray = new String[LocalStorageAccessExercise.getColumns().length];
                dateIndex = 8;
                timeIndex = 9;
                break;
            default:
                columnArray = new String[0];
                return columnArray;
        }

        String username = LocalAccount.DEFAULT_NAME;

        if (LocalAccount.isLoggedIn()) {
            username = LocalAccount.GetInstance().GetUsername();
        }

        columnArray[0] = null;
        columnArray[1] = username;
        columnArray[2] = null;
        columnArray[dateIndex] = dateString;

        if (timeIndex != -1 && timeString != null)
        {
            columnArray[timeIndex] = timeString;
        }

        updateEntryStringFromVisibleViews(view, tableName, columnArray);

        return columnArray;
    }

    /**
     * Repopulates the text views/spinners/sliders with the values that are stored in the local
     * database for that
     * @param view The parent view that houses all of the spinners/sliders/text views
     * @param tableName The name of the table that is being queried
     * @param id The ID number to grab from the local database
     */
    public static void repopulateEntryPage(View view, String tableName, Integer id)
    {
        List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> quintets = null;
        Cursor cursor = null;
        int dateRID = -1;
        int timeRID = -1;
        String dateColName = null;
        String timeColName = null;

        switch (tableName)
        {
            case LocalStorageAccessSleep.TABLE_NAME:
                quintets = getSleepViewDependencies();
                cursor = LocalStorageAccess.selectEntryByID(view.getContext(), LocalStorageAccessSleep.TABLE_NAME,
                        LocalStorageAccessSleep.LOCAL_SLEEP_ID, id);
                dateRID = R.id.sleepStartDateTextView;
                timeRID = R.id.sleepStartTimeTextView;
                dateColName = LocalStorageAccessSleep.DATE;
                timeColName = LocalStorageAccessSleep.TIME;
                break;
            case LocalStorageAccessMood.TABLE_NAME:
                quintets = getMoodViewDependencies();
                cursor = LocalStorageAccess.selectEntryByID(view.getContext(), LocalStorageAccessMood.TABLE_NAME,
                        LocalStorageAccessMood.LOCAL_MOOD_ID, id);
                dateRID = R.id.moodCreateEntryDateSelect;
                timeRID = R.id.moodCreateEntryTimeSelect;
                dateColName = LocalStorageAccessMood.DATE;
                timeColName = LocalStorageAccessMood.TIME;
                break;
            case LocalStorageAccessDiet.TABLE_NAME:
                quintets = getDietViewDependencies();
                cursor = LocalStorageAccess.selectEntryByID(view.getContext(), LocalStorageAccessDiet.TABLE_NAME,
                        LocalStorageAccessDiet.LOCAL_DIET_ID, id);
                dateRID = R.id.DietStartDateTextView;
                timeRID = -1;
                dateColName = LocalStorageAccessDiet.DATE;
                timeColName = null;
                break;
            case LocalStorageAccessExercise.TABLE_NAME:
                quintets = getExerciseViewDependencies();
                cursor = LocalStorageAccess.selectEntryByID(view.getContext(), LocalStorageAccessExercise.TABLE_NAME,
                        LocalStorageAccessExercise.LOCAL_EXERCISE_ID, id);
                dateRID = R.id.ex_tv_date;
                timeRID = R.id.ex_tv_time;
                dateColName = LocalStorageAccessExercise.DATE;
                timeColName = LocalStorageAccessExercise.TIME;
                break;
            case LocalStorageAccessMedication.TABLE_NAME:
                quintets = getMedicationViewDependencies();
                cursor = LocalStorageAccess.selectEntryByID(view.getContext(), LocalStorageAccessMedication.TABLE_NAME,
                        LocalStorageAccessMedication.LOCAL_MEDICATION_ID, id);
                dateRID = R.id.MedicationStartDateTextView;
                timeRID = R.id.MedicationStartTimeTextView;
                dateColName = LocalStorageAccessMedication.DATE;
                timeColName = LocalStorageAccessMedication.TIME;
                break;
        }

        if(cursor != null)
        {
            if (cursor.moveToFirst())
            {
                TextView dateTextView = (TextView)view.findViewById(dateRID);
                TextView timeTextView = null;

                SimpleDateFormat noDayOfWeek = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date entryDate = noDayOfWeek.parse(cursor.getString(cursor.getColumnIndex(dateColName)));
                    SimpleDateFormat withDayOfWeek = new SimpleDateFormat("EEE, MM/dd/yyyy");
                    dateTextView.setText("Date: " + withDayOfWeek.format(entryDate));
                }
                catch (Exception e)
                {
                    Log.i("ParseFail", e.getMessage());
                }


                if (timeRID != -1)
                {
                    try {


                        timeTextView = (TextView) view.findViewById(timeRID);
                        timeTextView.setText("Time: " + cursor.getString(cursor.getColumnIndex(timeColName)));
                    }
                    catch (Exception e)
                    {
                        e.getMessage();
                    }
                }



                for (Quintet<Integer, Integer, VIEW_TYPE, String, String> quintet : quintets)
                {
                    View element = view.findViewById(quintet.first);

                    int colIndex = cursor.getColumnIndex(quintet.fifth);

                    if (!cursor.isNull(colIndex) )
                    {
                        switch (quintet.third)
                        {
                            case TEXT_VIEW:
                                ((TextView)element).setText(cursor.getString(colIndex));
                                break;
                            case SEEKBAR:
                                ((SeekBar)element).setProgress(cursor.getInt(colIndex));
                                break;
                            case SPINNER:
                                try {
                                    Spinner spinnerRef = ((Spinner)element);
                                    spinnerRef.setSelection(0);
                                    int position = 1;

                                    while (!spinnerRef.getSelectedItem().equals(cursor.getString(colIndex)) )
                                    {
                                        spinnerRef.setSelection(position);
                                        position++;
                                    }
                                }
                                catch(Exception e)
                                {
                                    Log.d("SpinnerException", e.getMessage());
                                    ((Spinner)element).setSelection(0);
                                }

                                break;
                            case SLEEP_DURATION:
                                String durationString = cursor.getString(colIndex);
                                int colonIndex = durationString.indexOf(':');
                                int hours = Integer.parseInt(durationString.substring(0, colonIndex));
                                int minutes = Integer.parseInt(durationString.substring(colonIndex + 1, colonIndex + 3));

                                ((SeekBar) view.findViewById(R.id.sleepHoursSeekBar)).setProgress(hours);
                                ((SeekBar) view.findViewById(R.id.sleepMinutesSeekBar)).setProgress(minutes);
                                break;
                            case TEXT_VIEW_INT:
                                ((TextView) element).setText(Integer.toString(cursor.getInt(colIndex)));
                                break;
                        }
                    }

                }
            }

            cursor.close();
        }
    }

    /**
     * Grabs the value from the view and puts it into the associated place in the array
     * @param view The view that should have all of the views to check visibility on it.
     * @param tableName The name of the table the entry is for
     * @param arrayToUpdate The array of strings that needs to be updated before being entered
     */
    private static void updateEntryStringFromVisibleViews(View view, String tableName, String[] arrayToUpdate)
    {
        List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> quintets = null;

        switch (tableName)
        {
            case LocalStorageAccessSleep.TABLE_NAME:
                quintets = getSleepViewDependencies();
                break;
            case LocalStorageAccessMood.TABLE_NAME:
                quintets = getMoodViewDependencies();
                break;
            case LocalStorageAccessDiet.TABLE_NAME:
                quintets = getDietViewDependencies();
                break;
            case LocalStorageAccessExercise.TABLE_NAME:
                quintets = getExerciseViewDependencies();
                break;
            case LocalStorageAccessMedication.TABLE_NAME:
                quintets = getMedicationViewDependencies();
                break;
        }

        for(Quintet<Integer, Integer, VIEW_TYPE, String, String> quintet : quintets)
        {
            View element = view.findViewById(quintet.first);
            //If the view is visible, update the element that corresponds to that view in the array
            //with the default value.
            if(element.getVisibility() == View.GONE)
            {
                arrayToUpdate[quintet.second] = quintet.fourth;
            }
            //Otherwise, grab the value as expected based on the type
            else
            {
                //String foodName = ((TextView)dietView.findViewById(R.id.Food_Name)).getText().toString();
                //String meal = ((Spinner)dietView.findViewById(R.id.Meal_Select)).getSelectedItem().toString();
                switch (quintet.third)
                {
                    case TEXT_VIEW:
                        arrayToUpdate[quintet.second] = ((TextView)element).getText().toString();
                        break;
                    case SEEKBAR:
                        arrayToUpdate[quintet.second] = Integer.toString(((SeekBar) element).getProgress());
                        break;
                    case SPINNER:
                        arrayToUpdate[quintet.second] = ((Spinner)element).getSelectedItem().toString();
                        break;
                    case SLEEP_DURATION:
                        String duration = ((TextView)element).getText().toString();
                        arrayToUpdate[quintet.second] = duration.substring(duration.indexOf(":") + 2).trim();
                        break;
                    case TEXT_VIEW_INT:
                        arrayToUpdate[quintet.second] = ((TextView)element).getText().toString();
                        if (arrayToUpdate[quintet.second].equals(""))
                        {
                            arrayToUpdate[quintet.second] = "0";
                        }
                        break;
                }
            }
        }
    }



    //Module disable/enable settings
    public static final String MOOD_MODULE = "MoodModuleEnabled";
    public static final String SLEEP_MODULE = "SleepModuleEnable";
    public static final String EXERCISE_MODULE = "ExerciseModuleEnable";
    public static final String DIET_MODULE = "DietModuleEnable";
    public static final String MEDICATION_MODULE = "MedicationModuleEnable";

    public static String[][] getModuleSettingsAndNames()
    {
        return new String[][]{
                {MOOD_MODULE, LocalStorageAccessMood.TABLE_NAME},
                {SLEEP_MODULE, LocalStorageAccessSleep.TABLE_NAME},
                {EXERCISE_MODULE, LocalStorageAccessExercise.TABLE_NAME},
                {DIET_MODULE, LocalStorageAccessDiet.TABLE_NAME},
                {MEDICATION_MODULE, LocalStorageAccessMedication.TABLE_NAME}};
    }

    /**
     * Returns list of strings that correspond to all enabled column names
     * @return A list of strings that includes only the names of enabled modules
     */
    public static List<String> getEnabledModuleNames(Context context)
    {
        List<String> stringList = new ArrayList<>(5);

        for (String[] array : getModuleSettingsAndNames())
        {
            if(LocalAccount.GetInstance().getBoolean(context, array[0], true))
            {
                stringList.add(array[1]);
            }
        }

        return stringList;
    }

    /**
     * Returns a 2 dimensional array that has a list of all the setting keys
     * @return The aforementioned 2D array
     */
    public static String[][] getAllModuleKeysAndRIDs() {
        return new String[][]{
                {MOOD_MODULE, Integer.toString(R.id.DisableSwitchMoodModule)},
                {SLEEP_MODULE, Integer.toString(R.id.DisableSwitchSleepModule)},
                {EXERCISE_MODULE, Integer.toString(R.id.DisableSwitchExerciseModule)},
                {DIET_MODULE, Integer.toString(R.id.DisableSwitchDietModule)},
                {MEDICATION_MODULE, Integer.toString(R.id.DisableSwitchMedicationModule)} };
    }

    //Diet module settings
    public static final String DIET_FOOD_TYPE = "DietFoodTypeEnabled";
    public static final String DIET_MEAL = "DietMealEnabled";
    public static final String DIET_SERVING_SIZE = "DietServingSizeEnabled";
    public static final String DIET_CALORIES = "DietCaloriesEnabled";
    public static final String DIET_TOTAL_FAT = "DietTotalFatEnabled";
    public static final String DIET_SAT_FAT = "DietSatFatEnabled";
    public static final String DIET_TRANS_FAT = "DietTransFatEnabled";
    public static final String DIET_CHOL = "DietCholesterolEnabled";
    public static final String DIET_SODIUM = "DietSodiumEnabled";
    public static final String DIET_TOTAL_CARBS = "DietTotalCarbsEnabled";
    public static final String DIET_FIBER = "DietFiberEnabled";
    public static final String DIET_SUGARS = "DietSugarsEnabled";
    public static final String DIET_PROTEIN = "DietProteinEnabled";
    public static final String DIET_VITAMINA = "DietVitaminAEnabled";
    public static final String DIET_VITAMINB = "DietVitaminBEnabled";
    public static final String DIET_CALCIUM = "DietCalciumEnabled";
    public static final String DIET_IRON = "DietIronEnabled";
    public static final String DIET_NOTES = "DietNotesEnabled";

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the resource ID for that
     * diet column. For use in settings
     * @return The aforementioned 2D array
     */
    public static String[][] getAllDietKeysAndRIDs() {
        return new String[][]{
                {DIET_FOOD_TYPE, Integer.toString(R.id.DisableSwitchFoodType)},
                {DIET_MEAL, Integer.toString(R.id.DisableSwitchMealSelection)},
                {DIET_SERVING_SIZE, Integer.toString(R.id.DisableSwitchServingSizeSelection)},
                {DIET_CALORIES, Integer.toString(R.id.DisableSwitchCaloriesInput)},
                {DIET_TOTAL_FAT, Integer.toString(R.id.DisableSwitchTotalFatInput)},
                {DIET_SAT_FAT, Integer.toString(R.id.DisableSwitchSaturatedFatInput)},
                {DIET_TRANS_FAT, Integer.toString(R.id.DisableSwitchTransFatInput)},
                {DIET_CHOL, Integer.toString(R.id.DisableSwitchCholesterolInput)},
                {DIET_SODIUM, Integer.toString(R.id.DisableSwitchSodiumInput)},
                {DIET_TOTAL_CARBS, Integer.toString(R.id.DisableSwitchTotalCarbohydratesInput)},
                {DIET_FIBER, Integer.toString(R.id.DisableSwitchDietaryFiberInput)},
                {DIET_SUGARS, Integer.toString(R.id.DisableSwitchSugarsInput)},
                {DIET_PROTEIN, Integer.toString(R.id.DisableSwitchProteinInput)},
                {DIET_VITAMINA, Integer.toString(R.id.DisableSwitchVitaminAInput)},
                {DIET_VITAMINB, Integer.toString(R.id.DisableSwitchVitaminBInput)},
                {DIET_CALCIUM, Integer.toString(R.id.DisableSwitchCalciumInput)},
                {DIET_IRON, Integer.toString(R.id.DisableSwitchIronInput)},
                {DIET_NOTES, Integer.toString(R.id.DisableSwitchDietNotesInput)}};
    }

    /**
     * Returns a list of the strings that correspond to local account keys and the view IDs associated
     * with those IDs. If all Keys are listed as disabled, the view should be disabled.
     * @return A list of pairs of strings and integers
     */
    private static List<Pair<String[], Integer[]>> getDietInvisibilityDependencies()
    {
        List<Pair<String[], Integer[]>> returnList = new ArrayList<>(18);
        returnList.add(new Pair<>(new String[] {DIET_FOOD_TYPE}, new Integer[] {R.id.Food_Name, R.id.FoodType_View}));
        returnList.add(new Pair<>(new String[] {DIET_MEAL}, new Integer[] {R.id.Meal_View, R.id.Meal_Select}));
        returnList.add(new Pair<>(new String[] {DIET_SERVING_SIZE}, new Integer[] {R.id.ServingSize_View, R.id.ServingSize_Select}));
        returnList.add(new Pair<>(new String[] {DIET_CALORIES}, new Integer[] {R.id.Calories_Amt, R.id.Calories_View}));
        returnList.add(new Pair<>(new String[] {DIET_TOTAL_FAT}, new Integer[] {R.id.TotalFat_Amt, R.id.TotalFat_View}));
        returnList.add(new Pair<>(new String[] {DIET_SAT_FAT}, new Integer[] {R.id.SaturatedFat_Amt, R.id.SaturatedFat_View}));
        returnList.add(new Pair<>(new String[] {DIET_TRANS_FAT}, new Integer[] {R.id.TransFat_Amt, R.id.TransFat_View}));
        returnList.add(new Pair<>(new String[] {DIET_CHOL}, new Integer[] {R.id.Cholesterol_Amt, R.id.Cholesterol_View}));
        returnList.add(new Pair<>(new String[] {DIET_SODIUM}, new Integer[] {R.id.Sodium_Amt, R.id.Sodium_View}));
        returnList.add(new Pair<>(new String[] {DIET_TOTAL_CARBS}, new Integer[] {R.id.TotalCarb_Amt, R.id.TotalCarbs_View}));
        returnList.add(new Pair<>(new String[] {DIET_FIBER}, new Integer[] {R.id.DietaryFiber_Amt, R.id.DietaryFiber_View}));
        returnList.add(new Pair<>(new String[] {DIET_SUGARS}, new Integer[] {R.id.Sugars_Amt, R.id.Sugars_View}));
        returnList.add(new Pair<>(new String[] {DIET_PROTEIN}, new Integer[] {R.id.Protein_Amt, R.id.Protein_View}));
        returnList.add(new Pair<>(new String[] {DIET_VITAMINA}, new Integer[] {R.id.VitaminA_Amt, R.id.VitaminA_View}));
        returnList.add(new Pair<>(new String[] {DIET_VITAMINB}, new Integer[] {R.id.VitaminB_Amt, R.id.VitaminB_View}));
        returnList.add(new Pair<>(new String[] {DIET_CALCIUM}, new Integer[] {R.id.Calcium_Amt, R.id.Calcium_View}));
        returnList.add(new Pair<>(new String[] {DIET_IRON}, new Integer[] {R.id.Iron_Amt, R.id.Iron_View}));
        returnList.add(new Pair<>(new String[] {DIET_NOTES}, new Integer[] {R.id.dietDetailsEditText}));


        return returnList;
    }

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the column they go with
     * for every column that is analyzed
     * @return The aforementioned 2D array
     */
    public static String[][] getAnalysisDietKeysAndColumns() {
        return new String[][]{
                {DIET_CALORIES, LocalStorageAccessDiet.CALORIES},
                {DIET_TOTAL_FAT, LocalStorageAccessDiet.TOTALFAT},
                {DIET_SAT_FAT, LocalStorageAccessDiet.SATFAT},
                {DIET_TRANS_FAT, LocalStorageAccessDiet.TRANSFAT},
                {DIET_CHOL, LocalStorageAccessDiet.CHOLESTEROL},
                {DIET_SODIUM, LocalStorageAccessDiet.SODIUM},
                {DIET_TOTAL_CARBS, LocalStorageAccessDiet.TOTALCARBS},
                {DIET_FIBER, LocalStorageAccessDiet.FIBER},
                {DIET_SUGARS, LocalStorageAccessDiet.SUGARS},
                {DIET_PROTEIN, LocalStorageAccessDiet.PROTEIN},
                {DIET_VITAMINA, LocalStorageAccessDiet.VITAMINA},
                {DIET_VITAMINB, LocalStorageAccessDiet.VITAMINB},
                {DIET_CALCIUM, LocalStorageAccessDiet.CALCIUM},
                {DIET_IRON, LocalStorageAccessDiet.IRON}};
    }

    /**
     * Returns a list of quartets for the diet table. The first item is the ID of the view
     * that should be checked for visibility. The second is the position in the entry array to place
     * the value of element 4 in if the view is invisible. Element three is the type of view that the
     * element corresponds to
     * @return A list of pairs of integers
     */
    private static List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> getDietViewDependencies()
    {
        List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> returnList = new ArrayList<>(18);
        returnList.add(new Quintet<>(R.id.Food_Name, 4, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessDiet.TYPE));
        returnList.add(new Quintet<>(R.id.Meal_Select, 5, VIEW_TYPE.SPINNER, "", LocalStorageAccessDiet.MEAL));
        returnList.add(new Quintet<>(R.id.ServingSize_Select, 6, VIEW_TYPE.SPINNER, "", LocalStorageAccessDiet.SERVING));
        returnList.add(new Quintet<>(R.id.Calories_Amt, 7, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.CALORIES));
        returnList.add(new Quintet<>(R.id.TotalFat_Amt, 8, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.TOTALFAT));
        returnList.add(new Quintet<>(R.id.SaturatedFat_Amt, 9, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.SATFAT));
        returnList.add(new Quintet<>(R.id.TransFat_Amt, 10, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.TRANSFAT));
        returnList.add(new Quintet<>(R.id.Cholesterol_Amt, 11, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.CHOLESTEROL));
        returnList.add(new Quintet<>(R.id.Sodium_Amt, 12, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.SODIUM));
        returnList.add(new Quintet<>(R.id.TotalCarb_Amt, 13, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.TOTALCARBS));
        returnList.add(new Quintet<>(R.id.DietaryFiber_Amt, 14, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.FIBER));
        returnList.add(new Quintet<>(R.id.Sugars_Amt, 15, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.SUGARS));
        returnList.add(new Quintet<>(R.id.Protein_Amt, 16, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.PROTEIN));
        returnList.add(new Quintet<>(R.id.VitaminA_Amt, 17, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.VITAMINA));
        returnList.add(new Quintet<>(R.id.VitaminB_Amt, 18, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.VITAMINB));
        returnList.add(new Quintet<>(R.id.Calcium_Amt, 19, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.CALCIUM));
        returnList.add(new Quintet<>(R.id.Iron_Amt, 20, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessDiet.IRON));
        returnList.add(new Quintet<>(R.id.dietDetailsEditText, 21, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessDiet.NOTE));

        return returnList;
    }
    
    //Exercise module settings
    public static final String EXERCISE_NAME = "ExerciseNameEnabled";
    public static final String EXERCISE_DURATION = "ExerciseDurationEnabled";
    public static final String EXERCISE_TYPE= "ExerciseTypeEnabled";
    public static final String EXERCISE_INTENSITY = "ExerciseIntensityEnabled";
    public static final String EXERCISE_NOTES = "ExerciseNotesEnabled";

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the resource ID for that
     * exercise column. For use in settings
     * @return The aforementioned 2D array
     */
    public static String[][] getAllExerciseKeysAndRIDs() {
        return new String[][]{
                {EXERCISE_NAME, Integer.toString(R.id.DisableSwitchExerciseName)},
                {EXERCISE_DURATION, Integer.toString(R.id.DisableSwitchExerciseDuration)},
                {EXERCISE_TYPE, Integer.toString(R.id.DisableSwitchExerciseType) },
                {EXERCISE_INTENSITY, Integer.toString(R.id.DisableSwitchExerciseIntensity)},
                {EXERCISE_NOTES, Integer.toString(R.id.DisableSwitchExerciseNotesInput)}};
    }

    /**
     * Returns a list of the strings that correspond to local account keys and the view IDs associated
     * with those IDs. If all Keys are listed as disabled, the view should be disabled.
     * @return A list of pairs of strings and integers
     */
    private static List<Pair<String[], Integer[]>> getExerciseInvisibilityDependencies()
    {
        List<Pair<String[], Integer[]>> returnList = new ArrayList<>(6);
        returnList.add(new Pair<>(new String[] {EXERCISE_NAME}, new Integer[]{R.id.ex_title, R.id.ex_title_text}));
        returnList.add(new Pair<>(new String[] {EXERCISE_DURATION}, new Integer[]{R.id.ex_length, R.id.ex_length_text}));
        returnList.add(new Pair<>(new String[] {EXERCISE_NAME, EXERCISE_DURATION}, new Integer[]{R.id.exerciseSpaceAfterMinutes}));

        returnList.add(new Pair<>(new String[] {EXERCISE_INTENSITY}, new Integer[]{R.id.ex_intensity_seekbar,
        R.id.ex_intensity_text, R.id.exerciseSpaceAfterIntensity}));

        returnList.add(new Pair<>(new String[] {EXERCISE_TYPE}, new Integer[]{R.id.ex_type, R.id.exerciseSpaceAfterType}));

        returnList.add(new Pair<>(new String[] {EXERCISE_NOTES}, new Integer[]{R.id.exDetailsEditText}));

        return returnList;
    }

    /**
     * Returns a list of quartets for the exercise table. The first item is the ID of the view
     * that should be checked for visibility. The second is the position in the entry array to place
     * the value of element 4 in if the view is invisible. Element three is the type of view that the
     * element corresponds to
     * @return A list of pairs of integers
     */
    private static List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> getExerciseViewDependencies()
    {
        List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> returnList = new ArrayList<>(5);
        returnList.add(new Quintet<>(R.id.ex_title, 3, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessExercise.TITLE));
        returnList.add(new Quintet<>(R.id.ex_type, 4, VIEW_TYPE.SPINNER, "", LocalStorageAccessExercise.TYPE));
        returnList.add(new Quintet<>(R.id.ex_length, 5, VIEW_TYPE.TEXT_VIEW_INT, (String)null, LocalStorageAccessExercise.MINUTES));
        returnList.add(new Quintet<>(R.id.ex_intensity_seekbar, 6, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessExercise.INTY));
        returnList.add(new Quintet<>(R.id.exDetailsEditText, 7, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessExercise.NOTES));

        return returnList;
    }

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the column they go with.
     * Returns only columns that are analyzed in analysis
     * @return The aforementioned 2D array
     */
    public static String[][] getAnalysisExerciseKeysAndColumns() {
        return new String[][]{
                {EXERCISE_DURATION, LocalStorageAccessExercise.MINUTES},
                {EXERCISE_INTENSITY, LocalStorageAccessExercise.INTY}};
    }
    
    //Mood module settings
    public static final String MOOD_DEP = "MoodDepEnabled";
    public static final String MOOD_ELEV = "MoodElevEnabled";
    public static final String MOOD_IRRITABLE = "MoodIrritableEnabled";
    public static final String MOOD_ANX = "MoodAnxEnabled";
    public static final String MOOD_SAD = "MoodSadEnabled";
    public static final String MOOD_HAPPY = "MoodHappyEnabled";
    public static final String MOOD_ANGER = "MoodAngerEnabled";
    public static final String MOOD_NOTES = "MoodNotesEnabled";

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the resource ID for that
     * mood column. For use in settings
     * @return The aforementioned 2D array
     */
    public static String[][] getAllMoodKeysAndRIDs() {
        return new String[][]{
                {MOOD_DEP, Integer.toString(R.id.DisableSwitchDepressedSlider)},
                {MOOD_ELEV, Integer.toString(R.id.DisableSwitchElevatedSlider)},
                {MOOD_IRRITABLE, Integer.toString(R.id.DisableSwitchIrritabilitySlider)},
                {MOOD_ANX, Integer.toString(R.id.DisableSwitchAnxietySlider)},
                {MOOD_SAD, Integer.toString(R.id.DisableSwitchSadnessSlider)},
                {MOOD_HAPPY, Integer.toString(R.id.DisableSwitchHappinessSlider)},
                {MOOD_ANGER, Integer.toString(R.id.DisableSwitchAngerSlider)},
                {MOOD_NOTES, Integer.toString(R.id.DisableSwitchMoodNotesInput)}};
    }

    /**
     * Returns a list of the strings that correspond to local account keys and the view IDs associated
     * with those IDs. If all Keys are listed as disabled, the view should be disabled.
     * @return A list of pairs of strings and integers
     */
    private static List<Pair<String[], Integer[]>> getMoodInvisibilityDependencies()
    {
        List<Pair<String[], Integer[]>> returnList = new ArrayList<>(9);
        returnList.add(new Pair<>(new String[] {MOOD_DEP}, new Integer[]{R.id.moodDepressedDesc, R.id.moodDepressedLabel,
        R.id.moodDepressedRating}));
        returnList.add(new Pair<>(new String[] {MOOD_ANX}, new Integer[]{R.id.moodAnxietyDesc, R.id.moodAnxietyLabel,
                R.id.moodAnxietyRating}));
        returnList.add(new Pair<>(new String[] {MOOD_IRRITABLE}, new Integer[]{R.id.moodIrritabilityDesc, R.id.moodIrritabilityLabel,
                R.id.moodIrritabilityRating}));
        returnList.add(new Pair<>(new String[] {MOOD_ELEV}, new Integer[]{R.id.moodElevatedDesc, R.id.moodElevatedLabel,
                R.id.moodElevatedRating}));
        returnList.add(new Pair<>(new String[] {MOOD_SAD}, new Integer[]{ R.id.moodSadLabel,
                R.id.moodSadRating}));
        returnList.add(new Pair<>(new String[] {MOOD_HAPPY}, new Integer[]{R.id.moodHappyLabel,
                R.id.moodHappyRating}));
        returnList.add(new Pair<>(new String[] {MOOD_ANGER}, new Integer[]{R.id.moodAngerLabel,
                R.id.moodAngerRating}));

        returnList.add(new Pair<>(new String[] {MOOD_DEP, MOOD_ANX, MOOD_ELEV, MOOD_IRRITABLE, MOOD_HAPPY, MOOD_SAD, MOOD_ANGER},
                new Integer[]{R.id.moodSpaceAfterAnxiety}));

        returnList.add(new Pair<>(new String[] {MOOD_NOTES}, new Integer[]{R.id.moodDetailsEditText}));

        return returnList;
    }

    /**
     * Returns a list of quartets for the mood table. The first item is the ID of the view
     * that should be checked for visibility. The second is the position in the entry array to place
     * the value of element 4 in if the view is invisible. Element three is the type of view that the
     * element corresponds to
     * @return A list of pairs of integers
     */
    private static List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> getMoodViewDependencies()
    {
        List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> returnList = new ArrayList<>(8);
        returnList.add(new Quintet<>(R.id.moodDepressedRating, 5, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessMood.DEP));
        returnList.add(new Quintet<>(R.id.moodElevatedRating, 6, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessMood.ELEV));
        returnList.add(new Quintet<>(R.id.moodIrritabilityRating, 7, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessMood.IRR));
        returnList.add(new Quintet<>(R.id.moodAnxietyRating, 8, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessMood.ANX));
        returnList.add(new Quintet<>(R.id.moodSadRating, 9, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessMood.SAD));
        returnList.add(new Quintet<>(R.id.moodHappyRating, 10, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessMood.HAPPY));
        returnList.add(new Quintet<>(R.id.moodAngerRating, 11, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessMood.ANGER));
        returnList.add(new Quintet<>(R.id.moodDetailsEditText, 12, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessMood.NOTE));

        return returnList;
    }

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the column they go with
     * returns only columns that are analyzed in analysis
     * @return The aforementioned 2D array
     */
    public static String[][] getAnalysisMoodKeysAndColumns() {
        return new String[][]{
                {MOOD_DEP, LocalStorageAccessMood.DEP},
                {MOOD_ELEV, LocalStorageAccessMood.ELEV},
                {MOOD_IRRITABLE, LocalStorageAccessMood.IRR},
                {MOOD_ANX, LocalStorageAccessMood.ANX},
                {MOOD_SAD, LocalStorageAccessMood.SAD},
                {MOOD_HAPPY, LocalStorageAccessMood.HAPPY},
                {MOOD_ANGER, LocalStorageAccessMood.ANGER}};
    }
    
    //Sleep module settings
    public static final String SLEEP_HOURS = "SleepHoursEnabled";
    public static final String SLEEP_MINUTES = "SleepMinutesEnabled";
    public static final String SLEEP_QUALITY = "SleepQualityEnabled";
    public static final String SLEEP_NOTES = "SleepNotesEnabled";

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the resource ID for that
     * sleep column. For use in settings
     * @return The aforementioned 2D array
     */
    public static String[][] getAllSleepKeysAndRIDs() {
        return new String[][]{
                {SLEEP_HOURS, Integer.toString(R.id.DisableSwitchHoursSlept)},
                {SLEEP_MINUTES, Integer.toString(R.id.DisableSwitchMinutesSlept)},
                {SLEEP_QUALITY, Integer.toString(R.id.DisableSwitchSleepQuality)},
                {SLEEP_NOTES, Integer.toString(R.id.DisableSwitchSleepNotesInput)}};
    }

    /**
     * Returns a list of the strings that correspond to local account keys and the view IDs associated
     * with those IDs. If all Keys are listed as disabled, the view should be disabled.
     * @return A list of pairs of strings and integers
     */
    private static List<Pair<String[], Integer[]>> getSleepInvisibilityDependencies()
    {
        List<Pair<String[], Integer[]>> returnList = new ArrayList<>(5);
        returnList.add(new Pair<>(new String[] {SLEEP_HOURS}, new Integer[]{R.id.sleepHoursSleptTextView, R.id.sleepHoursSeekBar}));
        returnList.add(new Pair<>(new String[] {SLEEP_MINUTES}, new Integer[]{R.id.sleepMinutesSleptTextView, R.id.sleepMinutesSeekBar}));
        returnList.add(new Pair<>(new String[] {SLEEP_HOURS, SLEEP_MINUTES}, new Integer[]{R.id.sleepEndTimeTextView, R.id.sleepSpaceAfterMinutes,
        R.id.sleepSpaceAfterEndTime, R.id.sleepTimeSleptTextView}));

        returnList.add(new Pair<>(new String[] {SLEEP_QUALITY}, new Integer[]{R.id.sleepQualityTextView,
                R.id.sleepQualitySeekBar, R.id.sleepQualityNumberTextView, R.id.sleepSpaceAfterQuality}));

        returnList.add(new Pair<>(new String[] {SLEEP_NOTES}, new Integer[]{R.id.sleepNotesEditText}));

        return returnList;
    }

    /**
     * Returns a list of quartets for the sleep table. The first item is the ID of the view
     * that should be checked for visibility. The second is the position in the entry array to place
     * the value of element 4 in if the view is invisible. Element three is the type of view that the
     * element corresponds to
     * @return A list of pairs of integers
     */
    private static List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> getSleepViewDependencies()
    {
        List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> returnList = new ArrayList<>(3);
        returnList.add(new Quintet<>(R.id.sleepTimeSleptTextView, 5, VIEW_TYPE.SLEEP_DURATION, (String)null, LocalStorageAccessSleep.DURATION));
        returnList.add(new Quintet<>(R.id.sleepQualitySeekBar, 6, VIEW_TYPE.SEEKBAR, (String)null, LocalStorageAccessSleep.QUALITY));
        returnList.add(new Quintet<>(R.id.sleepNotesEditText, 7, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessSleep.NOTES));

        return returnList;
    }

    //Sleep getAnalysisSleepKeysAndColumns is not implemented because it does not make sense.
    //There are only two columns and they depend on different factors for whether or not they are
    //enabled

    /**
     * Returns whether the sleep duration is enabled based on the settings
     * @param context The current context, used to get user settings
     * @return A boolean for whether the duration is enabled or not
     */
    public static boolean isSleepDurationEnabled(Context context) {
        LocalAccount localAccount = LocalAccount.GetInstance();

        return (localAccount.getBoolean(context, SLEEP_HOURS, true)
                || localAccount.getBoolean(context, SLEEP_MINUTES, true));
    }

    /**
     * Returns whether the sleep quality is enabled.
     * @param context The current context. Needed for user settings
     * @return A boolean value for whether the quality field is enabled or not.
     */
    public static boolean isSleepQualityEnabled(Context context)
    {
        return LocalAccount.GetInstance().getBoolean(context, SLEEP_QUALITY, true);
    }

    //Medication module settings
    public static final String MED_BRAND = "MedBrandEnabled";
    public static final String MED_PRESCRIBER = "MedPrescriberEnabled";
    public static final String MED_DOSE = "MedDoseEnabled";
    public static final String MED_INSTRUCTIONS = "MedInstructionsEnabled";
    public static final String MED_WARNINGS = "MedWarningsEnabled";
    public static final String MED_NOTES = "MedNotesEnabled";

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the resource ID for that
     * medication column. For use in settings
     * @return The aforementioned 2D array
     */
    public static String[][] getAllMedicationKeysAndRIDs() {
        return new String[][]{
                {MED_BRAND, Integer.toString(R.id.DisableSwitchBrandName)},
                {MED_PRESCRIBER, Integer.toString(R.id.DisableSwitchPrescribersName)},
                {MED_DOSE, Integer.toString(R.id.DisableSwitchDoseAmount)},
                {MED_INSTRUCTIONS, Integer.toString(R.id.DisableSwitchConsumptionInstructions)},
                {MED_WARNINGS, Integer.toString(R.id.DisableSwitchConsumptionWarnings)},
                {MED_NOTES, Integer.toString(R.id.DisableSwitchMedicationNotesInput)}};
    }

    /**
     * Returns a list of the strings that correspond to local account keys and the view IDs associated
     * with those IDs. If all Keys are listed as disabled, the view should be disabled.
     * @return A list of pairs of strings and integers
     */
    private static List<Pair<String[], Integer[]>> getMedicationInvisibilityDependencies()
    {
        List<Pair<String[], Integer[]>> returnList = new ArrayList<>(7);
        returnList.add(new Pair<>(new String[] {MED_BRAND}, new Integer[]{R.id.MedicationEditBrandName}));
        returnList.add(new Pair<>(new String[] {MED_PRESCRIBER}, new Integer[]{R.id.MedicationPrescriberName}));
        returnList.add(new Pair<>(new String[] {MED_DOSE}, new Integer[]{R.id.MedicationDoseAmount}));
        returnList.add(new Pair<>(new String[] {MED_INSTRUCTIONS}, new Integer[]{R.id.MedicationInstructionEditText}));
        returnList.add(new Pair<>(new String[] {MED_WARNINGS}, new Integer[]{R.id.MedicationWarningsEditText}));

        returnList.add(new Pair<>(new String[] {MED_BRAND, MED_PRESCRIBER, MED_DOSE, MED_INSTRUCTIONS, MED_WARNINGS},
                new Integer[]{R.id.medicationSpaceAfterWarnings}));

        returnList.add(new Pair<>(new String[] {MED_NOTES}, new Integer[]{R.id.medicationDetailsEditText}));

        return returnList;
    }

    /**
     * Returns a list of quartets for the exercise table. The first item is the ID of the view
     * that should be checked for visibility. The second is the position in the entry array to place
     * the value of element 4 in if the view is invisible. Element three is the type of view that the
     * element corresponds to
     * @return A list of pairs of integers
     */
    private static List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> getMedicationViewDependencies()
    {
        List<Quintet<Integer, Integer, VIEW_TYPE, String, String>> returnList = new ArrayList<>(5);
        returnList.add(new Quintet<>(R.id.MedicationEditBrandName, 5, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessMedication.BRAND_NAME));
        returnList.add(new Quintet<>(R.id.MedicationPrescriberName, 6, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessMedication.PRESCRIBER));
        returnList.add(new Quintet<>(R.id.MedicationDoseAmount, 7, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessMedication.DOSE));
        returnList.add(new Quintet<>(R.id.MedicationInstructionEditText, 8, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessMedication.INSTRUCTIONS));
        returnList.add(new Quintet<>(R.id.MedicationWarningsEditText, 9, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessMedication.WARNINGS));
        returnList.add(new Quintet<>(R.id.medicationDetailsEditText, 10, VIEW_TYPE.TEXT_VIEW, "", LocalStorageAccessMedication.NOTES));

        return returnList;
    }

    //Integer value for the sleep module. Is the number of the hour 0-24 where entries before that hour
    //count as being for the previous day. Default is 8.
    public static final String SLEEP_INT_CUTOFF_HOUR = "SleepCutoffHour";

    public enum VIEW_TYPE {TEXT_VIEW, TEXT_VIEW_INT, SEEKBAR, SPINNER, SLEEP_DURATION};
}

