/*
 * Copyright (C) 2016 SecuGen Corporation
 *
 */

package org.openmrs.mobile.activities.pbsverification;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.pbs.FingerPositions;
import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.activities.pbs.PatientBiometricSyncResponseModel;
import org.openmrs.mobile.api.FingerPrintSyncService;
import org.openmrs.mobile.api.FingerPrintVerificationSyncService;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.ServiceLogDAO;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.listeners.retrofit.GenericResponseCallbackListener;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FingerPrintVerificationUtility;
import org.openmrs.mobile.utilities.NetworkUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGImpressionType;

public class PatientBiometricVerificationActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, Runnable {

    private static final String TAG = "SecuGen USB";
    private static final int IMAGE_CAPTURE_QUALITY = 60; //The default value here is 50 so i changed it to 60

    private Button mButtonRegister;
    private Button mButtonSaveCapture;
    private Button mButtonClearUnsyncFingerPrint;

    private PendingIntent mPermissionIntent;
    private ImageView mImageViewFingerprint;

    private byte[] mRegisterImage;
    private byte[] mRegisterTemplate;
    private int[] mMaxTemplateSize;
    private int mImageWidth;
    private int mImageHeight;
    private int mImageDPI;
    private String mDeviceSN;

    private Bitmap grayBitmap;
    private IntentFilter filter;
    private boolean bSecuGenDeviceOpened;
    private JSGFPLib sgfplib;
    private boolean usbPermissionRequested;
    private FingerPositions fingerPosition = null;
    private ImageView fingerPrintImageDisplay;
    private Button fingerLeftThumb, fingerLeftIndex, fingerLeftMiddle, fingerLeftRing, fingerLeftPinky, fingerRightThumb, fingerRightIndex, fingerRightMiddle, fingerRightRing, fingerRightPinky;
    private TextView lt, li, lm, lr, lp,
            rt, ri, rm, rr, rp;
    private List<PatientBiometricVerificationContract> patientFingerPrints;

    boolean haveNotReplace = true;//todo get from store
    String patientId = "";
    String visitDate = "";
    String patientUUID = "";
    FingerPrintVerificationDAO FingerPrintVerificationDAO;
    FingerPrintVerificationUtility fingerPrinVerificationUtility;
    private final int minFingerPrintCount = ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT; //minFingerPrintCount is minimum count to be captured before saving to local/online db


    public PatientBiometricVerificationActivity() {
        FingerPrintVerificationDAO = new FingerPrintVerificationDAO();
        patientFingerPrints = new ArrayList<>();
    }


    public void onClick(View v) {

        if (v == mButtonSaveCapture) {
            saveFingerPrints();
        } else if (v == this.mButtonRegister) {
            CapturePrint();
        } else if (v == this.mButtonClearUnsyncFingerPrint) {
            deleteUnsyncedFingerPrint(Long.parseLong(patientId));
        } else {
            setViewItem(v);
        }
    }


    int fingerPrintCaptureCount = 0;

