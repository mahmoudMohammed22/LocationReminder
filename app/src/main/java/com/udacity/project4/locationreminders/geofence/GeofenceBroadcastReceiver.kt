package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

//complete TODO: implement the onReceive method to receive the geofencing events at the background

        //
        if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_EVENT){
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if(geofencingEvent.hasError()){
                val errorMessage = errorMessage(context,geofencingEvent.errorCode)
                Log.e(TAG,errorMessage)
                return
            }

            // use this fun to work GeoFencing in background
            // use to send notfication if user enter reminder
            GeofenceTransitionsJobIntentService.enqueueWork(context,intent)
        }

    }
}
// use this to know error is happen in BroadcastReceiver
private const val TAG = "BroadcastReceiverErorr"