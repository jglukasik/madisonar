package com.madisonar.iotlab;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

import com.madisonar.iotlab.model.Building;
import com.madisonar.iotlab.model.Response;
import com.madisonar.madisonar.R;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ResponseManager implements OrientationManager.OnChangedListener
{
    private Context mContext;
    private Response mCurrentResp;
    private LocationManager mLocationManager;
    private OrientationManager mOrientationManager;

    public ResponseManager(Context context, LocationManager locationManager,
                           OrientationManager orientationManager)
    {
        Log.w("MADISONAR", "Constructor for ResponseManager called.");
        mContext = context;
        mLocationManager = locationManager;
        mOrientationManager = orientationManager;
        mOrientationManager.addOnChangedListener(this);
    }


    private class updateCurrentRespTask extends AsyncTask<Location, Void, Response>
    {
        @Override
        protected Response doInBackground(Location...params) {
            try{
                Log.w("MADISONAR", "Started Background Job");
                String rawJSON = madisonarPOST(params[0]);
                return parseMadisonarJSON(rawJSON);
            }
            catch(Exception e){
                Log.w("MADISONAR", "Error: ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Response newResp) {
            if (newResp != null){
                mCurrentResp = newResp;
                for (Building b : mCurrentResp.getBuildings()){
                    Log.w("MADISONAR", "Building found: "+b.getName());
                }
            }
            else{
                Log.w("MADISONAR", "Error: Response was null");
            }
        }

        @Override protected void onCancelled() {
            //TODO: Log.
        }

        private String madisonarPOST(Location currLocation)
                throws IOException, ClientProtocolException, UnsupportedEncodingException,
                        IllegalArgumentException
        {
            if (currLocation == null)
            {
                throw new IllegalArgumentException();
            }
            if (currLocation.getLatitude() == 43.075171 && currLocation.getLongitude() == -89.402343 ){
                Log.w("MADISONAR", "Using static JSON");
                InputStream is = mContext.getResources().openRawResource(R.raw.bascomesponsereal);
                StringBuffer buffer = new StringBuffer();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                        buffer.append('\n');
                    }
                } catch (IOException e) {
                    Log.e("MADISONAR", "Could not read bascom resource", e);
                    return null;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            Log.e("MADISONAR", "Could not close bascom resource stream", e);
                        }
                    }
                }
                return buffer.toString();
            }
            HttpClient httpC = new DefaultHttpClient();
            String url = "http://www.madisonar.com:8000/locationService";
            HttpPost httpP = new HttpPost(url);
            // To hold our paramters.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            // MadisonAR server expects two key value pairs, lat and lng to determine location and
            // return JSON corresponding to information that the application needs.
            params.add(new BasicNameValuePair("lat", "43.075171"));
            params.add(new BasicNameValuePair("lng", "-89.402343"));
            // Update our HTTPPost with the new parameters
            httpP.setEntity(new UrlEncodedFormEntity(params));
            // Execute the command and get the entity representing it so we can get the content.
            HttpEntity entity = httpC.execute(httpP).getEntity();
            // This strips the content of the POST and returns this raw string.
            return EntityUtils.toString(entity);
        }

        private Response parseMadisonarJSON(String rawJSON) throws JSONException
        {
            Response toReturn;
            JSONObject container = new JSONObject(rawJSON);
            JSONObject response = container.getJSONObject("response");
            JSONArray buildingJSONArray = response.getJSONArray("buildings");
            ArrayList<Building> newBuildings = new ArrayList<Building>();
            for (int i = 0; i < buildingJSONArray.length(); i++)
            {
                JSONObject currentBuilding = buildingJSONArray.getJSONObject(i);
                Building toAdd = new Building(
                        currentBuilding.getDouble("lLat"),
                        currentBuilding.getDouble("lLng"),
                        currentBuilding.getDouble("rLat"),
                        currentBuilding.getDouble("rLng"),
                        (float) currentBuilding.getDouble("rHeading"),
                        (float) currentBuilding.getDouble("lHeading"),
                        currentBuilding.getString("bName"));
                newBuildings.add(toAdd);
            }
            toReturn = new Response(newBuildings,
                            response.getDouble("unixTime"),
                            response.getDouble("requestLat"),
                            response.getDouble("requestLng"));
            return toReturn;
        }
    }

    public void forceUpdateCurrentRespTask()
    {
        Location BascomHill = new Location("");
        BascomHill.setLatitude(43.075171);
        BascomHill.setLongitude(-89.402343);
        new updateCurrentRespTask().execute(BascomHill);
        // Edit out for demo purposes.
        // new updateCurrentRespTask().execute(mOrientationManager.getLocation());
    }

    @Override
    public void onOrientationChanged(OrientationManager orientationManager) {
        // We do not ever use the compass.
    }

    @Override
    public void onLocationChanged(OrientationManager orientationManager) {
        new updateCurrentRespTask().execute(orientationManager.getLocation());
    }

    @Override
    public void onAccuracyChanged(OrientationManager orientationManager) {
        // Nothing we can do about it.
    }

    public Response getCurrentResp()
    {
        return mCurrentResp;
    }

}
