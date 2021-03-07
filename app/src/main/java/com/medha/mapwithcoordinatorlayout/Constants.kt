package com.medha.mapwithcoordinatorlayout

class Constants {

    interface Receivers {
        companion object {
            val packageName: String = LocationHelper.context?.packageName?:""
            val LatLongToAddressReceiver = packageName + "LatLongToAddressReceiver.RECEIVER"
            const val address = "address"
            const val location = "location"
            const val FAILED = 0
            const val SUCCESS_RESULT = 1
            val RESULT_DATA_KEY = packageName + ".LAT_LONG_RESULT_DATA_KEY"
            const val city = "city"
            const val state = "state"
            const val country = "country"
            const val area = "area"
            const val area2 = "area2"
            const val updateMap = "update_map"
            const val updateMapAndAddress = "update_map_and_address"
        }
    }
}