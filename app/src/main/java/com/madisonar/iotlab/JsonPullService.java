package com.madisonar.iotlab;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * JsonPullService is a small class to handle IO for MadisonAR.
 * @author  Joseph Lukasik & Nicholas Sielicki
 * @version .1
 * @since 2014-11-22
 * <p>
 *     This is a small class which handles constructing queries for
 *     the MadisonAR server and passes the JSON back to the caller.
 * </p>
 */
public class JsonPullService extends IntentService{

    /** Constructor. Simply calls superclass constructor with class name.
     */
    public JsonPullService()
    {
        super("JsonPullService");
    }

    /** The handler when this class is invoked.
     * @param general android Intent.
     */
    @Override
    protected void onHandleIntent(Intent intentP) {

        // make a location manager from the context we inherit from intentP.
        LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        // grab the current location from LM
        Location currLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // make an HttpClient object.
        HttpClient httpC = new DefaultHttpClient();
        // The URL at which we are making requests.
        String url = "http://www.madisonar.com:8000/locationService";
        // Feed the HTTP post the URL.
        HttpPost httpP = new HttpPost(url);
        try
        {
            // To hold our paramters.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            // MadisonAR server expects two key value pairs, lat and lng to determine location and
            // return JSON corresponding to information that the application needs.
            params.add(new BasicNameValuePair("lat", String.valueOf(currLocation.getLatitude())));
            params.add(new BasicNameValuePair("lng", String.valueOf(currLocation.getLongitude())));
            // Update our HTTPPost with the new parameters
            httpP.setEntity(new UrlEncodedFormEntity(params));
            // Execute the query into a new HttpResponse.
            HttpResponse response = httpC.execute(httpP);
            Intent responseIntent = new Intent();
            responseIntent.setAction(NearbyBuilder.ResponseReceiver)
            response.toString();
        }
        catch (Exception e)
        {
            // TODO: Tell the intent it failed so that our caller knows well enough.
        }
    }
}
