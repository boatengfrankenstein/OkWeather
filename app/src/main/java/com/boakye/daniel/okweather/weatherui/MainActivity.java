package com.boakye.daniel.okweather.weatherui;/*

 */


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;

import android.location.Location;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.boakye.daniel.okweather.R;
import com.boakye.daniel.okweather.voice.DataCollection;
import com.boakye.daniel.okweather.voice.GetMethodEx;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ShakeDetector.OnShakeListener,TextToSpeech.OnInitListener, GoogleApiClient.ConnectionCallbacks,LocationListener{

    private static final int REQUEST_CODE = 1234;
    private ListView wordsList;
    private static final String TAG = "FusedLocation-test";
    public static String temploc;


    public static TextToSpeech tts;
    // The default search radius when searching for places nearby.
    public static int DEFAULT_RADIUS = 150;
    // The maximum distance the user should travel between location updates.
    public static int MAX_DISTANCE = DEFAULT_RADIUS/2;
    // The maximum time that should pass before the user gets a location update.
    public static long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    // Intent action for udpating location quickly once
    public static String SINGLE_LOCATION_UPDATE_ACTION = "WEATHER_SINGLE_LOC_UPDATE";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;
    String temp;

    public static String   result;
    public static String getlocationforweather;
    public static String temp_loc;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            View mActionBarView = getLayoutInflater().inflate(R.layout.main_action_bar, null);
            actionBar.setCustomView(mActionBarView);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        }

        tts = new TextToSpeech(this, this);
        wordsList = (ListView) findViewById(R.id.list);
        // Disable button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
          //  speakButton.setEnabled(false);
            //  speakButton.setText("Recognizer not present");
        }
        // First we need to check availability of play services



       // startVoiceRecognitionActivity();
        //NEW CHANGE! Fetches curr philly data b4 button press

        ShakeDetector.create(this, new ShakeDetector.OnShakeListener() {
            @Override
            public void OnShake() {
                Toast.makeText(getApplicationContext(), "Device shaken!", Toast.LENGTH_SHORT).show();
                startVoiceRecognitionActivity();
            }
        });

    }



    @Override
    public void onLocationChanged(Location location) {

    }




    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        else if (id == R.id.action_map) {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        // Don't forget to shut down tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                this. finish();
            }
            return false;
        }
        return true;
    }

    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
