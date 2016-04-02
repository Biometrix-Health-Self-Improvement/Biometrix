package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.widget.Toast;

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
    public static void getIDColumns(int colArray[], JSONObject jsonObject, Context context) throws ArrayIndexOutOfBoundsException
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
            Toast.makeText(context, "Unable to match backup data with local data", Toast.LENGTH_LONG).show();
        }
    }

}
