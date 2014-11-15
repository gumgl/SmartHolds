package com.example.android.bluetoothlegatt;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Guillaume on 2014-09-28.
 */
public class Route implements Serializable {
    public String name = "";
    public Date date_set;
    public int setter_id;
    public int setter_grade;

    public String start_addr;
    public String end_addr;

    public int attempts = 0;
    public int sends = 0;

    public int rssi = 0;

    int mAverageAttempts;
    int mAverageStars;
    int mAverageTime;
    float mAverageDifficulty;

    public Route(String name, Date date_set, int setter_grade, int setter_id, String start_addr, String end_addr, int averageAttempts, int averageStars, int averageTime, float averageDifficulty) {
        this.name = name;
        this.date_set = date_set;
        this.setter_id = setter_id;
        this.setter_grade = setter_grade;
        this.start_addr = start_addr;
        this.end_addr = end_addr;
        mAverageAttempts = averageAttempts;
        mAverageStars = averageStars;
        mAverageTime = averageTime;
        mAverageDifficulty = averageDifficulty;

    }

    public void addAttempt() {
        attempts ++;
    }

    public void addSend() {
        sends ++;
    }
    public String toString(){
        return name;
    }

}

