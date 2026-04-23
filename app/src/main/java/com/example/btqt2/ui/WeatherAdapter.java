package com.example.btqt2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Nhớ thêm thư viện Glide vào build.gradle
import com.example.btqt2.R;
import com.example.btqt2.data.ForecastResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    private List<ForecastResponse.ForecastItem> forecastList;

    public WeatherAdapter(List<ForecastResponse.ForecastItem> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_daily, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        ForecastResponse.ForecastItem item = forecastList.get(position);

        // 1. Xử lý làm đẹp chuỗi thời gian (từ "2024-10-25 15:00:00" thành "15:00" hoặc ngày)
        String formattedDate = item.dateTime;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(item.dateTime);
            if (date != null) {
                // Tùy chỉnh format hiển thị ở đây (VD: "HH:mm" cho giờ, "dd/MM" cho ngày)
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm - dd/MM", new Locale("vi", "VN"));
                formattedDate = outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDate.setText(formattedDate);

        // 2. Làm tròn nhiệt độ
        int temp = Math.round(item.main.temperature);
        holder.tvTemp.setText(temp + "°");

        // 3. Xử lý hiển thị Icon thời tiết bằng Glide
        if (item.weather != null && !item.weather.isEmpty()) {
            String iconCode = item.weather.get(0).icon;
            // Link lấy icon chuẩn của OpenWeatherMap
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

            Glide.with(holder.itemView.getContext())
                    .load(iconUrl)
                    .into(holder.ivIcon);
        }
    }

    @Override
    public int getItemCount() {
        return forecastList == null ? 0 : forecastList.size();
    }

    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTemp;
        ImageView ivIcon; // Thêm ImageView cho icon

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTemp = itemView.findViewById(R.id.tvTempMax);
            // Bạn nhớ khai báo id này trong file item_weather_daily.xml nhé
            ivIcon = itemView.findViewById(R.id.ivWeatherIcon);
        }
    }
}