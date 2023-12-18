package com.android.weatherapp;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.util.Log;


import com.koushikdutta.ion.Ion;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Weather extends AsyncTask<String, Void, String> {
    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    String result; String error;
    @Override
    protected String doInBackground(String... urls) {
        result = "";
        URL link;
        HttpURLConnection myConnection;

        try{
            link = new URL(urls[0]);
            myConnection = (HttpURLConnection)link.openConnection();
            InputStream in = myConnection.getInputStream();
            InputStreamReader myStreamReader = new InputStreamReader(in);
            int data = myStreamReader.read();
            while (data != -1){
                char current = (char)data;
                result += current;
                data = myStreamReader.read();
            }
            return result;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            error = "City Not Found!";
            Log.d("IOError: ", error);
            MainActivity.error = error;
        }
        return null;

    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        try{

            JSONObject myObject = new JSONObject(result);

            JSONObject city = new JSONObject(myObject.getString("city"));
            String placeName = city.getString("name");
            String country = city.getString("country");
            String location = placeName+", "+country;

            JSONArray list = myObject.getJSONArray("list");

            JSONObject list0 = list.getJSONObject(0);

            JSONArray weather = list0.getJSONArray("weather");

            String desc = weather.getJSONObject(0).getString("description");
            desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);

            JSONObject main = list0.getJSONObject("main");
            double mainT = main.getDouble("temp");
            int temp = (int) Math.round(mainT);
            String mainTemp = String.valueOf(temp) ;

            String humidity = main.getString("humidity") + " %";

            String pressure = main.getString("pressure") + " hPa";

            JSONObject wind = list0.getJSONObject("wind");
            double windTemp = wind.getDouble("speed");
            windTemp = windTemp * 3.6;
            final DecimalFormat df = new DecimalFormat("0.0");
            String windSpeed = df.format(windTemp) + " km/h";

            int visible = list0.getInt("visibility");
            int tempVisible = visible/1000;
            String visibilityT = String.valueOf(tempVisible);
            String visibility = visibilityT + " km";

            List<String> day = new ArrayList<>();
            List<String> dayIcon = new ArrayList<>();
            List<String> dayTemp = new ArrayList<>();

            String[] currDateCheck = list0.getString("dt_txt").split(" ");
            String currDate = currDateCheck[0];

            int counter = 0;
            int index = 0;
            int min = Integer.MAX_VALUE;

            for (int i = 0; i < 40; i++){
                JSONObject listTemp = list.getJSONObject(i);
                String tempDate = listTemp.getString("dt_txt");
                String[] tempTime = tempDate.split(" ");
                String timeCheck = tempTime[1];

                String currTempDate = tempTime[0];

                JSONObject mainLoop = listTemp.getJSONObject("main");
                String timeLoop = listTemp.getString("dt_txt");

                String[] timeSplit = timeLoop.split(" ");
                String loopDate = timeSplit[0];
                String[] loopDateList = loopDate.split("-");
                String dayLoop = loopDateList[2];

                String timeTemp = timeSplit[1];
                String[] hourList = timeTemp.split(":");
                String hour = hourList[0];

                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String currDateTemp = formatter.format(date);
                String[] currTimeSplit = currDateTemp.split(" ");
                String currDate1 = currTimeSplit[0];
                String[] currDateList = currDate1.split("-");
                String dayNow = currDateList[2];

                String currTime = currTimeSplit[1];
                String[] currHourList = currTime.split(":");
                String currHour = currHourList[0];

                int hourInt = Integer.parseInt(hour);
                int currHourInt = Integer.parseInt(currHour);
                int dayNowInt = Integer.parseInt(dayNow);
                int dayLoopInt = Integer.parseInt(dayLoop);

                if (hourInt == 0 && currHourInt != 0){
                    hourInt = 24;
                }

                int diff = currHourInt - hourInt;

                if (diff >= 0 && diff < min && dayNowInt == dayLoopInt){
                    min = diff;
                    index = i;
                }

                if (currTempDate.compareTo(currDate) > 0 && timeCheck.compareTo("12:00:00") == 0 && counter < 4){
                    currDate = currTempDate;
                    counter++;

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
                    @SuppressLint("SimpleDateFormat") Format f = new SimpleDateFormat("EEEE");
                    Date dateLoop = simpleFormat.parse(tempTime[0]);
                    String dayFoo = f.format(dateLoop);
                    day.add(dayFoo);

                    double mainTempMin = mainLoop.getDouble("temp_min");
                    int tempCc = (int) Math.floor(mainTempMin);
                    double mainTempMax = mainLoop.getDouble("temp_max");
                    int tempCc2 = (int) Math.ceil(mainTempMax);
                    String mainTempCc = String.valueOf(tempCc);
                    String mainTempCc2 = String.valueOf(tempCc2);
                    String mainTempStr = mainTempCc2 + "° | " + mainTempCc + "°";
                    dayTemp.add(mainTempStr);


                    JSONArray weatherLoop = listTemp.getJSONArray("weather");
                    String iconLoop = weatherLoop.getJSONObject(0).getString("icon");
                    dayIcon.add(iconLoop);

                }

            }

            int fiveHourIndex = index + 5;
            List<String> hours = new ArrayList<>();
            List<String> hoursIcon = new ArrayList<>();
            List<String> hoursTemp = new ArrayList<>();

            for (int i = index; i < fiveHourIndex; i++){
                JSONObject listTemp = list.getJSONObject(i);
                JSONObject mainLoop = listTemp.getJSONObject("main");


                String timeLoop = listTemp.getString("dt_txt");
                String[] loopSplit = timeLoop.split(" ");
                String[] timeStr = loopSplit[1].split(":");
                int hourStr = Integer.parseInt(timeStr[0]);
                String hour;

                if (hourStr > 12){
                    int tempLoop = hourStr - 12;
                    String hourT = String.valueOf(tempLoop);
                    hour = hourT + " PM";

                }
                else if (hourStr == 12){
                    String hourT = String.valueOf(hourStr);
                    hour = hourT + " PM";
                }
                else if (hourStr == 0){
                    String hourT = String.valueOf(12);
                    hour = hourT + " AM";
                }
                else{
                    String hourT = String.valueOf(hourStr);
                    hour = hourT + " AM";
                }

                hours.add(hour);
                double hourTempD = mainLoop.getDouble("temp");

                int tempInt = (int) Math.round(hourTempD);
                String tempIntT = String.valueOf(tempInt);
                String hourTemp = tempIntT + "°";

                hoursTemp.add(hourTemp);
                JSONArray weatherLoop = listTemp.getJSONArray("weather");
                String hourIcon = weatherLoop.getJSONObject(0).getString("icon");
                hoursIcon.add(hourIcon);
            }

            MainActivity.location.setText(location);
            MainActivity.mainTemp.setText(mainTemp);
            MainActivity.description.setText(desc);
            MainActivity.humidity.setText(humidity);
            MainActivity.pressure.setText(pressure);
            MainActivity.visibility.setText(visibility);
            MainActivity.windSpeed.setText(windSpeed);
            MainActivity.day1.setText(day.get(0));
            MainActivity.day2.setText(day.get(1));
            MainActivity.day3.setText(day.get(2));
            MainActivity.day4.setText(day.get(3));

            Ion.with(MainActivity.day1img)
                    .load("http://openweathermap.org/img/wn/"+dayIcon.get(0)+"@2x.png");
            Ion.with(MainActivity.day2img)
                    .load("http://openweathermap.org/img/wn/"+dayIcon.get(1)+"@2x.png");
            Ion.with(MainActivity.day3img)
                    .load("http://openweathermap.org/img/wn/"+dayIcon.get(2)+"@2x.png");
            Ion.with(MainActivity.day4img)
                    .load("http://openweathermap.org/img/wn/"+dayIcon.get(3)+"@2x.png");

            MainActivity.day1temp.setText(dayTemp.get(0));
            MainActivity.day2temp.setText(dayTemp.get(1));
            MainActivity.day3temp.setText(dayTemp.get(2));
            MainActivity.day4temp.setText(dayTemp.get(3));

            MainActivity.hour1.setText(hours.get(0));
            MainActivity.hour2.setText(hours.get(1));
            MainActivity.hour3.setText(hours.get(2));
            MainActivity.hour4.setText(hours.get(3));
            MainActivity.hour5.setText(hours.get(4));

            Ion.with(MainActivity.hour1img)
                    .load("http://openweathermap.org/img/wn/"+hoursIcon.get(0)+"@2x.png");
            Ion.with(MainActivity.hour2img)
                    .load("http://openweathermap.org/img/wn/"+hoursIcon.get(1)+"@2x.png");
            Ion.with(MainActivity.hour3img)
                    .load("http://openweathermap.org/img/wn/"+hoursIcon.get(2)+"@2x.png");
            Ion.with(MainActivity.hour4img)
                    .load("http://openweathermap.org/img/wn/"+hoursIcon.get(3)+"@2x.png");
            Ion.with(MainActivity.hour5img)
                    .load("http://openweathermap.org/img/wn/"+hoursIcon.get(4)+"@2x.png");

            MainActivity.hour1temp.setText(hoursTemp.get(0));
            MainActivity.hour2temp.setText(hoursTemp.get(1));
            MainActivity.hour3temp.setText(hoursTemp.get(2));
            MainActivity.hour4temp.setText(hoursTemp.get(3));
            MainActivity.hour5temp.setText(hoursTemp.get(4));

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            error = "City Not Found!";
        }
    }
}

