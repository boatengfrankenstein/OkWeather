package com.boakye.daniel.okweather.voice;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//uses string data retrieved from WeatherHttp and organizes it into seperate variables
//variables can be retrieved ex: DataCollection.getTempMin();
public class DataCollection {

	private static double temp;
	private static double temp_min;
	private static double temp_max;
	private static double pressure;
	private static double hum;
	
	private static double speed;	
	
	private static JsonParser jp;
	private static JsonElement root;
	private static JsonObject rootobj;
	
	
	public static void syncData(String data){
		
		jp = new JsonParser();
		root = jp.parse(data);
		rootobj = root.getAsJsonObject();
		
		JsonArray j = rootobj.get("list").getAsJsonArray();
		
		for(int x = 0; x <j.size(); x++){
			JsonObject jsonObj = j.get(x).getAsJsonObject();
			temp_min = jsonObj.get("main").getAsJsonObject().get("temp_min").getAsDouble();
			temp_max = jsonObj.get("main").getAsJsonObject().get("temp_max").getAsDouble();
			hum = jsonObj.get("main").getAsJsonObject().get("humidity").getAsDouble();
			temp = jsonObj.get("main").getAsJsonObject().get("temp").getAsDouble();
			pressure = jsonObj.get("main").getAsJsonObject().get("pressure").getAsDouble();
			speed = jsonObj.get("wind").getAsJsonObject().get("speed").getAsDouble();
		}
		Log.d("DATACOLLECTION", "" + temp);
	
	}
	
	public static double getTempMin(){
		return temp_min;
	}
	
	public static double getTempMax(){
		return temp_max;
	}
	
	public static double getHum(){
		return hum;
	}
	
	public static double getTemp(){
		return temp;
	}
	
	public static double getPressure() {
		return pressure;
	}
	
	public static double getSpeed() {
		return speed;
	}
	
}
