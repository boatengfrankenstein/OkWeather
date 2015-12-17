package com.boakye.daniel.okweather.voice;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

public class ForecastDataCollection {
	
	private static int size = 7;
	private static double temp[] = new double[size];
	private static double temp_min[] = new double[size];
	private static double temp_max[] = new double[size];
	private static double pressure[] = new double[size];
	private static double sea_level[] = new double[size];
	private static double grnd_level[] = new double[size];
	private static double hum[]= new double[size];
	
	private static double speed[] = new double[size];
	
	
	private static JsonParser jp;
	private static JsonElement root;
	private static JsonObject rootobj;
	private List dates;

	private static int counter = 0;
	
	public static void syncData(String data){
		
		Log.d("FORECASTDATACOLLECTION", data);
		jp = new JsonParser();//parses
		root = jp.parse(data);//root of all data/evil
		rootobj = root.getAsJsonObject();//data in object form
		
		JsonArray j = rootobj.get("list").getAsJsonArray();//list in array
		//String 
		
		JsonArray k = new JsonArray();//should contain every main per element
		
		for(int x = 0; x<temp.length;x++){
			JsonObject jsonObj = j.get(x).getAsJsonObject();
			//k.add(element);
			k.add(jsonObj.get("main").getAsJsonObject());
			//temp[x]=k.get(x).getAsJsonObject().get("temp").getAsDouble();
			//pressure[x]=k.get(x).getAsJsonObject().get("pressure").getAsDouble();
			
			temp_min[x] = jsonObj.get("main").getAsJsonObject().get("temp_min").getAsDouble();
			temp_max[x] = jsonObj.get("main").getAsJsonObject().get("temp_max").getAsDouble();
			hum[x] = jsonObj.get("main").getAsJsonObject().get("humidity").getAsDouble();
			temp[x] = jsonObj.get("main").getAsJsonObject().get("temp").getAsDouble();
			pressure [x]= jsonObj.get("main").getAsJsonObject().get("pressure").getAsDouble();
			speed[x] = jsonObj.get("wind").getAsJsonObject().get("speed").getAsDouble();
			
		}
		
		//Log.d(temp);
	}
	
	public static double getTemp(int x){
		return temp[x];
	}
	
	public static double getTempMin(int x){
		return temp_min[x];
	}
	
	public static double getTempMax(int x){
		return temp_max[x];
	}
	
	public static double getPressure(int x){
		return pressure[x];
	}
	
	public static double getSpeed(int x){
		return speed[x];
	}
	
	
	
}
