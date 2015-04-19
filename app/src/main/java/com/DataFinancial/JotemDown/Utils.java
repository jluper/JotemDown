package com.DataFinancial.JotemDown;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import java.io.File;

//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
//import com.google.api.client.http.FileContent;
//import com.google.api.services.drive.Drive;

public class Utils {

    static public String convertDate(String date, String fromFormat, String toFormat) {

        SimpleDateFormat oldDateFormat = new SimpleDateFormat(fromFormat, Locale.getDefault());
        SimpleDateFormat newDateFormat = new SimpleDateFormat(toFormat, Locale.getDefault());
        Date oldDate = new Date();
        Date newDate = new Date();

        try {
            oldDate = oldDateFormat.parse(date);
        } catch (ParseException e) {
            return (date);
        }

        return (newDateFormat.format(oldDate));
    }

    static public boolean isValidPhone(String target) {

        // the check in the return statement was considering a string of digits as valid. ex: "12345"
        if (target.length() < 10) {
            return false;
        }

        return  !TextUtils.isEmpty(target) && android.util.Patterns.PHONE.matcher(target).matches();
    }

    static public boolean isValidEmail(CharSequence target) {

        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    static public boolean isValidURL(String pUrl) {

        URL u = null;
        try {
            u = new URL(pUrl);
        } catch (MalformedURLException e) {
            return false;
        }
        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }

    public boolean isValidDate(String target) {

        int[] daysInMonth = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        // valid date is in format mm/dd/yy
        String[] dateParts = target.split("/");

        if (dateParts.length != 3) {
            return false;
        }

        if (dateParts[0].isEmpty() || dateParts[1].isEmpty() || dateParts[2].isEmpty())
            return false;

        //check the month
        int mm = Integer.parseInt(dateParts[0]);
        try {
            if (mm < 1 || mm > 12)
                return false;
        } catch (NumberFormatException e) {
            return false;
        }

        //check the day
        try {
            int dd = Integer.parseInt(dateParts[1]);
            if (dd < 1 || dd > daysInMonth[mm - 1])
                return false;
        } catch (NumberFormatException e) {
            return false;
        }

        //check the year
        try {
            int yy = Integer.parseInt(dateParts[2]);
            int curYear = Calendar.getInstance().get(Calendar.YEAR);
            if ((2000 + yy) < curYear || dateParts[2].length() > 2)
                return false;
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public boolean isValidTime(String target) {


        // valid date is in format hh:mm
        String[] timeParts = target.split(":");
        if (timeParts.length != 2) {
            return false;
        }

        if (timeParts[0].isEmpty() || timeParts[1].isEmpty())
            return false;

        //check the hr
        try {
            int hh = Integer.parseInt(timeParts[0]);
            if (hh < 0 || hh > 24)
                return false;
        } catch (NumberFormatException e) {
            return false;
        }

        //check the min
        try {
            int mm = Integer.parseInt(timeParts[1]);
            if (mm < 0 || mm > 60 || timeParts[1].length() != 2)
                return false;
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public String incrementDay(String date) {

        SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd");
        Date remDate;
        try {
            remDate = df.parse(date);
        } catch (ParseException e) {
            return (date);
        }

        Calendar c = Calendar.getInstance();
        c.setTime(remDate);
        c.add(Calendar.DATE, 1);  // number of days to add
        String strDate = df.format(c.getTime());  // dt is now the new date

        return (strDate);
    }


    public String incrementMinute(String time, int min) {

        String[] timeParts = time.split(":");
        String strHour = timeParts[0];
        String strMinute = timeParts[1];
        int intMinute = (Integer.parseInt(strMinute) + min) % 60;
        int intHour = Integer.parseInt(strHour);
        if (intMinute == 0) {
            intHour = (intHour + 1) % 24;
        }

        strHour = String.valueOf(intHour);
        if (strHour.length() == 1) {
            strHour = "0" + strHour;
        }
        if (strMinute.length() == 1) {
            strMinute = "0" + strMinute;
        }

        String newTime = String.valueOf(intHour) + ":" + String.valueOf(intMinute);

        return newTime;
    }

    public void copyFile(java.io.File src, java.io.File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public void copyFile(InputStream src, OutputStream dst) throws IOException {

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = src.read(buf)) > 0) {
            dst.write(buf, 0, len);
        }
        src.close();
        dst.close();
    }

    List<Intent> filterIntents(Context context) {

        List<Intent> targetedShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("message/rfc822");
        //shareIntent.setType("text/plain");
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(shareIntent, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                Intent targetedShareIntent = new Intent(android.content.Intent.ACTION_SEND);
                targetedShareIntent.setType("message/rfc822");
                targetedShareIntent.setPackage(packageName);

                //Log.d(MainActivity.DEBUGTAG, "packagename = " + packageName );

                if (packageName.equals("com.lge.email") || packageName.equals("com.google.android.gm") || packageName.contains("mail")) {
                    targetedShareIntents.add(targetedShareIntent);
                }
            }
        }

        return targetedShareIntents;
    }

    static public void log(String msg) {

        Log.d(MainActivity.DEBUGTAG, msg);
    }

    static public String customizeFilename(String fileName, boolean addDate) {

        String[] nameParts = fileName.split("\\.");

        String strToday = "";

        SimpleDateFormat df = new SimpleDateFormat("MMddyyHHmm");
        Date today = new Date();
        strToday = df.format(today);

        if (addDate) {
            return nameParts[0] + "_" + strToday + "_JED." + nameParts[1];  //appends date and time
        } else {
            return nameParts[0] + "_JED." + nameParts[1];
        }
    }

    static private List<com.google.api.services.drive.model.File> mResultList;
//    private ListView mListView;
//    private String[] mFileArray;
//    private String mDLVal;
//    private ArrayAdapter mAdapter;
//    private static int selectedFilePosition = -1;
    private static Uri mFileUri;
    private static Drive mService;
    private GoogleAccountCredential mCredential;
    private static String filePath = null;
    //private static String token;
    private static boolean gotToken = false;
    Object tokenLockObject = new Object ();

    public void backupNotes(String file, String destination, Context context) {

        makeLocalDatabaseBackup(file, context);

        final String fileName = file;
        final Context ct = context;
        mCredential = GoogleAccountCredential.usingOAuth2(ct, Arrays.asList(DriveScopes.DRIVE));

        Account acct = getAccount(context);
        Log.d(MainActivity.DEBUGTAG, "account = " + acct.type);
        //getToken(acct, context);

        mCredential.setSelectedAccountName(acct.name);
        mService = getDriveService(mCredential);
        Log.d(MainActivity.DEBUGTAG, "creds = " + mCredential.toString());

        Log.d(MainActivity.DEBUGTAG, "acct name = " + acct.name);
        Log.d(MainActivity.DEBUGTAG, "service = " + mService.toString());


        // mService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), mCredential).build();

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d(MainActivity.DEBUGTAG, "check1x");

//                synchronized(tokenLockObject) {
//                    while (!gotToken) {
//                        try {
//                            tokenLockObject.wait();
//                        } catch (InterruptedException e) {
//                        }
//                    }

                    mResultList = new ArrayList<>();
                    mResultList.clear();
                    Log.d(MainActivity.DEBUGTAG, "check2x");

                    com.google.api.services.drive.Drive.Files f1 = mService.files();

                    com.google.api.services.drive.Drive.Files.List request = null;

                    ContentResolver cR = ct.getContentResolver();

                    String filePath = getBackupFileDir(ct).getAbsolutePath() + "/" + fileName;
                    Log.d(MainActivity.DEBUGTAG, "filePath = " + filePath);

                    mFileUri = Uri.fromFile(new java.io.File(filePath));

                    java.io.File fileContent = new java.io.File(mFileUri.getPath());

                    FileContent mediaContent = new FileContent(cR.getType(mFileUri), fileContent);


                    try {
                        Log.d(MainActivity.DEBUGTAG, "check9");

                        request = f1.list();

                        request.setQ("title = '" + fileContent.getName() + "'" + "and trashed = false");
                        com.google.api.services.drive.model.FileList fileList = new com.google.api.services.drive.model.FileList();

                        fileList.clear();
                        fileList = request.execute();

                        mResultList.clear();
                        mResultList.addAll(fileList.getItems());
                        //request.setPageToken(fileList.getNextPageToken());
//                        com.google.api.services.drive.model.File savedFile;
                        File savedFile;

                        // check if file already exists
                        // if so, then update it rather than create new one
                        if (mResultList.size() > 0) {
                            File file = mResultList.get(0);
                            Log.d(MainActivity.DEBUGTAG, "file = " + file.toString());
                            //File file = mService.files().get(mResultList.get(0).getId()).execute();


                            //backup file already exists so update it
                            file.setTitle(file.getTitle());
                            file.setDescription(file.getDescription());
                            file.setMimeType(file.getMimeType());

                            Log.d(MainActivity.DEBUGTAG, "file id = " + file.getId());
                            Log.d(MainActivity.DEBUGTAG, "file title= " + file.getTitle());
                            Log.d(MainActivity.DEBUGTAG, "check16");

                            Log.d(MainActivity.DEBUGTAG, "id = " + file.getId());

                            savedFile = mService.files().update(file.getId(), file, mediaContent).execute();
                            Log.d(MainActivity.DEBUGTAG, "savedFile = " + savedFile);


                        } else {
                            //backup file doesn't exists so create it
                            mFileUri = Uri.fromFile(new java.io.File(filePath));
                            Log.d(MainActivity.DEBUGTAG, "mFileUri = " + mFileUri.toString());

//                            com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
                            File body = new File();

                            body.setTitle(fileContent.getName());
                            body.setMimeType(cR.getType(mFileUri));
                            Log.d(MainActivity.DEBUGTAG, "check18");

                            savedFile = mService.files().insert(body, mediaContent).execute();
                            Log.d(MainActivity.DEBUGTAG, "savedFile = " + savedFile);

                        }

                        if (savedFile != null) {
                            //send notification
                        } else {
                            //send notification
                        }

                    } catch (UserRecoverableAuthIOException e) {
                        Log.d(MainActivity.DEBUGTAG, "AuthException: " + e.getMessage());

                        //send notification
                    } catch (IOException e) {
                        Log.d(MainActivity.DEBUGTAG, "IOException: " + e.getMessage());

                        //send notification
                    }
                //}
            }
        });

        t.start();
    }