    public void CapturePrint() {
        if (fingerPosition == null) {
            CustomDebug("Please select the finger position before capturing", false);
        } else {
            //DEBUG Log.d(TAG, "Clicked REGISTER");
            debugMessage("Clicked REGISTER\n");
            if (mRegisterImage != null)
                mRegisterImage = null;

            mRegisterImage = new byte[mImageWidth * mImageHeight];

            long result = sgfplib.GetImage(mRegisterImage);
            debugMessage("GetImage() returned:" + result);

            if (result != 0) {
                String errorMsg = fingerPrinVerificationUtility.getDeviceErrors((int) result);
                CustomDebug(errorMsg, false);
                return;
            }

            mImageViewFingerprint.setImageBitmap(this.toGrayscale(mRegisterImage));
            result = sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
            debugMessage("SetTemplateFormat() returned:" + result + "\n");

            int[] quality1 = new int[1];
            result = sgfplib.GetImageQuality(mImageWidth, mImageHeight, mRegisterImage, quality1);
            debugMessage("GetImageQuality() ret:" + result + "quality [" + quality1[0] + "]\n");

            SGFingerInfo fpInfo = new SGFingerInfo();
            fpInfo.FingerNumber = 1;
            fpInfo.ImageQuality = quality1[0];
            fpInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
            fpInfo.ViewNumber = 1;

            Arrays.fill(mRegisterTemplate, (byte) 0);

            result = sgfplib.CreateTemplate(fpInfo, mRegisterImage, mRegisterTemplate);
            debugMessage("GetImageQuality() ret:" + result + "\n");

            //Save fingerprint
            boolean isGoodQuality = checkForQuality(fpInfo.ImageQuality);

            int[] size = new int[1];
            result = sgfplib.GetTemplateSize(mRegisterTemplate, size);
            debugMessage("GetTemplateSize() ret:" + result + " size [" + size[0] + "]\n");

            String template = Base64.encodeToString(mRegisterTemplate, Base64.DEFAULT);
            if (template != null && isGoodQuality) {

                //add to dictionary
                PatientBiometricVerificationContract theFinger = new PatientBiometricVerificationContract();

                theFinger.setImageHeight(mImageHeight);
                theFinger.setImageWidth(mImageWidth);
                theFinger.setFingerPositions(fingerPosition);
                theFinger.setCreator(1);
                theFinger.setImageQuality(fpInfo.ImageQuality);
                theFinger.setPatienId(Integer.parseInt(patientId));

                theFinger.setTemplate(Base64.encodeToString(mRegisterTemplate, Base64.NO_WRAP));
                theFinger.setImageDPI(mImageDPI);
                theFinger.setSerialNumber(mDeviceSN);
                theFinger.setImageByte(mRegisterTemplate);
                theFinger.setSyncStatus(0);


                //recapture Additional details

                theFinger.setDateCreated(visitDate);


                //reject finger print if already capture for another finger. Accept and replace if this is the same finger
                String previousCapture = fingerPrinVerificationUtility.CheckIfFingerAlreadyCaptured(theFinger.getTemplate(), patientFingerPrints);

                //OpenMRSCustomHandler.writeLogToFile("The template is " + theFinger.getTemplate());

                String previousCaptureOnDevice = fingerPrinVerificationUtility.CheckIfFingerAlreadyCapturedOnDevice(theFinger.getTemplate());
                if (previousCapture != null && !previousCapture.isEmpty()) {
                    // allow replace of finger on same finger
                    if (previousCapture.equals(fingerPrinVerificationUtility.decodeFingerPosition(theFinger.getFingerPositions().name()))) {
                        storeFinger(theFinger);
                    } else {
                        CustomDebug("This finger has been recaptured before for " + previousCapture, false);
                    }
                } else if (previousCaptureOnDevice != null && !previousCaptureOnDevice.isEmpty()) {
                    CustomDebug("This finger has been recaptured before for " + previousCaptureOnDevice + " on this device", false);
                } else {
                    storeFinger(theFinger);
                }
            } else {
                //color warning. this capture is not counted
                colorCapturedButton(fingerPosition, android.R.color.holo_orange_light, Typeface.NORMAL, fpInfo.ImageQuality, false);// bad prints will not match a good base
            }
            //enable the save button when  fingers has been captured
            if (fingerPrintCaptureCount >= minFingerPrintCount) {
                this.mButtonSaveCapture.setClickable(true);
                this.mButtonSaveCapture.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
        mRegisterImage = null;
    }

    private void storeFinger(PatientBiometricVerificationContract theFinger) {


        //scan through list and remove finger print with same position before add the new to the list
        boolean printExist = false;
        for (PatientBiometricVerificationContract item : patientFingerPrints) {
            if (item.getFingerPositions().equals(theFinger.getFingerPositions())) {
                patientFingerPrints.remove(item);
                printExist = true;
                break; // consistency with counter
            }
        }

        //save to temp list to be discard later
        boolean recaptureMatchBase = compareWithBasePrint(theFinger, false);
        theFinger.setSyncStatus(0);// recaptureMatchBase ? 0 : -1   sync to -1 for the one that doest not match the base to disable immediate syncing
        patientFingerPrints.add(theFinger);


        //save to the database directly
        Long db_id = FingerPrintVerificationDAO.saveFingerPrint(theFinger);
        debugMessage(String.valueOf(db_id));
        // print is not existing on list then add counter
        if (!printExist)
            fingerPrintCaptureCount += 1;

        //color the button and save locally
        colorCapturedButton(fingerPosition, android.R.color.holo_green_light, Typeface.BOLD, theFinger.getImageQuality(), recaptureMatchBase);
        //  testLogs();
    }

    private boolean compareWithBasePrint(PatientBiometricVerificationContract captureFinger, boolean showDialog) {
        // todo remove this line next
        if(true) return  true;

        String res = fingerPrinVerificationUtility.checkAlready(captureFinger, patientId);
        if (fingerPrinVerificationUtility.MATCH.equals(res)) {
            return true;
        } else {
            if (showDialog)
                CustomDebug(res, false);
            else
                Util.log("Match message: " + res);
        }
        return false;
    }


    /*
        public void CheckIfAlreadyCapturedOnServer(String patientUUID){

            if(NetworkUtils.isOnline()){
                FingerPrintSyncService fingerPrintSyncService = new FingerPrintSyncService();
                fingerPrintSyncService.CheckForPreviousCapture(patientUUID, new GenericResponseCallbackListener<List<PatientBiometricVerificationContract>>() {
                    @Override
                    public void onResponse(List<PatientBiometricVerificationContract> obj) {
                        if(obj != null && obj.size() > 0){
                            CustomDebug("Finger Print already captured for this patient on the server.", true);
                        }
                    }
                    @Override
                    public void onErrorResponse(List<PatientBiometricVerificationContract> errorMessage) {
                    }
                    @Override
                    public void onErrorResponse(String errorMessage) {
                    }
                });
            }
        }
    */
    public void deleteUnsyncedFingerPrint(long patientId) {
        FingerPrintVerificationDAO dao = new FingerPrintVerificationDAO();
        dao.deletePrint(patientId);
        fingerPrintCaptureCount = 0;
        // quality -1 for init state with finger print a data
        colorCapturedButton(FingerPositions.RightSmall, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.RightWedding, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.RightMiddle, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.RightIndex, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.RightThumb, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.LeftMiddle, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.LeftIndex, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.LeftSmall, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.LeftWedding, android.R.color.black, Typeface.NORMAL, -1, false);
        colorCapturedButton(FingerPositions.LeftThumb, android.R.color.black, Typeface.NORMAL, -1, false);

        patientFingerPrints = new ArrayList<>();
        CustomDebug("Fingerprints successfully cleared", false);
    }

    public void CheckIfAlreadyCapturedOnLocalDB(String patientId, boolean showDialog) {
        FingerPrintVerificationDAO dao = new FingerPrintVerificationDAO();
        // sync pbs are remove by default, all pbs with once that decision have not been made
        List<PatientBiometricVerificationContract> pbs = dao.getSinglePatientPBS(Long.valueOf(patientId));
// remove all before adding
        patientFingerPrints.clear();
        if (pbs != null && pbs.size() > 0) {
            if (showDialog) {
                CustomDebug("Some Finger Print already exit for this patient. You can capture more or clear the existing ones to start afresh",
                        false);
            }
            fingerPrintCaptureCount = pbs.size();
            //load in temp list
            patientFingerPrints.addAll(pbs);

            for (PatientBiometricVerificationContract item : pbs) {
                if (showDialog) {
                    colorCapturedButton(item.getFingerPositions(),
                            android.R.color.holo_green_light, Typeface.NORMAL,
                            item.getImageQuality());
                } else {
                    boolean recaptureMatchBase = compareWithBasePrint(item, false);
                    colorCapturedButton(item.getFingerPositions(),
                            android.R.color.holo_green_light, Typeface.NORMAL,
                            item.getImageQuality(), recaptureMatchBase);
                    //   the print over write the decision
                    item.setSyncStatus(recaptureMatchBase?0:-1);
                    dao.updatePatientFingerPrintSyncStatus(Long.valueOf(patientId), item );
                }
            }
        }
        //
//        else{ //check if already sync
//            pbs = dao.getAll(true, patientId);
//            if(pbs !=null && pbs.size() > 0){
//                CustomDebug("Finger print has been captured for this patient", true);
//            }
//        }
    }

    boolean existAllow = false;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //   add  control to check if user save before leaving activity
        //  activity in case of not replace and not match
        /*
          if( patientFingerPrints.size()<minFingerPrintCount) {
              CustomDebug("You recaptured  " + patientFingerPrints.size() +
                              " out of 10 fingers",
                      true);
          }
          else
        if (existAllow || !haveNotReplace) {
            super.onBackPressed();
        } else {
            int match = 0;
            for (PatientBiometricVerificationContract fp : patientFingerPrints) {
                if (fp.getSyncStatus() == 0)
                    match++;
            }
            if (match == patientFingerPrints.size()) {
                super.onBackPressed();
            } else {
//                new FingerPrintVerificationDAO().getSinglePatientPBS(Long.valueOf(patientId))
           // show the analysis only when the device is connected since it only validate when the device is connected
            if(!disableDelete)
                CustomDebug(match + " matched out of " +patientFingerPrints.size() +
                                " recaptured, You must decide either to replace or not to replace before you can syn this patient",
                        true);
            }
        }
        */

    }

    private boolean checkForQuality(int imageQuality) {
        if (imageQuality < IMAGE_CAPTURE_QUALITY) {
            CustomDebug("Please re-capture this finger. The quality is low (" + imageQuality + " %).", false);
            return false;
        }
        return true;
    }

    private void testLogs() {
        FingerPrintVerificationDAO dao = new FingerPrintVerificationDAO();
        List<PatientBiometricVerificationContract> pbs = patientFingerPrints; // dao.getAll(true, patientId);
        for (int index = 0; index < pbs.size(); index++) {
            Util.log(  " FingerPrint position " +index+": "+ pbs.get(index).getFingerPositions());
        }
        Util.log( "  fingerPrintCaptureCount:  " + fingerPrintCaptureCount);
    }


    private void saveFingerPrints() {
        try {
            if (fingerPrintCaptureCount < minFingerPrintCount) {
                CustomDebug("Please captured a minimum of " +
                        minFingerPrintCount +
                        " print before saving", false);
                return;
            }
            int match = 0;
            for (PatientBiometricVerificationContract fp : patientFingerPrints) {
                if (fp.getSyncStatus() == 0)
                    match++;
            }
            if (match == patientFingerPrints.size()) {
                saveFingerPrints(false);
            }

            if (haveNotReplace) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setTitle("Patient  PBS");
                builder.setMessage("Some prints did not matched the base will replace or continue ");
                // Add the buttons
                builder.setPositiveButton("Replace", (dialog, id) -> {
                    // copy all print to base and delete the recapture
                    FingerPrintDAO baseDoa = new FingerPrintDAO();
                    baseDoa.deletePrint(Long.valueOf(patientId));// remove previous base
                    baseDoa.saveFingerPrint(patientFingerPrints, 0);// save the recaptured as base with sync to 0
                    new FingerPrintVerificationDAO().deletePrint(Long.valueOf(patientId));// delete from the verification table
                    saveFingerPrints(true); // make a request
                    existAllow = true;
                });
                builder.setNegativeButton("Save as recapture", (dialog, id) -> {
                    //   make all prints to have sync zero on this patient
                    new FingerPrintVerificationDAO().updateSync(Long.valueOf(patientId), 0);
                    saveFingerPrints(false);
                    existAllow = true;
                });
                int finalMatch = match;
                builder.setNeutralButton("Cancel", (dialog, id) -> {
                    //  before auto sync all prints prints of a patient in verification must be equal
                    CustomDebug("Finger prints save offline\n" +
                            finalMatch + " matched out of " + patientFingerPrints.size()+
                            " recaptured, You must decide either to replace or not to replace before you can syn this patient" +
                            "\nClick save button to make your decision", true);
                    existAllow = true;


                });
                androidx.appcompat.app.AlertDialog dialog = builder.create();
                dialog.show();


            } else {
                saveFingerPrints(false);
            }


        } catch (Exception ex) {
            CustomDebug(ex.getMessage(), false);
        }


    }

