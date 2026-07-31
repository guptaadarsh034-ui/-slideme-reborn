package com.slideme.reborn;

import android.os.Bundle;
import android.view.Gravity;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        drawer = findViewById(R.id.drawer);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        
        // Start with your site
        webView.loadUrl("https://guptaadarsh034-ui.github.io/-slideme-reborn");

        // Button clicks
        findViewById(R.id.btn_github).setOnClickListener(v -> loadUrl("https://guptaadarsh034-ui.github.io/-slideme-reborn"));
        findViewById(R.id.btn_apkmirror).setOnClickListener(v -> loadUrl("https://www.apkmirror.com"));
        findViewById(R.id.btn_apkpure).setOnClickListener(v -> loadUrl("https://apkpure.com"));
        findViewById(R.id.btn_uptodown).setOnClickListener(v -> loadUrl("https://uptodown.com"));
    }

    private void loadUrl(String url){
        webView.loadUrl(url);
        drawer.closeDrawer(Gravity.START); // Close menu
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.START)) drawer.closeDrawer(Gravity.START);
        else if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}