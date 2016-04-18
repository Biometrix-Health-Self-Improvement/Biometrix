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
    //public static
    public CursorPair mCursorPair;
    public int mRows = 0; //index
    public String mTitleStrings[];
    public  String mTimeStrings[];

    //
    public CursorHelper(CursorPair cp, String titleCol, String timeCol) {
        mCursorPair = cp;

        if (mCursorPair.query.moveToFirst()) {
            while (!mCursorPair.query.isAfterLast()) {
               mTitleStrings[mRows] = mCursorPair.query.getString( mCursorPair.query.getColumnIndex(titleCol) );
                mTimeStrings[mRows] = mCursorPair.query.getString( mCursorPair.query.getColumnIndex(timeCol) );
                    mRows++; //count the rows (Same thing as cursor's count method)
                mCursorPair.query.moveToNext();
            }
        }
        mCursorPair.query.close();
    }

    //use cursor public abstract String[] getColumnNames () to error check
}
