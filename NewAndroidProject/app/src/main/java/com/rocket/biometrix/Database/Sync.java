package com.rocket.biometrix.Database;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TJ on 4/16/2016.
 * This class is designed to hold the methods that will be used when making synchronization requests
 * to the webdatabase
 */
public class Sync implements AsyncResponse
{
    private Context context;

    /**
     * Constructor for the class
     * @param curContext The context that will be used to make toast and do database connections
     */
    public Sync(Context curContext)
    {
        context = curContext;
    }

    /**
     * Performs a sync between the local database and the webdatabase
     */
    public void SyncDatabases()
    {
        //TestChanges();
        JSONObject masterJson = new JSONObject();

        //All tables and their primary keys
        String tableNamesAndID[][] =
                { new String[] {LocalStorageAccessMood.TABLE_NAME, LocalStorageAccessMood.LOCAL_MOOD_ID},
                new String[] {LocalStorageAccessDiet.TABLE_NAME, LocalStorageAccessDiet.LOCAL_DIET_ID},
                new String[] {LocalStorageAccessExercise.TABLE_NAME, LocalStorageAccessExercise.LOCAL_EXERCISE_ID},
                new String[] {LocalStorageAccessMedication.TABLE_NAME, LocalStorageAccessMedication.LOCAL_MEDICATION_ID},
                new String[] {LocalStorageAccessSleep.TABLE_NAME, LocalStorageAccessSleep.LOCAL_SLEEP_ID}};

        Integer syncTypes[] = {LocalStorageAccess.SYNC_NEEDS_ADDED, LocalStorageAccess.SYNC_NEEDS_UPDATED,
                LocalStorageAccess.SYNC_NEEDS_DELETED};

        JSONObject jsonArray[] = {new JSONObject(), new JSONObject(), new JSONObject(), new JSONObject(),
                new JSONObject()};

        String columnLists[][] = {LocalStorageAccessMood.getColumns(),
                LocalStorageAccessDiet.getColumns(),
                LocalStorageAccessExercise.getColumns(),
                LocalStorageAccessMedication.getColumns(),
                LocalStorageAccessSleep.getColumns()};

        String deleteColumnLists[][] =
                {new String[] {LocalStorageAccessMood.LOCAL_MOOD_ID, LocalStorageAccessMood.WEB_MOOD_ID},
                        new String[] {LocalStorageAccessDiet.LOCAL_DIET_ID, LocalStorageAccessDiet.WEB_DIET_ID},
                        new String[] {LocalStorageAccessExercise.LOCAL_EXERCISE_ID, LocalStorageAccessExercise.WEB_EXERCISE_ID},
                        new String[] {LocalStorageAccessMedication.LOCAL_MEDICATION_ID, LocalStorageAccessMedication.WEB_MEDICATION_ID},
                        new String[] {LocalStorageAccessSleep.LOCAL_SLEEP_ID, LocalStorageAccessSleep.WEB_SLEEP_ID}};

        for(int i = 0; i < tableNamesAndID.length; ++i)
        {
            for(int j = 0; j < syncTypes.length; ++j)
            {
                if (syncTypes[j] == LocalStorageAccess.SYNC_NEEDS_DELETED)
                {
                    GetAllPendingInfoOfType(tableNamesAndID[i][0], tableNamesAndID[i][1],
                            deleteColumnLists[i], jsonArray[i], syncTypes[j]);
                }
                else
                {
                    GetAllPendingInfoOfType(tableNamesAndID[i][0], tableNamesAndID[i][1],
                            columnLists[i], jsonArray[i], syncTypes[j]);
                }

                try
                {
                    masterJson.put(tableNamesAndID[i][0], jsonArray[i]);
                }
                catch(JSONException except)
                {
                    except.getMessage();
                }
            }
        }

        //String test = masterJson.toString();
    }

    /*
    public void TestChanges()
    {
        LocalStorageAccess.getInstance(context).insertOrUpdateSyncTable(context, LocalStorageAccessMood.TABLE_NAME,
                4, LocalStorageAccess.SYNC_NEEDS_UPDATED);
        LocalStorageAccess.getInstance(context).insertOrUpdateSyncTable(context, LocalStorageAccessMood.TABLE_NAME,
                5, LocalStorageAccess.SYNC_NEEDS_DELETED);
    }*/

    /**
     * Updates the passed in jsonObject with all of the data corresponding to an add in the
     * @param tableName The name of the table to pull from
     * @param keyName The name of the localID/primary key
     * @param columns All of the columns that are to be returned (should be all of the tables columns
     *                for this method.
     * @param jsonObject The JSONObject to update with the returned values
     * @param syncType The type of operation to check the sync table for
     */
    private void GetAllPendingInfoOfType(String tableName, String keyName, String columns[],
                                      JSONObject jsonObject, int syncType)
    {
        LocalStorageAccess localDB = LocalStorageAccess.getInstance(context);
        Cursor cursor = localDB.selectPendingEntries(context, tableName,
                keyName, columns, true, syncType);

        JSONObject opJson = new JSONObject();
        Integer curRow = 0;

        if(cursor.moveToFirst())
        {
            int numColumns = cursor.getColumnCount();

            //Creates a json object for each row in the returned results, and adds it to
            //the entryJson object
            while(!cursor.isAfterLast())
            {
                JSONObject entryJSON = new JSONObject();

                try
                {
                    //Adds each column into the entry JSON object
                    for (int i = 0; i < numColumns; ++i)
                    {
                        //e.g. LocalMoodID=2
                        entryJSON.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                    opJson.put(curRow.toString(), entryJSON);
                }
                catch (JSONException except)
                {
                    except.getMessage();
                }
                cursor.moveToNext();
                ++curRow;
            }
        }

        try
        {
            //Adds a row count value to the opJson object so that the indices can be determined
            opJson.put("NumRows", curRow);

            switch (syncType)
            {
                case LocalStorageAccess.SYNC_NEEDS_ADDED:
                    jsonObject.put("Add", opJson);
                    break;
                case LocalStorageAccess.SYNC_NEEDS_UPDATED:
                    jsonObject.put("Updated", opJson);
                    break;
                case LocalStorageAccess.SYNC_NEEDS_DELETED:
                    jsonObject.put("Deleted", opJson);
                    break;
                default:
                    jsonObject.put("Error", "Invalid sync type chosen");
                    break;
            }

        }
        catch(JSONException except)
        {
            try
            {
                //This should never fail.. But just to make Android Studio happy...
                jsonObject.put("Error", except.getMessage());
            }
            catch (JSONException e)
            {
                e.getMessage();
            }
        }

        localDB.close();
        cursor.close();
    }

    /**
     * The asynchronous method that is called when the database connection is closed.
     * @param result The return string from the webserver. Should usually be JSON encoded
     */
    @Override
    public void processFinish(String result)
    {
        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
    }
}
