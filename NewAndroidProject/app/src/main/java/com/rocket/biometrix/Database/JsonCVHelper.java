package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rocket.biometrix.Login.LocalAccount;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TJ on 4/2/2016.
 * Contains helper methods for the conversions back and forth between content values and Json that
 * is returned by the Webserver
 */
public class JsonCVHelper
{
    /**
     * Converts a ContentValues into JSON to prepare it for passing to the webserver and then the
     * database
     * @param contentValues The content values to convert
     * @return A string object that is formatted as JSON
     */
    public static String convertToJSON(ContentValues contentValues)
    {
        StringBuilder jsonBuilder = new StringBuilder(contentValues.toString());

        //Reads backwards through the string. Essentially, there needs to be a , between every element
        //so this looks for = and then sets the next whitespace before that = to a , and then adds a new
        //space
        //Every key and value also needs to be quoted in the JSON pair (note this means all values
        //are originally interpreted as strings, SQL should be able to handle the type conversion)
        boolean readEqual = false;
        jsonBuilder.append('"');

        for (int i = jsonBuilder.length() - 1; i > 0; --i)
        {
            if (jsonBuilder.charAt(i) == '=')
            {
                readEqual = true;

                jsonBuilder.insert(i+1, '"');
                jsonBuilder.insert(i, '"');
            }
            else if (jsonBuilder.charAt(i) == ' ' && readEqual)
            {
                readEqual = false;
                jsonBuilder.setCharAt(i, ',');
                jsonBuilder.insert(i+1, ' ');

                jsonBuilder.insert(i+2, '"');
                jsonBuilder.insert(i, '"');
            }
        }

        jsonBuilder.insert(0, '"');

        //Json strings start with { and end with }
        jsonBuilder.insert(0, '{');
        jsonBuilder.append('}');

        //Json strings have : instead of =, so make that replacement
        //Also, "" means an empty string so it can be null instead (blank in json)
        return jsonBuilder.toString().replace('=', ':');
    }

