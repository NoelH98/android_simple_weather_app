package com.noel.bar_hub;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*  @Author Noel.Eugene.Habaa */

public class MainActivity extends AppCompatActivity {

      // Variable declaration
    private RelativeLayout home;
    private ProgressBar loading;
    private TextView cityName, temp , condition;
    private EditText editCity;
    private ImageView background , search , icon;
    private LocationManager locManager;
    private int PERMISSION_CODE = 1;
    private String cityNamer;

  @Override
  public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                           WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );
      setContentView(R.layout.activity_main);
      home = findViewById(R.id.home);
      loading = findViewById(R.id.loading);
      cityName = findViewById(R.id.cityName);
      temp = findViewById(R.id.temp);
      condition = findViewById(R.id.condition);
      editCity = findViewById(R.id.editCity);
      background = findViewById(R.id.background);
      search = findViewById(R.id.search);
      icon = findViewById(R.id.icon);

      locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
              ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

          ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
      }

      Location location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      cityNamer = getCityName(location.getLongitude(),location.getLatitude());
      weatherInfo(cityNamer);

      search.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              String city = editCity.getText().toString();
              if(city.isEmpty()){
                  Toast.makeText(MainActivity.this, "Enter city name", Toast.LENGTH_SHORT).show();
              }else{
                  cityName.setText(cityNamer);
                  weatherInfo(city);
              }
          }
      });

      //OR
      // check for permissions (needs update)
     // if(ContextCompat.checkSelfPermission( this, Manifest.permission) != PackageManager.PERMISSION_GRANTED)

  }

  private String getCityName(double longi,double lat){
      String cityNamer = "Not found";
      Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
      try{
          List<Address> addresses = gcd.getFromLocation(lat,longi,10);

          for(Address adr : addresses){
              if(adr!=null){
                  String city = adr.getLocality();
                  if(city!=null & !city.equals("")){
                      cityNamer = city;
                  }else{
                      Log.d("TAG","CITY NOT FOUND");
                      Toast.makeText(this,"User location not found..",Toast.LENGTH_SHORT).show();
                  }
              }
          }
      }catch(IOException e){
          e.printStackTrace();
      }
      return cityNamer;
  }

  private void weatherInfo(String cityNamer){
      // Your weather api. change url to match your own
      String url = "https://api.weatherapi.com/v1/forecast.json?key=dca892fc2d534382a146146187211207="+cityNamer+"5days=l&aqi=yes";
      cityName.setText(cityNamer);
      RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

      JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
              loading.setVisibility(View.GONE);
              home.setVisibility(View.VISIBLE);

              try{

                  // getting jSon response according to json format. could change depending on the api use.
                  String temperature = response.getJSONObject("current").getString("temp_C");
                  temp.setText(temperature+"'C");

                  int isDay = response.getJSONObject("current").getInt("is_day");
                  String con = response.getJSONObject("current").getJSONObject("condition").getString("text");
                  String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                  Picasso.get().load("http:".concat(conditionIcon).into(icon));
                  condition.setText(con);

                  if(isDay == 1){
                      // add url for background images
                      Picasso.get().load("https://").into(background);
                  }else{
                      Picasso.get().load("https://").into(background);
                  }
              }catch(JSONException e){
                  e.printStackTrace();
              }
          }
      }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
              Toast.makeText(MainActivity.this,"Enter valid city name",Toast.LENGTH_SHORT).show();
          }
      });

      requestQueue.add(jor);
  }

  @Override
    public void onDestroy(){
      super.onDestroy();

  }

}
