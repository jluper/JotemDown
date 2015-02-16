package com.DataFinancial.NoteJackal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
//import android.location.Criteria; 
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
//import android.widget.TextView; 
import android.widget.Toast;

//import com.yannick.diary.R; 

public class GetLocation implements LocationListener {
	private static LocationManager locationManager;
	private static Context mContext;
	private static String provider;
	private double GPSLat;
	private double GPSLon;

	public GetLocation(Context context) {
		mContext = context;
	}

	public void getLatitude(double GPSLat) {
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this,	null);
		Location location = locationManager.getLastKnownLocation(provider);
		this.setGPSLat((location.getLatitude()));		
	};

	public void getLongitude(double GPSLon) {
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this,	null);
		Location location = locationManager.getLastKnownLocation(provider);
		this.GPSLon = (location.getLongitude());
		
	};

	public Location getLocation() {
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		} else {
			// FAIL
		}
		locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this,	null);
		return locationManager.getLastKnownLocation(provider);
	}

	// try this by doing GetLocation.getLocation().getLongitude();

	public double getGPSLat() {
		return GPSLat;
	}

	public void setGPSLat(double gPSLat) {
		GPSLat = gPSLat;
	}

	public double getGPSLon() {
		return GPSLon;
	}

	public void setGPSLon(double gPSLon) {
		GPSLon = gPSLon;
	}

	public void onLocationChanged(Location location) {		

	}

	public void onProviderDisabled(String provider) {		

	}

	public void onProviderEnabled(String provider) {
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {		

	}
}
