package com.rocket.biometrix.MoodModule;

/**
 * Created by tannalynn on 1/22/2016.
 */
public class MoodData {

    String date, time, notes;
    int dep, elev, irr, anx;

    public MoodData(String _date, String _time, int _dep, int _elev, int _irr, int _anx, String _note)
    {
        date = _date;
        time = _time;
        notes = _note;
        dep = _dep;
        elev = _elev;
        anx = _anx;
        irr = _irr;
    }

}
