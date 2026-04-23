package com.example.btqt2.utils;

public class UnitConverter {
    // Chuyển đổi từ độ C sang độ F
    public static int celsiusToFahrenheit(double celsius) {
        return (int) Math.round((celsius * 9 / 5) + 32);
    }

    // Chuyển đổi từ độ F sang độ C
    public static int fahrenheitToCelsius(double fahrenheit) {
        return (int) Math.round((fahrenheit - 32) * 5 / 9);
    }
}