/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.dialog.CustomFragmentDialog;
import org.openmrs.mobile.activities.formlist.ip.EnforceChangeActivity;
import org.openmrs.mobile.activities.login.LoginActivity;
import org.openmrs.mobile.activities.settings.SettingsActivity;
import org.openmrs.mobile.activities.troubleshoot.TroubleshootActivity;
import org.openmrs.mobile.api.FormListService;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.bundle.CustomDialogBundle;
import org.openmrs.mobile.dao.LocationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.models.Location;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.models.SystemSetting;
import org.openmrs.mobile.net.AuthorizationManager;
import org.openmrs.mobile.sync.LogResponse;
import org.openmrs.mobile.sync.SyncNewService;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.ForceClose;
import org.openmrs.mobile.utilities.FormVersionUtil;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ThemeUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Completable;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ACBaseActivity extends AppCompatActivity {

    private RestApi restApi;
    protected FragmentManager mFragmentManager;
    protected final OpenMRS mOpenMRS = OpenMRS.getInstance();
    protected final OpenMRSLogger mOpenMRSLogger = mOpenMRS.getOpenMRSLogger();
    protected AuthorizationManager mAuthorizationManager;
    protected CustomFragmentDialog mCustomFragmentDialog;
    private MenuItem mSyncbutton, uploadbutton;

    private List<String> locationList;
    private Snackbar snackbar;
    private IntentFilter mIntentFilter;


    public boolean isLoginActive() {
        return loginActive;
    }

    public void setLoginActive(boolean loginActive) {
        this.loginActive = loginActive;
    }

    boolean loginActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ForceClose(this));
        this.restApi = RestServiceBuilder.createService(RestApi.class);
        setupTheme();

        mFragmentManager = getSupportFragmentManager();
        mAuthorizationManager = new AuthorizationManager();
        locationList = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Boolean flag = extras.getBoolean("flag");
            String errorReport = extras.getString("error");
            if (flag) {
                showAppCrashDialog(errorReport);
            }
        }
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ApplicationConstants.BroadcastActions.AUTHENTICATION_CHECK_BROADCAST_ACTION);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private BroadcastReceiver mPasswordChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showCredentialChangedDialog();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();

        if (!(this instanceof LoginActivity) && !mAuthorizationManager.isUserLoggedIn()) {
           Util.log("User is not logged in "+mOpenMRS.getLocation()+ " Session "+mOpenMRS.getSessionToken());
            mAuthorizationManager.moveToLoginActivity();
            setLoginActive(true);
        }
        registerReceiver(mPasswordChangedReceiver, mIntentFilter);
        ToastUtil.setAppVisible(true);

    }

    @Override
    protected void onPause() {
        setLoginActive(false);
        unregisterReceiver(mPasswordChangedReceiver);
        super.onPause();
        ToastUtil.setAppVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.basic_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mSyncbutton = menu.findItem(R.id.syncbutton);
        uploadbutton  = menu.findItem(R.id.uploadbutton);
        MenuItem logoutMenuItem = menu.findItem(R.id.actionLogout);
        if (logoutMenuItem != null) {
            logoutMenuItem.setTitle(getString(R.string.action_logout) + " " + mOpenMRS.getUsername());
        }
        boolean syncState = OpenMRS.getInstance().getSyncState();
        setSyncButtonState(syncState);
        /*if (mSyncbutton != null) {
            final Boolean syncState = NetworkUtils.isOnline();
            setSyncButtonState(syncState);
        }*/
        return true;
    }

    private void setSyncButtonState(boolean syncState) {
        if (syncState) {
            mSyncbutton.setIcon(R.drawable.ic_sync_on);
            uploadbutton.setVisible(true);

        } else {
            mSyncbutton.setIcon(R.drawable.ic_sync_off);
            uploadbutton.setVisible(false);
        }
    }
    private ProgressDialog progressDialog;
    public void startSyncing()  {
        startValidate( true);
        //new SyncData(getApplicationContext()).runSyncAwait();
        //Toast.makeText(getApplicationContext(), "Uplaoding", Toast.LENGTH_LONG).show();
    }

    public void startValidate(boolean sync) {
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Checking dependencies...");
        progressDialog.show();
        // Perform network request in a separate threggad
        new Thread(() ->
        {
            FormVersionUtil formVersionUtil=  new FormVersionUtil();
            //this method must be first, to retrieve the previously  store validation for metadata
            if (NetworkUtils.isOnline()&& ApplicationConstants.EMPTY_STRING.equals(OpenMRS.getInstance().getMetadataVersion())) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Util.log("Error "+e.getMessage());
                }
            }
            final LogResponse  metadataVersionOkayOffline= formVersionUtil.metadataOkayOffline();
            // this method will get it online or offline for both pbs and metata
            final LogResponse validateDependencies=  formVersionUtil.validateDependencies();

            // A situation where the metal data have just been replace and the store JSON are pick the previous data
            if(!metadataVersionOkayOffline.isSuccess() && validateDependencies.isSuccess()&& NetworkUtils.isOnline()){
             // LogResponse logResponseFormsFromServer=   formVersionUtil.loadFormsFromServer();
             // if(!logResponseFormsFromServer.isSuccess()){  }
                validateDependencies.setSuccess(false);
                validateDependencies.setMessage("App required reopening");
                validateDependencies.setRecommendation("Wait for a 10 seconds after closing with close button");
                //Re initialization
                OpenMRS.getInstance().setMetadataVersion("");
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    if (progressDialog != null) {

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        // Update UI or handle the result as needed
                        if (validateDependencies.isSuccess()) {

                            if (sync) {

                                if(!getSyncState()) {
                                    Intent sy = new Intent(ACBaseActivity.this, SyncNewService.class);
                                    startService(sy);
                                } else{
                                     ToastUtil.warning("You already syncing. Toggle network to start again.");
                                }
                            } else {
                                //  showValidation(logResponse);
                            }
                        } else {
                            showValidation(validateDependencies);
                        }

                    }
                }catch (Exception e){
                    Util.log(e.toString());

                }
            });
        }).start();

    }

    private void showValidation(LogResponse logResponse) {



        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(logResponse.isSuccess()?R.string.dependency_validation_title:R.string.warning_dialog_title))
                .setIcon( logResponse.isSuccess()? R.drawable.toast_success:R.drawable.toast_error)
                .setCancelable(logResponse.isSuccess())
                .setMessage(logResponse.getMessage()
                        + "\n"+  ( logResponse.isSuccess()?"":
                        logResponse.getRecommendation()))

                .setNegativeButton("Close", (dialog, which) -> {
                    if(logResponse.isSuccess()){
                        // empty
                    }else {
                        if (!NetworkUtils.isOnline()) {
                            switchNetwork();
                        }
                        Intent i = new Intent(this, FormListService.class);
                        startService(i);
                        finish();
                        moveTaskToBack(true);


                    }

                })
                .setOnDismissListener(dialogInterface -> {

                })
                .create();

        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.actionSettings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.uploadbutton:
                startSyncing();
                return true;
            case R.id.actionSearchLocal:
                return true;
            case R.id.actionLogout:
                this.showLogoutDialog();
                return true;
            case R.id.actionReleaseRes:
                this.showReleaseResourseDialog();
                return true;
            case R.id.syncbutton:
                switchNetwork();
                return true;

            case R.id.actionEnforce:
                Intent ip = new Intent(this, EnforceChangeActivity.class);
                startActivity(ip);
                return true;
            case R.id.actionTroubleShoot:
                Intent troubleShoot = new Intent(this, TroubleshootActivity.class);
                startActivity(troubleShoot);
                return true;
            case R.id.actionLocation:
                if (!locationList.isEmpty()) {
                    locationList.clear();
                }
                Observable<List<Location>> observableList = new LocationDAO().getLocations();
                observableList.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(getLocationList());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void switchNetwork() {
        setSyncState(false);
        boolean syncState = OpenMRS.getInstance().getSyncState();
        if (syncState) {
            OpenMRS.getInstance().setSyncState(false);
            setSyncButtonState(false);
            showNoInternetConnectionSnackbar();
            ToastUtil.showShortToast(getApplicationContext(), ToastUtil.ToastType.NOTICE, R.string.disconn_server);
        } else if (NetworkUtils.hasNetwork()) {
            //get the datim of the server and ensure that it is the same with the datim code on the device
            Call<Results<SystemSetting>> call2 = restApi.getSystemSettingByKey("facility_datim_code");
            call2.enqueue(new Callback<Results<SystemSetting>>() {
                @Override
                public void onResponse(Call<Results<SystemSetting>> call, Response<Results<SystemSetting>> response) {
                    if (response.isSuccessful()) {
                        Results<SystemSetting> datimCodeData = response.body();
                        if (datimCodeData.getResults().size() > 0) {
                            String[] datimCodeArray = datimCodeData.getResults().get(0).getDisplay().split("=");
                            String datimCode = datimCodeArray[1].trim();
                            String datimCodeFromDevice = ACBaseActivity.this.getDatimCodeFromShared();
                            if (datimCodeFromDevice != null) {//that means that the user has used this device before. and we need to check and ensure that the dbs are the same.
                                if (!datimCode.equals(datimCodeFromDevice)) {
                                    //AlertDialog
                                    OpenMRSCustomHandler.writeLogToFile(new LogResponse(false,"Datim Code",
                                            "You are trying to connect to a different instance of NMRS " + "Current Instance:" + datimCodeFromDevice + ". New Instance " + datimCode,"Look for the current instance before trying to sync","ACBaseAtctivity").getFullMessage());
                                    ACBaseActivity.this.showWarning("Warning", "You are trying to connect to a different instance of NMRS ");

                                }
                                else{
                                    goOnline();
                                }
                            }  else{//if the datim code from the device is null, then something is seriously wrong. May be the user is using an old db with the new app
                                //either ways, don't go online
                                Util.log("Save code in null");
                           OpenMRSCustomHandler.writeLogToFile(new LogResponse(false,"Datim Code",                                       "datim code from the device is null, then something is seriously wrong. May be the user is using an old db with the new app. " +
                                   "datimCodeFromDevice" + datimCodeFromDevice + ". New Instance " + datimCode,"Look for the current instance before trying to sync","ACBaseAtctivity").getFullMessage());

                            }
                            saveDatimCodeToSharedPreference(datimCode);
                          goOnline();
                        }else{
                            goOnline();
                        }
                    }else{
                        OpenMRSCustomHandler.writeLogToFile(new LogResponse(false,"Datim Code",                                       "datim code from the device is null, then something is seriously wrong. May be the user is using an old db with the new app. " +
                                "Unable to go online.   "+"datimCodeFromDevice" + ACBaseActivity.this.getDatimCodeFromShared()
                                ,"Make is IP on web instance PC is same as "+mOpenMRS.getServerUrl()+". "+ response.message(),"ACBaseAtctivity").getFullMessage());

                    }
                }
                @Override
                public void onFailure(Call<Results<SystemSetting>> call, Throwable t) {
                    ToastUtil.error("Check server status and IP address");
                    OpenMRSCustomHandler.writeLogToFile(new LogResponse(false,"Datim Code",                                       "Failed to get the data" +
                            "onFailure: Unable to go online.  "+"datimCodeFromDevice" + ACBaseActivity.this.getDatimCodeFromShared()
                            ,"Make is IP on web instance PC is same as "+mOpenMRS.getServerUrl(),"ACBaseAtctivity").getFullMessage());


                }
            });

        } else {
            showNoInternetConnectionSnackbar();
        }
    }

    private void saveDatimCodeToSharedPreference(String datimCode) {
        SharedPreferences sharedPref =mOpenMRS.getOpenMRSSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("datim_code", datimCode);
        editor.apply();
    }
    private void goOnline()
    {
        OpenMRS.getInstance().setSyncState(true);
        setSyncButtonState(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
           // Use the upload btn for android 8 upward
//            Intent  sy= new Intent(this, SyncNewService.class);
//            this.startService(sy);
//
        }else{
            Intent intent = new Intent("org.openmrs.mobile.intent.action.SYNC_PATIENTS");
            getApplicationContext().sendBroadcast(intent);
        }

        ToastUtil.showShortToast(getApplicationContext(), ToastUtil.ToastType.NOTICE, R.string.reconn_server);
        if (snackbar != null)
            snackbar.dismiss();
        ToastUtil.showShortToast(getApplicationContext(), ToastUtil.ToastType.SUCCESS, R.string.connected_to_server_message);

    }
    public void showWarning(String title, String body )
    {
        Snackbar.make(findViewById(android.R.id.content), body, Snackbar.LENGTH_INDEFINITE).
                setAction("okay ", v -> {
                }).
                show();
    }
    private String getDatimCodeFromShared() {
        return mOpenMRS.getOpenMRSSharedPreferences().getString("datim_code", null);
    }

    private Observer<List<Location>> getLocationList() {
        return new Observer<List<Location>>() {
            @Override
            public void onCompleted() {
                showLocationDialog(locationList);
            }

            @Override
            public void onError(Throwable e) {
                mOpenMRSLogger.e(e.getMessage());
            }

            @Override
            public void onNext(List<Location> locations) {
                for (Location locationItem : locations) {
                    locationList.add(locationItem.getName());
                }

            }
        };
    }

    public void showNoInternetConnectionSnackbar() {
        snackbar = Snackbar.make(findViewById(android.R.id.content),
                getString(R.string.no_internet_connection_message), Snackbar.LENGTH_INDEFINITE);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void logout() {
        mOpenMRS.clearUserPreferencesData();
        mAuthorizationManager.moveToLoginActivity();
        ToastUtil.showShortToast(getApplicationContext(), ToastUtil.ToastType.SUCCESS, R.string.logout_success);
        OpenMRSDBOpenHelper.getInstance().closeDatabases();
    }

    private void showCredentialChangedDialog() {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.credentials_changed_dialog_title));
        bundle.setTextViewMessage(getString(R.string.credentials_changed_dialog_message));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.LOGOUT);
        bundle.setRightButtonText(getString(R.string.ok));
        mCustomFragmentDialog = CustomFragmentDialog.newInstance(bundle);
        mCustomFragmentDialog.setCancelable(false);
        mCustomFragmentDialog.setRetainInstance(true);
        mCustomFragmentDialog.show(mFragmentManager, ApplicationConstants.DialogTAG.CREDENTIAL_CHANGED_DIALOG_TAG);
    }
    private void setSyncState(boolean b) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pbs_sync", b);
        editor.apply();
    }
    private boolean getSyncState() {
        SharedPreferences sharedPref =  getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        return sharedPref.getBoolean("pbs_sync", false);
    }
    private void releaseResourcesInBackground() {
        Completable.fromAction(() -> {
                    PatientDAO patientDAO = new PatientDAO();
                    // Get all patients from DB
                    List<Patient> patientList = patientDAO.getAllPatientsLocal();
                    for (Patient patient : patientList) {
                        // Delete patient only if data is not available for syncing
                        patientDAO.deletePatient(patient.getId());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                     ToastUtil.showShortToast(this, ToastUtil.ToastType.SUCCESS, "Redundant data successfully removed ");
                }, throwable -> {
                    ToastUtil.showShortToast(this, ToastUtil.ToastType.SUCCESS, "Error occur in redundant data");

                    throwable.printStackTrace();
                });
    }

    private void showReleaseResourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Release Resources");
        builder.setMessage("Are you sure you want to release the resources?");
        builder.setPositiveButton("Release", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                releaseResourcesInBackground();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the cancellation here
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showLogoutDialog() {
        setSyncState(false);
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.logout_dialog_title));
        bundle.setTextViewMessage(getString(R.string.logout_dialog_message));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.LOGOUT);
        bundle.setRightButtonText(getString(R.string.logout_dialog_button));
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.LOGOUT_DIALOG_TAG);
    }

    public void showStartVisitImpossibleDialog(CharSequence title) {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.start_visit_unsuccessful_dialog_title));
        bundle.setTextViewMessage(getString(R.string.start_visit_unsuccessful_dialog_message, title));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setRightButtonText(getString(R.string.dialog_button_ok));
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.START_VISIT_IMPOSSIBLE_DIALOG_TAG);
    }

    public void showStartVisitDialog(CharSequence title) {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.start_visit_dialog_title));
        bundle.setTextViewMessage(getString(R.string.start_visit_dialog_message, title));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.START_VISIT);
        bundle.setRightButtonText(getString(R.string.dialog_button_confirm));
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.START_VISIT_DIALOG_TAG);
    }

    public void showDeletePatientDialog() {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.action_delete_patient));
        bundle.setTextViewMessage(getString(R.string.delete_patient_dialog_message));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.DELETE_PATIENT);
        bundle.setRightButtonText(getString(R.string.dialog_button_confirm));
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.DELET_PATIENT_DIALOG_TAG);
    }

    public void showMultiDeletePatientDialog(ArrayList<Patient> selectedItems){
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(org.openmrs.mobile.R.string.delete_multiple_patients));
        bundle.setTextViewMessage(getString(org.openmrs.mobile.R.string.delete_multiple_patients_dialog_message));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.MULTI_DELETE_PATIENT);
        bundle.setRightButtonText(getString(R.string.dialog_button_confirm));
        bundle.setSelectedItems(selectedItems);
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.MULTI_DELETE_PATIENT_DIALOG_TAG);
    }

    private void showLocationDialog(List<String> locationList) {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.location_dialog_title));
        bundle.setTextViewMessage(getString(R.string.location_dialog_current_location) + mOpenMRS.getLocation());
        bundle.setLocationList(locationList);
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.SELECT_LOCATION);
        bundle.setRightButtonText(getString(R.string.dialog_button_select_location));
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.LOCATION_DIALOG_TAG);
    }

    public void createAndShowDialog(CustomDialogBundle bundle, String tag) {
        CustomFragmentDialog instance = CustomFragmentDialog.newInstance(bundle);
        instance.show(mFragmentManager, tag);
    }

    public void moveUnauthorizedUserToLoginScreen() {
        OpenMRSDBOpenHelper.getInstance().closeDatabases();
        mOpenMRS.clearUserPreferencesData();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    public void showProgressDialog(int dialogMessageId) {
        showProgressDialog(getString(dialogMessageId));
    }

    public void dismissCustomFragmentDialog() {
        if (mCustomFragmentDialog != null) {
            mCustomFragmentDialog.dismiss();
        }
    }

    protected void showProgressDialog(String dialogMessage) {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setProgressDialog(true);
        bundle.setTitleViewMessage(dialogMessage);
        mCustomFragmentDialog = CustomFragmentDialog.newInstance(bundle);
        mCustomFragmentDialog.setCancelable(false);
        mCustomFragmentDialog.setRetainInstance(true);
        mCustomFragmentDialog.show(mFragmentManager, dialogMessage);
    }

    public void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                      @NonNull Fragment fragment, int frameId) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public void showAppCrashDialog(String error) {
        //Log the error into a file on the phone
        OpenMRSCustomHandler.writeCrashToFile(error);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle(R.string.crash_dialog_title);
        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.crash_dialog_message)
                .setCancelable(false)
                .setPositiveButton(R.string.crash_dialog_positive_button, (dialog, id) -> dialog.cancel())
                .setNegativeButton(R.string.crash_dialog_negative_button, (dialog, id) -> finishAffinity())
                .setNeutralButton(R.string.crash_dialog_neutral_button, (dialog, id) -> {
                    String filename = OpenMRS.getInstance().getOpenMRSDir()
                            + File.separator + mOpenMRSLogger.getLogFilename();
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_SUBJECT, R.string.error_email_subject_app_crashed);
                    email.putExtra(Intent.EXTRA_TEXT, error);
                    email.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filename));
                    //need this to prompts email client only
                    email.setType("message/rfc822");

                    startActivity(Intent.createChooser(email, getString(R.string.choose_a_email_client)));
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void setupTheme(){
        if(ThemeUtils.isDarkModeActivated()){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}