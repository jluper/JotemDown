package com.DataFinancial.NoteJackal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends ActionBarActivity {

    int groupId;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setTitle(getResources().getString(R.string.map_activity_title));		
		actionBar.setDisplayShowTitleEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		Double latitude = extras.getDouble("latitude");
		Double longitude = extras.getDouble("longitude");
		String title = extras.getString("title");		
		String text = extras.getString("text");
        groupId = extras.getInt("group");
        groupName = extras.getString("group_name");
        sortCol = extras.getString("sort_col");
        sortName = extras.getString("sort_name");
        sortDir = extras.getString("sort_dir");

		if (latitude != 0.0 && longitude != 0.0) {
			
			GoogleMap map;
	
			map = ((MapFragment) 
			        getFragmentManager().findFragmentById(R.id.map)).getMap();

			if (map != null)
				map.setMyLocationEnabled(true);
			
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			
			UiSettings mapSettings = map.getUiSettings();
			
			mapSettings.setZoomControlsEnabled(true);

			LatLng noteLoc = new LatLng(latitude, longitude);

			Marker noteMarker = map.addMarker(new MarkerOptions()
			                          .position(noteLoc)
			                          .title(title)
			                          .snippet(text));

			CameraPosition cameraPosition = new CameraPosition.Builder()
		    .target(noteLoc)
		    .zoom(15)
		    .bearing(70)
		    .tilt(25)
		    .build();

		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}
		else {
			Toast.makeText(MapActivity.this,"Location not available...",Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.map_demo, menu);
		return true;
	}

    @Override
    public Intent getSupportParentActivityIntent() {
        super.onResume();

        Intent i = new Intent(MapActivity.this, MainActivity.class);

        i.putExtra("group", groupId);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;

    }
}
