package com.DataFinancial.NoteJackal;

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
            url = extras.getString("url");
        }

        Log.d(MainActivity.DEBUGTAG, "In webview 1...url = " + url);
        browser = (WebView) findViewById(R.id.webView1);
        Log.d(MainActivity.DEBUGTAG, "In webview 2...url = " + url);

        browser.setWebViewClient(new PageBrowser());
        open(browser);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void open(View view) {

        Log.d(MainActivity.DEBUGTAG, "In webview 3...url = " + url);
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        Log.d(MainActivity.DEBUGTAG, "In webview 4...url = " + url);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        Log.d(MainActivity.DEBUGTAG, "In webview 5...url = " + url);
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
            Log.d(MainActivity.DEBUGTAG, "In webview 6...url = " + url);
            view.loadUrl(url);
            return true;
        }
    }
}