package com.example.android.bluetoothlegatt;

import java.util.Date;

public class Player {
    public String name = "";
    public Date date_set;
    public int setter_id;
    public int setter_grade;

    public String start_addr;
    public String end_addr;

    public int rssi = 0;

    public Player(String name, Date date_set, int setter_grade, int setter_id, String start_addr, String end_addr) {
        this.name = name;
        this.date_set = date_set;
        this.setter_id = setter_id;
        this.setter_grade = setter_grade;
        this.start_addr = start_addr;
        this.end_addr = end_addr;
    }
}
