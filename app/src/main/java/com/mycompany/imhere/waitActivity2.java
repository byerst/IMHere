package com.mycompany.imhere;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mycompany.imhere.R;



/**
 * Created by Tim on 11/23/15.
 */
public class waitActivity2 extends FragmentActivity {


    private double destLat; //destination latitude
    private double destLong;    //destination longitude

    private Location currentLoc;  //current Location
    Location destLoc;   //destination location

    private float distBetween; //distance between start and end points in meters
    private float distToSend;  //distance between points when message should be sent (meters)

    private float numOfMiles; //number of miles from destination to send text
    private static final float mile = 1609; //value of a mile in meters


    private String phoneNo; //phone Number
    private String message; //user message string
    Chronometer timer;  //elapsed time timer
    ProgressBar progressBar;    //circular progress meter
    int progressStatus = 0; //initial progress
    Button startButton; //begin button view

    LocationManager locationManager;    //locationManager

    public boolean sent = false;    //sent sms flag

    NotificationManager myNotificationManager;  //Notification
    NotificationCompat.Builder myNotificationBuilder;
    Notification myNotification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait2);

        // Get values from intent
        Intent intent = getIntent();

        phoneNo = intent.getStringExtra("phoneNo");
        message = intent.getStringExtra("message");
        destLat = intent.getDoubleExtra("destLat", 0);
        destLong = intent.getDoubleExtra("destLong", 0);
        numOfMiles = intent.getFloatExtra("distToSend", 0);

        //Create location variable for destination
        destLoc = new Location(Context.LOCATION_SERVICE);

        //initialize locationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 1000, 10, locationListener);

        //initialize notificationManager
        myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //initialize notification builder
        myNotificationBuilder = new NotificationCompat.Builder(this);
        myNotificationBuilder.setSmallIcon(R.drawable.ic_stat_name);
        myNotificationBuilder.setContentTitle(getString(R.string.app_name));
        myNotificationBuilder.setContentText("Your Message Has Been Sent");

        myNotification = myNotificationBuilder.build();


        // Set up view elements
        timer = (Chronometer) findViewById(R.id.chronometer);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        startButton = (Button) findViewById(R.id.Bstart);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void onBegin(View view) {

        //calculate dist from destination to send sms
        distToSend = numOfMiles * mile;

        // start timer for elapsed time
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        //give lat/lng values to destination location
        destLoc.setLongitude(destLong);
        destLoc.setLatitude(destLat);

        // get current location
        currentLoc = getCurrent();

        //calculate distance between start point and end point
        distBetween = destLoc.distanceTo(currentLoc);

        //Initialize max value for progress bar to be distance to send sms point
        progressBar.setMax(Math.round(distBetween - distToSend));

        //Remove button from view
        ((ViewGroup) startButton.getParent()).removeView(startButton);

    }

    Location getCurrent() {
    /* function that returns the current location using locationManager services
         */

        Location myLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        return myLocation;
    }

    protected void sendSMS() {
        try {
            // initialize an SmsManager class called smsManager
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);

            //Produce alert dialog box stating message sent
            new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage("Message Sent!").setNeutralButton("Close", null).show();

            //send notification
            myNotificationManager.notify(1, myNotification);

            //set sent flag to be true
            sent = true;

            //stop the timer
            timer.stop();

        } catch (Exception e) {
            //toast if sms unable to send
            Toast.makeText(getApplicationContext(),
                    "SMS failed",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {

            //Calculate new progress
            progressStatus = Math.round(distBetween - destLoc.distanceTo(location));

            // update view to reflect new location
            progressBar.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(progressStatus);
                }
            });

            // if it is not time to send sms
            if (destLoc.distanceTo(location) > distToSend) {

                //do nothing
            }

            // if it is time to send sms and have not sent yet
            else if (!sent) {
                //send the sms
                sendSMS();
                sent = true;
                //Updates no longer necessary so remove listener
                locationManager.removeUpdates(locationListener);
            }

        }
    };
}
