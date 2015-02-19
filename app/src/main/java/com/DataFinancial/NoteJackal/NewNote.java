package com.DataFinancial.NoteJackal;

//import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewNote extends ActionBarActivity {

	private DatabaseNotes db = new DatabaseNotes(this);
	private DatabaseReminders dbReminders = new DatabaseReminders(this);
	private boolean editFunction = false;
	private Note note = new Note();
	private Menu optionsMenu;
	private AlertDialog confirmDelete;
	private boolean confirmDeleteReminder = false;
	private LocationManager locManager;
	private String phoneContact;
	private String emailContact;
	private String urlContact;
	private boolean help = false;
	private EditText noteText;
    private File imageFile;

    private String noteImagePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_note);

		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setTitle(getResources().getString(R.string.edit_note_activity_title));
		actionBar.setDisplayShowTitleEnabled(true);

		noteText = (EditText) findViewById(R.id.note_text);
		noteText.setGravity(Gravity.TOP);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			editFunction = true;
			note.setId(extras.getInt("id"));
			note.setPriority(extras.getInt("priority"));
			note.setCreateDate(extras.getString("createDate"));
			note.setEditDate(extras.getString("editDate"));
			note.setBody(extras.getString("body"));
			note.setLatitude(extras.getString("latitude"));
			note.setLongitude(extras.getString("longitude"));
            note.setHasReminder(extras.getString("hasReminder"));
			noteText.setText(note.getBody());
			
			
			String strHelp = (String) getResources().getText(R.string.txt_help_search);
			if (note.getBody().length() >= strHelp.length()) {
				if (note.getBody().substring(0, strHelp.length()).equals(strHelp)) {
					help = true;
					noteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);			
					noteText.setText(Html.fromHtml(note.getBody()));				
					noteText.setFocusable(false);				
					actionBar.setTitle(getResources()
							.getString(R.string.help_title));
				}
			}else {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					noteText.requestFocus();
					imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
				
			}
		}

		TextView lblPriority = (TextView) findViewById(R.id.lbl_priority);
		TextView lblCreateDate = (TextView) findViewById(R.id.lbl_create_date);
		TextView lblGeocode = (TextView) findViewById(R.id.lbl_geocoded);
		lblCreateDate.setText(Utils.convertDate(note.getCreateDate(),
				"yy/MM/dd", "MM/dd/yy"));
		lblPriority.setText(note.getPriority() == 0 ? "" : "Priority");
		//Log.d(MainActivity.DEBUGTAG, "latitude=" + note.getLatitude().isEmpty());
		if (!note.getLatitude().isEmpty()) {
			lblGeocode.setText("Geocoded");
		}

		// TextView lblGeocode = (TextView)findViewById(R.id.lbl_geocoded);
		EditText editText = (EditText) findViewById(R.id.note_text);
		int textLength = editText.getText().length();
		editText.setSelection(textLength, textLength);

		Reminder reminder = new Reminder();
		reminder = dbReminders.getReminder(note.getId());
		if (reminder != null) {
			TextView txtReminder = (TextView) findViewById(R.id.lbl_note_reminder);
			txtReminder.setText("Reminder");
			SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
			Date today = new Date();
			try {
				Date reminderDate = df.parse(reminder.getDate() + " "
						+ reminder.getTime());
				Log.d(MainActivity.DEBUGTAG, "today:" + today + "reminderDate:"
						+ reminderDate);
				if (reminderDate.compareTo(today) < 0) {
					txtReminder.setTextColor(getResources().getColor(
							R.color.light_red));
					// txtReminder.setText("Reminder");
				}

			} catch (ParseException e) {
				// e.printStackTrace();
			}
		}

	}

	private String getPhoneContact(String text) {

		String[] lines = text.split("\n");
		if (lines.length == 0) {
			return null;
		}

		for (int i = 0; i < lines.length; i++) {

			if (Utils.isValidPhone(lines[i])) {
				phoneContact = lines[i];
				return lines[i];
			}
		}
		return null;

	}

	private String getEmailContact(String text) {

		String[] lines = text.split("\n");
		if (lines.length == 0) {
			return null;
		}

		for (int i = 0; i < lines.length; i++) {

			if (Utils.isValidEmail(lines[i])) {
				emailContact = lines[i];
				return lines[i];
			}
		}
		return null;

	}

	private String getUrlContact(String text) {

		String[] lines = text.split("\n");
		if (lines.length == 0) {
			return null;
		}

		for (int i = 0; i < lines.length; i++) {

			if (Utils.isValidURL(lines[i])) {
				urlContact = lines[i];
				return lines[i];
			}
		}
		return null;

	}

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first

	}

	@Override
	public Intent getSupportParentActivityIntent() {
		 super.onResume(); // Always call the superclass method first
			//Log.d(MainActivity.DEBUGTAG, "help = " + help);
		 
		 Intent i = new Intent(NewNote.this, MainActivity.class);
		 if (help) {
			i.putExtra("help", "true");				
		 } 

		
		return i;
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (editFunction == false) {
			getMenuInflater().inflate(R.menu.menu_new_note, menu);
		} else {
			getMenuInflater().inflate(R.menu.menu_edit_note, menu);
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		optionsMenu = menu;

		if (help) {
			// optionsMenu.findItem(R.id.menu_delete).setVisible(false);
			// optionsMenu.findItem(R.id.menu_save).setVisible(false);
		}

		if (editFunction == true) {
			if (note.getLatitude().isEmpty() || note.getLongitude().isEmpty()) {
				// optionsMenu.getItem(R.id.menu_map).setVisible(false);
				optionsMenu.findItem(R.id.menu_map).setVisible(false);
			}

			phoneContact = getPhoneContact(note.getBody());
			if (phoneContact == null) {
				optionsMenu.findItem(R.id.menu_call_phone).setVisible(false);
			}

			emailContact = getEmailContact(note.getBody());
			if (emailContact == null) {
				optionsMenu.findItem(R.id.menu_send_email).setVisible(false);
			}

			urlContact = getUrlContact(note.getBody());
			if (urlContact == null) {
				optionsMenu.findItem(R.id.menu_goto_webpage).setVisible(false);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.menu_save) {

			saveNote();

			return true;
		}

		if (id == R.id.menu_send_note) {

			Intent i = new Intent(NewNote.this, SendNote.class);
			i.putExtra("id", note.getId());
			i.putExtra("priority", Integer.toString(note.getPriority()));
			i.putExtra("createDate", note.getCreateDate());
			i.putExtra("editDate", note.getEditDate());
			i.putExtra("body", note.getBody());
			startActivity(i);

			return true;
		}

		if (id == R.id.menu_delete) {

			buildConfirmDeleteDialog();
			confirmDelete.show();

			return true;
		}

		if (id == R.id.menu_priority) {

			note.setPriority((note.getPriority() + 1) % 2);

			return true;
		}

		if (id == R.id.menu_reminder) {

			Intent i = new Intent(NewNote.this, ReminderActivity.class);
			i.putExtra("id", note.getId());
			startActivity(i);

			return true;
		}

		if (id == R.id.menu_geotag) {

			GetLocation geoTagger = new GetLocation(this);
			Location loc = geoTagger.getLocation();

			if (loc != null) {
				double lat = loc.getLatitude();
				double lon = loc.getLongitude();

				EditText v = (EditText) findViewById(R.id.note_text);
				// noteText.setText("in menu geo_ tag: lat = " + lat + "lon = "
				// + lon);

				note.setLatitude(Double.toString(lat));
				note.setLongitude(Double.toString(lon));

				noteText.setText("in menu geo_ tag: lat = "
						+ note.getLatitude() + "lon = " + note.getLongitude());
				long rowId = db.updateNote(note);

				if (rowId != -1) {
					Toast.makeText(NewNote.this, "Note location updated...",
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(NewNote.this,
							"Unable to update location...", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				Toast.makeText(NewNote.this, "Unable to get location...",
						Toast.LENGTH_LONG).show();
			}

			return true;
		}

		if (id == R.id.menu_map) {
			Double lat = 0.0;
			Double lon = 0.0;

			// GetLocation geoTagger = new GetLocation(this);
			// Location loc = geoTagger.getLocation();
			// Log.d(MainActivity.DEBUGTAG, "note in menu_map = " +
			// note.toString());
			if (!note.getLatitude().isEmpty() && !note.getLongitude().isEmpty()) {
				lat = Double.parseDouble(note.getLatitude());
				lon = Double.parseDouble(note.getLongitude());
			} else {
				Toast.makeText(NewNote.this,
						"Unable to show map, location unavailable...",
						Toast.LENGTH_LONG).show();
			}
			// Log.d(MainActivity.DEBUGTAG, "chk in newnote 2...");
			EditText v = (EditText) findViewById(R.id.note_text);
			noteText.setText("in menu_map: lat = " + lat + "lon = " + lon);
			// Log.d(MainActivity.DEBUGTAG, "chk in newnote 3...");
			Intent i = new Intent(NewNote.this, MapActivity.class);
			i.putExtra("latitude", lat);
			i.putExtra("longitude", lon);
			i.putExtra("title", "Note: " + note.getEditDate());
			i.putExtra("text", note.getBody());
			startActivity(i);

			return true;
		}

		if (id == R.id.menu_call_phone) {

			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:" + phoneContact));
			startActivity(intent);
		}

		if (id == R.id.menu_send_email) {
			String[] TO = { "jluper@triad.rr.com" };
			;
			String[] CC;

			TO[0] = emailContact;
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setData(Uri.parse("mailto:"));
			emailIntent.setType("text/plain");
			Log.d(MainActivity.DEBUGTAG, "valid email: " + emailContact);
			// String emailSubject = "";
			// String emailText = "";
			emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
			emailIntent.putExtra(Intent.EXTRA_TEXT, "test text");
			Intent intent = Intent.createChooser(emailIntent, "Send email...");
			startActivity(intent);

		}

		if (id == R.id.menu_goto_webpage) {

			Intent i = new Intent(NewNote.this, WebviewActivity.class);
			i.putExtra("url", urlContact);

			startActivity(i);
		}

        if (id == R.id.menu_add_image) {

            getNoteImage();
//            Intent i = new Intent(NewNote.this, WebviewActivity.class);
//            i.putExtra("url", urlContact);
//
//            startActivity(i);
        }

        if (id == R.id.menu_view_image) {

            Intent i = new Intent(NewNote.this, NoteImageActivity.class);
            i.putExtra("image_path", noteImagePath);
            startActivity(i);
        }
		return super.onOptionsItemSelected(item);
	}

	private long saveNote() {

		EditText editText = (EditText) findViewById(R.id.note_text);
		String noteBody = editText.getText().toString();
		note.setBody(noteBody);

		// String s = note.getCreateDate() + ", " + note.getPriority() + ", " +
		// note.getEditDate() + ", " + note.getBody();
		// //Log.d(MainActivity.DEBUGTAG, s);

		long noteId = -1;
		// //Log.d(MainActivity.DEBUGTAG, "note id= " + note.getId());
		try {
			if (editFunction == true) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd");
				Date curr_date = new Date();
				note.setEditDate(dateFormat.format(curr_date));
				noteId = db.updateNote(note);
				Toast.makeText(NewNote.this, "Note edited...",
						Toast.LENGTH_SHORT).show();
			} else {
                Log.d(MainActivity.DEBUGTAG, "note in SaveNote = " + note.toString());
				noteId = db.addNote(note);
               // Log.d(MainActivity.DEBUGTAG, " note afte add = " + db.getNote((int)noteId).toString());
				Toast.makeText(NewNote.this, "Note added...", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(NewNote.this, "Unable to add/update note...",
					Toast.LENGTH_LONG).show();
		}

		Intent i = new Intent(NewNote.this, MainActivity.class);
		startActivity(i);

		return noteId;

	}

	public void buildConfirmDeleteDialog() {

		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
		dlgBuilder.setTitle("Confirm Delete");
		dlgBuilder.setIcon(R.drawable.btn_check_buttonless_on);
		dlgBuilder.setMessage(R.string.dialog_delete_note);
		dlgBuilder.setCancelable(true);
		dlgBuilder.setPositiveButton(R.string.dialog_positive,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						db.deleteNote(note.getId());

						dialog.cancel();
						Intent i = new Intent(NewNote.this, MainActivity.class);
						startActivity(i);

					}
				});
		dlgBuilder.setNegativeButton(R.string.dialog_negative,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						dialog.cancel();
					}
				});

		confirmDelete = dlgBuilder.create();
	}

	public void buildConfirmLocSettingsDialog() {

		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
		dlgBuilder.setTitle("Confirm Enable Location Settings");
		dlgBuilder.setIcon(R.drawable.btn_check_buttonless_on);
		dlgBuilder.setMessage(R.string.dialog_location_settings);
		dlgBuilder.setCancelable(true);
		dlgBuilder.setPositiveButton(R.string.dialog_positive,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						Log.d(MainActivity.DEBUGTAG,
								"confirm location settings dialog");
						dialog.cancel();

						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);

						// LocationManager locManager = (LocationManager)
						// getSystemService(Context.LOCATION_SERVICE);
						// if (locManager != null) {
						// Log.d(MainActivity.DEBUGTAG, " locationManager = " +
						// locManager.toString());
						// Location location =
						// locManager.getLastKnownLocation(LOCATION_SERVICE);
						// Log.d(MainActivity.DEBUGTAG, "location = " +
						// location.toString());
						//
						// double lat = location.getLatitude();
						// double lon = location.getLongitude();
						// Log.d(MainActivity.DEBUGTAG, "lat = " + lat +
						// "lon = " + lon);
						// }
						// else {
						// Log.d(MainActivity.DEBUGTAG,
						// " locationManager = null");
						// }

						// Intent i = new Intent(NewNote.this,
						// MainActivity.class);
						// startActivity(i);

					}
				});
		dlgBuilder.setNegativeButton(R.string.dialog_negative,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						dialog.cancel();
					}
				});

		confirmDelete = dlgBuilder.create();
	}

    private void getNoteImage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.replace_lock_image, null);
        builder.setTitle(R.string.replace_lock_image);
        builder.setView(v);
        builder.setMessage(R.string.replace_image_dialog);

        final AlertDialog dlg = builder.create();
        dlg.show();

        Button takePhoto = (Button) dlg.findViewById(R.id.take_photo);
        Button browseGallery = (Button) dlg.findViewById(R.id.browse_gallery);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePhoto();
            }
        });

        browseGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                browseGallery();

            }
        });

    }

    private void browseGallery() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, MainActivity.BROWSE_GALLERY_REQUEST);
    }

    private void takePhoto() {

        File picsDirectory = getFilesDir();
        imageFile = new File(picsDirectory,
                getString(R.string.PASSPOINTS_PHOTO));

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(i, MainActivity.PHOTO_TAKEN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == MainActivity.BROWSE_GALLERY_REQUEST) {
            if (intent != null) {
                String[] columns = { MediaStore.Images.Media.DATA };
                Uri imageUri = intent.getData();
                Cursor cursor = getContentResolver().query(imageUri, columns, null, null, null);

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(columns[0]);
                noteImagePath = cursor.getString(columnIndex);
                cursor.close();

                Uri image = Uri.parse(noteImagePath);

                Toast.makeText(this, "Gallery result" + image, Toast.LENGTH_LONG).show();

//                try {
//                    copyImageFile(imagePath);
//                } catch (IOException e) {
//                    Toast.makeText(this,"Unable to copy image file from gallery...", Toast.LENGTH_LONG).show();
//                }

                //Intent i = new Intent(MainActivity.this, ImageActivity.class);
                //startActivity(i);
            } else {
                Toast.makeText(this, "Gallery result: no data", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == MainActivity.PHOTO_TAKEN_REQUEST) {

            if (intent != null) {
                Bitmap photo = BitmapFactory.decodeFile(imageFile
                        .getAbsolutePath());

                if (photo != null) {
                   Toast.makeText(this, R.string.PHOTO_NOT_SAVED, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


}
