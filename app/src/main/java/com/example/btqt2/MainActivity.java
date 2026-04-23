package com.example.btqt2;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.btqt2.ui.DashboardActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển hướng ngay sang màn hình Dashboard
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);

        // Đóng MainActivity để người dùng bấm Back không quay lại màn hình trắng này
        finish();
    }
}