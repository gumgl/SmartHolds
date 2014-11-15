package com.example.android.bluetoothlegatt;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by desjardj on 28/09/2014.
 */
public class Player {
    private int nbAttempts;
    private Calendar startTime;
    private Calendar endTime;
    private Route mRoute;
    static private Player mPlayer = null;

    static public Player getInstance()
    {
        if (mPlayer == null)
        {
            mPlayer = new Player();
        }
        return mPlayer;
    }

    public void setCurrentRoute(Route route)
    {
        mRoute = route;
    }
    public void startRoute()
    {
        if (nbAttempts == 0)
        {
            startTime = Calendar.getInstance();
        }
        nbAttempts++;
    }
    public void endRoute()
    {
        endTime = Calendar.getInstance();
        nbAttempts = 0;
    }

    public Route getRoute(){return mRoute;}

    public float getTimeElapsedInSeconds()
    {
        float millis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        return millis / (1000);
    }

    public int getNbAttempts()
    {
        return nbAttempts;
    }

    private void Player(){};
}
