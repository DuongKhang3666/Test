package com.example.btqt2.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("main")
    public MainData main;

    @SerializedName("name")
    public String cityName;

    // 1. Thêm cái này để Khang lấy Icon và mô tả (Trời nhiều mây, mưa...)
    @SerializedName("weather")
    public List<WeatherInfo> weather;

    // 2. Thêm cái này để Khang lấy Tốc độ gió (12 km/h)
    @SerializedName("wind")
    public Wind wind;

    @SerializedName("clouds")
    public Clouds clouds;

    public static class MainData {
        @SerializedName("temp")
        public float temperature;
        @SerializedName("humidity")
        public int humidity;
    }

    public static class Clouds {
        @SerializedName("all")
        public int cloudiness;
    }

    // Class bổ sung cho Weather Info
    public static class WeatherInfo {
        @SerializedName("description")
        public String description; // "Trời nhiều mây"
        @SerializedName("icon")
        public String icon; // Mã icon để load hình
    }

    // Class bổ sung cho Wind
    public static class Wind {
        @SerializedName("speed")
        public float speed;
    }
}