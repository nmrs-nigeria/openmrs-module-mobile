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

package org.openmrs.mobile.activities.patientdashboard;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientActivity;
import org.openmrs.mobile.activities.patientdashboard.charts.PatientChartsFragment;
import org.openmrs.mobile.activities.patientdashboard.charts.PatientDashboardChartsPresenter;
import org.openmrs.mobile.activities.patientdashboard.details.PatientDashboardDetailsPresenter;
import org.openmrs.mobile.activities.patientdashboard.details.PatientDetailsFragment;
import org.openmrs.mobile.activities.patientdashboard.entries.PatientDashboardEntriesPresenter;
import org.openmrs.mobile.activities.patientdashboard.entries.PatientEntriesFragment;
import org.openmrs.mobile.activities.patientdashboard.visits.PatientDashboardVisitsPresenter;
import org.openmrs.mobile.activities.patientdashboard.visits.PatientVisitsFragment;
import org.openmrs.mobile.activities.patientdashboard.vitals.PatientDashboardVitalsPresenter;
import org.openmrs.mobile.activities.patientdashboard.vitals.PatientVitalsFragment;
import org.openmrs.mobile.activities.patientprogram.PatientProgramActivity;
import org.openmrs.mobile.activities.pbs.PatientBiometricActivity;
import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationActivity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.ServiceLogDAO;
import org.openmrs.mobile.models.FingerPrintLog;
import org.openmrs.mobile.sync.LogResponse;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.ImageUtils;
import org.openmrs.mobile.utilities.LogOutTimerUtil;
import org.openmrs.mobile.utilities.TabUtil;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class PatientDashboardActivity extends ACBaseActivity implements LogOutTimerUtil.LogOutListener {

    private String mId;

    public PatientDashboardContract.PatientDashboardMainPresenter mPresenter;

    static boolean isActionFABOpen = false;
    public static FloatingActionButton additionalActionsFAB, updateFAB, deleteFAB, visitFAB, pbsFAB, commodityFAB;
    public static LinearLayout deleteFabLayout, updateFabLayout;
    public static Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);
        getSupportActionBar().setElevation(0);
        Bundle patientBundle = savedInstanceState;
        if (null != patientBundle) {
            patientBundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
        } else {
            patientBundle = getIntent().getExtras();
        }
        mId = String.valueOf(patientBundle.get(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE));
        initViewPager(new PatientDashboardPagerAdapter(getSupportFragmentManager(), this, mId));

        resources = getResources();
        setupUpdateDeleteActionFAB();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        attachPresenterToFragment(fragment);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, mId);
    }

    @Override
    public void onConfigurationChanged(final Configuration config) {
        super.onConfigurationChanged(config);
        TabUtil.setHasEmbeddedTabs(getSupportActionBar(), getWindowManager(), TabUtil.MIN_SCREEN_WIDTH_FOR_PATIENTDASHBOARDACTIVITY);
    }

    @Override
    public void onBackPressed() {
        if (isActionFABOpen) {
            closeFABMenu();
            animateFAB(true);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.patient_dashboard_menu, menu);
        return true;
    }

    private void initViewPager(PatientDashboardPagerAdapter adapter) {
        final ViewPager viewPager = findViewById(R.id.pager);
        TabLayout tabHost = findViewById(R.id.tabhost);
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);
        viewPager.setAdapter(adapter);
        tabHost.setupWithViewPager(viewPager);
    }

    private void attachPresenterToFragment(Fragment fragment) {
        Bundle patientBundle = getIntent().getExtras();
        String id = String.valueOf(patientBundle.get(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE));
        if (fragment instanceof PatientDetailsFragment) {
            mPresenter = new PatientDashboardDetailsPresenter(id, ((PatientDetailsFragment) fragment));
//        } else if (fragment instanceof PatientDiagnosisFragment) {
//            mPresenter = new PatientDashboardDiagnosisPresenter(id, ((PatientDiagnosisFragment) fragment));
        } else if (fragment instanceof PatientEntriesFragment) {
            mPresenter = new PatientDashboardEntriesPresenter(id, ((PatientEntriesFragment) fragment));
        } else if (fragment instanceof PatientVisitsFragment) {
            mPresenter = new PatientDashboardVisitsPresenter(id, ((PatientVisitsFragment) fragment));
        } else if (fragment instanceof PatientVitalsFragment) {
            mPresenter = new PatientDashboardVitalsPresenter(id, ((PatientVitalsFragment) fragment));
        } else if (fragment instanceof PatientChartsFragment) {
            mPresenter = new PatientDashboardChartsPresenter(id, ((PatientChartsFragment) fragment));
        }
    }

    public void setBackdropImage(Bitmap backdropImage, String patientName) {
        ImageView imageView = findViewById(R.id.activity_patient_dashboard_backdrop);
        imageView.setImageBitmap(backdropImage);
        imageView.setOnClickListener(view -> ImageUtils.showPatientPhoto(this, backdropImage, patientName));
    }

    public void setupUpdateDeleteActionFAB() {
        additionalActionsFAB = findViewById(R.id.activity_patient_dashboard_action_fab);
        updateFAB = findViewById(R.id.activity_patient_dashboard_update_fab);
        deleteFAB = findViewById(R.id.activity_patient_dashboard_delete_fab);
        updateFabLayout = findViewById(R.id.custom_fab_update_ll);
        deleteFabLayout = findViewById(R.id.custom_fab_delete_ll);
        visitFAB = findViewById(R.id.activity_patient_visit_action_fab);
        pbsFAB = findViewById(R.id.activity_patient_pbs_action_fab);

        additionalActionsFAB.setOnClickListener(v -> {
            animateFAB(isActionFABOpen);
            if (!isActionFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });

        deleteFAB.setOnClickListener(v -> showDeletePatientDialog());
        updateFAB.setOnClickListener(v -> startPatientUpdateActivity(mPresenter.getPatientId()));
        visitFAB.setOnClickListener(v -> startPatientProgramActivity(mPresenter.getPatientId()));
        pbsFAB.setOnClickListener(v -> autoSelectPBSActivity(mPresenter.getPatientId()));

    }

    // select the patient PBS base on available PBS base finger prints
    private void autoSelectPBSActivity(long patientId) {
        //Util.logTable("service_logs");
        List<PatientBiometricContract> dao = new FingerPrintDAO().getSinglePatientPBS(patientId);
        String visitDate = new ServiceLogDAO().getVisitDate(patientId);
        if (visitDate == null) {
            registerARTServiceDialog("PBS activity info","PBS Capture/Recapture activity MUST be tied to an ART service.\nKindly ensure you document any of the ART service for this patient and try again." );
            return;
        }

        if (dao.size() >= ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT) {
            if (dao.get(0).getSyncStatus() > 0) {
                // print have sync to the server
                startPatientPBSVerificationActivity(patientId, visitDate, false);
/*
                FingerPrintLog  fingerPrintLog = new Select().from(FingerPrintLog.class).where(
                        "pid = ?",patientId
                ).executeSingle();
                 if(fingerPrintLog!=null) {
                   //   check the capture date is not frequent .. for now allow for 30 days
                       if(isNotFrequent(String.valueOf(patientId), fingerPrintLog.getLastCapturedDate())) {
                          startPatientPBSVerificationActivity(patientId, visitDate, fingerPrintLog.getReplaceCount() < 1);
/*
                       }
                 } else {
                   registerARTServiceDialog("PBS activity info", "patient is lock for pbs activities," +
                           " Re-download the patient for  pbs activity");
                     //", replacement of base for patient[ "+patientId+"] patient is lock"
                     OpenMRSCustomHandler.writeLogToFile(new LogResponse( false, String.valueOf(patientId),
                             "patient is lock for pbs activities","Re-download the patient for  pbs activity","Start recapture").getFullMessage());
                 }
                 */


            } else {
                //print not sync to the the server yet
                startPatientPBSActivity(patientId, visitDate);
            }
        } else {
            //no print found base available
            startPatientPBSActivity(patientId, visitDate);
        }
    }

    // check if the capture is not too frequent.
    private boolean isNotFrequent(String patientId, String lastCapturedDate) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            try {
                LocalDateTime recentCaptureDate = LocalDateTime.parse(lastCapturedDate);
                long daysDifference = ChronoUnit.DAYS.between(recentCaptureDate, currentDateTime);
                if(daysDifference<=ApplicationConstants.MINIMUM_REQUIRED_DAYS_BEFORE_RECAPTURE){
                    return  true;
                } else {
                    registerARTServiceDialog("PBS activity info", "Patient recent capture is not up to " +
                            ApplicationConstants.MINIMUM_REQUIRED_DAYS_BEFORE_RECAPTURE +
                            " days. Recent-capture date is "+ lastCapturedDate);
                }
            }catch (Exception e){
                OpenMRSCustomHandler.writeLogToFile(new LogResponse( false, String.valueOf(patientId),
                        e.getMessage(),"1-Report this bugThe date is "+lastCapturedDate,"recentCaptureIsAbove30").getFullMessage());
                registerARTServiceDialog("PBS activity info", "Patient recent capture  date failed to decoded. Report the log file " +
                        "\nRecent-capture date is "+ lastCapturedDate);

            }

        }else{
            try {
                Date  currentDate= new Date();
                Long  recentCaptureDate  =Date.parse( lastCapturedDate);
                long timeDifference = recentCaptureDate//.getTime()4
                        - currentDate.getTime();
                // Calculate the number of days in the time difference
                long daysDifference = timeDifference / (24L * 60L * 60L * 1000L);
                if(daysDifference<=ApplicationConstants.MINIMUM_REQUIRED_DAYS_BEFORE_RECAPTURE) {
                    return true;
                }else {
                    registerARTServiceDialog("PBS activity info", "Patient recent capture is not up to " +
                            ApplicationConstants.MINIMUM_REQUIRED_DAYS_BEFORE_RECAPTURE +
                            " days. Recent-capture date is "+ lastCapturedDate);
                }
            }catch (Exception e){
                OpenMRSCustomHandler.writeLogToFile(new LogResponse( false, String.valueOf(patientId),
                        e.getMessage(),"Report this bug.The date is "+lastCapturedDate,"2-recentCaptureIsAbove30").getFullMessage());
                registerARTServiceDialog("PBS activity info", "Patient recent capture  date failed to decoded. Report the log file " +
                        "\nRecent-capture date is "+ lastCapturedDate);
            }
        }

        return  false;
    }


    private void selectPBS(long patientId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Patient Biometric Data Selection");
        builder.setMessage("Select 'Recapture' for patients who already have biometric data. Otherwise, select 'Capture' for new patients.");

        // Add the buttons
        builder.setPositiveButton("Recapture", (dialog, id) -> {
            Toast.makeText(PatientDashboardActivity.this, "Recapture selected", Toast.LENGTH_SHORT).show();
            //startPatientPBSVerificationActivity(patientId);

        });
        builder.setNegativeButton("Capture", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(PatientDashboardActivity.this, "Tx new capture selected", Toast.LENGTH_SHORT).show();
                //  startPatientPBSActivity(patientId, visitDate);
            }
        });
        builder.setNeutralButton("Cancel", (dialog, id) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void registerARTServiceDialog( String title, String body) {

//        Util.log("Date: "+DateUtils.convertTime( DateUtils.convertTime("2023-05-20T00:00:00.000+0100")
//                , DateUtils.OPEN_MRS_PBS_DATE_FORMAT));
//        String todayDate = DateUtils.convertTime(DateUtils.convertTime(DateUtils.getCurrentDateTime()), DateUtils.OPEN_MRS_PBS_DATE_FORMAT);
//        Util.log("TDate: " + todayDate);

        //Util.logTable("service_logs");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(body);
        builder.setPositiveButton("Okay", (dialog, id) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showFABMenu() {
        isActionFABOpen = true;
        deleteFabLayout.setVisibility(View.VISIBLE);
        updateFabLayout.setVisibility(View.VISIBLE);
        deleteFabLayout.animate().translationY(-resources.getDimension(R.dimen.custom_fab_bottom_margin_55));
        updateFabLayout.animate().translationY(-resources.getDimension(R.dimen.custom_fab_bottom_margin_105));

    }

    public static void closeFABMenu() {
        isActionFABOpen = false;
        deleteFabLayout.animate().translationY(0);
        updateFabLayout.animate().translationY(0);
        deleteFabLayout.setVisibility(View.GONE);
        updateFabLayout.setVisibility(View.GONE);
    }

    public void startPatientUpdateActivity(long patientId) {
        Intent updatePatient = new Intent(this, AddEditPatientActivity.class);
        updatePatient.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE,
                String.valueOf(patientId));
        startActivity(updatePatient);
    }

    public void startPatientProgramActivity(long patientId) {
        Intent patientProgram = new Intent(this, PatientProgramActivity.class);
        patientProgram.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE,
                String.valueOf(patientId));
        startActivity(patientProgram);
    }

    public void startPatientPBSActivity(long patientId, String visitDate) {
        Intent pbsProgram = new Intent(this, PatientBiometricActivity.class);
        pbsProgram.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE,
                String.valueOf(patientId));
        pbsProgram.putExtra(ApplicationConstants.BundleKeys.VISIT_DATE, visitDate);
        startActivity(pbsProgram);
    }


    public void startPatientPBSVerificationActivity(long patientId, String visitDate, boolean replaceBase) {
        Intent pbsProgram = new Intent(this, PatientBiometricVerificationActivity.class);
        pbsProgram.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE,
                String.valueOf(patientId));
        pbsProgram.putExtra(ApplicationConstants.BundleKeys.VISIT_DATE,
                visitDate);
        pbsProgram.putExtra(ApplicationConstants.BundleKeys.REPLACE_BASE,
                replaceBase);
        startActivity(pbsProgram);
    }


    /**
     * This method is called from other Fragments only when they are visible to the user.
     *
     * @param hide To hide the FAB menu depending on the Fragment visible
     */
    @SuppressLint("RestrictedApi")
    public static void hideFABs(boolean hide) {
        closeFABMenu();
        if (hide) {
            additionalActionsFAB.setVisibility(View.GONE);
            visitFAB.setVisibility(View.GONE);
            updateFAB.setVisibility(View.GONE);
            pbsFAB.setVisibility(View.GONE);
        } else {
            additionalActionsFAB.setVisibility(View.VISIBLE);
            visitFAB.setVisibility(View.VISIBLE);
            pbsFAB.setVisibility(View.VISIBLE);

            // will animate back the icon back to its original angle instantaneously
            ObjectAnimator.ofFloat(additionalActionsFAB, "rotation", 180f, 0f).setDuration(0).start();
            additionalActionsFAB.setImageDrawable(resources
                    .getDrawable(R.drawable.ic_edit_white_24dp));
        }
    }

    @SuppressLint("RestrictedApi")
    public static void showVisitFABs() {
        visitFAB.setVisibility(View.VISIBLE);
    }

    private static void animateFAB(boolean isFABClosed) {
        if (!isFABClosed) {
            ObjectAnimator.ofFloat(additionalActionsFAB, "rotation", 0f, 180f).setDuration(500).start();
            final Handler handler = new Handler();
            handler.postDelayed(() -> additionalActionsFAB.setImageDrawable(resources
                    .getDrawable(R.drawable.ic_close_white_24dp)), 400);
        } else {
            ObjectAnimator.ofFloat(additionalActionsFAB, "rotation", 180f, 0f).setDuration(500).start();

            final Handler handler = new Handler();
            handler.postDelayed(() -> additionalActionsFAB.setImageDrawable(resources
                    .getDrawable(R.drawable.ic_edit_white_24dp)), 400);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        LogOutTimerUtil.startLogoutTimer(this, this);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        LogOutTimerUtil.startLogoutTimer(this, this);
    }

    @Override
    public void doLogout() {
        logout();
    }
}
