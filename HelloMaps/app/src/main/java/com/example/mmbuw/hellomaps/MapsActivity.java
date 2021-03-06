package com.example.mmbuw.hellomaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraChangeListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    EditText mEdit;
    SharedPreferences sPreferences;
    private List<Circle> circles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sPreferences = getSharedPreferences("mySharedPreferences", 0);
        mEdit   = (EditText)findViewById(R.id.editText);
        setUpMapIfNeeded();


        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        mMap.addMarker(new MarkerOptions().position(point).title(mEdit.getText().toString()));
        addToSharedPreferences((point.latitude + ":" + point.longitude + ":" + mEdit.getText().toString()));
        drawCircles(point.latitude,point.longitude);
    }

    public void addToSharedPreferences(String str){
        Set<String> stringHashSet = sPreferences.getStringSet("markerSet", new HashSet<String>());
        Set<String> tempCopySet = new HashSet<String>(stringHashSet);
        tempCopySet.add(str);
        sPreferences.edit().putStringSet("markerSet", tempCopySet).commit();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
       for (Circle circle : circles) {
            double radius = getRadiusOfCircle(circle, cameraPosition);
            circle.setRadius(radius);
        }
    }
    public double getRadiusOfCircle(Circle circle, CameraPosition cameraPosition){
        LatLngBounds screenBoundary = mMap.getProjection().getVisibleRegion().latLngBounds;
        double diff = distanceBetween(cameraPosition.target,circle.getCenter());
        if (screenBoundary.contains(circle.getCenter()))
            return 0;
        else
            return diff*300000;
    }
    public void drawCircles(double latitude, double longitude)
    {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(latitude, longitude));
        circleOptions.radius(0);
        circleOptions.strokeColor(Color.RED);
        Circle circle = mMap.addCircle(circleOptions);
        circles.add(circle);
    }

    private double distanceBetween(LatLng firstPoint, LatLng SecondPoint) {
        float[] result = new float[1];
        Location.distanceBetween(firstPoint.latitude, firstPoint.longitude, SecondPoint.latitude, SecondPoint.longitude, result);
        return result[0];
    }


    public void loadMarkersAndCircles()
    {
        Set<String> stringHashSet = sPreferences.getStringSet("markerSet", new HashSet<String>());
        String[] markerData = null;
        for (String s : stringHashSet) {
            markerData = s.split(":");
            double latitude = Double.parseDouble(markerData[0]);
            double longitude = Double.parseDouble(markerData[1]);
            String message = "";
            if (markerData.length == 3){
                message = markerData[2];
            }
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(message));
            drawCircles(latitude, longitude);
        }
    }

    private void setUpMap() {
        // Enable MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Your Current Location!!"));
        loadMarkersAndCircles();
    }
}
