package com.rocket.biometrix.Login;

import android.content.Context;
import android.view.View;
import android.widget.Switch;

import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessDiet;
import com.rocket.biometrix.Database.LocalStorageAccessExercise;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.R;

/**
 * Created by TJ on 4/18/2016.
 * The key names for the settings used by local account
 *
 * Modified by TJ on 4/30/2016
 * Changed to SettingsHelper class. Now contains key names as well as methods to set or retrieve
 * settings based on keys, or retrieve a list of keys that correspond to resource IDs
 */
public class SettingsHelper {

    /**
     * Sets the status of all switches for the passed in 2D string array based on the default value
     * and the user's settings
     * @param view The view that holds the switch that needs to be modified
     * @param keysAndIDs A 2D array containing all keys and their ID, should be retrieved from
     *                   SettingsHelper as well
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
     *                   SettingsHelper as well
     */
    public static void storeSwitchValues(View view, String[][] keysAndIDs)
    {
        LocalAccount localAccount = LocalAccount.GetInstance();

        for (String[] stringArray : keysAndIDs) {
            boolean isChecked = ((Switch) view.findViewById(Integer.parseInt(stringArray[1]))).isChecked();
            localAccount.setBoolean(view.getContext(), stringArray[0], isChecked);
        }
    }

    //Module disable/enable settings
    public static final String MOOD_MODULE = "MoodModuleEnabled";
    public static final String SLEEP_MODULE = "SleepModuleEnable";
    public static final String EXERCISE_MODULE = "ExerciseModuleEnable";
    public static final String DIET_MODULE = "DietModuleEnable";
    public static final String MEDICATION_MODULE = "MedicationModuleEnable";

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
        
    
    //Exercise module settings
    public static final String EXERCISE_NAME = "ExerciseNameEnabled";
    public static final String EXERCISE_DURATION = "ExerciseDurationEnabled";
    public static final String EXERCISE_TYPE= "ExerciseTypeEnabled";
    public static final String EXERCISE_REPS_LAPS = "ExerciseRepsEnabled";
    public static final String EXERCISE_WEIGHT_INTENSITY = "ExerciseWeightEnabled";
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
                {EXERCISE_REPS_LAPS, Integer.toString(R.id.DisableSwitchExerciseRepsLaps)},
                {EXERCISE_WEIGHT_INTENSITY, Integer.toString(R.id.DisableSwitchExerciseWeightIntensity)},
                {EXERCISE_NOTES, Integer.toString(R.id.DisableSwitchExerciseNotesInput)}};
    }

    /**
     * Returns a 2 dimensional array that has a list of all keys along with the column they go with.
     * Returns only columns that are analyzed in analysis
     * @return The aforementioned 2D array
     */
    public static String[][] getAnalysisExerciseKeysAndColumns() {
        return new String[][]{
                {EXERCISE_DURATION, LocalStorageAccessExercise.MINUTES},
                {EXERCISE_REPS_LAPS, LocalStorageAccessExercise.REPS},
                {EXERCISE_REPS_LAPS, LocalStorageAccessExercise.LAPS},
                {EXERCISE_WEIGHT_INTENSITY, LocalStorageAccessExercise.WEIGHT},
                {EXERCISE_WEIGHT_INTENSITY, LocalStorageAccessExercise.INTY}};
    }
    
    //Mood module settings
    public static final String MOOD_DEP = "MoodDepEnabled";
    public static final String MOOD_ELEV = "MoodElevEnabled";
    public static final String MOOD_IRRITABLE = "MoodIrritableEnabled";
    public static final String MOOD_ANX = "MoodAnxEnabled";
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
                {MOOD_NOTES, Integer.toString(R.id.DisableSwitchMoodNotesInput)}};
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
                {MOOD_NOTES, LocalStorageAccessMood.NOTE}};
    }
    
    //Sleep module settings
    public static final String SLEEP_HOURS = "SleepHoursEnabled";
    public static final String SLEEP_MINUTES = "SleepMinutesEnabled";
    public static final String SLEEP_QUALITY = "SleepQualityEnabled";
    public static final String SLEEP_GEN_HEALTH = "SleepGenHealthEnabled";
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
                {SLEEP_GEN_HEALTH, Integer.toString(R.id.DisableSwitchGeneralHealth)},
                {SLEEP_NOTES, Integer.toString(R.id.DisableSwitchSleepNotesInput)}};
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
}
