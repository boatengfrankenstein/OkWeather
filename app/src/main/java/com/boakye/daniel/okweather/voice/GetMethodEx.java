package com.boakye.daniel.okweather.voice;

import android.os.StrictMode;

import com.boakye.daniel.okweather.weatherui.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;


public class GetMethodEx {

	public String getInternetData() throws Exception {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		BufferedReader in = null;
		String location;
		location = MainActivity.temploc;
		String locuri="Alexandria,VA";
		String data = null;
		try{

			HttpClient client = new DefaultHttpClient();
			URI website = new URI("http://api.openweathermap.org/data/2.5/find?q="+locuri+"&units=imperial&APPID=6df083056986e30bae309be3b50d3e8c");
			HttpGet request = new HttpGet();
			request.setURI(website);
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String l = "";
			String nl = System.getProperty("line.separator");
			while((l = in.readLine()) != null){
				sb.append(l + nl);
			}
			in.close();
			data = sb.toString();
			return data;
		}finally{
			if(in != null){
				try{
					in.close();
					return data;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
}
