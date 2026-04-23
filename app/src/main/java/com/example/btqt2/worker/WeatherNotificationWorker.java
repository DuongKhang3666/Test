package com.example.btqt2.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.btqt2.MainActivity;
import com.example.btqt2.R;
import com.example.btqt2.data.RetrofitClient;
import com.example.btqt2.data.WeatherApiService;
import com.example.btqt2.data.WeatherResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class WeatherNotificationWorker extends Worker {

    private static final String CHANNEL_ID = "weather_alerts";
    // TODO: Thay bằng API Key thật của bạn
    private static final String API_KEY = "cf16c2fe942b6f317fa731d18f6371c7";

    public WeatherNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("WeatherWorker", "Đang chạy tác vụ ngầm kiểm tra thời tiết...");

        WeatherApiService apiService = RetrofitClient.getClient().create(WeatherApiService.class);

        // TODO: Cập nhật tọa độ thực tế (Tạm thời để TP.HCM)
        double lat = 10.8231;
        double lon = 106.6297;

        Call<WeatherResponse> call = apiService.getCurrentWeather(lat, lon, API_KEY, "metric");

        try {
            // Sử dụng execute() để chạy đồng bộ trong Worker thay vì enqueue()
            Response<WeatherResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                WeatherResponse weather = response.body();

                if (weather.weather != null && !weather.weather.isEmpty()) {
                    String mainCondition = weather.weather.get(0).main;

                    // Kiểm tra nếu thời tiết là Mưa (Rain), Sấm sét (Thunderstorm) hoặc Tuyết (Snow)
                    if ("Rain".equalsIgnoreCase(mainCondition) ||
                            "Thunderstorm".equalsIgnoreCase(mainCondition) ||
                            "Drizzle".equalsIgnoreCase(mainCondition)) {

                        String message = "Thời tiết hiện tại ở " + weather.cityName + " đang có " + weather.weather.get(0).description + ". Hãy nhớ mang theo ô (dù) nhé!";
                        sendNotification("Cảnh báo thời tiết xấu 🌧️", message);
                    }
                }
                return Result.success();
            } else {
                Log.e("WeatherWorker", "Lỗi API: " + response.code());
                return Result.retry(); // Yêu cầu WorkManager thử lại sau
            }

        } catch (IOException e) {
            Log.e("WeatherWorker", "Lỗi mạng: " + e.getMessage());
            return Result.retry(); // Yêu cầu WorkManager thử lại nếu rớt mạng
        }
    }

    private void sendNotification(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Bắt buộc phải tạo NotificationChannel cho Android 8.0 (API 26) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cảnh báo thời tiết",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo khi có thời tiết xấu");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Tạo Intent để mở MainActivity khi người dùng bấm vào thông báo
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Thay bằng icon thời tiết thực tế của app nếu có
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Cho phép hiển thị text dài
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null) {
            // Dùng ID ngẫu nhiên hoặc cố định. ID cố định (VD: 1) sẽ ghi đè thông báo cũ.
            notificationManager.notify(1, builder.build());
        }
    }
}