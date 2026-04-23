package com.example.btqt2.data; // Sửa lại tên package cho khớp với máy bạn nếu cần

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    // Gọi API thời tiết hiện tại dựa vào Tọa độ (Lat, Lon)
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units, // Truyền "metric" để lấy độ C
            @Query("lang") String lang // Thêm cái này để có tiếng Việt
    );

    @GET("data/2.5/forecast")
    Call<ForecastResponse> getForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );
}