package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment() , OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var addMarker: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var Select : PointOfInterest

    companion object{
        const val REQUEST_LOCATION_PERMISSION = 1
        const val TAG = "Se"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())



//      complete  TODO: add the map setup implementation
//      complete  TODO: zoom to the user location after taking his permission
//       complete TODO: add style to the map
//       complete TODO: put a marker to location that the user selected


//       complete TODO: call this function after the user confirms on the selected location
        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }


        return binding.root
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near the Googleplex.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap



        setLongClickMark(map)
        setPoil(map)
        addStyleMap(map)
        userGivePermission()

    }

    private fun onLocationSelected() {
        //      complete  TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        if (addMarker != null){
           _viewModel.latitude.value = addMarker?.position?.latitude
           _viewModel.longitude.value = addMarker?.position?.longitude
           _viewModel.reminderSelectedLocationStr.value = addMarker?.title
           _viewModel.navigationCommand.value = NavigationCommand.Back

       }else{
           // if user dont add point interest
           Toast.makeText(requireContext(),getString(R.string.select_poi),Toast.LENGTH_LONG).show()
        }
    }




    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    // this fun use to replace  style map
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        //  Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }



    // called when user add long press in map
    private fun setLongClickMark(map:GoogleMap){
        map.setOnMapLongClickListener {
            // A Snippet is Additional text is displayed about the title.
            val sinppet =getString(
                R.string.lat_long_snippet,
                it.latitude,it.longitude
            )
            map.clear()
            addMarker = map.addMarker(
                MarkerOptions().position(it).title(getString(R.string.dropped_pin)).snippet(sinppet)
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it,18f))

            Log.d("mark", addMarker?.title.toString())

        }
    }

    // caled when user press in pointInterested
    private fun setPoil(map: GoogleMap){
        map.setOnPoiClickListener{
            // to delet any mark in map
            map.clear()
            addMarker = map.addMarker(
                MarkerOptions().position(it.latLng).title(it.name)
            )
            addMarker?.showInfoWindow()
             Select = PointOfInterest(it.latLng,it.placeId,getString(R.string.select_poi))

            //move camera when click place
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it.latLng,18f))

            Log.d("mark", addMarker?.title.toString())


        }
    }


    //take permission to work map
    private fun permissionGranted():Boolean{
        val foregroundLocationApproved = (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),Manifest.permission.ACCESS_FINE_LOCATION
        ))
        return foregroundLocationApproved
    }

    // check if user give me permission add mark in my loction
    private fun userGivePermission(){
        if(permissionGranted()){
            map.setMyLocationEnabled(true)
            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                val latLng = LatLng(location?.latitude ?:29.210086 , location?.longitude ?:30.980355 )
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18f))
                map.addMarker(MarkerOptions().position(latLng).title("MyLocation"))

            }
        } else{
            // request permission from user
            requestPermissions(
                 arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )

        }
    }

    // to know what is user answer about permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // check if location is granted or not
        if(requestCode == REQUEST_LOCATION_PERMISSION){
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                userGivePermission()
            }
            else{
                // premission denied
                Snackbar.make(
                    binding.map,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                )
                    // Display app setting screen
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
        }
    }

    // desgin differnt map style to diplay from user
    private fun addStyleMap(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ")
        }
    }









}
