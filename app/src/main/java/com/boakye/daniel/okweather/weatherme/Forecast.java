package com.boakye.daniel.okweather.weatherme;

/**
 * Created by Daniel Boakye 12/27/14.
 */
public class Forecast {

    Weather weather = new Weather();
    long date;

    public long getDate() {
        return date;
    }
    public void setDate(float date) {
        this.date = (long)date;
    }
}
