package com.example.btqt2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btqt2.R;
import com.example.btqt2.data.ForecastResponse;
import com.example.btqt2.data.LocationHelper;
import com.example.btqt2.data.RetrofitClient;
import com.example.btqt2.data.WeatherApiService;
import com.example.btqt2.data.WeatherResponse;
import com.example.btqt2.utils.UnitConverter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvLocation, tvCurrentTemp, tvWeatherDesc, tvHumidity, tvWind;
    private RecyclerView rvForecastHourly, rvForecastDaily;
    private WeatherApiService apiService;
    private LocationHelper locationHelper;
    private boolean isCelsius = true;
    private double currentTempRaw = 0;

    private static final String API_KEY = "cf16c2fe942b6f317fa731d18f6371c7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupUnitToggle();

        apiService = RetrofitClient.getClient().create(WeatherApiService.class);
        locationHelper = new LocationHelper(this);

        // Lấy tọa độ thực tế từ GPS
        locationHelper.getLastLocation(location -> {
            if (location != null) {
                fetchWeatherData(location.getLatitude(), location.getLongitude());
            } else {
                // Mặc định Sài Gòn nếu không lấy được GPS
                fetchWeatherData(10.8231, 106.6297);
            }
        });
    }

    private void initViews() {
        tvLocation = findViewById(R.id.tvLocation);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);

        rvForecastHourly = findViewById(R.id.rvForecastHourly);
        rvForecastHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rvForecastDaily = findViewById(R.id.rvForecastDaily);
        rvForecastDaily.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnOpenMap).setOnClickListener(v ->
                startActivity(new Intent(this, WeatherMapActivity.class)));
    }

    private void setupUnitToggle() {
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.unitToggleGroup);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isCelsius = (checkedId == R.id.btnCelsius);
                updateTempDisplay();
            }
        });
    }

    private void updateTempDisplay() {
        if (isCelsius) {
            tvCurrentTemp.setText(Math.round(currentTempRaw) + "°");
        } else {
            tvCurrentTemp.setText(UnitConverter.celsiusToFahrenheit(currentTempRaw) + "°");
        }
    }

    private void fetchWeatherData(double lat, double lon) {
        apiService.getCurrentWeather(lat, lon, API_KEY, "metric", "vi").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    currentTempRaw = weather.main.temperature;
                    tvLocation.setText(weather.cityName);
                    tvWeatherDesc.setText(weather.weather.get(0).description);
                    tvHumidity.setText(weather.main.humidity + "%");
                    tvWind.setText(weather.wind.speed + " km/h");
                    updateTempDisplay();
                }
            }
            @Override public void onFailure(Call<WeatherResponse> call, Throwable t) {}
        });

        apiService.getForecast(lat, lon, API_KEY, "metric", "vi").enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherAdapter adapter = new WeatherAdapter(response.body().forecastList);
                    rvForecastDaily.setAdapter(adapter);
                    rvForecastHourly.setAdapter(adapter); // Dùng chung adapter để demo
                }
            }
            @Override public void onFailure(Call<ForecastResponse> call, Throwable t) {}
        });
    }
}