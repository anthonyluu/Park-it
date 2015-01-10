package com.example.anthonyluu.parkingapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ParkingListActivity extends Activity {
    Context ctx;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i("LocationStatus", "Location Changed: " + location.toString());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("LocationStatus", "Status Changed: " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("LocationStatus", "Provider enabled ");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("LocationStatus", "Provider disabled ");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_list);
        ctx = this;

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, mLocationListener);

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Log.i("Latitude: ", "Latitude of " + latitude);
        Log.i("Longitude: ", "Longitude of " +longitude);

        String url = "http://www1.toronto.ca/City_Of_Toronto/Information_&_Technology/Open_Data/Data_Sets/Assets/Files/greenPParking.json";

        GetParkingDataTask task = new GetParkingDataTask();
        task.execute(url);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parking_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ParkingListArrayAdapter extends ArrayAdapter<JSONObject> {
        private List<JSONObject> items;
        public ParkingListArrayAdapter(Context context, ArrayList<JSONObject> items) {
            super(context, 0, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.parking_list_item, parent, false);
            }
            // Lookup view for data population
            TextView tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
            TextView tvCost = (TextView) convertView.findViewById(R.id.tvCost);
            TextView tvIntersection = (TextView) convertView.findViewById(R.id.tvIntersection);
            // Populate the data into the template view using the data object

            try {
                tvDistance.setText(items.get(position).getString("id"));
                tvCost.setText(items.get(position).getString("rate"));
                tvIntersection.setText(items.get(position).getString("address"));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            // Return the completed view to render on screen
            return convertView;
        }
    }

    private class GetParkingDataTask extends AsyncTask<String, Void, String> {
        private String[] responses;
        ParkingListArrayAdapter parkingListArrayAdapter;
        protected String doInBackground(String... urls) {


            String responseString = getHttpRequest(urls[0]);
            responses = new String[2];
            responses[0] = "test";
            responses[1] = "test";
            return responseString;
        }

        // Should use this to add a spinner of some sort
//        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
//        }

        protected void onPostExecute(String result) {
            final ListView listView = (ListView) findViewById(R.id.list);
            ArrayList vals = new ArrayList<JSONObject>();
//            vals.add("5km");
//            vals.add("10km");
//            vals.add("100km");

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                JSONArray carParks = jsonObject.getJSONArray("carparks");
                for(int i = 0; i < carParks.length(); i ++) {
                    vals.add(carParks.get(i));
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }

//            Log.i("vals at 0: ", vals.get(0).toString());

            parkingListArrayAdapter = new ParkingListArrayAdapter(ctx, vals);
            listView.setAdapter(parkingListArrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int position, long arg3) {
                    int itemPosition = position;

                    // ListView Clicked item value
                    JSONObject jsonObj = (JSONObject) listView.getItemAtPosition(position);

                    // Show Alert
                    try {
                        Toast.makeText(getApplicationContext(),
                                "Position :" + itemPosition + "  ListItem : " + jsonObj.get("address").toString(), Toast.LENGTH_SHORT)
                                .show();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        // method to send HTTP GET request and return as a string
        private String getHttpRequest(String url) {
            StringBuilder sBuilder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();

            HttpGet getRequest = new HttpGet(url);
            HttpResponse response;
            try {
                response = client.execute(getRequest);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if(statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    // Starting to create InputStream to read data and append to String Builder
                    InputStream is = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    // checking to see if line is empty
                    while((line = reader.readLine()) != null) {
                        sBuilder.append(line);
                    }
                }
                else {
                    Log.e("HttpGetError", "failed to GET information from " + url);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sBuilder.toString();
        }

    }

//    public static void longInfo(String tag, String str) {
//        if(str.length() > 4000) {
//            Log.i(tag, str.substring(0, 4000));
//            longInfo(tag, str.substring(4000));
//        } else
//            Log.i(tag, str);
//    }




}