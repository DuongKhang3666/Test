package com.example.btqt2.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    public List<ForecastItem> forecastList;

    public static class ForecastItem {
        @SerializedName("dt_txt")
        public String dateTime; // Dùng để hiển thị "Bây giờ", "1 PM", "Thứ 3"...

        @SerializedName("main")
        public WeatherResponse.MainData main;

        @SerializedName("weather")
        public List<WeatherResponse.WeatherInfo> weather;
    }
}