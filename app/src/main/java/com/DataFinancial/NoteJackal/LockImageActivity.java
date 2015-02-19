package com.DataFinancial.NoteJackal;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class LockImageActivity extends ActionBarActivity implements PointCollectorListener {

	private PointCollector pointCollector = new PointCollector();
	private DatabasePasspoints db = new DatabasePasspoints(this);
	public static final String PASSPOINTS_SET = "PASSPOINTS_SET";
	private static final int POINT_CLOSENESS = 80;
	public static final String SHARED_PREF_FILE = "NoteJackalSharedPreferences";	
	
	public static ImageView activityImageView;
	private BitmapFactory.Options options;
	private Bitmap reusedBitmap;
	//private List<Point> pointsTouched;
	//private File imageFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		
		//setPassPointsSaved(false);
		
		addTouchListener();
		pointCollector.setListener(this);
		
		activityImageView = (ImageView)findViewById(R.id.touch_image);
		////Log.d(MainActivity.DEBUGTAG, "imageview.height=" + activityImageView.getMeasuredHeight());
		////Log.d(MainActivity.DEBUGTAG, "imageview.width=" + activityImageView.getMeasuredWidth());
		
		//File picsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File picsDirectory = getFilesDir();
		File imageFile = new File(picsDirectory, getString(R.string.PASSPOINTS_PHOTO));
		//Log.d(MainActivity.DEBUGTAG, "imageFile1=" + imageFile);
		
		Boolean passPointsSet;
		if (!imageFile.exists()) {
			
			LockImageActivity.activityImageView.setImageResource(R.drawable.default_passpoints_image);
			
			
			passPointsSet = false;
			
			//Log.d(MainActivity.DEBUGTAG, "image file does not exist");
			
			Bitmap bm = BitmapFactory.decodeResource( getResources(), R.drawable.default_passpoints_image);
			
		    FileOutputStream outStream;
			try {
				outStream = new FileOutputStream(imageFile);
			    bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			    outStream.flush();
			    outStream.close();
			} catch (FileNotFoundException e) {
				//Log.d(MainActivity.DEBUGTAG, "Problem copying default image..." + e.getMessage());
			} catch (IOException e) {
				//Log.d(MainActivity.DEBUGTAG, "Problem copying default image..." + e.getMessage());
			}
		
			
//			Intent i = new Intent(ImageActivity.this, Password.class);
//			startActivity(i);		
//			//Log.d(MainActivity.DEBUGTAG, "afterStartactivity");
//			finish();
		}
		else {
			//create and reuse bitmap memory to prevent getting out of memory exception 
			//set the size to option, the images we will load by using this option
			options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			
			BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
			int imageHeight = options.outHeight;
			int imageWidth = options.outWidth;
			String imageType = options.outMimeType;
				
			//Log.d(MainActivity.DEBUGTAG, "imageHeight=" + imageHeight);
			//Log.d(MainActivity.DEBUGTAG, "imageWidth=" + imageWidth);
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

			int inSampleSize = calculateInSampleSize(options, width, height);
			////Log.d(MainActivity.DEBUGTAG, "inSampleSize=" + inSampleSize);
			
			// we will create empty bitmap by using the option
			reusedBitmap = Bitmap.createBitmap(options.outWidth, options.outHeight, Bitmap.Config.RGB_565);  
			
			// set the option to allocate memory for the bitmap
			options.inJustDecodeBounds = false;
			options.inSampleSize = inSampleSize;
			options.inMutable = true;
			options.inBitmap = reusedBitmap;
			
			reusedBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
			
			if (reusedBitmap != null) {
				reusedBitmap = fixOrientation(reusedBitmap);
				LockImageActivity.activityImageView.setImageBitmap(reusedBitmap);
			} else {
				Resources res = getResources(); 
				LockImageActivity.activityImageView.setImageDrawable(res.getDrawable(R.drawable.default_passpoints_image));
			}
					
			SharedPreferences prefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
			passPointsSet = prefs.getBoolean(PASSPOINTS_SET,  false);
			
			
		}
			
		////Log.d(MainActivity.DEBUGTAG, "PassPointsSet 1=" + passPointsSet);
		if (!passPointsSet) {
			showSetPasspointsPrompt();			
		}
		
	}

	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }

    return inSampleSize;
	}
	
	
	 public Bitmap fixOrientation(Bitmap photo) {
	     if (photo.getWidth() > photo.getHeight()) {
	         Matrix matrix = new Matrix();
	         matrix.postRotate(90);
	         photo = Bitmap.createBitmap(photo , 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
	     }
	     
	     return photo;
	 }	
	 
	 
	private void showSetPasspointsPrompt() {
		
		AlertDialog.Builder builder = new Builder(this);
		
		builder.setPositiveButton("OK", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {				
				
			}
		});		
				
		builder.setTitle("Set Passpoints");
		builder.setMessage("Touch 3 points on the image to set the passpoint sequence. You must then click the same points to access your notes in the future.");
		
		AlertDialog dlg = builder.create();
		
		dlg.show();
				
		////Log.d(MainActivity.DEBUGTAG, "Show setpoints prompt");
	}

	private void addTouchListener() {
		
		ImageView image = (ImageView)findViewById(R.id.touch_image);
		image.setOnTouchListener(pointCollector);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return false;		
	}
	
	private void savePassPoints( final List<Point> points) {
		////Log.d(MainActivity.DEBUGTAG, "Collected points: " + points.size());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.storing_data);
		
		final AlertDialog dlg = builder.create();
		
		dlg.show();
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				
				db.createPointsTable();
				db.storePoints(points);
				////Log.d(MainActivity.DEBUGTAG, "Points saved..." + points.size());
				
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				
				Boolean passpointsSet = true;
				SharedPreferences prefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(PASSPOINTS_SET,  true);
				editor.commit();
				
				passpointsSet = prefs.getBoolean(PASSPOINTS_SET,  false);
				
				Toast.makeText(LockImageActivity.this, "Passpoints saved...", Toast.LENGTH_LONG).show();
				
				////Log.d(MainActivity.DEBUGTAG, "saved passpointsSet=" + passpointsSet);
				pointCollector.clear();
				dlg.dismiss();
				
				
				//super.onPostExecute(result);
			}			
		};
		
			task.execute();	
		
	}
	
	private void verifyPasspoints( final List<Point> touchedPoints) {
		
		activityImageView = (ImageView)findViewById(R.id.touch_image);
		////Log.d(MainActivity.DEBUGTAG, "imageview.height=" + activityImageView.getMeasuredHeight());
		////Log.d(MainActivity.DEBUGTAG, "imageview.width=" + activityImageView.getMeasuredWidth());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Checking passpoints...");
		
		final AlertDialog dlg = builder.create();
		dlg.show();
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				
				List<Point> savedPoints = db.getPoints();
				////Log.d(MainActivity.DEBUGTAG, "Saved points: " + savedPoints.size());
				
				if (savedPoints.size() != PointCollector.NUM_POINTS || touchedPoints.size() != PointCollector.NUM_POINTS) {
					////Log.d(MainActivity.DEBUGTAG, "Saved points: " + savedPoints.size());
					////Log.d(MainActivity.DEBUGTAG, "Touched points: " + touchedPoints.size());
					
					return false;
				};
				

				////Log.d(MainActivity.DEBUGTAG, "requesting password authentication");
				////Log.d(MainActivity.DEBUGTAG, "touchedPoints 1=" + touchedPoints.toString());
				boolean touchesOK = true;
				
				// check if touches close enough to saved points
				for (int i = 0; i < PointCollector.NUM_POINTS; i++) {
					
					Point saved = savedPoints.get(i);
					Point touched = touchedPoints.get(i);
					
					int xDiff = saved.x - touched.x;
					int yDiff = saved.y - touched.y;
					
					int distSquared = xDiff*xDiff + yDiff*yDiff;
					////Log.d(MainActivity.DEBUGTAG, "Dist squared for point: " + i + ", " + distSquared);
					
					if (distSquared > POINT_CLOSENESS*POINT_CLOSENESS) {
						////Log.d(MainActivity.DEBUGTAG, "Touches not close enough");
						//touchedPoints.clear();
						touchesOK = false;
					}
				}
				
				if (!touchesOK) {
					return false;
				}
 				
				////Log.d(MainActivity.DEBUGTAG, "touches close enough");
				return true;
			}

		
	
			@Override
			protected void onPostExecute(Boolean pass) {
				////Log.d(MainActivity.DEBUGTAG, "Verify task returned: " + pass);
				
				dlg.dismiss();
											
			    //check if touched same point three times. If so then get password
				////Log.d(MainActivity.DEBUGTAG, "touchedPoints 2=" + touchedPoints.toString());
				boolean pwAuthentication = checkSamePoints(touchedPoints);
				if (pwAuthentication) {
					
					touchedPoints.clear();
					
					////Log.d(MainActivity.DEBUGTAG, "pw authentication true, start pw activity");
					Intent i = new Intent(LockImageActivity.this, Password.class);
					reusedBitmap = null;   // set the bitmap top null so gc will get soon as possible 
					startActivity(i);
					
					return;
										
				}
				
				pointCollector.clear();
				
				if (pass == true) {					
					Intent i = new Intent(LockImageActivity.this, MainActivity.class);
					reusedBitmap = null;   // set the bitmap top null so gc will get soon as possible 
					startActivity(i);
				} else {
					touchedPoints.clear();
					SharedPreferences prefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);   //jl
					Boolean passPointsSet = prefs.getBoolean(PASSPOINTS_SET,  false);  //jl
					if (!passPointsSet) {  //jl
						showSetPasspointsPrompt();  //jl
					} else {    //jl
						Toast.makeText(LockImageActivity.this, "Access denied", Toast.LENGTH_LONG).show();
						pointCollector.clear();//jl
					}
					
				}
				
			}
		
			
		};
		
		task.execute();
	}	
	

	boolean checkSamePoints(List<Point> touchedPoints) {
		
		//check if same point touched 3 times. If so then ask for password
		
		Point touched_1 = touchedPoints.get(0);
		Point touched_2 = touchedPoints.get(1);
		Point touched_3 = touchedPoints.get(2);
		
		int diffx_1and2 =  touched_1.x - touched_2.x;
		int diffy_1and2 = touched_1.y - touched_2.y;
		int dist_1and2 = diffx_1and2*diffx_1and2 + diffy_1and2*diffy_1and2;
		
		int diffx_1and3 =  touched_1.x - touched_3.x;
		int diffy_1and3 = touched_1.y - touched_3.y;
		int dist_1and3 = diffx_1and3*diffx_1and3 + diffy_1and3*diffy_1and3;
		
		int diffx_3and2 =  touched_1.x - touched_3.x;
		int diffy_31and2 = touched_1.y - touched_3.y;
		int dist_3and2 = diffx_1and3*diffx_1and3 + diffy_1and2*diffy_1and2;
		
		if (dist_1and2 < POINT_CLOSENESS*POINT_CLOSENESS & dist_1and3 < POINT_CLOSENESS*POINT_CLOSENESS && dist_3and2 < POINT_CLOSENESS*POINT_CLOSENESS) {
			////Log.d(MainActivity.DEBUGTAG, "Touched same point");
			
			return true;
		}
		
		return false;				
	}
	
	
	public void pointsCollected(final List<Point> points) {
		
		//pointsTouched = points;
		
		SharedPreferences prefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
		Boolean passpointsSet = prefs.getBoolean(PASSPOINTS_SET,  false);
				
		////Log.d(MainActivity.DEBUGTAG, "PassPointsSet 2=" + passpointsSet);		
		if (!passpointsSet) {
			////Log.d(MainActivity.DEBUGTAG, "Saving passpoints...");
			savePassPoints(points);
		}
		else {
			////Log.d(MainActivity.DEBUGTAG, "Verifying passpoints...");	
			verifyPasspoints(points);		
		}
	}
	
	protected void setPassPointsSaved(boolean state) {

		SharedPreferences prefs = getSharedPreferences(
				LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(LockImageActivity.PASSPOINTS_SET, state);
		editor.commit();
	}
}