    /**
     * Processes the string returned by the server and attempts to convert it into Json. Displays
     * error messages if the parsing fails, the return has an error, or the return is not verified.
     * @param jsonString The string variable that is to be converted into Json
     * @param context The context to display the toasts when the operation fails
     * @param unverifiedMessage The message to display if the Verified flag is not set by the server
     *                          upon return
     * @return The JSONObject that corresponds to the string passed in. If the parse failed, or the
     * message is unverified, this returns null instead.
     */
    public static JSONObject processServerJsonString(String jsonString, Context context, String unverifiedMessage)
    {
        JSONObject jsonObject;

        //Tries to parse the returned result as a json object.
        try
        {
            jsonObject = new JSONObject(jsonString);
        }
        catch (JSONException jsonExcept)
        {
            jsonObject = null;
        }

        //If the return could not be parsed, then it was not a successful login
        if (jsonObject == null)
        {
            Toast.makeText(context, "Something went wrong with the server's return", Toast.LENGTH_LONG).show();
        }
        else
        {
            try
            {
                if (jsonObject.has("Error"))
                {
                    Toast.makeText(context, jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                    jsonObject = null;
                }
                //If the operation succeeded
                else if ((Boolean)jsonObject.get("Verified") )
                {

                }
                else
                {
                    jsonObject = null;
                    Toast.makeText(context, unverifiedMessage, Toast.LENGTH_LONG).show();
                }
            }
            catch (JSONException jsonExcept)
            {
                jsonObject = null;
                Toast.makeText(context, "Unable to retrieve needed data from server's return", Toast.LENGTH_LONG).show();
            }
        }

        return jsonObject;
    }

    /**
     * Returns the number in the webserver ID column in element 1 of the array and the number in the
     * local ID column in element 0 of the array. Returns -1s in both if there is an error
     * @param colArray An array of exactly two integers that corresponds to the local and web IDs
     *                 respectively
     * @param jsonObject The json object to pull the IDs out of
     */
    public static void getIDColumns(int colArray[], JSONObject jsonObject)
    {
        if(colArray.length != 2)
        {
            throw new ArrayIndexOutOfBoundsException("Called getIDColumns with an array that does not have exactly 2 values");
        }

        try
        {
            colArray[0] = jsonObject.getJSONObject("Row").getInt("Local");
            colArray[1] = jsonObject.getJSONObject("Row").getInt("Web");
        }catch (JSONException except)
        {
            colArray[0] = -1;
            colArray[0] = -1;
        }
    }

    /**
     * Performs the needed processes on the json object that is returned after a sync is called
     * (e.g. updates the web IDs for columns that were just inserted)
     * @param jsonObject The object to process. This should be the json object that is returned
     *                   by the webserver
     * @param context The current context, this is needed since this operation does
     */
    public static void processSyncJsonReturn(JSONObject jsonObject, Context context)
    {
        int num_elements = 0;

        try
        {
            num_elements = jsonObject.getInt("NumElements");
        }
        catch (JSONException except)
        {
            except.getMessage();
        }

        for(int i = 0; i < num_elements; ++i)
        {
            try
            {
                JSONObject opJson = jsonObject.getJSONObject(Integer.toString(i));

                String tableName = opJson.getString("Table");
                String operation = opJson.getString("Operation");

                JSONObject resultJson = opJson.getJSONObject("Results");

                switch(operation)
                {
                    case "Insert":
                        HandleInsertSyncData(resultJson, tableName, context);
                        break;
                    case "Update":
                        HandleUpdateSyncData(resultJson, tableName, context);
                        break;
                    case "Delete":
                        HandleDeleteSyncData(resultJson, tableName, context);
                        break;
                    case "PullData":
                        HandlePulledData(resultJson, tableName, context);
                        break;
                    default:
                        break;
                }
            }
            catch(JSONException except)
            {
                except.getMessage();
            }
        }
    }

    /**
     * Calls the needed update methods to remove the inserts from the sync table as well as to
     * update the web ID references that they have on the local database
     * @param insertStatus The JSON object that contains all of the data for the insert
     * @param tableName The name of the table that the insert was performed on
     * @param context The current context, needed due to database operations.
     */
    private static void HandleInsertSyncData(JSONObject insertStatus, String tableName, Context context)
    {
        int idRay[] = new int[2];
        JsonCVHelper.getIDColumns(idRay, insertStatus);

        switch(tableName)
        {
            case LocalStorageAccessMood.TABLE_NAME:
                LocalStorageAccessMood.updateWebIDReference(idRay[0], idRay[1], context, false);
                break;
            case LocalStorageAccessExercise.TABLE_NAME:
                LocalStorageAccessExercise.updateWebIDReference(idRay[0], idRay[1], context, false);
                break;
            case LocalStorageAccessDiet.TABLE_NAME:
                LocalStorageAccessDiet.updateWebIDReference(idRay[0], idRay[1], context, false);
                break;
            case LocalStorageAccessMedication.TABLE_NAME:
                LocalStorageAccessMedication.updateWebIDReference(idRay[0], idRay[1], context, false);
                break;
            case LocalStorageAccessSleep.TABLE_NAME:
                LocalStorageAccessSleep.updateWebIDReference(idRay[0], idRay[1], context, false);
                break;
            default:
                break;
        }
    }

    /**
     * Calls the needed update methods to remove the updates from the sync table if they succeeded
     * @param updateStatus The JSON object that contains information about the update
     * @param tableName The name of the table that the insert was performed on
     * @param context The current context, needed due to database operations.
     */
    private static void HandleUpdateSyncData(JSONObject updateStatus, String tableName, Context context)
    {
        if (updateStatus.has("Error"))
        {
            try
            {
                //Log.i("UpdateSyncError", updateStatus.getString("Error"));
                updateStatus.getString("Error");
            }
            catch(JSONException except)
            {
                except.getMessage();
            }
        }
        else
        {
            try
            {
                LocalStorageAccess.getInstance(context).deleteEntryFromSyncTable(context, tableName,
                        updateStatus.getInt("WebKey"), false);
            }
            catch(JSONException except)
            {
                except.getMessage();
            }
        }
    }

    /**
     * Calls the needed update methods to remove the deletes from the sync table if they succeeded
     * @param deleteStatus The JSON object that contains information about the update
     * @param tableName The name of the table that the insert was performed on
     * @param context The current context, needed due to database operations.
     */
    private static void HandleDeleteSyncData(JSONObject deleteStatus, String tableName, Context context)
    {
        if (deleteStatus.has("Error"))
        {
            try
            {
                //Log.i("UpdateSyncError", updateStatus.getString("Error"));
                deleteStatus.getString("Error");
            }
            catch(JSONException except)
            {
                except.getMessage();
            }
        }
        else
        {
            try
            {
                LocalStorageAccess.getInstance(context).deleteEntryFromSyncTable(context, tableName,
                        deleteStatus.getInt("WebKey"), false);
            }
            catch(JSONException except)
            {
                except.getMessage();
            }
        }
    }

    /**
     * Calls the needed update methods to remove the inserts from the sync table as well as to
     * update the web ID references that they have on the local database
     * @param pulledData The JSON object that contains all of the data that now needs to be inserted
     * @param tableName The name of the table that the insert was performed on
     * @param context The current context, needed due to database operations.
     */
    private static void HandlePulledData(JSONObject pulledData, String tableName, Context context)
    {
        if (pulledData.has("Error"))
        {
            try
            {
                //Log.i("UpdateSyncError", updateStatus.getString("Error"));
                pulledData.getString("Error");
            }
            catch(JSONException except)
            {
                except.getMessage();
            }
        }
        else
        {
            try
            {
                String unneededColumn  = "";

                String username = LocalAccount.DEFAULT_NAME;

                if (LocalAccount.isLoggedIn())
                {
                    username = LocalAccount.GetInstance().GetUsername();
                }

                switch (tableName)
                {
                    case LocalStorageAccessDiet.TABLE_NAME:
                        unneededColumn = LocalStorageAccessDiet.LOCAL_DIET_ID;
                        break;
                    case LocalStorageAccessExercise.TABLE_NAME:
                        unneededColumn = LocalStorageAccessExercise.LOCAL_EXERCISE_ID;
                        break;
                    case LocalStorageAccessMedication.TABLE_NAME:
                        unneededColumn = LocalStorageAccessMedication.LOCAL_MEDICATION_ID;
                        break;
                    case LocalStorageAccessMood.TABLE_NAME:
                        unneededColumn = LocalStorageAccessMood.LOCAL_MOOD_ID;
                        break;
                    case LocalStorageAccessSleep.TABLE_NAME:
                        unneededColumn = LocalStorageAccessSleep.LOCAL_SLEEP_ID;
                        break;
                }

                int numRows = pulledData.getInt("NumRows");
                int numCols = pulledData.getInt("NumColumns");

                for(int i = 0; i < numRows; ++i)
                {
                    ContentValues rowToBeInserted = new ContentValues();

                    JSONObject rowJson = pulledData.getJSONObject(Integer.toString(i));

                    for(int j = 0; j < numCols; ++j)
                    {
                        JSONObject colJson = rowJson.getJSONObject(Integer.toString(j));
                        String columnName = colJson.getString("ColumnName");

                        if (!columnName.equals(unneededColumn) && !columnName.equals("UserID"))
                        {
                            String value = colJson.getString("Value");

                            if (columnName.equals(LocalStorageAccessSleep.DURATION) )
                            {
                                value = trimDurationString(value);
                            }
                            else if(columnName.equals(LocalStorageAccessSleep.TIME) || columnName.equals(LocalStorageAccessExercise.TIME) ||
                                    columnName.equals(LocalStorageAccessMedication.TIME) || columnName.equals(LocalStorageAccessMood.TIME))
                            {
                                value = convertTimeString(value);
                            }

                            rowToBeInserted.put(columnName, value);
                        }
                    }

                    rowToBeInserted.put(LocalStorageAccessMood.USER_NAME, username);
                    LocalStorageAccess.getInstance(context).safeInsert(tableName, null, rowToBeInserted);

                }

            }
            catch (JSONException except) {
                except.getMessage();
            }
        }

    }

    /**
     * Trims the duration string by removing the leading zero if it exists
     * @param durationString String to trim
     * @return The trimmed string
     */
    private static String trimDurationString(String durationString)
    {
        String returnString;

        if (durationString.charAt(0) == '0')
        {
            returnString = durationString.substring(1);
        }
        else
        {
            returnString = durationString;
        }

        return returnString;
    }

    /**
     * Converts a time string from the format used by SQL server (on the webdatabase) to the format
     * used by the local database (e.g. 14:00 to 2:00 PM)
     * @param timeString The string to convert
     * @return The converted string
     */
    private static String convertTimeString(String timeString)
    {
        String returnString;
        String stringSuffix;

        String hourPart = timeString.substring(0, 2);
        String minutePart = timeString.substring(3, 5);
        Integer hourInt = Integer.parseInt(hourPart);

        if (hourInt > 11)
        {
            stringSuffix = " PM";

            if (hourInt != 12) {
                hourInt -= 12;
            }
        }
        else
        {
            stringSuffix = " AM";
            if (hourInt == 0)
            {
                hourInt += 12;
            }
        }

        returnString = hourInt.toString() + ":" + minutePart + stringSuffix;

        return returnString;
    }
}
