package com.example.sukappusers.UserSection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.sukappusers.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Objects;

public class DeveloperActivity extends AppCompatActivity {

    private WebView webView;
    private LinearProgressIndicator toolbarProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_activity);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Developer");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);
        toolbarProgressBar.setVisibility(View.VISIBLE);

        // Find the WebView by its ID
        webView = findViewById(R.id.webView);

        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setJavaScriptEnabled(true);

        // Set a WebViewClient to handle page navigation and loading within the WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://api.whatsapp.com/") ||
                        url.startsWith("https://t.me/") ||
                        url.startsWith("https://www.instagram.com/") ||
                        url.startsWith("https://www.linkedin.com/") ||
                        url.startsWith("https://www.facebook.com/")) {
                    // Open WhatsApp, Telegram, Instagram, LinkedIn, and Facebook links in respective applications
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    // For other URLs, load them in the WebView as usual
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
        String url = "https://sachinnimbal.github.io/";
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
