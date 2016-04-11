package com.rocket.biometrix.EditPastEntries;

/**
 * Created by JP on 2/23/2016.
 * Helper class for editing past entries,
 * this class will parse a cursor to strings given a
 * context, Column Names, strings to sniff for.
 *
 * The point of this is to feed the decorator in MyEntryCandiesRecyclerViewAdapter with simple strings
 * to draw the UI 'Candies'/ Cards / 'Entries'
 */
public class CursorHelper {
    //TODO: Write statics that return primitives from the specified cursor pair 'signal';;; used to fill UI elements
    //public static

    public static String getType(CursorPair cp) {
        String typeString = "FAILURE";

        return typeString;
    }
}
