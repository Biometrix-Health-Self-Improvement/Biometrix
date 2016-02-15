package com.rocket.biometrix.Common;

/**
 * Created by tannalynn on 1/22/2016.
 */

import android.view.View;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//Code in the praise of the keeper of the star fields
public class StringDateTimeConverter
{
    //Call various helper methods to make the  edit texts
    // go from human readable to more Database friendly formats
    public static String fixDate (String date){
        date = removeChars(date, 12); //Date:_Fri,__ = 12 characters.
        date = convertDateString(date);
        return date;
    }
    //Call various helper methods to make the  edit texts
    // go from human readable to more Database friendly formats
    public static String fixTime (String time){
        time = removeChars(time, 7); //Time:__ = 7 characters
        time = convertTimeString(time);
        return time;
    }


    //HElper function to convert CalendarView date strings to TEXT as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS").
    public static String convertCalDateString (String[] dayt){

        StringBuilder builder = new StringBuilder();
        for(String s : dayt) {
            builder.append(s);
            builder.append('/');
        }
        //Making the passed in date a string (Year month day)
        String yearMonthDay  = builder.toString();

        //use SimpleDateFormat to first parse() String to Date and then format() Date to String
        Date date = null;
        try {
            //make date time and parse string passed in dayt
            date = new SimpleDateFormat("yyyy/MM/dd").parse(yearMonthDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Make string with right SQL date format.
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(date);

        //cut out time information
        StringBuffer buf = new StringBuffer(formattedDate);
        buf.delete(10,buf.length());

        return buf.toString();
    }

    //Example feed me with: GetStringFromEditText(EditText)v.findViewById(R.id.ex_title);
    public static String GetStringFromEditText(View v) {
        String endResult = "ERROR in GetStringFromEditText: Resource ID does not exist";
        if (v != null)
            try {
                final EditText et = (EditText) v;
                endResult = et.getText().toString();
            }//end try
            catch (IllegalArgumentException | ClassCastException exceptionName) {
                endResult = "ERROR in GetStringFromEditText: try block";
            }
        return endResult;
    }

    //String cleaner, just removes a number of characters from the front of the string.
    //http://docs.oracle.com/javase/7/docs/api/java/lang/StringBuffer.html#delete%28int,%20int%29
    private static String removeChars(String s, int num) {
        StringBuffer buf = new StringBuffer(s);
        int front = 0;
        //Simple error checking. To avoid exception below (this is just a wrapper function around String Buffer's delete() function)
        //StringIndexOutOfBoundsException - if start is negative, greater than length(), or greater than end.
        if (num < s.length()) {
            buf.delete(front, num - 1);
        }
        return buf.toString();
    }

    //HElper function to convert datetime strings to TEXT as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS").
    public static String convertDateString (String date){
        //use SimpleDateFormat to first parse() String to Date and then format() Date to String
        Date date2 = null;
        try {
            //make date time and parse string passed in dayt
            date2 = new SimpleDateFormat("MM/dd/yyyy").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Make string with right SQL date format.
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(date2);

        //cut out time information
        StringBuffer buf = new StringBuffer(formattedDate);
        buf.delete(10,buf.length());

        return buf.toString();
    }


    //Helper function to convert 07:30 PM (wall clock) to military time
    public static String convertTimeString (String time){

        //Locale to prevent non USA zone breakzzz
        SimpleDateFormat sDF=new SimpleDateFormat("hh:mm aa", Locale.getDefault());

        Date date = null;
        try {
            //parse string passed in tyme
            date = sDF.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Make string with right SQL date format.
        String formattedTime = new SimpleDateFormat("HH:mm").format(date);

        return formattedTime;
    }

}
