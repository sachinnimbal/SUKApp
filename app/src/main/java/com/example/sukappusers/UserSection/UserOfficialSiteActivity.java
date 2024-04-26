package com.example.sukappusers.UserSection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.widget.Toolbar;

import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Objects;

public class UserOfficialSiteActivity extends BaseActivity {

    private WebView webView;
    private LinearProgressIndicator toolbarProgressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_official_site);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Official Info");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);
        toolbarProgressBar.setVisibility(View.VISIBLE);

        // Find the WebView by its ID
        webView = findViewById(R.id.webView);

        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://")) {
                    // Handle other external links (starting with "http://" or "https://") using the device's default web browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    // Load the main page URL in the WebView
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Page loading is complete, hide the progress indicator
                toolbarProgressBar.setVisibility(View.GONE);
            }
        });


        // Load the desired URL in the WebView
        String url = "https://en.wikipedia.org/wiki/Sharnbasva_University";
        webView.loadUrl(url);
    }


    @Override
    public void onBackPressed() {
        // Handle WebView navigation history
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
