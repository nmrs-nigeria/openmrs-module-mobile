/*
 * Copyright (C) 2016 SecuGen Corporation
 *
 */

package org.openmrs.mobile.activities.pbs;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.api.FingerPrintSyncService;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.listeners.retrofit.GenericResponseCallbackListener;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGImpressionType;
import androidx.appcompat.app.AppCompatActivity;

public class PatientBiometricActivity extends AppCompatActivity
        implements View.OnClickListener, Runnable {

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
    //private final Map<FingerPositions, PatientBiometricContract> patientFingerPrints = new HashMap<>();
    private final Map<Long, String> deviceErrors = new HashMap<Long, String>(){{
        put(1L, "CREATION FAILED");
        put(2L, "FUNCTION FAILED");
        put(3L, "INVALID PARAM");
        put(4L, "NOT USED");
        put(5L, "DLL LOAD FAILED");
        put(6L, "DLL LOAD FAILED DRV");
        put(7L, "DLL LOAD FAILED ALGO");
        put(8L, "No LONGER SUPPORTED");
        put(51L, "SYS LOAD FAILED");
        put(52L, "INITIALIZE FAILED");
        put(53L, "LINE DROPPED");
        put(54L, "TIME OUT");
        put(55L, "DEVICE NOT FOUND");
        put(56L, "Driver LOAD FAILED");
        put(57L, "WRONG IMAGE");
        put(58L, "LACK OF BANDWIDTH");
        put(59L, "DEV ALREADY OPEN");
        put(60L, "GET Serial Number FAILED");
        put(61L, "UNSUPPORTED DEV");
        put(101L, "FEAT NUMBER");
        put(102L, "INVALID TEMPLATE TYPE");
        put(103L, "INVALID TEMPLATE1");
        put(104L, "INVALID TEMPLATE2");
        put(105L, "EXTRACT FAIL");
        put(106L, "MATCH FAIL");
    }};

    String patientId = "";
    String patientUUID = "";
    FingerPrintDAO fingerPrintDAO;

    public PatientBiometricActivity(){
        fingerPrintDAO = new FingerPrintDAO();
    }


    public void onClick(View v) {

        if(v == mButtonSaveCapture){
            saveFingerPrints();
        } else if (v == this.mButtonRegister) {
            CapturePrint();
        } else if (v == this.mButtonClearUnsyncFingerPrint){
            deleteUnsyncedFingerPrint(Long.parseLong(patientId));
        }
        else {
            setViewItem(v);
        }
    }


    int fingerPrintCaptureCount = 0;
    public void CapturePrint(){

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

            if(result != 0){
                String errorMsg =  deviceErrors.get(result);
                CustomDebug(errorMsg, false);
                return;
            }

            mImageViewFingerprint.setImageBitmap(this.toGrayscale(mRegisterImage));
            result = sgfplib.SetTemplateFormat(SecuGen.FDxSDKPro.SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
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


            String template = Base64.encodeToString(mRegisterTemplate,Base64.DEFAULT);
            //debugMessage("template: " +template);
            if(template != null){

                //reject finger print if already capture for another finger. Accept and replace if this is the same finger
//                PatientBiometricContract oldCapture =    patientFingerPrints.get(fingerPosition);
//                if(CheckIfAlreadyCaptured(mRegisterTemplate, fingerPosition) && oldCapture !=null){
//                    CustomDebug("This finger has been captured before for "+ oldCapture.getFingerPositions(), false);
//                }
//                else{

                    //replace old captured value in case it has been captured before
                    //fingerPrintDAO.deletePrintPosition(Long.valueOf(patientId), fingerPosition);

                    //color the button
                    if(isGoodQuality) {

                        //add to dictionary
                        PatientBiometricContract theFinger = new PatientBiometricContract();
                        theFinger.setImage(Base64.encodeToString(mRegisterTemplate, Base64.DEFAULT));
                        theFinger.setImageHeight(mImageHeight);
                        theFinger.setImageWidth(mImageWidth);
                        theFinger.setFingerPositions(fingerPosition);
                        theFinger.setCreator(1);
                        theFinger.setImageQuality(fpInfo.ImageQuality);
                        theFinger.setPatienId(Integer.parseInt(patientId));

                        //String base64Template =  java.util.Base64.getEncoder().encodeToString(mRegisterTemplate);
                        //theFinger.setTemplate(base64Template);

                        theFinger.setTemplate(Base64.encodeToString(mRegisterTemplate, Base64.NO_WRAP));
                        theFinger.setImageDPI(mImageDPI);
                        theFinger.setSerialNumber(mDeviceSN);
                        theFinger.setImageByte(mRegisterTemplate);
                        theFinger.setSyncStatus(0);

                        //save to the database directly
                        Long db_id = fingerPrintDAO.saveFingerPrint(theFinger);
                        debugMessage(String.valueOf(db_id));
                        fingerPrintCaptureCount += 1;
                        //patientFingerPrints.put(fingerPosition, theFinger);
                        colorCapturedButton(fingerPosition, android.R.color.holo_green_light, Typeface.BOLD);
                    } else {
                        colorCapturedButton(fingerPosition, android.R.color.holo_orange_light, Typeface.NORMAL);
                    }

                    //enable the save button when 6 fingers has been captured
                    if(fingerPrintCaptureCount >= 6){
                        this.mButtonSaveCapture.setClickable(true);
                        this.mButtonSaveCapture.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                //}
            }
            mRegisterImage = null;
        }
    }


//    public boolean CheckIfAlreadyCaptured(byte[] mTemplateToMatch, FingerPositions fingerPosition){
//
//        boolean[] matched = new boolean[1];
//        for(PatientBiometricContract contract: patientFingerPrints.values()) {
//
//           sgfplib.MatchTemplate(mTemplateToMatch, contract.getImageByte(), SGFDxSecurityLevel.SL_NORMAL, matched);
//            if (matched[0]) {
//                if(contract.getFingerPositions() != fingerPosition) {
//                    //reject if already captured for another finger
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public void CheckIfAlreadyCapturedOnServer(String patientUUID){

        if(NetworkUtils.isOnline()){
            FingerPrintSyncService fingerPrintSyncService = new FingerPrintSyncService();
            fingerPrintSyncService.CheckForPreviousCapture(patientUUID, new GenericResponseCallbackListener<List<PatientBiometricContract>>() {
                @Override
                public void onResponse(List<PatientBiometricContract> obj) {
                    if(obj != null && obj.size() > 0){
                        CustomDebug("Finger Print already captured for this patient on the server.", true);
                    }
                }
                @Override
                public void onErrorResponse(List<PatientBiometricContract> errorMessage) {
                }
                @Override
                public void onErrorResponse(String errorMessage) {
                }
            });
        }
    }

    public void deleteUnsyncedFingerPrint(long patientId) {
        FingerPrintDAO dao = new FingerPrintDAO();
        dao.deletePrint(patientId);

        colorCapturedButton(FingerPositions.RightSmall, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.RightWedding, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.RightMiddle, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.RightIndex, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.RightThumb, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.LeftMiddle, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.LeftIndex, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.LeftSmall, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.LeftWedding, android.R.color.black, Typeface.NORMAL);
        colorCapturedButton(FingerPositions.LeftThumb, android.R.color.black, Typeface.NORMAL);


        CustomDebug("Fingerprints successfully cleared", false);
    }

    public void CheckIfAlreadyCapturedOnLocalDB(String patientId) {
        FingerPrintDAO dao = new FingerPrintDAO();
        List<PatientBiometricContract> pbs = dao.getAll(false, patientId);
        if(pbs !=null && pbs.size() > 0){
            CustomDebug("Some Finger Print already exit for this patient. You can capture more or clear the existing ones to start afresh", false);
            for (PatientBiometricContract item : pbs) {
                colorCapturedButton(item.getFingerPositions(), android.R.color.holo_green_light, Typeface.NORMAL);
            }
        }
        else{ //check if already sync
            pbs = dao.getAll(true, patientId);
            if(pbs !=null && pbs.size() > 0){
                CustomDebug("Finger print has been captured for this patient", true);
            }
        }



//        if (dao.checkIfFingerPrintUptoSixFingers(patientId)) {
//            //fingerPrintCaptureCount = 6;
//            CustomDebug("Finger Print already exit for this patient.", true);
//        } else {
//            List<PatientBiometricContract> pbs = dao.getAll(false, patientId);
//            for (PatientBiometricContract item : pbs) {
//                colorCapturedButton(item.getFingerPositions(), android.R.color.holo_green_light);
//            }
//        }
    }

    private boolean checkForQuality(int imageQuality) {
        if(imageQuality < IMAGE_CAPTURE_QUALITY){
            CustomDebug("Please re-capture this finger. The quality is low ("+imageQuality+" %).", false);
            return false;
        }
        return true;
    }

    private void saveFingerPrints() {

        //FingerPrintSyncService sync = new FingerPrintSyncService();
        ////sync.autoSyncFingerPrint();
        try{
        if (fingerPrintCaptureCount < 6) {
            CustomDebug("Please captured a minimum of 6 print before saving", false);
            return;
        }

        FingerPrintDAO dao = new FingerPrintDAO();

//        PatientBiometricDTO dto = new PatientBiometricDTO();
//        dto.setFingerPrintList(new ArrayList<>(patientFingerPrints.values()));


    if (NetworkUtils.isOnline() && NetworkUtils.hasNetwork() && patientUUID != null) {

        List<PatientBiometricContract> pbs = dao.getAll(false, patientId);
        PatientBiometricDTO dto = new PatientBiometricDTO();
        dto.setFingerPrintList(new ArrayList<>(pbs));
        dto.setPatientUUID(patientUUID);

        new FingerPrintSyncService().startSync(dto, new GenericResponseCallbackListener<PatientBiometricSyncResponseModel>() {
            @Override
            public void onResponse(PatientBiometricSyncResponseModel obj) {
                if(obj !=null && obj.getIsSuccessful()){
                    CustomDebug(obj.getErrorMessage(), false);

                    PatientBiometricContract _temp =  pbs.get(0);
                    _temp.setSyncStatus(1);
                    _temp.setTemplate("");
                    dao.updatePatientFingerPrintSyncStatus(Long.valueOf(patientId), _temp);

                    CustomDebug("Successfully saved to server.", true);
                }
            }

            @Override
            public void onErrorResponse(PatientBiometricSyncResponseModel errorMessage) {
                if(errorMessage !=null){
                    CustomDebug(errorMessage.getErrorMessage(), false);
                }
                //already saved
                //dao.saveFingerPrint(dto.getFingerPrintList());
                CustomDebug("Finger Prints saved offline", true);
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                CustomDebug(errorMessage, false);

                //save locally
                //already saved
                //dao.saveFingerPrint(dto.getFingerPrintList());
                CustomDebug("Finger Prints saved offline", true);
            }
        });
    }
    else {
        //save locally
        //dao.saveFingerPrint(dto.getFingerPrintList()); --they are already saved
        CustomDebug("Saved offline", true);
    }
}catch (Exception ex) {
    CustomDebug(ex.getMessage(), false);
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
        } else {
            savedInstanceState = getIntent().getExtras();
        }

        if (savedInstanceState != null) {
            patientId = savedInstanceState.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
        }

        createViewObject();
        Patient patient = new PatientDAO().findPatientByID(patientId);
        //fingerPrintDAO.deletePrint(Long.valueOf(patientId));
        patientUUID = patient.getUuid();
//       if(patientUUID == null){
//           CustomDebug("Patient must be synced first before capturing finger print!",true);
//       }
        if(patientUUID != null) {
            CheckIfAlreadyCapturedOnServer(patientUUID);
        }
        CheckIfAlreadyCapturedOnLocalDB(patientId);

        mMaxTemplateSize = new int[1];

        //USB Permissions
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        sgfplib = new JSGFPLib((UsbManager) getSystemService(Context.USB_SERVICE));

        bSecuGenDeviceOpened = false;
        usbPermissionRequested = false;

        debugMessage("Starting Activity\n");
        debugMessage("JSGFPLib version: " + sgfplib.GetJSGFPLibVersion() + "\n");
        //mLed = false;
        //mAutoOnEnabled = false;
        //autoOn = new SGAutoOnEventNotifier(sgfplib, this);
        Log.d(TAG, "Exit onCreate()");
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

        if(patientUUID !=null){
            CheckIfAlreadyCapturedOnServer(patientUUID);
        }
        CheckIfAlreadyCapturedOnLocalDB(patientId);

        Log.d(TAG, "Enter onResume()");
        super.onResume();
        DisableControls();
        registerReceiver(mUsbReceiver, filter);
        long error = sgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
                dlgAlert.setMessage("The attached fingerprint device is not supported on Android");
            else
                dlgAlert.setMessage("Fingerprint device initialization failed!");
            dlgAlert.setTitle("SecuGen Fingerprint SDK");
            dlgAlert.setPositiveButton("OK",
                    (dialog, whichButton) -> finish()
            );
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
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
            } else {
                boolean hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                if (!hasPermission) {
                    if (!usbPermissionRequested) {
                        debugMessage("Requesting USB Permission\n");
                        //Log.d(TAG, "Call GetUsbManager().requestPermission()");
                        usbPermissionRequested = true;
                        sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent);
                    } else {
                        //wait up to 20 seconds for the system to grant USB permission
                        hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                        debugMessage("Waiting for USB Permission\n");
                        int i = 0;
                        while ((!hasPermission) && (i <= 40)) {
                            ++i;
                            hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (hasPermission) {
                    debugMessage("Opening SecuGen Device\n");
                    error = sgfplib.OpenDevice(0);
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
                        debugMessage("Image width: " + mImageWidth + "\n");
                        debugMessage("Image height: " + mImageHeight + "\n");
                        debugMessage("Image resolution: " + mImageDPI + "\n");
                        debugMessage("Serial Number: " + new String(deviceInfo.deviceSN()) + "\n");

                        sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
                        sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
                        debugMessage("TEMPLATE_FORMAT_SG400 SIZE: " + mMaxTemplateSize[0] + "\n");
                        mRegisterTemplate = new byte[(int) mMaxTemplateSize[0]];

                        EnableControls();
                    } else {
                        debugMessage("Waiting for USB Permission\n");
                    }
                }
            }
        }
        Log.d(TAG, "Exit onResume()");
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

        this.mButtonRegister.setClickable(true);
        this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        this.mButtonSaveCapture.setClickable(true);
        this.mButtonSaveCapture.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void DisableControls() {

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
                    if(finishOnOk){
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


    public void colorCapturedButton(FingerPositions fingerPosition, int color, int typeface){
//
        if (fingerPosition == FingerPositions.LeftThumb) {
            this.fingerLeftThumb.setTextColor(getResources().getColor(color));
            this.fingerLeftThumb.setTypeface(this.fingerLeftThumb.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.LeftIndex) {
            this.fingerLeftIndex.setTextColor(getResources().getColor(color));
            this.fingerLeftIndex.setTypeface(this.fingerLeftIndex.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.LeftMiddle) {
            this.fingerLeftMiddle.setTextColor(getResources().getColor(color));
            this.fingerLeftMiddle.setTypeface(this.fingerLeftMiddle.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.LeftWedding) {
            this.fingerLeftRing.setTextColor(getResources().getColor(color));
            this.fingerLeftRing.setTypeface(this.fingerLeftRing.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.LeftSmall) {
            this.fingerLeftPinky.setTextColor(getResources().getColor(color));
            this.fingerLeftPinky.setTypeface(this.fingerLeftPinky.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.RightThumb) {
            this.fingerRightThumb.setTextColor(getResources().getColor(color));
            this.fingerRightThumb.setTypeface(this.fingerRightThumb.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.RightIndex) {
            this.fingerRightIndex.setTextColor(getResources().getColor(color));
            this.fingerRightIndex.setTypeface(this.fingerRightIndex.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.RightMiddle) {
            this.fingerRightMiddle.setTextColor(getResources().getColor(color));
            this.fingerRightMiddle.setTypeface(this.fingerRightMiddle.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.RightWedding) {
            this.fingerRightRing.setTextColor(getResources().getColor(color));
            this.fingerRightRing.setTypeface(this.fingerRightRing.getTypeface(), typeface);
        } else if (fingerPosition == FingerPositions.RightSmall) {
            this.fingerRightPinky.setTextColor(getResources().getColor(color));
            this.fingerRightPinky.setTypeface(this.fingerRightPinky.getTypeface(), typeface);
        }
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

    public void createViewObject(){
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

        //Changing selected Image View
        fingerPrintImageDisplay = (ImageView) findViewById(R.id.fingerPrintImage);

        mButtonRegister = (Button) findViewById(R.id.buttonRegister);
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


    //////////////THESE AREA CONTAIN SECUGEN STANDARD CONFIGURATION CODE/////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //This broadcast receiver is necessary to get user permissions to access the attached USB device
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            debugMessage("Vendor ID : " + device.getVendorId() + "\n");
                            debugMessage("Product ID: " + device.getProductId() + "\n");
                            debugMessage("USB BroadcastReceiver VID : " + device.getVendorId() + "\n");
                            debugMessage("USB BroadcastReceiver PID: " + device.getProductId() + "\n");
                        } else
                            Log.e(TAG, "mUsbReceiver.onReceive() Device is null");
                    } else
                        Log.e(TAG, "mUsbReceiver.onReceive() permission denied for device " + device);
                }
            }
        }
    };

//    //////////////////////////////////////////////////////////////////////////////////////////////
//    //////////////////////////////////////////////////////////////////////////////////////////////
//    public void SGFingerPresentCallback() {
//        autoOn.stop();
//        fingerDetectedHandler.sendMessage(new Message());
//    }


//    //////////////////////////////////////////////////////////////////////////////////////////////
//    //////////////////////////////////////////////////////////////////////////////////////////////
//    //This message handler is used to access local resources not
//    //accessible by SGFingerPresentCallback() because it is called by
//    //a separate thread.
//    public Handler fingerDetectedHandler = new Handler() {
//        // @Override
//        public void handleMessage(Message msg) {
//            //Handle the message
//            //CaptureFingerPrint();
//            if (mAutoOnEnabled) {
//                EnableControls();
//            }
//        }
//    };
}