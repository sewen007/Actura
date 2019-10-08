package com.zkc.commandmcu;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {
    public static double latitude;

    public static double longitude;
    public static Location location;

    @Override
    public void onLocationChanged(Location loc)
    {
        location = loc;
        loc.getLatitude();
        loc.getLongitude();
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
    }
    @Override
    public void onProviderDisabled(String provider)
    {
    }
    @Override
    public void onProviderEnabled(String provider)
    {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }
    protected boolean isRouteDisplayed() {
        return false;
    }
}
