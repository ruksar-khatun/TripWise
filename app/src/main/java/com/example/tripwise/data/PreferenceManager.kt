package com.example.tripwise.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("TripWisePrefs", Context.MODE_PRIVATE)

    var themeMode: String
        get() = prefs.getString("themeMode", "System") ?: "System"
        set(value) = prefs.edit().putString("themeMode", value).apply()

    var selectedCurrency: String
        get() = prefs.getString("selectedCurrency", "India — Indian Rupee (INR ₹)") ?: "India — Indian Rupee (INR ₹)"
        set(value) = prefs.edit().putString("selectedCurrency", value).apply()

    var userName: String
        get() = prefs.getString("userName", "Alex Johnson") ?: "Alex Johnson"
        set(value) = prefs.edit().putString("userName", value).apply()

    var userEmail: String
        get() = prefs.getString("userEmail", "alex.johnson@tripwise.com") ?: "alex.johnson@tripwise.com"
        set(value) = prefs.edit().putString("userEmail", value).apply()
        
    var userPhone: String
        get() = prefs.getString("userPhone", "+91 98765 43210") ?: "+91 98765 43210"
        set(value) = prefs.edit().putString("userPhone", value).apply()

    var userAvatarUri: String?
        get() = prefs.getString("userAvatarUri", null)
        set(value) = prefs.edit().putString("userAvatarUri", value).apply()
        
    var userAvatarRes: Int
        get() = prefs.getInt("userAvatarRes", com.example.tripwise.R.drawable.img_15)
        set(value) = prefs.edit().putInt("userAvatarRes", value).apply()
}
