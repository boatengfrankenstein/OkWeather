package com.boakye.daniel.okweather.weatherme;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherHttpClient {

	private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static String FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
	
	public String getWeatherData(Double latitude,Double longitude,String unitSystem) {
		HttpURLConnection con = null ;
		InputStream is = null;

		try {
			con = (HttpURLConnection) ( new URL(BASE_URL + "lat=" + latitude +"&lon="+longitude+ "&type=accurate&units="+unitSystem)).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("x-api-key", "54b8edccc652317dcb64c02bc99dda8a");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();
			
			// Let's read the response
			StringBuffer buffer = new StringBuffer();
			is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while (  (line = br.readLine()) != null )
				buffer.append(line + "\r\n");
			
			is.close();
			con.disconnect();
			return buffer.toString();
	    }
		catch(Throwable t) {
			t.printStackTrace();
		}
		finally {
			try { is.close(); } catch(Throwable t) {}
			try { con.disconnect(); } catch(Throwable t) {}
		}

		return null;
				
	}

    public String getForecast(Double latitude,Double longitude, String unitSystem,String daysOfForecast) {
        HttpURLConnection con = null ;
        InputStream is = null;

        try {
            con = (HttpURLConnection) ( new URL(FORECAST_URL + "lat=" + latitude +"&lon="+longitude+ "&type=accurate&cnt="+daysOfForecast+"&units="+unitSystem)).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("x-api-key", "54b8edccc652317dcb64c02bc99dda8a");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while (  (line = br.readLine()) != null )
                buffer.append(line + "\r\n");

            is.close();
            con.disconnect();
            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }
}
