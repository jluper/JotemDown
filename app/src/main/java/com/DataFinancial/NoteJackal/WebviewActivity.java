package com.DataFinancial.NoteJackal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class WebviewActivity extends ActionBarActivity {

    private EditText field;
    private WebView browser;
    private String url;
    private int group;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_viewer);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle(getResources().getString(R.string.webview_activity_title));
        actionBar.setDisplayShowTitleEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = extras.getInt("group");
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
            url = extras.getString("url");
        }
        browser = (WebView) findViewById(R.id.webView1);
        browser.setWebViewClient(new PageBrowser());
        open(browser);
    }

    @Override
    public Intent getSupportParentActivityIntent() {

        Intent i = new Intent(WebviewActivity.this, MainActivity.class);

        i.putExtra("group", group);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);
        Log.d(MainActivity.DEBUGTAG, "Group: " + group + " group_name: " + groupName + " sort_col: " + sortCol);
        return i;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void open(View view) {

        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        browser.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class PageBrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

            return true;
        }
    }
}