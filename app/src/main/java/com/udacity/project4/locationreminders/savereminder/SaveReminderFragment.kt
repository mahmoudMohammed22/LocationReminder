package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    //instance value from reminderDataItem
    private lateinit var reminderDataItem :ReminderDataItem
    //instance value from GeofencingClient
    private lateinit var fencingClient : GeofencingClient
    // use this to know android os to request permission background
    private val requestQruningPermission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val fencingPendingIntent by lazy {
        val intent = Intent(requireContext(),GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // use flag update to send new geofence
        PendingIntent.getBroadcast(requireContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        //instance from Client to add geofence
        fencingClient = LocationServices.getGeofencingClient(requireActivity())


        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }




        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value

            // use this to add data in local db
            reminderDataItem = ReminderDataItem(title,description.value,location,latitude.value,longitude)

//            complete TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

            //check title and description not null
            if (_viewModel.validateEnteredData(reminderDataItem)){
                // check permissionLocation
                if (resultPermissionForegroundAndBackgroundLocation()){
                    //check device turnOn and add Geofencing
                    checkDeviceLocationTurnON()
                }else{
                    //check request permission from user
                    checkResultPermission()
                }
            }


        }
    }


    /**
     * start check permission to know the user give me premission or not
     * if not
     * i will request permission to work app
     */
    @TargetApi(29)
    private fun resultPermissionForegroundAndBackgroundLocation() : Boolean{
        val ForegroundPermissionLocation = (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),Manifest.permission.ACCESS_FINE_LOCATION
        ))

        val BackgroundPermissionLocation =
            if (requestQruningPermission){
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext()
            ,Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }else{
            true
            }

        //return true if request permission is taken
        return ForegroundPermissionLocation && BackgroundPermissionLocation

    }

    // this function use to request from user if you want app run give me permission
    @TargetApi(29)
    private fun checkResultPermission(){
        if (resultPermissionForegroundAndBackgroundLocation())
            return
        val ListPermission = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultBackround = when{
            requestQruningPermission ->{
                ListPermission.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else->{
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
        }
        Log.d(TAG, "Request foreground only location permission")
        requestPermissions(
            ListPermission.toTypedArray(),resultBackround
        )

    }

    //This function to know what is user result to give him accsess

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // if user refused premission
        if (grantResults.all { it == PackageManager.PERMISSION_DENIED })
         {
            // premission denied
            Snackbar.make(
                binding.saveReminderFragment,
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
        }else {
            // if user give me permission you  dvice Location is turn on
            checkDeviceLocationTurnON()
        }


    }

    /*
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationTurnON(resolver:Boolean = true){
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationStringResonseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationStringResonseTask.addOnFailureListener{
            if(it is ResolvableApiException && resolver){
                // location setting is not working
                // showin the user a dialog to know what is error
                try {
                    startIntentSenderForResult(it.resolution.intentSender,1,null,0,0,0,null)
                }catch (sendErorr : IntentSender.SendIntentException){
                    Log.d(TAG, "Error getting Location $sendErorr")
                }
            }else{
                // if user not working device Location
                // i ask him to work  to app is working and user features
                Snackbar.make(
                    binding.saveReminderFragment,
                    R.string.location_required_error,Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok){
                    checkDeviceLocationTurnON()
                }.show()
            }
        }
        // after device lcation is working go to add GeoFencing
        locationStringResonseTask.addOnCompleteListener{
            if (it.isSuccessful){
                addGeofencing()
            }
        }

    }

    /*
 *  When we get the result from asking the user to turn on device location, we call
 *  checkDeviceLocationTurnON again to make sure it's actually on, but
 *  we don't resolve the check to keep the user from seeing an endless loop.
 */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //check that the user turned on their device location and ask
        if (requestCode == 1)
        {
            if (resultCode == Activity.RESULT_OK){
            addGeofencing()
        } else checkDeviceLocationTurnON(false)

        }
    }

    /*
   * Adds a Geofence
   * method should be called after the user has granted the location permission
"
   */
   private fun createGeofencingRequest() :  GeofencingRequest{
        // give data of reminder place
        val reminderData = reminderDataItem



        // build geoFence object
        val geofence = Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            .setRequestId(reminderData.id)
            // Set the circular region of this geofence.
            .setCircularRegion(reminderData.latitude!!,reminderData.longitude!!,100F)
                // the geofencing never expire
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                // when enter send reminder
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // build the geoFencing Request
        val RequestGeoFenceRimnder =GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                // add geoFence new
            .addGeofence(geofence).build()

        return RequestGeoFenceRimnder

    }

    private fun addGeofencing(){
        //add the new geofece request whit new geofence
        fencingClient.addGeofences(createGeofencingRequest(),fencingPendingIntent)?.run {
            // when geoFence is add
            addOnSuccessListener{
                // diplay this to know geo fencing is add
               Log.d(TAG,"Enter")
                // add geoFence in listReminder
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }
            // failed add geofence
            addOnFailureListener{
                // to know is fail add geofence
                Toast.makeText(requireContext(),getString(R.string.error_adding_geofence),Toast.LENGTH_SHORT).show()
            }
        }

    }




    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object{
        const val TAG = "RequestPermission"

        // action to know if user is enter place and happen error
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }
}
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33