    private void saveFingerPrints(boolean isBase) {

        if (NetworkUtils.isOnline() && NetworkUtils.hasNetwork() && patientUUID != null) {


            if (isBase) {
                //  base syn here
                FingerPrintDAO dao  = new FingerPrintDAO();
                List<PatientBiometricContract> pbs = dao.getAll(false, patientId);
                org.openmrs.mobile.activities.pbs.PatientBiometricDTO dto = new PatientBiometricDTO();
                dto.setFingerPrintList(new ArrayList<>(pbs));
                dto.setPatientUUID(patientUUID);

                new FingerPrintSyncService().startSync(dto, new GenericResponseCallbackListener<PatientBiometricSyncResponseModel>() {
                    @Override
                    public void onResponse(PatientBiometricSyncResponseModel obj) {

                        if(obj !=null && obj.getIsSuccessful()){
                            CustomDebug(obj.getErrorMessage(), false);
                            dao.updateSync(Long.valueOf(patientId),1);
                            // setting void to one for all records that matches the the UUID
                            new ServiceLogDAO().set_patient_PBS_void(patientId,patientUUID,1);
                            CustomDebug("Successfully saved to server.", true);
                        }else{
                            if(obj !=null){
                                CustomDebug(obj.getErrorMessage(), false);
                            }
                            CustomDebug("An error occurred while saving prints on the server.", true);

                        }
                    }

                    @Override
                    public void onErrorResponse(PatientBiometricSyncResponseModel errorMessage) {
                        if(errorMessage !=null){
                            CustomDebug(errorMessage.getErrorMessage(), false);
                        }
                        //already saved
                        //dao.saveFingerPrint(dto.getFingerPrintList());
                        CustomDebug("An error occurred while saving prints on the server.", true);
                    }

                    @Override
                    public void onErrorResponse(String errorMessage) {
                        Log.d(TAG, "Log_C "+errorMessage);
                        CustomDebug(errorMessage, false);

                        //save locally
                        //already saved
                        //dao.saveFingerPrint(dto.getFingerPrintList());
                        CustomDebug("Finger Prints saved offline", true);
                    }
                });
            }
            else {

// recapture sync here
                FingerPrintVerificationDAO dao = new FingerPrintVerificationDAO();
                List<PatientBiometricVerificationContract> pbs = dao.getAll(false, patientId);
                PatientBiometricVerificationDTO dto = new PatientBiometricVerificationDTO();
                dto.setFingerPrintList(new ArrayList<>(pbs));
                dto.setPatientUUID(patientUUID);
                new FingerPrintVerificationSyncService().startSync(dto, new GenericResponseCallbackListener<PatientBiometricSyncResponseModel>() {
                    @Override
                    public void onResponse(PatientBiometricSyncResponseModel obj) {
                        if (obj != null && obj.getIsSuccessful()) {
                            CustomDebug(obj.getErrorMessage(), false);
                            // deleting the prints to enable recapture
                            dao.deletePrint(Long.valueOf(patientId));
                            CustomDebug("Successfully saved to server.", true);
                            // setting void to one for all records that matches the the UUID
                            new ServiceLogDAO().set_patient_PBS_void(patientId, patientUUID, 1);
                        } else {
                            if (obj != null) {
                                CustomDebug(obj.getErrorMessage(), false);
                            }
                            CustomDebug("An error occurred while saving prints on the server.", true);
                        }
                    }

                    @Override
                    public void onErrorResponse(PatientBiometricSyncResponseModel errorMessage) {
                        if (errorMessage != null) {
                            CustomDebug(errorMessage.getErrorMessage(), false);
                        }
                        CustomDebug("An error occurred while saving prints on the server.", true);
                    }

                    @Override
                    public void onErrorResponse(String errorMessage) {
                        Log.d(TAG, "Log_C " + errorMessage);
                        CustomDebug(errorMessage, false);
                        CustomDebug("Finger Prints saved offline", true);
                    }
                });
            }
        } else {
            CustomDebug("Saved offline", true);
        }


    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "Enter onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secugen_launcher);

        if (savedInstanceState != null) {
            patientId = savedInstanceState.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
            visitDate = savedInstanceState.getString(ApplicationConstants.BundleKeys.VISIT_DATE);
            haveNotReplace = savedInstanceState.getBoolean(ApplicationConstants.BundleKeys.REPLACE_BASE);
        } else {
            savedInstanceState = getIntent().getExtras();
        }

        if (savedInstanceState != null) {
            patientId = savedInstanceState.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
            visitDate = savedInstanceState.getString(ApplicationConstants.BundleKeys.VISIT_DATE);
            haveNotReplace = savedInstanceState.getBoolean(ApplicationConstants.BundleKeys.REPLACE_BASE);
        }

        createViewObject();
        Patient patient = new PatientDAO().findPatientByID(patientId);
        //FingerPrintVerificationDAO.deletePrint(Long.valueOf(patientId));
        patientUUID = patient.getUuid();
//       if(patientUUID == null){
//           CustomDebug("Patient must be synced first before capturing finger print!",true);
//       }
        /*
        Not check because it recapture
        if(patientUUID != null) {
            CheckIfAlreadyCapturedOnServer(patientUUID);
        }

         */

        mMaxTemplateSize = new int[1];

        //USB Permissions
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter();
        // add more action to listen to connected and disconnected
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);

