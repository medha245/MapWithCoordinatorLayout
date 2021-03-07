package com.medha.mapwithcoordinatorlayout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class AddressViewmodel : ViewModel(){
    var geocoderProgressLiveData = MutableLiveData<PickupMapData.ProgressStatus>()
}

class PickupMapData {
    data class ProgressStatus(var status:Boolean,var msgToShow:String)
}