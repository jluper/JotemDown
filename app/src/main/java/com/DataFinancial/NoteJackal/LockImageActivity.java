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

   // public static final String PASSPOINTS_SET = "PASSPOINTS_SET";
    public static final String PRIVATE = "Private";
    public static final String SHARED_PREF_FILE = "NoteJackalSharedPreferences";
    private static final int POINT_CLOSENESS = 80;
    public static ImageView activityImageView;
    private PointCollector pointCollector = new PointCollector();
    private DatabasePasspoints db = new DatabasePasspoints(this);
    private BitmapFactory.Options options;
    private Bitmap reusedBitmap;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isPrivate()) {
            Intent i = new Intent(LockImageActivity.this, MainActivity.class);
            startActivity(i);
        }

        setContentView(R.layout.activity_image);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);

        addTouchListener();
        pointCollector.setListener(this);

        activityImageView = (ImageView) findViewById(R.id.touch_image);

        File picsDirectory = getFilesDir();
        File imageFile = new File(picsDirectory, getString(R.string.PASSPOINTS_PHOTO));

        Boolean passPointsSet;
        if (!imageFile.exists()) {

            LockImageActivity.activityImageView.setImageResource(R.drawable.default_passpoints_image);

            passPointsSet = false;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_passpoints_image);

            FileOutputStream outStream;
            try {
                outStream = new FileOutputStream(imageFile);
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Exception (1) reading lock image file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "Exception (2) reading lock image file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            //create and reuse bitmap memory to prevent getting out of memory exception
            //set the size to option, the images we will load by using this option
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            String imageType = options.outMimeType;

            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            int inSampleSize = calculateInSampleSize(options, width, height);

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
        }
        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
        passPointsSet = prefs.getBoolean(MainActivity.PASSPOINTS_SET, false);
        if (!passPointsSet) {
            showSetPasspointsPrompt();
        }
    }

    private boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    public boolean isPrivate() {

        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        boolean isPrivate = prefs.getBoolean(LockImageActivity.PRIVATE, true);

        return isPrivate;
    }

    public Bitmap fixOrientation(Bitmap photo) {
        if (photo.getWidth() > photo.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
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
    }


    private void addTouchListener() {

        ImageView image = (ImageView) findViewById(R.id.touch_image);
        image.setOnTouchListener(pointCollector);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return false;
    }

    private void savePassPoints(final List<Point> points) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.storing_data);

        final AlertDialog dlg = builder.create();

        dlg.show();

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {

                db.createPointsTable();
                db.storePoints(points);

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                Boolean passpointsSet = true;
                SharedPreferences prefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(MainActivity.PASSPOINTS_SET, true);
                editor.apply();

               //passpointsSet = prefs.getBoolean(MainActivity.PASSPOINTS_SET, false);

                Toast.makeText(LockImageActivity.this, "Passpoints saved...", Toast.LENGTH_LONG).show();

                pointCollector.clear();
                dlg.dismiss();
            }
        };

        task.execute();
    }

    private void verifyPasspoints(final List<Point> touchedPoints) {

        activityImageView = (ImageView) findViewById(R.id.touch_image);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Checking passpoints...");

        final AlertDialog dlg = builder.create();
        dlg.show();

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                List<Point> savedPoints = db.getPoints();

                if (savedPoints.size() != PointCollector.NUM_POINTS || touchedPoints.size() != PointCollector.NUM_POINTS) {

                    return false;
                }
                ;

                boolean touchesOK = true;

                // check if touches close enough to saved points
                for (int i = 0; i < PointCollector.NUM_POINTS; i++) {

                    Point saved = savedPoints.get(i);
                    Point touched = touchedPoints.get(i);

                    int xDiff = saved.x - touched.x;
                    int yDiff = saved.y - touched.y;

                    int distSquared = xDiff * xDiff + yDiff * yDiff;

                    if (distSquared > POINT_CLOSENESS * POINT_CLOSENESS) {

                        touchesOK = false;
                    }
                }

                if (!touchesOK) {
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean pass) {

                dlg.dismiss();

                //check if touched same point three times. If so then get password
                boolean pwAuthentication = checkSamePoints(touchedPoints);
                if (pwAuthentication) {

                    touchedPoints.clear();

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
                    SharedPreferences prefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
                    Boolean passPointsSet = prefs.getBoolean(MainActivity.PASSPOINTS_SET, false);
                    //Log.d(MainActivity.DEBUGTAG, "PassPointsEt = " + passPointsSet);
                    if (!passPointsSet) {
                        showSetPasspointsPrompt();
                    } else {
                        Toast.makeText(LockImageActivity.this, "Access denied", Toast.LENGTH_LONG).show();
                        pointCollector.clear();
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

        int diffx_1and2 = touched_1.x - touched_2.x;
        int diffy_1and2 = touched_1.y - touched_2.y;
        int dist_1and2 = diffx_1and2 * diffx_1and2 + diffy_1and2 * diffy_1and2;

        int diffx_1and3 = touched_1.x - touched_3.x;
        int diffy_1and3 = touched_1.y - touched_3.y;
        int dist_1and3 = diffx_1and3 * diffx_1and3 + diffy_1and3 * diffy_1and3;

        int diffx_3and2 = touched_1.x - touched_3.x;
        int diffy_31and2 = touched_1.y - touched_3.y;
        int dist_3and2 = diffx_1and3 * diffx_1and3 + diffy_1and2 * diffy_1and2;

        if (dist_1and2 < POINT_CLOSENESS * POINT_CLOSENESS & dist_1and3 < POINT_CLOSENESS * POINT_CLOSENESS && dist_3and2 < POINT_CLOSENESS * POINT_CLOSENESS) {
            return true;
        }

        return false;
    }


    public void pointsCollected(final List<Point> points) {

        //pointsTouched = points;

        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
        Boolean passpointsSet = prefs.getBoolean(MainActivity.PASSPOINTS_SET, false);


        if (!passpointsSet) {
            savePassPoints(points);

            //don't require the user to re-enter the passpoints after just choosing new ones.
            Intent i = new Intent(LockImageActivity.this, MainActivity.class);
            reusedBitmap = null;   // set the bitmap top null so gc will get soon as possible
            startActivity(i);
        } else {
            verifyPasspoints(points);
        }
    }

    protected void setPassPointsSaved(boolean state) {

        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(MainActivity.PASSPOINTS_SET, state);

        editor.apply();
    }
}