// Break now we are getting lattitude and longitute which we are updating the text so what this mean
            // is that we can retrieve the geocaodine adrees. and do stuf.
            temp_loc = getAddressFromLocation(latitude,longitude,this, new GeocoderHandler());
            temp = null;
            try {
                if (temp_loc != null)
                    temp = getInternetData(temp_loc);
                else temp = getInternetData("Alexandria,VA");

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            DataCollection.syncData(temp);
         //   locawe.execute(temp_loc, "imperial");



        } else {

           //
        }
    }


    @Override
    public void OnShake() {

    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                tts.setSpeechRate((float) .8);
            } else {
                tts.speak("Hello, and welcome to Ok Weather! My name is Ama, and I'm here to inform you about the weather. Say temperature for the current temperature. Say details for a more detailed analysis on the current weather. Say forecast for a five day weather forecast. And finally, say clothing for clothing suggestions based on the current weather. Start speaking after the beep!", TextToSpeech.QUEUE_FLUSH, null);
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    startVoiceRecognitionActivity();

    }


    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say an OKweather command!");
        startActivityForResult(intent, REQUEST_CODE);
    }

    //Handle the results from the voice recognition activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            wordsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));
            if ("temperature".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("The current temperature is " + DataCollection.getTemp() + "degrees.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Temperature".contentEquals((String) wordsList.getAdapter().getItem(0))) {
               tts.speak("The current temperature is " + DataCollection.getTemp() + "degrees.", TextToSpeech.QUEUE_FLUSH, null);
            }

            else if ("Hello".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("Hi. How are you?", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("hello".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("Hi. How are you?", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Hi".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("Hi. How are you?", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("hi".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("Hi. How are you?", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("good".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("Glad to hear it.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Good".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("Glad to hear it.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Thanks".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("No problem. Now onto the weather.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("thanks".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("No problem. Now onto the weather.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("thank you".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("No problem. Now onto the weather.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Thank you".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("No problem. Now onto the weather.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("detail".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("The minimum temperature of the day is " + DataCollection.getTempMin() + "degrees, and the maximum is " + DataCollection.getTempMax() + "degrees. The wind is moving at a speed of " + DataCollection.getSpeed() + "miles per hour. The atmospheric pressure measures at " + DataCollection.getPressure() + "hectopascals, and the outdoor humidity is " + DataCollection.getHum() + "percent.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Detail".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("The minimum temperature of the day is " + DataCollection.getTempMin() + "degrees, and the maximum is " + DataCollection.getTempMax() + "degrees. The wind is moving at a speed of " + DataCollection.getSpeed() + "miles per hour. The atmospheric pressure measures at " + DataCollection.getPressure() + "hectopascals, and the outdoor humidity is " + DataCollection.getHum() + "percent.", TextToSpeech.QUEUE_FLUSH, null);

            }
            else if ("3".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("This will give you a five day weather forecast.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("forecast".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("This will give you a more detailed analysis of the current weather.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Forecast".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("This will give you a more detailed analysis of the current weather.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("clothing".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                if (DataCollection.getTemp()>=75 && DataCollection.getTemp()<90) {
                    tts.speak("Well, since it's currently " + DataCollection.getTemp() + "degrees outside, a short sleeved shirt and shorts would be a good idea.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (DataCollection.getTemp()>=90) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside, so a short sleeved shirt or tank top with shorts would be a good idea considering the heat.", TextToSpeech.QUEUE_FLUSH, null);
                }
                if (DataCollection.getTemp()>=65 && DataCollection.getTemp()<75) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside right now, so shorts with a short sleeved or long sleeved shirt would be great, but think about bringing a sweatshirt or sweater along with you.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (DataCollection.getTemp()>=55 && DataCollection.getTemp()<65) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside right now, so I'd suggest a short sleeved or long sleeved shirt with jeans or long pants, plus a sweatshirt or light jacket.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (DataCollection.getTemp()>=45 && DataCollection.getTemp()<55) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside now, so you should probably wear a long sleeved shirt with jeans or long pants and a jacket or coat.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (DataCollection.getTemp()<45) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside. That's pretty chilly! I'd definitely recommend long pants with a long sleeved shirt with a sweater or sweatshirt and a heavy coat. Maybe add a hat too!", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            else if ("Clothing".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                if (DataCollection.getTemp()>=75 && DataCollection.getTemp()<90) {
                    tts.speak("Well, since it's currently " + DataCollection.getTemp() + "degrees outside, a short sleeved shirt and shorts would be a good idea.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (DataCollection.getTemp()>=90) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside, so a short sleeved shirt or tank top with shorts would be a good idea considering the heat.", TextToSpeech.QUEUE_FLUSH, null);
                }
                if (DataCollection.getTemp()>=65 && DataCollection.getTemp()<75) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside right now, so shorts with a short sleeved or long sleeved shirt would be great, but think about bringing a sweatshirt or sweater along with you.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (DataCollection.getTemp()>=55 && DataCollection.getTemp()<65) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside right now, so I'd suggest a short sleeved or long sleeved shirt with jeans or long pants, plus a sweatshirt or light jacket.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (DataCollection.getTemp()>=45 && DataCollection.getTemp()<55) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside now, so you should probably wear a long sleeved shirt with jeans or long pants and a jacket or coat.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (DataCollection.getTemp()<45) {
                    tts.speak("Well, it's " + DataCollection.getTemp() + "degrees outside. That's pretty chilly! I'd definitely recommend long pants with a long sleeved shirt with a sweater or sweatshirt and a heavy coat. Maybe add a hat too!", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            else {
                tts.speak("Can you repeat that? I might have misunderstood you because I don't think that command was a menu item.", TextToSpeech.QUEUE_FLUSH, null);

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    ///////////////////////////////////////////Getting Weather ///////////
    public static String getAddressFromLocation(final double latitude, final double longitude,
                                                final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                     /*   for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append("\n");
                        }

                        */

                        sb.append(address.getLocality()).append(",").append(address.getAdminArea());
                        //    Log.i("sbolo",     sb.append(address.getLocality() + ""+address.getCountryName()).toString());
                        //  sb.append(address.getPostalCode()).append("\n");
                        //  sb.append(address.getCountryName());

                        result = sb.toString();
                        getlocationforweather = address.getLocality();
                    }
                } catch (IOException e) {
                    Log.e("Geo taggin", "Unable connect to Geocoder", e);
                } finally {

                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (getlocationforweather != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        //    today_temperature.setText(getlocationforweather);

                        bundle.putString("address", getlocationforweather);
                        message.setData(bundle);


                        // Log.i("ReverseGeoCoding",message.getData().toString());
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Latitude: " + latitude + " Longitude: " + longitude +
                                "\n Unable to get address for this lat-long.";
                        bundle.putString("address", result);

                        message.setData(bundle);
                        Log.i("Here",message.toString());
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();

        return  getlocationforweather;
    }





    @Override
    public void onConnected(Bundle bundle) {
        displayLocation();

        if (mRequestingLocationUpdates) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }


    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            //tvAddress.setText(locationAddress);
             /* Call to the background work to load the data */
            //   GetWeather weather = new GetWeather();
            // weather.execute("22311", unit);
        }
}


    public String getInternetData(String locu) throws Exception {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        BufferedReader in = null;
        String location;
        location = MainActivity.temploc;
        String locuri=locu;
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
