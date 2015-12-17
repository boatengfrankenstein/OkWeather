package com.boakye.daniel.okweather.weatherme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.boakye.daniel.okweather.R;
import com.boakye.daniel.okweather.voice.DataCollection;
import com.boakye.daniel.okweather.weatherui.ShakeDetector;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,ShakeDetector.OnShakeListener,TextToSpeech.OnInitListener {
    private final String TAG = "WeatherNow";
    Typeface weatherFont;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    RecyclerView recList;
    private TextView weatherIcon;
    private TextView temp;
    private TextView windSpeed;
    private TextView cityName;
    private TextView weatherStatus;
    static int  weathercond;
    static int tempWindSpeed;
    static int tempPreciptation;
    static int tempMin;
    static int tempMax;
    static String tempCurrentWeather;
    private LinearLayout headerProgress;
    private AlertDialog.Builder alertDialog;
    String cityNameString = "";
    SharedPreferences sharedPrefs;
    private static final int REQUEST_CODE = 1234;
    private ListView wordsList;

    public static TextToSpeech tts;
    Weather weather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        recList.setLayoutManager(llm);

        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
        headerProgress = (LinearLayout) findViewById(R.id.headerProgress);
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        temp = (TextView) findViewById(R.id.info_text);
        windSpeed = (TextView) findViewById(R.id.wind_speed);
        cityName = (TextView) findViewById(R.id.city_name);
        weatherStatus = (TextView) findViewById(R.id.weather_status);
        weatherIcon.setTypeface(weatherFont);

        headerProgress.setVisibility(View.VISIBLE);

        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        wordsList = (ListView) findViewById(R.id.list);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            // speakButton.setEnabled(false);
            //  speakButton.setText("Recognizer not present");
        }
        tts = new TextToSpeech(this, this);
        ShakeDetector.create(this, new ShakeDetector.OnShakeListener() {
            @Override
            public void OnShake() {
                Toast.makeText(getApplicationContext(), "Device shaken!", Toast.LENGTH_SHORT).show();
                startVoiceRecognitionActivity();
            }
        });

       // startVoiceRecognitionActivity();
        Log.i("MonitorCall","Oncreate Finished");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent i = new Intent(this, UserSettingActivity.class);
                startActivityForResult(i, 1);
                break;

        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();

        Log.i("MonitorCall", "Onstart Finished");
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
        Log.i("MonitorCall", "OnStop Finished");
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setInterval(1000); // Update location every second

        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        updateUI(loc);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        updateUI(location);
    }

    private void updateUI(Location loc) {
        getCityName(loc);
        if (isConnected()) {
            if (loc != null) {
                JSONWeatherTask task = new JSONWeatherTask();
                task.execute(new Double[]{loc.getLatitude(),loc.getLongitude()});
                JSONForecastTask forecastTask = new JSONForecastTask();
                forecastTask.execute(new Double[]{loc.getLatitude(),loc.getLongitude()});
            }
        } else {
            showAlertDialog();
        }
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private String setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getResources().getString(R.string.weather_sunny);
            } else {
                icon = getResources().getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = getResources().getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getResources().getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = getResources().getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = getResources().getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = getResources().getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = getResources().getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    private void getCityName(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                cityNameString = address.getLocality().toString();
                Toast.makeText(this, " Your Location is " + cityNameString, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            showAlertDialog();
            e.printStackTrace();
        }
    }

    private String getDay(long timestamp) {
        return new SimpleDateFormat("EEEE").format(new Date(timestamp * 1000));
    }

    private int getRoundedValue(float num) {
        return (int) Math.round(num);
    }

    private void showAlertDialog() {
        if (alertDialog != null) return;
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Oops!");
        alertDialog.setMessage("Unable to connect. Please try again later.");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    @Override
    protected void onDestroy() {

        // Don't forget to shut down tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();

        Log.i("MonitorCall", "OnDestroy Finished");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MonitorCall", "OnPause Finished");

    }
  @Override
    public void onInit(int status) {


        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                tts.setSpeechRate((float) 0.9);
            } else {
                tts.speak("Hello, and welcome to Ok Weather!, My name is Ama, and I'm here to inform you about the weather. Say" +
                        " temperature for the" +
                        " current temperature. Say details for a more detailed analysis on the current weather. " +
                        "Say forecast for a five " +
                        "day weather forecast.Say clothing for clothing suggestions based on the " +
                        "current weather.And finally, say location to get your current location. Shake the device a little hard to activate the voice command", TextToSpeech.QUEUE_FLUSH, null);
               // startVoiceRecognitionActivity();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }



      Log.i("MonitorCall","Oncreate Finished");
      Log.i("MonitorCall","OnInit Finished");

    }

    @Override
    public void OnShake() {

    }

    private class JSONWeatherTask extends AsyncTask<Double, Void, Weather> {

        @Override
        protected Weather doInBackground(Double... params) {
            Weather weather = new Weather();
            try {
                String data = ((new WeatherHttpClient()).getWeatherData(params[0],params[1], sharedPrefs.getString("temperature_list", "imperial")));
                weather = JSONWeatherParser.getWeather(data);
            } catch (Exception e) {
                showAlertDialog();
                e.printStackTrace();
            }
            return weather;

        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            try {

                weathercond =   getRoundedValue(weather.temperature.getTemp());
                tempWindSpeed = getRoundedValue(weather.wind.getSpeed());
                tempPreciptation = getRoundedValue(weather.clouds.getPerc());
                tempCurrentWeather = weather.currentCondition.getCondition();
                tempMin = getRoundedValue(weather.temperature.getMinTemp());
                tempMax = getRoundedValue(weather.temperature.getMaxTemp());

                temp.setText("" + getRoundedValue(weather.temperature.getTemp()) + "° (" +
                        getRoundedValue(weather.temperature.getMaxTemp()) + "°/" + getRoundedValue(weather.temperature.getMinTemp()) + "°)");
                windSpeed.setText("Wind " + weather.wind.getSpeed()  + "/Precip. " + weather.clouds.getPerc() + "%");
                cityName.setText(cityNameString + ", " + weather.location.getCountry());
                weatherStatus.setText(weather.currentCondition.getCondition());
                weatherIcon.setText(setWeatherIcon(weather.currentCondition.getWeatherId(), weather.location.getSunrise(), weather.location.getSunset()));
            } catch (Exception e) {
                Log.i("MainActivity", "Caught Exception " + e);
            }

        }
    }

    private class JSONForecastTask extends AsyncTask<Double, Void, List<Forecast>> {

        @Override
        protected List<Forecast> doInBackground(Double... params) {
            List<Forecast> forecastList = new ArrayList<Forecast>();
            try {
                String forecast = (new WeatherHttpClient()).getForecast(params[0],params[1],sharedPrefs.getString("temperature_list", "imperial"),sharedPrefs.getString("days_list", "NULL"));
                forecastList = JSONWeatherParser.getForecastData(forecast);
            } catch (Exception e) {
                showAlertDialog();
                e.printStackTrace();
            }
            return forecastList;

        }

        @Override
        protected void onPostExecute(List<Forecast> forecastList) {
            super.onPostExecute(forecastList);
            try {
                List<WeatherInfo> result = new ArrayList<WeatherInfo>();
                for (int i = 0; i < forecastList.size(); i++) {
                    WeatherInfo ci = new WeatherInfo();
                    ci.weatherIcon = setWeatherIcon(forecastList.get(i).weather.currentCondition.getWeatherId(), 0, 0);
                    ci.infoText = getRoundedValue(forecastList.get(i).weather.temperature.getMaxTemp()) + "°/" + getRoundedValue(forecastList.get(i).weather.temperature.getMinTemp()) + "°";
                    ci.windSpeed = forecastList.get(i).weather.wind.getSpeed()+getWindSpeedUnit();
                    ci.status = forecastList.get(i).weather.currentCondition.getCondition();
                    ci.day = getDay(forecastList.get(i).date);
                    result.add(ci);

                }
                InfoCardAdapter ca = new InfoCardAdapter(result, weatherFont);
                recList.setAdapter(ca);
                headerProgress.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.i("MainActivity", "Caught Exception " + e);
            }

        }

        String getWindSpeedUnit()
        {
            if(sharedPrefs.getString("temperature_list", "NULL") == "imperial")
                return "kph";
            return "mph";
        }

    }
    //Handle the results from the voice recognition activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        weather = new Weather();
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            Log.i("City Data",cityNameString);
            Log.i("City Weather",weathercond+"");

            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            wordsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));
            if ("temperature".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("The current temperature is " + weathercond + "degrees.", TextToSpeech.QUEUE_FLUSH, null);
                Log.i("voice interpretation",wordsList.getAdapter().getItem(0).toString());
             }
            else if ("Temperature".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("The current temperature is " + weathercond + "degrees.", TextToSpeech.QUEUE_FLUSH, null);
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

            else if ("location".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                if(cityNameString != "") {
                    tts.speak("You are in "+cityNameString, TextToSpeech.QUEUE_FLUSH, null);
                }
                else {
                    tts.speak("Sorry your location is not yet available. I think your internet connection is bad. Why? Are you in Ghana!?", TextToSpeech.QUEUE_FLUSH, null);
                }
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
                tts.speak("You are currently in"+ cityNameString+" The minimum temperature of the day is " + tempMin + "degrees, and the maximum is " + tempMax + "degrees. The wind is moving at a speed of " + tempWindSpeed + "miles per hour", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Detail".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("You are currently in"+ cityNameString+ "The minimum temperature of the day is " + tempMin + "degrees, and the maximum is " + tempMax + "degrees. The wind is moving at a speed of " + tempWindSpeed + "miles per hour", TextToSpeech.QUEUE_FLUSH, null);

            }
            else if ("3".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("This will give you a five day weather forecast.", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("forecast".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("You are currently in"+ cityNameString+" The minimum temperature of the day is " + tempMin + "degrees, and the maximum is " + tempMax + "degrees. The wind is moving at a speed of " + tempWindSpeed + "miles per hour", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("Forecast".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                tts.speak("You are currently in"+ cityNameString+" The minimum temperature of the day is " + tempMin + "degrees, and the maximum is " + tempMax + "degrees. The wind is moving at a speed of " + tempWindSpeed + "miles per hour", TextToSpeech.QUEUE_FLUSH, null);
            }
            else if ("clothing".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                if (weathercond>=75 && weathercond<90) {
                    tts.speak("Well, since it's currently " + weathercond + "degrees outside, a short sleeved shirt and shorts would be a good idea.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (weathercond>=90) {
                    tts.speak("Well, it's " + weathercond + "degrees outside, so a short sleeved shirt or tank top with shorts would be a good idea considering the heat.", TextToSpeech.QUEUE_FLUSH, null);
                }
                if (weathercond>=65 && weathercond<75) {
                    tts.speak("Well, it's " + weathercond + "degrees outside right now, so shorts with a short sleeved or long sleeved shirt would be great, but think about bringing a sweatshirt or sweater along with you.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (weathercond>=55 && weathercond<65) {
                    tts.speak("Well, it's " + weathercond+ "degrees outside right now, so I'd suggest a short sleeved or long sleeved shirt with jeans or long pants, plus a sweatshirt or light jacket.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (weathercond>=45 && weathercond<55) {
                    tts.speak("Well, it's " + weathercond + "degrees outside now, so you should probably wear a long sleeved shirt with jeans or long pants and a jacket or coat.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (weathercond<45) {
                    tts.speak("Well, it's " + weathercond + "degrees outside. That's pretty chilly! I'd definitely recommend long pants with a long sleeved shirt with a sweater or sweatshirt and a heavy coat. Maybe add a hat too!", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            else if ("Clothing".contentEquals((String) wordsList.getAdapter().getItem(0))) {
                if (weathercond>=75 && weathercond<90) {
                    tts.speak("Well, since it's currently " + weathercond + "degrees outside, a short sleeved shirt and shorts would be a good idea.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (weathercond>=90) {
                    tts.speak("Well, it's " + weathercond+ "degrees outside, so a short sleeved shirt or tank top with shorts would be a good idea considering the heat.", TextToSpeech.QUEUE_FLUSH, null);
                }
                if (weathercond>=65 && weathercond<75) {
                    tts.speak("Well, it's " + weathercond + "degrees outside right now, so shorts with a short sleeved or long sleeved shirt would be great, but think about bringing a sweatshirt or sweater along with you.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (weathercond>=55 && weathercond<65) {
                    tts.speak("Well, it's " + weathercond + "degrees outside right now, so I'd suggest a short sleeved or long sleeved shirt with jeans or long pants, plus a sweatshirt or light jacket.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (weathercond>=45 && weathercond<55) {
                    tts.speak("Well, it's " +weathercond + "degrees outside now, so you should probably wear a long sleeved shirt with jeans or long pants and a jacket or coat.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (weathercond<45) {
                    tts.speak("Well, it's " + weathercond + "degrees outside. That's pretty chilly! I'd definitely recommend long pants with a long sleeved shirt with a sweater or sweatshirt and a heavy coat. Maybe add a hat too!", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            else {
                tts.speak("Can you repeat that? I might have misunderstood you because I don't think that command was a menu item.", TextToSpeech.QUEUE_FLUSH, null);


            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    //////////Weather Recogition

    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say an OKweather command!");
        startActivityForResult(intent, REQUEST_CODE);
    }

}
