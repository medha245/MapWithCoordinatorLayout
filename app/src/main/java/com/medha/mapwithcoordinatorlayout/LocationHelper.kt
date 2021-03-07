package com.medha.mapwithcoordinatorlayout

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class LocationHelper {

    companion object {
        var context: Context?=null

        fun locationPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

        fun checkLocationSettings(context: Context): Boolean {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var gps_enabled = false
            var network_enabled = false
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
                gps_enabled = false
                Log.d("EXCEPTION SETTINGS", "gps_enabled null")
            }
            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                Log.d("EXCEPTION SETTINGS", "network_enabled null")
            } catch (ex: Exception) {
                network_enabled = false
                Log.d("EXCEPTION SETTINGS", "gps_enabled null")
            }
            return gps_enabled || network_enabled
        }
    }
}