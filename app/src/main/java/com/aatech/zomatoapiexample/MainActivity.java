package com.aatech.zomatoapiexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private RequestQueue queue;
    private Button searchButton;
    private ListView restaurantListView;

    private EditText searchText;
    Double lat;
    Double longt;

    ArrayList<String> restaurantName ;
    ArrayList<String> restaurantAddress;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationManager locationManager;
    private String provider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restaurantListView = findViewById(R.id.listview);



        Toolbar appbar = findViewById(R.id.appbar);
        setSupportActionBar(appbar);

        //GEETING LOCATION
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        searchButton = findViewById(R.id.searchButton);
        searchText = findViewById(R.id.searchString);
        final String[] searchQuery = new String[1];
//        Log.e("TEXT TO BE SEARCHED", searchQuery);

        queue = Volley.newRequestQueue(this);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchQuery[0] = searchText.getText().toString();
                Log.e("SEARCH QUERY", searchQuery[0]);
                //                jsonParse(searchQuery[0]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    lat = location.getLatitude();
                                    longt = location.getLongitude();
                                    Log.e("LOCATION", "Latitude " + String.valueOf(lat) + "Longitude: " + String.valueOf(longt));
                                    restaurantName = new ArrayList<String>();
                                    restaurantAddress = new ArrayList<String>();
                                    jsonParse(searchQuery[0], lat, longt);

                                } else {
                                    Toast.makeText(MainActivity.this, "open GOOGLE MAPS to CHECK YOUR LOCATION ", Toast.LENGTH_LONG).show();
                                    Log.e("No Location", "else part of the success");
                                }
                            }
                        });

                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                    }
                }
            }
        });


    }

    private void jsonParse(String searchQuery, double lat, double longt) {

//        String searchEnityID = "";
//        String searchEnityType = "";
        int count=10;

        Log.e("OVER VARIABLES: ", "INITIALIZING VARIABLE OF THE METHOD");
        String searchURL = "https://developers.zomato.com/api/v2.1/search?q=" + searchQuery + "&count="+ count + "&lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(longt);

        String headerMain = "ac730ffe2a222795d99cbd6c3ce30ead";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, searchURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray restaurants = response.getJSONArray("restaurants");
                    Log.e("Length of the json array: ", String.valueOf(restaurants.length()));
                    for (int i = 0; i < restaurants.length(); i++) {
                        JSONObject restaurant = restaurants.getJSONObject(i);
                        JSONObject namemaybe = restaurant.getJSONObject("restaurant");

                        String name = namemaybe.getString("name");
                        Log.e("name of the restutrant", name);

                        JSONObject Rlocation = namemaybe.getJSONObject("location");
                        String address = Rlocation.getString("address");
                        Log.e("ADDRESS", address);

                        restaurantName.add(name);
                        restaurantAddress.add(address);

                        Log.e("In the loop", "True");
                    }




                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //ADAPTER
                ArrayAdapter<String> restaurantAdaptor = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_dropdown_item_1line, restaurantName) ;
                restaurantListView.setAdapter(restaurantAdaptor);
                restaurantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Toast.makeText(MainActivity.this, restaurantAddress.get(i), Toast.LENGTH_LONG).show();
                    }
                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
//                headers.put("Accept", "application/json");
                headers.put("user-key", headerMain);
                return headers;
            }
        };

        queue.add(request);
    }

}

