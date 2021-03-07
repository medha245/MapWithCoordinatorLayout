package com.medha.mapwithcoordinatorlayout

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.TriangleEdgeTreatment
import com.medha.mapwithcoordinatorlayout.databinding.ActivityMapsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bordered_edit_text.view.*

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(),OnMapReadyCallback, GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveCanceledListener {
    private lateinit var binding: ActivityMapsBinding

    /**
     * location based variables
     */

    private var mCurrentLocation: LatLng? = null
    private var enterCameraListener: Boolean = false
    private val ZOOM: Float = 16.0F
    private var googleMap: GoogleMap? = null
    private var mapSupport: SupportMapFragment? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest

    private lateinit var latLongResultReceiver: LatLongResultReceiver
    private var timer: CountDownTimer? = null
    private var backgroundDrawable: MaterialShapeDrawable? = null
    private var settingsClicked = false
    private val viewModel: AddressViewmodel by viewModels()

    private val TAG = javaClass.simpleName

    companion object {
        private val LOCATION_PERMISSION = 1234
        private val UPDATE_INTERVAL = (6 * 1000).toLong()  /* 6 secs */
        private val FASTEST_INTERVAL: Long = 0 /* 1 sec */
        private val REQUEST_GRANTED = 1235
        private val REQUEST_CHECK_SETTINGS = 1236
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarPickupAddress)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setBottomSheetCallback()
        //map initialization
        initializeMap()
        materialShape()

    }

    private fun materialShape() {
        val cornerSize = resources.getDimension(R.dimen.margin_smallest)
        backgroundDrawable = MaterialShapeDrawable()
        backgroundDrawable?.setTint(
            ContextCompat.getColor(
                this@MapsActivity,
                R.color.colorAccent
            )
        )
        backgroundDrawable?.paintStyle = Paint.Style.FILL
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, cornerSize)
            .setBottomEdge(TriangleEdgeTreatment(resources.getDimension(R.dimen.dp_8point5), false))
            .build()
        backgroundDrawable?.shapeAppearanceModel = shapeAppearanceModel
        binding.mapPointerText.background = backgroundDrawable
    }

    private fun setBottomSheetCallback() {
        val params: CoordinatorLayout.LayoutParams = binding.bottomSheetLayout.root.layoutParams as CoordinatorLayout.LayoutParams
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.root)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        bottomSheetBehavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        params.behavior = bottomSheetBehavior
    }

    override fun onMapReady(p0: GoogleMap?) {
        this.googleMap = p0
        googleMap?.uiSettings?.isCompassEnabled = false
        googleMap?.setOnCameraMoveListener(this)
        googleMap?.setOnCameraMoveCanceledListener(this)
        setMapAddress()

    }

    override fun onCameraMove() {
        binding.mapPointerText.visibility = View.GONE
    }

    override fun onCameraMoveCanceled() {

    }

    private fun initializeMap() {
        /* map:cameraBearing="112.5"
         map:cameraTargetLat="19.0760"
         map:cameraTargetLng="72.8777"
         map:cameraTilt="30"
         map:cameraZoom="16"*/
        mapSupport = SupportMapFragment
            .newInstance(
                GoogleMapOptions().camera(
                    CameraPosition(
                        LatLng(19.0760, 72.8777),
                        16f,
                        30f,
                        112.5f
                    )
                )
            )

        mapSupport?.let {
            supportFragmentManager.beginTransaction().replace(R.id.mapFragmentPickup, it)
                .commit()
        }
        latLongResultReceiver = LatLongResultReceiver(Handler())
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = LocationRequest.create()
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.interval = UPDATE_INTERVAL
        mLocationRequest.numUpdates = 1
        locationChecks()
        checkLocationServiceAndGetMapAsync()
    }

    private fun checkLocationServiceAndGetMapAsync() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE)
        if (locationManager != null) {
            mapSupport?.getMapAsync(this)
        }
    }

    val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val params = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT)
            params.setMargins(0, 0, 0, (slideOffset * bottomSheet.height).toInt())
            params.anchorId = R.id.bottomSheetLayout
            binding.mapFragmentPickup.layoutParams = params

            val paramsPointer = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT)
            paramsPointer.gravity = Gravity.CENTER
            paramsPointer.setMargins(0, 0, 0, (slideOffset * bottomSheet.height).toInt() + (resources.getDimension(R.dimen.margin_large).toInt()))
            binding.mapPointerIcon.layoutParams = paramsPointer


        }


        override fun onStateChanged(bottomSheet: View, newState: Int) {

        }

    }

    fun Context.dpToPx(dp: Int): Float {
        return (dp * this.resources.displayMetrics.density)
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    private fun startLatLongToAddressIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        val intent = Intent(this, LatLongToAddressIntentService::class.java).apply {
            // Pass the result receiver as an extra to the service.
            putExtra(Constants.Receivers.LatLongToAddressReceiver, latLongResultReceiver)

            // Pass the location data as an extra to the service.
            putExtra(Constants.Receivers.location, mCurrentLocation)
        }

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        viewModel.geocoderProgressLiveData.value = PickupMapData.ProgressStatus(true, "Retrieving Location Data")
        LatLongToAddressIntentService.enqueueWork(this@MapsActivity,intent)
    }

    fun locationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun setMapAddress(noCameraListener: Boolean = false) {
        if (mCurrentLocation != null && googleMap != null) {
            if (!enterCameraListener) {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, ZOOM))
                enterCameraListener = true
                if (locationPermissionGranted()) {
                        googleMap?.isMyLocationEnabled = true
                    //if(mapSupport!=null) {
                    val mapView = mapSupport?.view
                    //set use my current location at bottom
                    //set use my current location at bottom
                    mapView?.findViewById<View>(Integer.parseInt("1"))?.parent?.let {
                        val locationButton = (it as View).findViewById<View>(Integer.parseInt("2"))
                        val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
                        // position on right bottom
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                        rlp.setMargins(0, 0, 30, 30)
                    }
                    // }
                }
                googleMap?.uiSettings?.isCompassEnabled = false
                googleMap?.addCircle(
                    CircleOptions()
                    .center(mCurrentLocation)
                    .radius(10.0)
                    .strokeColor(0x55067dcc)
                    .strokeWidth(1f)  // Border color of the circle
                    .fillColor(0x55769fc7))

                googleMap?.setOnCameraIdleListener {
                    googleMap?.let {
                        mCurrentLocation = LatLng(
                            it.cameraPosition.target.latitude,
                            it.cameraPosition.target.longitude
                        )
                        getMapAddressText(
                            LatLng(
                                it.cameraPosition.target.latitude,
                                it.cameraPosition.target.longitude
                            )
                        )

                    }
                }
            }
        } else {
            if (mCurrentLocation == null) {
                requestAndUpdateLocation()
                //getLocation
            }
        }
    }

    fun getMapAddressText(latLng: LatLng) {
        timer?.cancel()
        timer = object : CountDownTimer(800, 100) {
            override fun onFinish() {
                if (Helper.isConnectedToInternet()) {
                    /*model.getLocationTextFromLatLong(latLng)*/
                    mCurrentLocation = latLng
                    binding.bottomSheetLayout.areaFromMap.text = "Locating..."
                    startLatLongToAddressIntentService()
                }
            }

            override fun onTick(p0: Long) {
            }

        }
        timer?.start()
    }

    private inner class LatLongResultReceiver internal constructor(
        handler: Handler
    ) : ResultReceiver(handler) {

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI .
         */
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {

            // Display the pincode or an error message sent from the intent service.
            val addressOutput = resultData.getString(Constants.Receivers.RESULT_DATA_KEY)

            // Show a toast message if an address was found
            if (resultCode == Constants.Receivers.SUCCESS_RESULT) {
                val city = resultData.getString(Constants.Receivers.city)
                val state = resultData.getString(Constants.Receivers.state)
                val country = resultData.getString(Constants.Receivers.country)
                val area = resultData.getString(Constants.Receivers.area)
                val area2 = resultData.getString(Constants.Receivers.area2)
                if (!addressOutput.isNullOrEmpty() && !city.isNullOrEmpty()
                    && !state.isNullOrEmpty() && !country.isNullOrEmpty()
                ) {
                    binding.bottomSheetLayout.pincodeEt.setText(addressOutput)
                    binding.bottomSheetLayout.cityEt.setText(city)
                    binding.bottomSheetLayout.stateEt.setText(state)

                    if (!area2.isNullOrEmpty()) {

                        // if address1 is empty then set text, if not check if it is triggered from SEARCH theny only update address1
                        // if pincode is changed manually by typing in @et_pincode then dont change Address1
                        binding.bottomSheetLayout.houseName.setText(area2)
                        binding.bottomSheetLayout.areaFromMap.setText(area)
                        binding.bottomSheetLayout.fullAddress.setText("$area , $state , $addressOutput , $country")
                        setTooltip("$area2, $area , $state , $addressOutput , $country")
                        if (!city.isNullOrEmpty() && city != state) {
                            binding.bottomSheetLayout.areaFromMap.setText("$area, $city, $state, $addressOutput, $country")
                            setTooltip("$area2 , $area, $city, $state, $addressOutput, $country")
                        }
                    }

                    if (!area.isNullOrEmpty()) {
                        binding.bottomSheetLayout.areaFromMap.text = area
                        binding.bottomSheetLayout.localityName.setText(area)
                        binding.bottomSheetLayout.areaFromMap.setText(area)
                        binding.bottomSheetLayout.fullAddress.setText("$area , $state , $addressOutput , $country")
                        setTooltip("$area , $state , $addressOutput , $country")

                    }
                } else {

                    binding.bottomSheetLayout.pincodeEt.setText(addressOutput?:"")

                    if (!area2.isNullOrEmpty()) {
                        binding.bottomSheetLayout.areaFromMap.setText(area)
                        binding.bottomSheetLayout.fullAddress.setText("$area , $state , $addressOutput , $country")
                        // if address1 is empty then set text, if not check if it is triggered from SEARCH theny only update address1
                        binding.bottomSheetLayout.houseName.setText(area2)
                    }

                    if (!area.isNullOrEmpty()) {
                        binding.bottomSheetLayout.areaFromMap.setText(area)
                        binding.bottomSheetLayout.fullAddress.setText("$area , $state , $addressOutput , $country")
                        binding.bottomSheetLayout.localityName.setText(area)
                    }
                }

            }  else if (!addressOutput.isNullOrEmpty()) {


                if (binding.bottomSheetLayout.houseName.etBordered.text.toString().isEmpty()) {
                    binding.bottomSheetLayout.houseName.setText("")
                }

                if (binding.bottomSheetLayout.localityName.etBordered.text.toString().isEmpty()) {
                    binding.bottomSheetLayout.localityName.setText("")
                } else {
                    binding.bottomSheetLayout.pincodeEt.setText(addressOutput)
                }

            }  else {

                binding.bottomSheetLayout.pincodeEt.setText("")

                binding.bottomSheetLayout.stateEt.setText("")
                //et_country.setText("India")
                binding.bottomSheetLayout.cityEt.setText("")

                if (binding.bottomSheetLayout.houseName.etBordered.text.toString().isEmpty()) {
                    binding.bottomSheetLayout.houseName.setText("")

                    if (binding.bottomSheetLayout.localityName.etBordered.text.toString().isEmpty()) {
                        binding.bottomSheetLayout.localityName.setText("")

                        Toast.makeText(this@MapsActivity,"No Pincode found for this address. " +
                                "Pincode is mandatory. Please enter your pincode",Toast.LENGTH_SHORT).show()

                    }

                } else if (resultCode == Constants.Receivers.FAILED) {
                    binding.bottomSheetLayout.pincodeEt.setText("")

                    binding.bottomSheetLayout.stateEt.setText("")
                    //et_country.setText("India")
                    binding.bottomSheetLayout.cityEt.setText("")

                    if (binding.bottomSheetLayout.houseName.etBordered.text.toString().isEmpty()) {
                        binding.bottomSheetLayout.houseName.setText("")
                    }

                    if (binding.bottomSheetLayout.localityName.etBordered.text.toString().isEmpty()) {
                        binding.bottomSheetLayout.localityName.setText("")
                    }

                    Toast.makeText(this@MapsActivity, addressOutput, Toast.LENGTH_SHORT).show()
                }

                // Reset. Enable the Fetch Address button and stop showing the progress bar.
                viewModel.geocoderProgressLiveData.value = PickupMapData.ProgressStatus(false, "Address Received")

            }
        }
    }

    private fun locationChecks() {
        if (LocationHelper.locationPermissionGranted(this)) {
            if (LocationHelper.checkLocationSettings(this)) {
                requestAndUpdateLocation()
            } else {
                displayLocationSettingsRequest()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
            }
        }
    }

    fun requestAndUpdateLocation() {
        try {
            binding.bottomSheetLayout.areaFromMap.text = "Requesting Location.."
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback,
                Looper.myLooper())

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message ?: "")

        }
    }

    fun displayLocationSettingsRequest() {
        val googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .build()
        googleApiClient.connect()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        builder.setAlwaysShow(true)
        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback {
            val status = it.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i("LocationSettings", "Successfully enabled")
                    try {
                        status.startResolutionForResult(this, REQUEST_GRANTED)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.i("LocationSettings", "PendingIntent unable to execute request.");
                    }
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    try {
                        status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                    } catch (e: IntentSender.SendIntentException) {
                        Log.i("LocationSettings", "PendingIntent unable to execute request.");
                    }
                    Log.i("LocationSettings", "Location settings are not satisfied.")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (settingsClicked) {
            settingsClicked = false
            if (LocationHelper.locationPermissionGranted(this@MapsActivity)) {

                if (LocationHelper.checkLocationSettings(this@MapsActivity)) {
                    requestAndUpdateLocation()
                } else {
                    displayLocationSettingsRequest()
                }
            } else {
                locationChecks()
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (LocationHelper.checkLocationSettings(this)) {
                        requestAndUpdateLocation()
                    } else {
                        displayLocationSettingsRequest()
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        // user denied permission but not permanently
                        showPermissionDeniedDialog(
                            getString(R.string.location_permission_denied),
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            resources.getString(R.string.permission_denied_address_location)
                        )

                    } else {
                        showPermissionEvokedDialog(
                            getString(R.string.location_permission_invoked),
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            resources.getString(R.string.permission_invoked_address_location)
                        )
                    }
                }

            }
        }
    }


    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                if (it.locations.size >0) {

                    val location = it.locations.get(0)
                    location?.let {

                        mCurrentLocation = LatLng(location.latitude ?: 0.0, location.longitude
                            ?: 0.0)
                        setMapAddress()
                    }

                } else {
                    mCurrentLocation = LatLng(it.lastLocation.latitude
                        ?: 0.0, locationResult.lastLocation?.longitude ?: 0.0)

                    setMapAddress()
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showPermissionDeniedDialog(dialogTitle: String, permission: String, dialogMsg: String) {
        val builder: AlertDialog.Builder
        builder = AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
            .setMessage(dialogMsg)
            .setPositiveButton("Retry", DialogInterface.OnClickListener
            { dialog, which ->
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION
                    )
                }
                dialog.dismiss()

            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .show()
    }

    fun showPermissionEvokedDialog(dialogTitle: String, permission: String, dialogMsg: String) {
        val builder: AlertDialog.Builder
        builder = AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
            .setMessage(dialogMsg)
            .setPositiveButton("Settings", DialogInterface.OnClickListener
            { dialog, which ->
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    settingsClicked = true
                }

                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", applicationContext?.packageName, null)
                startActivity(intent)
                dialog.dismiss()
            })

            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .show()

    }

    private fun setTooltip(text: String) {
        Log.e("tooltip", text)
        binding.mapPointerText.background = backgroundDrawable
            binding.mapPointerText.visibility = View.VISIBLE
            binding.mapPointerText.text =
                "Your order will be picked up from here : \n" + text

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GRANTED || requestCode == REQUEST_CHECK_SETTINGS) {
            binding.bottomSheetLayout.areaFromMap.text = "Fetching Location..."
            requestAndUpdateLocation()
        }
    }


}