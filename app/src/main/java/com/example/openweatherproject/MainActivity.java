package com.example.openweatherproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openweatherproject.R;
import com.example.openweatherproject.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    String zipCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.zipcodeInsert.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                zipCode = String.valueOf(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTaskDownloader downloader = new AsyncTaskDownloader();
                downloader.execute(zipCode);
                hideKeyboard();
            }
        });


    }

    class AsyncTaskDownloader extends AsyncTask<String, Void, String>{
        double lat, lon, currentTemp, feelsLike, dailyMin, dailyMax;
        String city, dateTime, currentDesc;
        int currentImageId;
        ArrayList<String> hourlyTimes, hourlyDesc;
        ArrayList<Double> hourlyTemp;
        ArrayList<String> hourlyImageIcon;
        ArrayList<Hourly> hourlyArrayList;
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("https://api.openweathermap.org/geo/1.0/zip?zip=" +zipCode+",US&appid=d1405ece9cbf369ecacde668444f66dd");
                URLConnection conn = url.openConnection();
                InputStream is = conn.getInputStream();
                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String jsonText = rd.readLine();
                    JSONObject jsonObject = new JSONObject(jsonText);
                    lat = jsonObject.getDouble("lat");
                    lon = jsonObject.getDouble("lon");
                    city = jsonObject.getString("name");
                }catch(Exception e){e.printStackTrace();}
            } catch (Exception e) {
                e.printStackTrace();
            }

            try{
                URL url = new URL("https://api.openweathermap.org/data/2.5/onecall?lat=" +lat+ "&lon=" +lon+ "&exclude=minutely,alerts&units=imperial&appid=d1405ece9cbf369ecacde668444f66dd");
                URLConnection conn = url.openConnection();

                InputStream is = conn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String jsonText = rd.readLine();
                while ((rd.readLine()) != null){
                    jsonText += rd.readLine();
                }
                JSONObject jsonObject = new JSONObject(jsonText);
                //current values
                JSONObject currentInfo = jsonObject.getJSONObject("current");
                //date
                long currentTimeUnix = currentInfo.getLong("dt");
                Date currentTime = new Date(currentTimeUnix*1000);
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("EE, MMM d, yyyy, h:mm a");
                dateTime = currentDateFormat.format(currentTime);
                //temp
                currentTemp= currentInfo.getDouble("temp");
                feelsLike = currentInfo.getDouble("feels_like");
                //day min and max
                dailyMin = jsonObject.getJSONArray("daily").getJSONObject(0).getJSONObject("temp").getDouble("min");
                dailyMax = jsonObject.getJSONArray("daily").getJSONObject(0).getJSONObject("temp").getDouble("max");
                //weather description and image
                currentDesc = currentInfo.getJSONArray("weather").getJSONObject(0).getString("description");
                currentImageId = setImageId(currentInfo.getJSONArray("weather").getJSONObject(0).getString("icon"));
                //hourly
                hourlyTimes = new ArrayList<>();
                hourlyDesc = new ArrayList<>();
                hourlyTemp = new ArrayList<>();
                hourlyImageIcon = new ArrayList<>();
                hourlyArrayList = new ArrayList<>();
                SimpleDateFormat hourlyTimeFormat = new SimpleDateFormat("h a");
                JSONArray hourlyInfo = jsonObject.getJSONArray("hourly");
                if (hourlyArrayList.size() > 0) {
                    for (int i = 0; i < 4; i++) {
                        JSONObject info = hourlyInfo.getJSONObject(i);
                        hourlyTimes.set(i, hourlyTimeFormat.format(new Date(info.getLong("dt") * 1000)));
                        hourlyTemp.set(i, info.getDouble("temp"));
                        String desc = info.getJSONArray("weather").getJSONObject(0).getString("description");
                        hourlyDesc.set(i, desc.substring(0, 1).toUpperCase() + desc.substring(1).toLowerCase());
                        hourlyImageIcon.set(i, info.getJSONArray("weather").getJSONObject(i).getString("icon"));

                        hourlyArrayList.set(i, new Hourly(hourlyTimes.get(i), hourlyTemp.get(i), hourlyDesc.get(i), hourlyImageIcon.get(i)));
                    }
                } else{
                    for (int i = 0; i < 4; i++) {
                        JSONObject info = hourlyInfo.getJSONObject(i);
                        hourlyTimes.add(hourlyTimeFormat.format(new Date(info.getLong("dt") * 1000)));
                        hourlyTemp.add(info.getDouble("temp"));
                        String desc = info.getJSONArray("weather").getJSONObject(0).getString("description");
                        hourlyDesc.add(desc.substring(0, 1).toUpperCase() + desc.substring(1).toLowerCase());
                        hourlyImageIcon.add(info.getJSONArray("weather").getJSONObject(0).getString("icon"));

                        hourlyArrayList.add(new Hourly(hourlyTimes.get(i), hourlyTemp.get(i), hourlyDesc.get(i), hourlyImageIcon.get(i)));
                    }
                }

            }catch(Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            binding.lat.setText("Latitude: "+ lat);
            binding.lon.setText("Longitude: " +lon);
            binding.city.setText(city +", USA");
            binding.dateTimeDisplay.setText(dateTime);

            binding.currentCardView.setCardElevation(5);
            binding.currentCardView.setCardBackgroundColor(getColor(R.color.blue_100));
            binding.currentTemp.setText(currentTemp+"°F");
            binding.currentFeelsLike.setText("Feels like " +feelsLike+"°F");
            binding.dayHighLow.setText(dailyMax+ "°↑ · " +dailyMin+ "°↓");
            binding.currentDesc.setText(currentDesc.substring(0,1).toUpperCase()+currentDesc.substring(1).toLowerCase());
            binding.currentImage.setImageResource(currentImageId);

            HourlyAdapter hourlyAdapter = new HourlyAdapter(hourlyArrayList);

            binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.recyclerView.setAdapter(hourlyAdapter);

        }
    }

    public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder>{

        private ArrayList<Hourly> list;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView hourlyTemp, hourlyTime, hourlyDesc;
            public ImageView hourlyImage;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                hourlyTemp = itemView.findViewById(R.id.hourlyTemp);
                hourlyTime = itemView.findViewById(R.id.hourlyTime);
                hourlyDesc = itemView.findViewById(R.id.hourlyDesc);
                hourlyImage = itemView.findViewById(R.id.hourlyImage);
            }
        }

        public HourlyAdapter(ArrayList<Hourly> list){
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View hourlyView = inflater.inflate(R.layout.hourly_info_layout, parent, false);
            ViewHolder viewHolder = new ViewHolder(hourlyView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Hourly hourly = list.get(position);

            holder.hourlyTime.setText(hourly.getTime());

            holder.hourlyTemp.setText(hourly.getTemp()+"°F");

            holder.hourlyDesc.setText(hourly.getDesc());

            holder.hourlyImage.setImageResource(setImageId(hourly.getImageIcon()));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


    }

    public int setImageId(String icon){
        switch(icon){
            case "01d": return R.drawable.clear_day;
            case "01n": return R.drawable.clear_night;
            case "02d": return R.drawable.few_clouds_day;
            case "02n": return R.drawable.few_clouds_night;
            case "03d": return R.drawable.partly_cloudy_day;
            case "03n": return R.drawable.partly_cloudy_night;
            case "04d":
            case "04n": return R.drawable.cloudy;
            case "09d":
            case "10d": return R.drawable.rain_day;
            case "09n":
            case "10n": return R.drawable.rain_night;
            case "11d": return R.drawable.thunder_day;
            case "11n": return R.drawable.thunder_night;
            case "13d": return R.drawable.snow_day;
            case "13n": return R.drawable.snow_night;
            case "50d": return R.drawable.mist_day;
            case "50n": return R.drawable.mist_night;
        }
        return 0;
    }

    public void hideKeyboard () {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}