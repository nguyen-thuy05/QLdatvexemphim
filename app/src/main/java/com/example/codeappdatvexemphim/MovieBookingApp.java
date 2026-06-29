package com.example.codeappdatvexemphim;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class MovieBookingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        // Default is true (cinematic dark theme)
        boolean isDarkMode = themePrefs.getBoolean("is_dark_mode", true);
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
