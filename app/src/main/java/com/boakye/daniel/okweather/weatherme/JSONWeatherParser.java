package com.boakye.daniel.okweather.weatherme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONWeatherParser {

	public static Weather getWeather(String data) throws JSONException {
		Weather weather = new Weather();

		// We create out JSONObject from the data
		JSONObject jObj = new JSONObject(data);
		
		// We start extracting the info
		Location loc = new Location();
		
		JSONObject coordObj = getObject("coord", jObj);
		loc.setLatitude(getFloat("lat", coordObj));
		loc.setLongitude(getFloat("lon", coordObj));
		
		JSONObject sysObj = getObject("sys", jObj);
		loc.setCountry(getString("country", sysObj));
		loc.setSunrise(getInt("sunrise", sysObj));
		loc.setSunset(getInt("sunset", sysObj));
		loc.setCity(getString("name", jObj));
		weather.location = loc;
		
		// We get weather info (This is an array)
		JSONArray jArr = jObj.getJSONArray("weather");
		
		// We use only the first value
		JSONObject JSONWeather = jArr.getJSONObject(0);
		weather.currentCondition.setWeatherId(getInt("id", JSONWeather));
		weather.currentCondition.setDescr(getString("description", JSONWeather));
		weather.currentCondition.setCondition(getString("main", JSONWeather));
		weather.currentCondition.setIcon(getString("icon", JSONWeather));
		
		JSONObject mainObj = getObject("main", jObj);
		weather.currentCondition.setHumidity(getInt("humidity", mainObj));
		weather.currentCondition.setPressure(getInt("pressure", mainObj));
		weather.temperature.setMaxTemp(getFloat("temp_max", mainObj));
		weather.temperature.setMinTemp(getFloat("temp_min", mainObj));
		weather.temperature.setTemp(getFloat("temp", mainObj));
		
		// Wind
		JSONObject wObj = getObject("wind", jObj);
		weather.wind.setSpeed(getFloat("speed", wObj));
		weather.wind.setDeg(getFloat("deg", wObj));
		
		// Clouds
		JSONObject cObj = getObject("clouds", jObj);
		weather.clouds.setPerc(getInt("all", cObj));		
		
		return weather;
	}

    public static List<Forecast> getForecastData(String data) throws JSONException {
        List<Forecast> forecastList = new ArrayList<Forecast>();
        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);
        JSONArray forecastArray = jObj.getJSONArray("list");

        for(int i = 0 ; i < forecastArray.length();i++){
            Forecast forecast = new Forecast();
            JSONObject tempObj = getObject("temp", forecastArray.getJSONObject(i));
            forecast.weather.temperature.setMaxTemp(getFloat("max", tempObj));
            forecast.weather.temperature.setMinTemp(getFloat("min", tempObj));
            JSONObject mainObj = forecastArray.getJSONObject(i);
            forecast.setDate(getFloat("dt",mainObj));
            forecast.weather.currentCondition.setPressure(getInt("pressure", mainObj));
            forecast.weather.currentCondition.setHumidity(getInt("humidity", mainObj));
            forecast.weather.wind.setSpeed(getFloat("speed", mainObj));
            forecast.weather.wind.setDeg(getFloat("deg", mainObj));
            JSONArray jArr = mainObj.getJSONArray("weather");
            // We use only the first value
            JSONObject JSONWeather = jArr.getJSONObject(0);
            forecast.weather.currentCondition.setWeatherId(getInt("id", JSONWeather));
            forecast.weather.currentCondition.setDescr(getString("description", JSONWeather));
            forecast.weather.currentCondition.setCondition(getString("main", JSONWeather));
            forecast.weather.currentCondition.setIcon(getString("icon", JSONWeather));
            forecastList.add(forecast);
        }
        return forecastList;
    }
	
	private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
		JSONObject subObj = jObj.getJSONObject(tagName);
		return subObj;
	}
	
	private static String getString(String tagName, JSONObject jObj) throws JSONException {
		return jObj.getString(tagName);
	}

	private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
		return (float) jObj.getDouble(tagName);
	}
	
	private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
		return jObj.getInt(tagName);
	}
	
}
