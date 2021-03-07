package com.medha.mapwithcoordinatorlayout

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.*

class LatLongToAddressIntentService(val TAG: String = "LatLongToAddressIntentService") : JobIntentService() {
    private var resultReceiver: ResultReceiver? = null

    companion object {

        // Job-ID must be unique across your whole app.
        private const val UNIQUE_JOB_ID = 1011

        fun enqueueWork(context: Context, intent: Intent) {
            Log.e("work","inside")
            enqueueWork(context, LatLongToAddressIntentService::class.java, UNIQUE_JOB_ID,intent )
        }
    }


    override fun onHandleWork(intent: Intent) {
        Log.e("work","insideOnHandle")

        var errorMessage: String = ""

        resultReceiver = intent?.getParcelableExtra(Constants.Receivers.LatLongToAddressReceiver)

        //check if receiver is registered
        if (intent == null || resultReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return
        }

        val location: LatLng? = intent.getParcelableExtra(Constants.Receivers.location)

        if (location == null || location.latitude <=0.0 || location.longitude <=0.0) {
            errorMessage = "No Location Data Provided"
            Log.wtf(TAG, errorMessage)
            deliverResultToReceiver(Constants.Receivers.FAILED, errorMessage)
            return
        }

        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(this.applicationContext, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                // Handle case where no address was found.
                if (addresses.isEmpty()) {
                    if (errorMessage.isEmpty()) {
                        errorMessage = "No address found "
                        Log.e(javaClass.simpleName, errorMessage)
                    }
                    deliverResultToReceiver(Constants.Receivers.FAILED, errorMessage)
                } else {
                    val address = addresses[0]

                    Log.e("addressFound", "postalcode" + address.postalCode + "locality" + address.locality
                            + "adminarea" + address.adminArea + "country" + address.countryName + "subadminarea" + address.subAdminArea
                            + "sublocality" + address.subLocality + "thoroughfare" + address.thoroughfare + "sibthorughfare" + address.subThoroughfare
                            + "premises" + address.premises + "featurename" + address.featureName)
                    val addressFragments = with(address) {
                        Log.e("addressFragmentsLine", maxAddressLineIndex.toString())
                        (0..maxAddressLineIndex).map {
                            getAddressLine(it)
                        }
                    }
                    var area2: String = ""
                    if (addressFragments.size > 0) {
                        val splitString = addressFragments.get(0)
                        val addressComponent = splitString.split(',')
                        var splitter = ""
                        for (item in addressComponent) {
                            if (item.trim().equals(address.subLocality) || item.trim().equals(address.locality)
                                || item.trim().equals(address.adminArea)) {
                                break
                            }
                            area2 += "$splitter$item"
                            splitter = ","
                        }
                    } else {
                        area2 = address.featureName ?: ""
                    }

                    Log.i(javaClass.simpleName, "Address Found")
                    deliverResultToReceiver(Constants.Receivers.SUCCESS_RESULT,
                        address.postalCode ?: "", address.subAdminArea
                            ?: "", address.adminArea ?: "",
                        address.countryName ?: "", address.subLocality ?: "", area2 ?: "")
                }

            } catch (ioException: IOException) {
                // Catch network or other I/O problems.
                errorMessage = "Geocoder service not available"
                Log.e(javaClass.simpleName, errorMessage, ioException)
            } catch (illegalArgumentException: IllegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = "Invalid lat long used "
                Log.e(javaClass.simpleName, "$errorMessage. Latitude = $location.latitude , " +
                        "Longitude = $location.longitude", illegalArgumentException)
            }


        } else {
            errorMessage = "Geocoder not present"
            Log.wtf(TAG, errorMessage)
            deliverResultToReceiver(Constants.Receivers.FAILED, errorMessage)
            return
        }
    }

    private fun deliverResultToReceiver(resultCode: Int, pincode: String? = null,
                                        city: String? = null, state: String? = null, country: String? = null,
                                        area: String? = null, area2: String? = null) {
        val bundle = Bundle()
        bundle.putString(Constants.Receivers.RESULT_DATA_KEY, pincode)
        if (!city.isNullOrEmpty()) {
            bundle.putString(Constants.Receivers.city, city)
        }
        if (!state.isNullOrEmpty()) {
            bundle.putString(Constants.Receivers.state, state)
        }
        if (!country.isNullOrEmpty()) {
            bundle.putString(Constants.Receivers.country, country)
        }
        if (!area.isNullOrEmpty()) {
            bundle.putString(Constants.Receivers.area, area)
        }
        if (!area2.isNullOrEmpty()) {
            bundle.putString(Constants.Receivers.area2, area2)
        }
        resultReceiver?.send(resultCode, bundle)

    }


}