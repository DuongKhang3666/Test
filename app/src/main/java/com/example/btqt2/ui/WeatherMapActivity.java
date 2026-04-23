package com.example.btqt2.ui;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.example.btqt2.R;

public class WeatherMapActivity extends AppCompatActivity {
    private WebView webViewMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_map);

        webViewMap = findViewById(R.id.webViewMap);

        // Cấu hình WebView để chạy mượt các hiệu ứng bản đồ
        WebSettings webSettings = webViewMap.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Đảm bảo mở link ngay trong app chứ không văng ra trình duyệt Chrome
        webViewMap.setWebViewClient(new WebViewClient());

        // Load URL bản đồ radar của OpenWeatherMap (Zoom vào khu vực Việt Nam)
        webViewMap.loadUrl("https://openweathermap.org/weathermap?basemap=map&cities=true&layer=radar&lat=14.0583&lon=108.2772&zoom=5");
    }
}