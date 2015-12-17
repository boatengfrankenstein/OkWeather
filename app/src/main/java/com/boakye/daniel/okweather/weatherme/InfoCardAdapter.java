package com.boakye.daniel.okweather.weatherme;

/**
 * Created by abhishekrai on 12/26/14.
 */
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boakye.daniel.okweather.R;

import java.util.List;

public class InfoCardAdapter extends RecyclerView.Adapter<InfoCardAdapter.InfoCardHolder> {

    private List<WeatherInfo> contactList;
    private Typeface weatherFont;

    public InfoCardAdapter(List<WeatherInfo> contactList,Typeface tf) {
        this.contactList = contactList;
        this.weatherFont = tf;
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public void onBindViewHolder(InfoCardHolder infoCardHolder, int i) {
        WeatherInfo ci = contactList.get(i);
        infoCardHolder.weatherIcon.setTypeface(weatherFont);
        infoCardHolder.weatherIcon.setText(ci.weatherIcon);
        infoCardHolder.infoText.setText(ci.infoText);
        infoCardHolder.windSpeed.setText(ci.windSpeed);
        infoCardHolder.status.setText(ci.status);
        infoCardHolder.day.setText(ci.day);
    }

    @Override
    public InfoCardHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new InfoCardHolder(itemView);
    }

    public static class InfoCardHolder extends RecyclerView.ViewHolder {

        protected TextView weatherIcon;
        protected TextView infoText;
        protected TextView windSpeed;
        protected TextView status;
        protected TextView day;

        public InfoCardHolder(View v) {
            super(v);
            weatherIcon =  (TextView) v.findViewById(R.id.weather_icon);
            infoText = (TextView)  v.findViewById(R.id.info_text);
            windSpeed = (TextView)  v.findViewById(R.id.wind_speed);
            status = (TextView) v.findViewById(R.id.weather_status);
            day = (TextView) v.findViewById(R.id.day);
        }
    }
}
