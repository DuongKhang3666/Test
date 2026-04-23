package com.example.btqt2.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {
    private FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission") // Lưu ý: Phải check quyền ở UI trước khi gọi hàm này
    public void getLastLocation(OnSuccessListener<Location> listener) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(listener);
    }
}