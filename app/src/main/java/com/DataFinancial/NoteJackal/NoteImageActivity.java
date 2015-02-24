package com.DataFinancial.NoteJackal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class NoteImageActivity extends ActionBarActivity {

    public static ImageView noteImageView;
    private BitmapFactory.Options options;
    private Bitmap reusedBitmap;
    private String noteImagePath;
    private File imageFile;
    private Note note = new Note();

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
        setContentView(R.layout.note_image_activity);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle(getResources().getString(R.string.title_noteimage_activity));
        actionBar.setDisplayShowTitleEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            noteImagePath = extras.getString("image");
            note.setId(extras.getInt("id"));
            note.setPriority(extras.getInt("priority"));
            note.setCreateDate(extras.getString("createDate"));
            note.setEditDate(extras.getString("editDate"));
            note.setBody(extras.getString("body"));
            note.setLatitude(extras.getString("latitude"));
            note.setLongitude(extras.getString("longitude"));
            note.setHasReminder(extras.getString("hasReminder"));
            note.setImage(extras.getString("image"));

        } else {
            Toast.makeText(NoteImageActivity.this, "Image not found.", Toast.LENGTH_SHORT).show();
        }

        noteImageView = (ImageView) findViewById(R.id.note_image);

        imageFile = new File(noteImagePath);

        if (!imageFile.exists()) {
            NoteImageActivity.noteImageView.setScaleType(ImageView.ScaleType.CENTER);
            NoteImageActivity.noteImageView.setImageResource(R.drawable.image_not_found);
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
                NoteImageActivity.noteImageView.setImageBitmap(reusedBitmap);
            } else {
                Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                myBitmap = fixOrientation(myBitmap);
                NoteImageActivity.noteImageView.setImageBitmap(myBitmap);
            }
        }
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        super.onResume(); // Always call the superclass method first

        Intent i = new Intent(NoteImageActivity.this, NewNote.class);
        i.putExtra("id", note.getId());
        i.putExtra("priority", note.getPriority());
        i.putExtra("createDate", note.getCreateDate());
        i.putExtra("editDate", note.getEditDate());
        i.putExtra("body", note.getBody());
        i.putExtra("latitude", note.getLatitude());
        i.putExtra("longitude", note.getLongitude());
        i.putExtra("hasReminder", note.getHasReminder());
        i.putExtra("image", note.getImage());
        startActivity(i);

        return i;
    }

    public Bitmap fixOrientation(Bitmap photo) {

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(noteImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        Log.d(MainActivity.DEBUGTAG, "width = " + photo.getWidth() + " height = " + photo.getHeight() + " orientation = " + rotation);

        if (photo.getWidth() > photo.getHeight() && rotation == ExifInterface.ORIENTATION_ROTATE_90) {
            Log.d(MainActivity.DEBUGTAG, "image width = " + photo.getWidth() + " image ht = " + photo.getHeight());
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
        }

        return photo;
    }

    private void addTouchListener() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }


}