    public Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
    }
    private void makeLocalDatabaseBackup(String file, Context context) {

        java.io.File backupDir;
        if (checkExternalMedia()) {
            backupDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            // Environment.getExternalStorageDirectory();
        } else {
            backupDir = context.getFilesDir();
        }

        FileChannel source;
        FileChannel destination;

        java.io.File currentDB = context.getDatabasePath(BackupNotes.DATABASE_NAME);

        String fileName = file;

        java.io.File backupDB = new java.io.File(backupDir, fileName);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (IOException e) {
            //Toast.makeText(this, "Exception creating notes backup: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public java.io.File getBackupFileDir(Context context) {

        java.io.File backupDir;
        if (checkExternalMedia()) {
            backupDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            backupDir = context.getFilesDir();
        }

        return backupDir;
    }

    public boolean checkExternalMedia() {
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable & mExternalStorageWriteable;
    }

    public Account getAccount(Context context) {

        AccountManager manager = AccountManager.get(context);
        Account[] list = manager.getAccounts();

        for(Account account: list)
        {
            //Log.d(MainActivity.DEBUGTAG, "account.type: " + account.type + "account.name: " + account.name);
            if(account.type.equalsIgnoreCase("com.google"))
            {
                return account;
            }
        }

        return null;
    }



    synchronized String getToken(Account account, Context context) {

        AccountManager manager = AccountManager.get(context);
        Log.d(MainActivity.DEBUGTAG, "accountManager = " + manager.toString());
        Log.d(MainActivity.DEBUGTAG, "account = " + account.type);


        manager.invalidateAuthToken("com.google", null);
        String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive"; //"oauth2:" + DriveScopes.DRIVE;
        manager.getAuthToken(account, AUTH_TOKEN_TYPE, null, true, new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    // If the user has authorized your application to use the tasks API
                    // a token is available.

                    String token1= future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                    Log.d(MainActivity.DEBUGTAG, "token = " + token1);
                    synchronized (tokenLockObject) {
                        gotToken = true;
                        tokenLockObject.notifyAll();
                    }

                } catch (Exception e) {
                    Log.d(MainActivity.DEBUGTAG, "Exception in getToken = " + e.getMessage() );
                }
            }
        }, null);

        return null;
    }

}
