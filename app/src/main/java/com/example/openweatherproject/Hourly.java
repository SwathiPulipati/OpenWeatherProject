package com.example.openweatherproject;

public class Hourly {
    String time, desc;
    double temp;
    String imageIcon;
    public Hourly(String time, double temp, String desc, String imageIcon){
        this.time = time;
        this.temp = temp;
        this.desc = desc;
        this.imageIcon = imageIcon;
    }

    public double getTemp() {
        return temp;
    }

    public String getImageIcon() {
        return imageIcon;
    }

    public String getDesc() {
        return desc;
    }

    public String getTime() {
        return time;
    }
}
