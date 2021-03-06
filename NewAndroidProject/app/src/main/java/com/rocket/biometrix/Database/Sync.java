package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.rocket.biometrix.Login.LocalAccount;

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
    public void syncDatabases()
    {
        if (!isNetworkAvailable(context) )
        {
            Toast.makeText(context, "No wifi available", Toast.LENGTH_LONG).show();
        }
        else if (LocalAccount.isLoggedIn())
        {
            //The Json object to hold all other json objects
            //Alternate names include the One JSON to rule them all...
            JSONObject masterJson = new JSONObject();

            //All tables and their primary keys
            String tableNames[] =
                    {LocalStorageAccessMood.TABLE_NAME,
                    LocalStorageAccessDiet.TABLE_NAME,
                    LocalStorageAccessExercise.TABLE_NAME,
                   LocalStorageAccessMedication.TABLE_NAME,
                   LocalStorageAccessSleep.TABLE_NAME};

            //Each type of status in the sync table
            Integer syncTypes[] = {LocalStorageAccess.SYNC_NEEDS_ADDED, LocalStorageAccess.SYNC_NEEDS_UPDATED,
                    LocalStorageAccess.SYNC_NEEDS_DELETED};

            //JSONObjects for each table
            JSONObject jsonArray[] = {new JSONObject(), new JSONObject(), new JSONObject(), new JSONObject(),
                    new JSONObject()};

            //Lists of columns for each table
            String columnLists[][] = {LocalStorageAccessMood.getColumns(),
                    LocalStorageAccessDiet.getColumns(),
                    LocalStorageAccessExercise.getColumns(),
                    LocalStorageAccessMedication.getColumns(),
                    LocalStorageAccessSleep.getColumns()};

            //Lists of primary keys for each table
            String primaryKeyColumnLists[][] =
                    {new String[] {LocalStorageAccessMood.LOCAL_MOOD_ID, LocalStorageAccessMood.WEB_MOOD_ID},
                            new String[] {LocalStorageAccessDiet.LOCAL_DIET_ID, LocalStorageAccessDiet.WEB_DIET_ID},
                            new String[] {LocalStorageAccessExercise.LOCAL_EXERCISE_ID, LocalStorageAccessExercise.WEB_EXERCISE_ID},
                            new String[] {LocalStorageAccessMedication.LOCAL_MEDICATION_ID, LocalStorageAccessMedication.WEB_MEDICATION_ID},
                            new String[] {LocalStorageAccessSleep.LOCAL_SLEEP_ID, LocalStorageAccessSleep.WEB_SLEEP_ID}};

            for(int i = 0; i < tableNames.length; ++i)
            {
                getAllIDInfo(tableNames[i], primaryKeyColumnLists[i][1], jsonArray[i]);

                for(int j = 0; j < syncTypes.length; ++j)
                {
                    if (syncTypes[j] == LocalStorageAccess.SYNC_NEEDS_DELETED)
                    {
                        getAllPendingInfoForDelete(tableNames[i], primaryKeyColumnLists[i][1], jsonArray[i], syncTypes[j]);
                    }
                    else
                    {
                        getAllPendingInfoOfType(tableNames[i], primaryKeyColumnLists[i][0],
                                columnLists[i], jsonArray[i], syncTypes[j]);
                    }
                }

                try
                {
                    masterJson.put(tableNames[i], jsonArray[i]);
                }
                catch(JSONException except)
                {
                    except.getMessage();
                }
            }

            new DatabaseConnect(this).execute(DatabaseConnectionTypes.SYNC_DATABASES, masterJson.toString(),
                    LocalAccount.GetInstance().GetToken());
        }
    }

    /**
     * Tries a database insertion on the web after adding the values to the sync table. If it
     * succeeds the sync table will have the entry removed.
     * @param responseClass The class that will handle the callback process.
     * @param rowToBeInserted The content values that were already pushed into the local database
     * @param tableName The name of the table for the operation
     */
    public void databaseInsertOrUpdateSyncTable(AsyncResponse responseClass, ContentValues rowToBeInserted, String tableName)
    {
        //Assumes that any user who is logged in wants their data backed up.
        //TODO: Local Account setting for turning off always backup?

        if (LocalAccount.isLoggedIn() )
        {
            int id = -1;
            String localIDName = null;
            String usernameColumnName = null;
            String dbConnectionType = null;

            switch (tableName)
            {
                case LocalStorageAccessDiet.TABLE_NAME:
                    id = LocalStorageAccessDiet.GetLastID(context);
                    localIDName = LocalStorageAccessDiet.LOCAL_DIET_ID;
                    usernameColumnName = LocalStorageAccessDiet.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.DIET_TABLE;
                    break;
                case LocalStorageAccessExercise.TABLE_NAME:
                    id = LocalStorageAccessExercise.GetLastID(context);
                    localIDName = LocalStorageAccessExercise.LOCAL_EXERCISE_ID;
                    usernameColumnName = LocalStorageAccessExercise.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.EXERCISE_TABLE;
                    break;
                case LocalStorageAccessMedication.TABLE_NAME:
                    id = LocalStorageAccessMedication.GetLastID(context);
                    localIDName = LocalStorageAccessMedication.LOCAL_MEDICATION_ID;
                    usernameColumnName = LocalStorageAccessMedication.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.MEDICATION_TABLE;
                    break;
                case LocalStorageAccessMood.TABLE_NAME:
                    id = LocalStorageAccessMood.GetLastID(context);
                    localIDName = LocalStorageAccessMood.LOCAL_MOOD_ID;
                    usernameColumnName = LocalStorageAccessMood.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.MOOD_TABLE;
                    break;
                case LocalStorageAccessSleep.TABLE_NAME:
                    id = LocalStorageAccessSleep.GetLastID(context);
                    localIDName = LocalStorageAccessSleep.LOCAL_SLEEP_ID;
                    usernameColumnName = LocalStorageAccessSleep.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.SLEEP_TABLE;
                    break;
            }


            //Adds the primary key of the field to the sync table along with the value marking it
            //needs to be added to the webdatabase
            LocalStorageAccess.getInstance(context).insertOrUpdateSyncTable(context,
                    tableName, id, -1, LocalStorageAccess.SYNC_NEEDS_ADDED);

            //Makes the change to the web database (which updates the sync table on success)
            rowToBeInserted.put(localIDName, id);
            rowToBeInserted.remove(usernameColumnName);

            String jsonToInsert = JsonCVHelper.convertToJSON(rowToBeInserted);

            if (!isNetworkAvailable(context) )
            {
                Toast.makeText(context, "No wifi available", Toast.LENGTH_LONG).show();
            }
            else {
                new DatabaseConnect(responseClass).execute(DatabaseConnectionTypes.INSERT_TABLE_VALUES, jsonToInsert,
                        LocalAccount.GetInstance().GetToken(), dbConnectionType);
            }
        }
    }

    /**
     * Tries a database update on the web after adding the values to the sync table. If it
     * succeeds the sync table will have the entry removed.
     * @param responseClass The class that will handle the callback process.
     * @param rowToBeUpdated The content values that were already pushed into the local database
     * @param id THe local id that corresponds to the entry to update
     * @param webid The web id that corresponds to the entry to update
     * @param tableName The name of the table for the operation
     */
    public void databaseUpdateOrUpdateSyncTable(AsyncResponse responseClass, ContentValues rowToBeUpdated,
                                                int id, int webid, String tableName)
    {
        //Assumes that any user who is logged in wants their data backed up.
        //TODO: Local Account setting for turning off always backup?
        if (LocalAccount.isLoggedIn() )
        {
            String usernameColumnName = null;
            String dbConnectionType = null;

            switch (tableName)
            {
                case LocalStorageAccessDiet.TABLE_NAME:
                    usernameColumnName = LocalStorageAccessDiet.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.DIET_TABLE;
                    break;
                case LocalStorageAccessExercise.TABLE_NAME:
                    usernameColumnName = LocalStorageAccessExercise.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.EXERCISE_TABLE;
                    break;
                case LocalStorageAccessMedication.TABLE_NAME:
                    usernameColumnName = LocalStorageAccessMedication.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.MEDICATION_TABLE;
                    break;
                case LocalStorageAccessMood.TABLE_NAME:
                    usernameColumnName = LocalStorageAccessMood.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.MOOD_TABLE;
                    break;
                case LocalStorageAccessSleep.TABLE_NAME:
                    usernameColumnName = LocalStorageAccessSleep.USER_NAME;
                    dbConnectionType = DatabaseConnectionTypes.SLEEP_TABLE;
                    break;
            }


            //Adds the primary key of the field to the sync table along with the value marking it
            //needs to be updated to the webdatabase
            LocalStorageAccess.getInstance(context).insertOrUpdateSyncTable(context,
                    tableName, id, webid, LocalStorageAccess.SYNC_NEEDS_UPDATED);

            rowToBeUpdated.remove(usernameColumnName);

            String jsonToInsert = JsonCVHelper.convertToJSON(rowToBeUpdated);

            if (!isNetworkAvailable(context) )
            {
                Toast.makeText(context, "No wifi available", Toast.LENGTH_LONG).show();
            }
            else {
                new DatabaseConnect(responseClass).execute(DatabaseConnectionTypes.UPDATE_TABLE_VALUES, jsonToInsert,
                        LocalAccount.GetInstance().GetToken(), dbConnectionType);
            }
        }
    }

    /**
     * Tries a database update on the web after adding the values to the sync table. If it
     * succeeds the sync table will have the entry removed.
     * @param responseClass The class that will handle the callback process.
     * @param webid The web id that corresponds to the entry to update
     * @param tableName The name of the table for the operation
     */
    public void databaseDeleteOrUpdateSyncTable(AsyncResponse responseClass, int id, int webid, String tableName)
    {
        //Assumes that any user who is logged in wants their data backed up.
        //TODO: Local Account setting for turning off always backup?
        if (LocalAccount.isLoggedIn() )
        {
            String webIDColumnName = null;
            String dbConnectionType = null;

            switch (tableName)
            {
                case LocalStorageAccessDiet.TABLE_NAME:
                    webIDColumnName = LocalStorageAccessDiet.WEB_DIET_ID;
                    dbConnectionType = DatabaseConnectionTypes.DIET_TABLE;
                    break;
                case LocalStorageAccessExercise.TABLE_NAME:
                    webIDColumnName = LocalStorageAccessExercise.WEB_EXERCISE_ID;
                    dbConnectionType = DatabaseConnectionTypes.EXERCISE_TABLE;
                    break;
                case LocalStorageAccessMedication.TABLE_NAME:
                    webIDColumnName = LocalStorageAccessMedication.WEB_MEDICATION_ID;
                    dbConnectionType = DatabaseConnectionTypes.MEDICATION_TABLE;
                    break;
                case LocalStorageAccessMood.TABLE_NAME:
                    webIDColumnName = LocalStorageAccessMood.WEB_MOOD_ID;
                    dbConnectionType = DatabaseConnectionTypes.MOOD_TABLE;
                    break;
                case LocalStorageAccessSleep.TABLE_NAME:
                    webIDColumnName = LocalStorageAccessSleep.WEB_SLEEP_ID;
                    dbConnectionType = DatabaseConnectionTypes.SLEEP_TABLE;
                    break;
            }

            //Adds the primary key of the field to the sync table along with the value marking it
            //needs to be updated to the webdatabase
            LocalStorageAccess.getInstance(context).insertOrUpdateSyncTable(context,
                    tableName, id, webid, LocalStorageAccess.SYNC_NEEDS_DELETED);

            ContentValues contentValues = new ContentValues();
            contentValues.put(webIDColumnName, webid);

            String jsonToInsert = JsonCVHelper.convertToJSON(contentValues);

            if (!isNetworkAvailable(context) )
            {
                Toast.makeText(context, "No wifi available", Toast.LENGTH_LONG).show();
            }
            else {
                new DatabaseConnect(responseClass).execute(DatabaseConnectionTypes.DELETE_TABLE_VALUES, jsonToInsert,
                        LocalAccount.GetInstance().GetToken(), dbConnectionType);
            }
        }
    }

    /**
     * Updates the passed in jsonObject with all of the data corresponding to an add in the
     * @param tableName The name of the table to pull from
     * @param keyName The name of the localID/primary key
     * @param columns All of the columns that are to be returned (should be all of the tables columns
     *                for this method.
     * @param jsonObject The JSONObject to update with the returned values
     * @param syncType The type of operation to check the sync table for
     */
    private void getAllPendingInfoOfType(String tableName, String keyName, String columns[],
                                         JSONObject jsonObject, int syncType)
    {
        Cursor cursor = LocalStorageAccess.selectPendingEntries(context, tableName,
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
            switch (syncType)
            {
                case LocalStorageAccess.SYNC_NEEDS_ADDED:
                    jsonObject.put("Insert", opJson);
                    break;
                case LocalStorageAccess.SYNC_NEEDS_UPDATED:
                    jsonObject.put("Update", opJson);
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

        cursor.close();
    }

    /**
     * Updates the passed in jsonObject with all of the data corresponding to an add in the
     * @param tableName The name of the table to pull from
     * @param webKeyColumnName The name of the webkey column that is being referenced. e.g. webdietID
     * @param jsonObject The JSONObject to update with the returned values
     * @param syncType The type of operation to check the sync table for
     */
    private void getAllPendingInfoForDelete(String tableName, String webKeyColumnName, JSONObject jsonObject, int syncType)
    {
        Cursor cursor = LocalStorageAccess.selectAllSyncDeletions(context, tableName, true);

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
                        entryJSON.put(webKeyColumnName, cursor.getString(i));
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
            if (syncType == LocalStorageAccess.SYNC_NEEDS_DELETED)
            {
                jsonObject.put("Delete", opJson);
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

        cursor.close();
    }

    /**
     * Retrieves all of the primary key info (web) for entries on the local database, including
     * entries that have been deleted locally but not on the web.
     * @param tableName The name of the table to pull from
     * @param webKeyColumn The name of the web primary key
     * @param jsonObject The json object to store all of the return data in
     *
     */
    private void getAllIDInfo(String tableName, String webKeyColumn, JSONObject jsonObject)
    {
        Cursor cursor = LocalStorageAccess.selectAllEntries(context, tableName, null, new String[]{webKeyColumn}, true);

        JSONObject tableJson = new JSONObject();
        Integer curRow = 0;
        boolean done = false;
        boolean switchToSync = false;

        while (!done) {
            if (cursor != null && cursor.moveToFirst()) {
                int numColumns = cursor.getColumnCount();

                //Creates a json object for each row in the returned results, and adds it to
                //the entryJson object
                while (!cursor.isAfterLast()) {
                    JSONObject entryJSON = new JSONObject();

                    try {

                        entryJSON.put(webKeyColumn, cursor.getString(0));

                        tableJson.put(curRow.toString(), entryJSON);
                    } catch (JSONException except) {
                        except.getMessage();
                    }
                    cursor.moveToNext();
                    ++curRow;
                }
            }

            cursor.close();
            if (switchToSync == false)
            {
                switchToSync = true;
                cursor = LocalStorageAccess.selectAllSyncDeletions(context, tableName, true);
            }
            else
            {
                done = true;
            }
        }

        try
        {
            jsonObject.put("PullData", tableJson);
        }
        catch(JSONException except)
        {
            except.getMessage();
        }


    }

    /**
     * The asynchronous method that is called when the database connection is closed.
     * @param result The return string from the webserver. Should usually be JSON encoded
     */
    @Override
    public void processFinish(String result)
    {
        JsonCVHelper.processServerJsonString(result, context, "Could not sync databases");
    }

    /**
     * A check for network availability? Taken from here
     * http://stackoverflow.com/questions/5474089/how-to-check-currently-internet-connection-is-available-or-not-in-android
     * TODO: Make use of this if it works. Currently untested...
     * @param context Current context
     * @return True if available, false otherwise
     */
    public static boolean isNetworkAvailable(Context context)
    {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }

        return connected;
    }
}
