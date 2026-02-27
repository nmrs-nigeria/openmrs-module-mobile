package org.openmrs.mobile.sync;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PersonAddress;
import org.openmrs.mobile.utilities.ToastUtil;

/**
 * The {@code GeoCoordinate} class provides functionalities to obtain the geographical
 * coordinates (latitude and longitude) of the device.
 * It uses the Fused Location Provider API for location services.
 */
public class GeoCoordinate {
    private static final int PERMISSION_ID = 1996;
    private String mLongitude;
    private String mLatitude;
    FusedLocationProviderClient mFusedLocationClient;
    Context context;
    Activity activity;

    /**
     * Constructs a {@code GeoCoordinate} object and initializes the location client.
     *
     * @param activity The activity from which the context is obtained.
     */
    public GeoCoordinate(Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        getLastLocation();
    }

    /**
     * Returns the longitude of the last known location.
     *
     * @return A string representing the longitude.
     */
    public String getLongitude() {
        return mLongitude;
    }

    /**
     * Returns the latitude of the last known location.
     *
     * @return A string representing the latitude.
     */
    public String getLatitude() {
        return mLatitude;
    }

    /**
     * Requests the last known location of the device.
     * If location permissions are granted and the location is enabled,
     * it retrieves the last known location or requests a new location update if necessary.
     */
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        task -> {
                            Location location = task.getResult();
                            if (location == null) {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                mLocationRequest.setInterval(0);
                                mLocationRequest.setFastestInterval(0);
                                mLocationRequest.setNumUpdates(1);
                                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                                        Looper.myLooper()
                                );
                            } else {
                                mLatitude = location.getLatitude() + "";
                                mLongitude = location.getLongitude() + "";
                            }
                        }
                );
            } else {
                Toast.makeText(context, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } else {
            // Permission not granted
            requestPermissions();
        }
    }

    /**
     * Callback for location updates.
     */
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            mLatitude = mLastLocation.getLatitude() + "";
            mLongitude = mLastLocation.getLongitude() + "";
        }
    };

    /**
     * Requests location permissions from the user.
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    /**
     * Checks if location services are enabled on the device.
     *
     * @return {@code true} if location services are enabled, {@code false} otherwise.
     */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    /**
     * Handles the result of the permission request.
     *
     * @param requestCode  The request code passed in {@link ActivityCompat#requestPermissions(Activity, String[], int)}.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                activity.onBackPressed();
            }
        }
    }

    /**
     * Initiates a request for the last known location.
     * If location services are off, prompts the user to turn them on.
     */
    public void requestLocation() {
        getLastLocation();
        Toast.makeText(context, "Turn on location", Toast.LENGTH_LONG).show();
    }

    public boolean updatePatient(long patientId) {
      //  Util.log("Coordinate: Lat " +  getLatitude() + "; Longi " +  getLongitude());
        if (getLatitude() == null || getLongitude() == null) {
            requestLocation();
            ToastUtil.showLongToast(context, ToastUtil.ToastType.WARNING, "Location is off. ["+getLatitude()+":"+getLongitude()+"]");
            return false;
        } else {
            //
            PatientDAO patientDAO = new PatientDAO();
            Patient patient = patientDAO.findPatientByID(Long.toString(patientId));
            if (patient != null) {
                PersonAddress personAddress = patient.getAddress();

                if (personAddress != null) {
                    // Patient already have an address
                    personAddress.setLatitude(getLatitude());
                    personAddress.setLongitude(getLongitude());
                    patient.setAddress(personAddress);
                }
                else {
                    //Don't have any address.
                    personAddress = new PersonAddress();
                    personAddress.setLatitude(getLatitude());
                    personAddress.setLongitude(getLongitude());
                    patient.setAddress(personAddress);
                }
                patientDAO.updatePatient(patientId, patient);
                return  true;
            } else {
                ToastUtil.showLongToast(context, ToastUtil.ToastType.WARNING, "Location update Issue, Error in finding the patient");
            }
        }
        return  false;
    }
}