        sgfplib = new JSGFPLib((UsbManager) getSystemService(Context.USB_SERVICE));
        fingerPrinVerificationUtility = new FingerPrintVerificationUtility(sgfplib);


        debugMessage("Starting Activity\n");
        debugMessage("JSGFPLib version: " + sgfplib.GetJSGFPLibVersion() + "\n");
        //mLed = false;
        //mAutoOnEnabled = false;
        //autoOn = new SGAutoOnEventNotifier(sgfplib, this);
        Log.d(TAG, "Exit onCreate()");
        CheckIfAlreadyCapturedOnLocalDB(patientId, true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onPause() {
        Log.d(TAG, "Enter onPause()");
        if (bSecuGenDeviceOpened) {
            //autoOn.stop();
            EnableControls();
            sgfplib.CloseDevice();
            bSecuGenDeviceOpened = false;
        }
        unregisterReceiver(mUsbReceiver);
        mRegisterImage = null;
        mRegisterTemplate = null;
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        super.onPause();
        Log.d(TAG, "Exit onPause()");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onResume() {
      /*
        // Not checking on server because it is recapture
        if(patientUUID !=null){
            CheckIfAlreadyCapturedOnServer(patientUUID);
        }
        */
        Log.d(TAG, "Enter onResume()");
        super.onResume();
        DisableControls();
        registerReceiver(mUsbReceiver, filter);
        openUSBDevice(true);
        Log.d(TAG, "Exit onResume()");
    }


    boolean loadingBiometric;

    private void openUSBDevice(boolean isResume) {
        Log.d(TAG, "Enter isResume  " + isResume);
        loadingBiometric = true;
        long error = sgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE) {
            Snackbar.make(mButtonClearUnsyncFingerPrint, "Plug in the SecuGen device properly", Snackbar.LENGTH_LONG).show();
            loadingBiometric = false;
            // Loading stop here
        } else {
            UsbDevice usbDevice = sgfplib.GetUsbDevice();
            if (usbDevice == null) {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage("SecuGen fingerprint sensor not found!");
                dlgAlert.setTitle("SecuGen Fingerprint SDK");
                dlgAlert.setPositiveButton("OK",
                        (dialog, whichButton) -> finish()
                );
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
                loadingBiometric = false;
            } else {
                boolean hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                if (hasPermission) {
                    Log.d(TAG, "hasPermission " + hasPermission);
                    // check if patient print existed when your device ready to use
                    CheckIfAlreadyCapturedOnLocalDB(patientId, false);
                    openSecuGen(usbDevice);// open the device
                } else {
                    // request permission if permission is not already requested
                    if (usbPermissionRequested) {
                        Toast.makeText(this, "Waiting for USB Permission", Toast.LENGTH_SHORT).show();
                    } else {
                        usbPermissionRequested = true;
                        Log.d(TAG, " Requesting permission  ");
                        sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent);
                    }
                    loadingBiometric = false;
                }

            }
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onDestroy() {
        Log.d(TAG, "Enter onDestroy()");
        sgfplib.CloseDevice();
        mRegisterImage = null;
        mRegisterTemplate = null;
        sgfplib.Close();
        super.onDestroy();
        Log.d(TAG, "Exit onDestroy()");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(byte[] mImageBuffer) {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
            Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }


    @Override
    public void run() {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void EnableControls() {
        disableDelete = false;
        this.mButtonClearUnsyncFingerPrint.setClickable(true);

        this.mButtonRegister.setClickable(true);
        this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        this.mButtonSaveCapture.setClickable(true);
        this.mButtonSaveCapture.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void DisableControls() {
        disableDelete = true;
        this.mButtonClearUnsyncFingerPrint.setClickable(false);

        this.mButtonRegister.setClickable(false);
        this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.black));

        this.mButtonSaveCapture.setClickable(false);
        this.mButtonSaveCapture.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void CustomDebug(String s, boolean finishOnOk) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(s);
        dlgAlert.setTitle("Patient's Biometric Capture");
        dlgAlert.setPositiveButton("OK",
                (dialog, whichButton) -> {
                    if (finishOnOk) {
                        finish();
                    }
                }
        );
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }


    private void debugMessage(String message) {
        System.out.print(message);
    }

    public void setViewItem(View v) {

        if (v == this.fingerLeftThumb) {
            fingerPosition = FingerPositions.LeftThumb;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_left_thumb);
        } else if (v == this.fingerLeftMiddle) {
            fingerPosition = FingerPositions.LeftMiddle;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_left_middle);
        } else if (v == this.fingerLeftIndex) {
            fingerPosition = FingerPositions.LeftIndex;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_left_index);
        } else if (v == this.fingerLeftRing) {
            fingerPosition = FingerPositions.LeftWedding;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_left_ring);
        } else if (v == this.fingerLeftPinky) {
            fingerPosition = FingerPositions.LeftSmall;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_left_pinky);
        } else if (v == this.fingerRightThumb) {
            fingerPosition = FingerPositions.RightThumb;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_right_thumb);
        } else if (v == this.fingerRightIndex) {
            fingerPosition = FingerPositions.RightIndex;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_right_index);
        } else if (v == this.fingerRightMiddle) {
            fingerPosition = FingerPositions.RightMiddle;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_right_middle);
        } else if (v == this.fingerRightRing) {
            fingerPosition = FingerPositions.RightWedding;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_right_ring);
        } else if (v == this.fingerRightPinky) {
            fingerPosition = FingerPositions.RightSmall;
            fingerPrintImageDisplay.setImageResource(R.drawable.finger_right_pinky);
        }
    }

    public void createViewObject() {
        //Selecting Different Fingers
        //Declare the variables for the different fingers
        fingerLeftThumb = (Button) findViewById(R.id.fingerLeftThumb);
        fingerLeftIndex = (Button) findViewById(R.id.fingerLeftIndex);
        fingerLeftMiddle = (Button) findViewById(R.id.fingerLeftMiddle);
        fingerLeftRing = (Button) findViewById(R.id.fingerLeftRing);
        fingerLeftPinky = (Button) findViewById(R.id.fingerLeftPinky);
        fingerRightThumb = (Button) findViewById(R.id.fingerRightThumb);
        fingerRightIndex = (Button) findViewById(R.id.fingerRightIndex);
        fingerRightMiddle = (Button) findViewById(R.id.fingerRightMiddle);
        fingerRightRing = (Button) findViewById(R.id.fingerRightRing);
        fingerRightPinky = (Button) findViewById(R.id.fingerRightPinky);

        //select overlay text
        rt = findViewById(R.id.rt);
        ri = findViewById(R.id.ri);
        rm = findViewById(R.id.rm);
        rr = findViewById(R.id.rr);
        rp = findViewById(R.id.rp);
        lt = findViewById(R.id.lt);
        li = findViewById(R.id.li);
        lm = findViewById(R.id.lm);
        lr = findViewById(R.id.lr);
        lp = findViewById(R.id.lp);

        //Set onclick listener
        fingerLeftThumb.setOnClickListener(this);
        fingerLeftIndex.setOnClickListener(this);
        fingerLeftMiddle.setOnClickListener(this);
        fingerLeftRing.setOnClickListener(this);
        fingerLeftPinky.setOnClickListener(this);
        fingerRightThumb.setOnClickListener(this);
        fingerRightIndex.setOnClickListener(this);
        fingerRightMiddle.setOnClickListener(this);
        fingerRightRing.setOnClickListener(this);
        fingerRightPinky.setOnClickListener(this);

        // longClick
        //Set onclick listener
        fingerLeftThumb.setOnLongClickListener(this);
        fingerLeftIndex.setOnLongClickListener(this);
        fingerLeftMiddle.setOnLongClickListener(this);
        fingerLeftRing.setOnLongClickListener(this);
        fingerLeftPinky.setOnLongClickListener(this);
        fingerRightThumb.setOnLongClickListener(this);
        fingerRightIndex.setOnLongClickListener(this);
        fingerRightMiddle.setOnLongClickListener(this);
        fingerRightRing.setOnLongClickListener(this);
        fingerRightPinky.setOnLongClickListener(this);


        //Changing selected Image View
        fingerPrintImageDisplay = (ImageView) findViewById(R.id.fingerPrintImage);

        mButtonRegister = (Button) findViewById(R.id.buttonRegister);
        mButtonRegister.setText("Recapture");
        mButtonRegister.setOnClickListener(this);
        mButtonSaveCapture = (Button) findViewById(R.id.btnSavePrints);
        mButtonSaveCapture.setOnClickListener(this);

        mButtonClearUnsyncFingerPrint = (Button) findViewById(R.id.buttonClearUnsyncFingerPrint);
        mButtonClearUnsyncFingerPrint.setOnClickListener(this);
        //mButtonClearUnsyncFingerPrint.setVisibility(View.GONE);

        mImageViewFingerprint = (ImageView) findViewById(R.id.imageViewFingerprint);

        int[] grayBuffer = new int[JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES * JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES];
        Arrays.fill(grayBuffer, Color.GRAY);

        grayBitmap = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES, Bitmap.Config.ARGB_8888);
        grayBitmap.setPixels(grayBuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES);
        mImageViewFingerprint.setImageBitmap(grayBitmap);

        int[] sintbuffer = new int[(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2) * (JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES / 2)];
        Arrays.fill(sintbuffer, Color.GRAY);

        Bitmap sb = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES / 2, Bitmap.Config.ARGB_8888);
        sb.setPixels(sintbuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES / 2);

    }

    public void colorCapturedButton(FingerPositions fingerPosition, int color, int typeface, int quality) {

        colorCapturedButton(fingerPosition, color, typeface, quality, false, false);
    }

    /* use the fingerprint position and set quality flag*/
    public void colorCapturedButton(FingerPositions fingerPosition, int color, int typeface, int quality, boolean matchBase) {
        colorCapturedButton(fingerPosition, color, typeface, quality, matchBase, true);
    }

    public void colorCapturedButton(FingerPositions fingerPosition, int color, int typeface, int quality, boolean matchBase, boolean matchDone) {

        if (fingerPosition == FingerPositions.LeftThumb) {
            setQualityFlag(this.lt, quality, fingerLeftThumb, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.LeftIndex) {
            setQualityFlag(this.li, quality, fingerLeftIndex, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.LeftMiddle) {
            setQualityFlag(this.lm, quality, fingerLeftMiddle, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.LeftWedding) {
            setQualityFlag(this.lr, quality, fingerLeftRing, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.LeftSmall) {
            setQualityFlag(this.lp, quality, fingerLeftPinky, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.RightThumb) {
            setQualityFlag(this.rt, quality, fingerRightThumb, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.RightIndex) {
            setQualityFlag(this.ri, quality, fingerRightIndex, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.RightMiddle) {
            setQualityFlag(this.rm, quality, fingerRightMiddle, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.RightWedding) {
            setQualityFlag(this.rr, quality, fingerRightRing, matchBase, matchDone);
        } else if (fingerPosition == FingerPositions.RightSmall) {
            setQualityFlag(this.rp, quality, fingerRightPinky, matchBase, matchDone);
        }
    }

    /* Set quality flag  int to the TextView  and change background  it background based on */
    private void setQualityFlag(TextView textViewQuality, int quality,
                                TextView textViewLabel,
                                boolean matchBase, boolean matchDone) {
       // textViewLabel.setTextColor(getResources().getColor(R.color.white));
        //match check
        /*
        if (matchBase) {
            if (matchBase) {
                textViewLabel.setTextColor(getResources().getColor(R.color.matched_green));
            } else {
                if (quality != -1)
                    textViewLabel.setTextColor(getResources().getColor(R.color.red));
                else {
                    textViewLabel.setTextColor(getResources().getColor(R.color.white));
                }
            }
        } else {
            textViewLabel.setTextColor(getResources().getColor(R.color.white));
        }

         */

        // quality check
        if (quality < 0) {
            textViewQuality.setBackground(getDrawable(R.drawable.fingerprint_quality_bg));
            textViewQuality.setText("");// set to empty for when prints are cleared using clear btn
            return;
        } else if (quality < 60) {
            textViewQuality.setBackground(getDrawable(R.drawable.fingerprint_quality_bg_red));
        } else if (quality < 75) {
            textViewQuality.setBackground(getDrawable(R.drawable.fingerprint_quality_bg_orange));
        } else {
            textViewQuality.setBackground(getDrawable(R.drawable.fingerprint_quality_bg_green));
        }

        textViewQuality.setText(String.valueOf(quality) + "%");


    }

    /*
 openSecuGen(UsbDevice device) open a secu gen device only  when permission is granted
 */
    private void openSecuGen(UsbDevice device) {
        boolean hasPermission = sgfplib.GetUsbManager().hasPermission(device);
        if (hasPermission) {
            debugMessage("Opening SecuGen Device\n");
            long error = sgfplib.OpenDevice(0);
            debugMessage("OpenDevice() ret: " + error + "\n");
            if (error == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                bSecuGenDeviceOpened = true;
                SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
                error = sgfplib.GetDeviceInfo(deviceInfo);

                debugMessage("GetDeviceInfo() ret: " + error + "\n");
                mImageWidth = deviceInfo.imageWidth;
                mImageHeight = deviceInfo.imageHeight;
                mImageDPI = deviceInfo.imageDPI;
                mDeviceSN = new String(deviceInfo.deviceSN());


                //
                debugMessage("Image width: " + mImageWidth + "\n");
                debugMessage("Image height: " + mImageHeight + "\n");
                debugMessage("Image resolution: " + mImageDPI + "\n");
                debugMessage("Serial Number: " + new String(deviceInfo.deviceSN()) + "\n");

                sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
                sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
                debugMessage("TEMPLATE_FORMAT_SG400 SIZE: " + mMaxTemplateSize[0] + "\n");
                mRegisterTemplate = new byte[(int) mMaxTemplateSize[0]];
                loadingBiometric = false;
                EnableControls();
            } else {
                debugMessage("Waiting for USB Permission\n");
            }


        }

        loadingBiometric = false;

    }

    //////////////THESE AREA CONTAIN SECUGEN STANDARD CONFIGURATION CODE/////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //This broadcast receiver is necessary to get user permissions to access the attached USB device

    private static final String ACTION_USB_PERMISSION = "org.openmrs.mobile.activities.USB_PERMISSION";//broadcast const listen to if permission is sent
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); //retrieve action constant passed in the filter.
            if (ACTION_USB_PERMISSION.equals(action)) {
                /*
                This is not return here and the activity paused  during permission request and resume at onResume()
                          Hence   openSecuGen(device); will not called here...
                          code not comment for device indifference
                 */
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            openSecuGen(device);
                            /*
                           Util.log("Vendor ID : " + device.getVendorId() + "\n");
                            Util.log("Product ID: " + device.getProductId() + "\n");
                            Util.log("USB BroadcastReceiver VID : " + device.getVendorId() + "\n");
                            Util.log("USB BroadcastReceiver PID: " + device.getProductId() + "\n"); */
                            Log.d(TAG, "Opening SecuGen device");

                        } else {
                            Log.e(TAG, "mUsbReceiver.onReceive() Device is null");
                        }
                    } else
                        Log.e(TAG, "mUsbReceiver.onReceive() permission denied for device " + device);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Util.log("USB device connected");
                if (!loadingBiometric) {
                    Toast.makeText(context, "USB device connected: Opening", Toast.LENGTH_LONG).show();
                    // if biometric is not in process of opening open it or if it is not already opened
                    openUSBDevice(false);
                } else
                    Toast.makeText(context, "USB device connected: Please wait and reconnect", Toast.LENGTH_LONG).show();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                bSecuGenDeviceOpened = false;// turn off device connection flag
                usbPermissionRequested = false;// turn off permission requested
                DisableControls();// disable capture  button
                Toast.makeText(context, "USB device disconnected", Toast.LENGTH_SHORT).show();// notify the user
                Util.log("USB device disconnected");
            }
        }
    };


    boolean disableDelete = true;

    @Override
    public boolean onLongClick(View view) {
        if (disableDelete) {
            CustomDebug("Plug in device to enable the the delete functionality"
                    , false);
        } else if (view == fingerLeftThumb) {
            showDialogDeletePrintAtPosition(FingerPositions.LeftThumb);
        } else if (view == fingerLeftIndex) {
            showDialogDeletePrintAtPosition(FingerPositions.LeftIndex);
        } else if (view == fingerLeftMiddle) {
            showDialogDeletePrintAtPosition(FingerPositions.LeftMiddle);
        } else if (view == fingerLeftRing) {
            showDialogDeletePrintAtPosition(FingerPositions.LeftWedding);
        } else if (view == fingerLeftPinky) {
            showDialogDeletePrintAtPosition(FingerPositions.LeftSmall);
        } else if (view == fingerRightThumb) {
            showDialogDeletePrintAtPosition(FingerPositions.RightThumb);
        } else if (view == fingerRightIndex) {
            showDialogDeletePrintAtPosition(FingerPositions.RightIndex);
        } else if (view == fingerRightMiddle) {
            showDialogDeletePrintAtPosition(FingerPositions.RightMiddle);
        } else if (view == fingerRightRing) {
            showDialogDeletePrintAtPosition(FingerPositions.RightWedding);
        } else if (view == fingerRightPinky) {
            showDialogDeletePrintAtPosition(FingerPositions.RightSmall);
        }

        return false;

    }

    private void showDialogDeletePrintAtPosition(final FingerPositions position) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Deleting Prints");
        builder.setMessage("Are sure you want to remove the "
                + fingerPrinVerificationUtility.decodeFingerPosition(position.name()) + " print");
        // Add the buttons
        builder.setPositiveButton("Delete", (dialog, id) -> {
            boolean printExist = false;
            for (PatientBiometricVerificationContract item : patientFingerPrints) {
                if (item.getFingerPositions().equals(position)) {
                    printExist = true;
                    patientFingerPrints.remove(item);
                    fingerPrintCaptureCount -= 1;
                    Long deleteCounts = new FingerPrintVerificationDAO().deletePrintPosition(Long.valueOf(patientId), position);
                    if (deleteCounts > 0) {
                        colorCapturedButton(position, android.R.color.black, Typeface.NORMAL, -1, false);
                        CustomDebug(fingerPrinVerificationUtility.decodeFingerPosition(position.name()) +
                                " deleted ", false);
                    }
                    break;
                }

            }

            if (!printExist)
                colorCapturedButton(position, android.R.color.black, Typeface.NORMAL, -1, false);


        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {


        });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }


}