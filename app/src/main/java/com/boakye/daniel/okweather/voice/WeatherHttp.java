package com.boakye.daniel.okweather.voice;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/*
 * Class should access url of openweathersource for Philly.  Suppose to return
 * string data of json file.  Currently crashes app when class is executed
 */
public class WeatherHttp {

	private static String sURL = "http://api.openweathermap.org/data/2.5/find?q=Philadelphia&units=imperial";

	public static String fetch()
	{

		HttpClient httpclient = new DefaultHttpClient();
		String result = null;

		// Prepare a request object
		HttpGet httpget = new HttpGet(sURL);

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status
			Log.i("Praeda", response.getStatusLine().toString());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				result= convertStreamToString(instream);
				//Log.d("HTTP", "" + result);
				// now you have the string representation of the HTML request
				instream.close();
				// return result;
			}


		} catch (Exception e) {}

		return result;
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String getData(){

		//sURL = "http://api.openweathermap.org/data/2.5/forecast?lat=" + la + "&lon=" + lo;


		HttpURLConnection con = null;
		InputStream is = null;

		try{
			con = (HttpURLConnection) (new URL(sURL)).openConnection();
			//con.setRequestMethod("GET");
			//con.setDoInput(false);
			//con.setDoOutput(true);
			con.connect();

			StringBuffer buffer = new StringBuffer();
			//is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;

			while((line=br.readLine()) != null)
				buffer.append(line + "\r\n");

			//is.close();
			con.disconnect();
			return buffer.toString();
		}catch(Throwable t){
			t.printStackTrace();
		}finally{
			try{
				//is.close();
			}catch(Throwable t){}
			try{
				con.disconnect();
			}catch(Throwable t){}
		}
		return null;
	}
	public static String getStringContent(String uri) throws Exception {

		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(uri));
			HttpResponse response = client.execute(request);
			InputStream ips  = response.getEntity().getContent();
			BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));

			StringBuilder sb = new StringBuilder();
			String s;
			while(true )
			{
				s = buf.readLine();
				if(s==null || s.length()==0)
					break;
				sb.append(s);

			}
			buf.close();
			ips.close();
			return sb.toString();

		} 
		finally {
			// any cleanup code...
		}
	} 
}
