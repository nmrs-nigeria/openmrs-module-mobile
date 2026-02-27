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


package org.openmrs.mobile.activities.settings;

import static org.openmrs.mobile.utilities.VersioningUtil.compareVersions;
import static org.openmrs.mobile.utilities.VersioningUtil.extractVersion;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.logs.LogsActivity;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.services.ConceptDownloadService;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.apache.commons.codec.binary.Hex;
import android.os.Build;
import android.widget.Toast;

public class SettingsFragment extends ACBaseFragment<SettingsContract.Presenter> implements SettingsContract.View {

    private BroadcastReceiver bReceiver;

    private TextView conceptsInDbTextView;
    private ImageButton downloadConceptsButton;
    private SwitchCompat darkModeSwitch;

    private View root;
    private TextView mMetadataVersionLabel;
    private TextView mPbsServiceVersionLabel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_settings, container, false);

        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPresenter.updateConceptsInDBTextView();
            }
        };

        setUpConceptsView();
        setMetadataVersion();
        setPbsServiceVersion();
        getDeviceInfo();

      View  viewToSave =  root ;//rootfindViewById(R.id.view_to_save);  // The view you want to capture
        Button saveButton = root.findViewById(R.id.save_version_report);  // The button to trigger saving

        // Request write permission if not granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveViewAsImage(viewToSave);
            }
        });




        return root;
    }



    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(bReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.updateConceptsInDBTextView();
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(bReceiver, new IntentFilter(ApplicationConstants.BroadcastActions.CONCEPT_DOWNLOAD_BROADCAST_INTENT_ID));
    }

    @Override
    public void setConceptsInDbText(String text) {
        if(text.equals("0")){
            downloadConceptsButton.setEnabled(true);
            ToastUtil.showShortToast(getActivity(), ToastUtil.ToastType.WARNING,                    R.string.settings_no_concepts_toast);
        }else{
            downloadConceptsButton.setEnabled(false);
        }
        conceptsInDbTextView.setText(text);
    }
// device name
    // facility name
    @Override
    public void addLogsInfo(long logSize, String logFilename) {
        LinearLayout logsLl = root.findViewById(R.id.frag_settings_logs_ll);
        TextView logsDesc1Tv = root.findViewById(R.id.frag_settings_logs_desc1_tv);
        TextView logsDesc2Tv = root.findViewById(R.id.frag_settings_logs_desc2_tv);

        logsDesc1Tv.setText(logFilename);
        logsDesc2Tv.setText(getContext().getString(R.string.settings_frag_size) + logSize + "kB");
        logsLl.setOnClickListener(view ->{
            Intent i = new Intent(view.getContext() , LogsActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void setUpConceptsView() {
        conceptsInDbTextView = root.findViewById(R.id.frag_settings_concepts_count_tv);

        downloadConceptsButton = root.findViewById(R.id.frag_settings_concepts_download_btn);

        downloadConceptsButton.setOnClickListener(view -> {
            downloadConceptsButton.setEnabled(false);
            Intent startIntent = new Intent(getActivity(), ConceptDownloadService.class);
            startIntent.setAction(ApplicationConstants.ServiceActions.START_CONCEPT_DOWNLOAD_ACTION);
            Objects.requireNonNull(getActivity()).startService(startIntent);
        });
    }

    @Override
    public void addBuildVersionInfo() {
        String versionName = "";
        int buildVersion = 0;

        PackageManager packageManager = this.getActivity().getPackageManager();
        String packageName = this.getActivity().getPackageName();

        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName;
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            buildVersion = ai.metaData.getInt("buildVersion");
        } catch (PackageManager.NameNotFoundException e) {
            mPresenter.logException("Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            mPresenter.logException("Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        TextView appName = root.findViewById(R.id.frag_settings_app_name_tv);
        TextView version = root.findViewById(R.id.frag_settings_version_tv);

        appName.setText(getResources().getString(R.string.app_name));
        version.setText(versionName + getContext().getString(R.string.frag_settings_build) + buildVersion);
    }

    @Override
    public void addPrivacyPolicyInfo() {
        LinearLayout privacyPolicyTv = root.findViewById(R.id.frag_settings_privacy_policy_ll);
        privacyPolicyTv.setOnClickListener(view ->{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(view.getContext().getString(R.string.url_privacy_policy)));
            startActivity(i);
        });
    }

    @Override
    public void rateUs() {
        LinearLayout rateUsLL = root.findViewById(R.id.frag_settings_rate_us_ll);
        rateUsLL.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=" + ApplicationConstants.PACKAGE_NAME);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);

            // Ignore Playstore backstack, on back press will take us back to our app
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            try{
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + ApplicationConstants.PACKAGE_NAME)));
            }
        });

    }

    @Override
    public void setDarkMode() {
        darkModeSwitch = root.findViewById(R.id.frag_settings_dark_mode_switch);
        darkModeSwitch.setChecked(mPresenter.isDarkModeActivated());

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPresenter.setDarkMode(isChecked);
            getActivity().recreate();
        });
    }


    private void setPbsServiceVersion() {
        mPbsServiceVersionLabel = root.findViewById(R.id.pbs_server_version);
        String pbsValues = OpenMRS.getInstance().getPBSserverVersion();
        String version = "Unknown";
        String time = "Unknown";
        try{
            String []  arrayValues = pbsValues.split("/");
            version=  arrayValues[0];
            time=arrayValues[1];
        }catch (Exception ignored){
        }
        mPbsServiceVersionLabel.setText("PBS Service Version: "+version+"\nLast checked "+time);
         pbsVersionOkay(version);
    }
    private void setMetadataVersion() {
        mMetadataVersionLabel = root.findViewById(R.id.metadata_version);
        String metadataValues = OpenMRS.getInstance().getMetadataVersion();
        boolean started = false;
        String version = "Unknown";
        try{
            String []  arrayValues = metadataValues.split("/");
            version=  arrayValues[0];
            if(arrayValues.length>1){
                if( "true".equals(arrayValues[1])){
                    started = true;
                }
            }
        }catch (Exception ignored){
        }
        mMetadataVersionLabel.setText("Metadata Version: "+version);

        if(started){
            metadataVersionOkay(version);
        } else{
            mMetadataVersionLabel.setTextColor(getResources().getColor(R.color.red));
            ToastUtil.error("Metadata is not started, start and reopen the App");

        }
        Util.log("done");


    }
    private boolean pbsVersionOkay(String serverVersion) {
        if(!ApplicationConstants.CURRENT_PBS_SERVICE_VERSION.equals(serverVersion))   {
            // Extract version numbers
            List<Integer> v1 = extractVersion(ApplicationConstants.CURRENT_PBS_SERVICE_VERSION);
            List<Integer> v2 = extractVersion(serverVersion);
            // Compare versions
            int result =    compareVersions(v1, v2);
            // Print the result
            if (result < 0) {  //
                Util.log (ApplicationConstants.CURRENT_PBS_SERVICE_VERSION + " is lower than " +serverVersion);
                mPbsServiceVersionLabel.setTextColor(getResources().getColor(R.color.design_default_color_primary_dark));

            } else if (result > 0) {
                mPbsServiceVersionLabel.setTextColor(getResources().getColor(R.color.red));
                Util.log (ApplicationConstants.CURRENT_PBS_SERVICE_VERSION + " is higher than " +serverVersion);
                ToastUtil.error("PBS service is is Lower than expected");
                return  false;
            } else {

                Util.log (serverVersion + " is equal to " + serverVersion);
            }
        }

        return  true;
    }


    private boolean metadataVersionOkay(String serverVersion) {
        if(!ApplicationConstants.CURRENT_METADATA_VERSION.equals(serverVersion))   {
            // Extract version numbers
            List<Integer> v1 = extractVersion(ApplicationConstants.CURRENT_METADATA_VERSION);
            List<Integer> v2 = extractVersion(serverVersion);
            // Compare versions
            int result =    compareVersions(v1, v2);
            // Print the result
            if (result < 0) {  //
                Util.log (ApplicationConstants.CURRENT_METADATA_VERSION + " is lower than " +serverVersion);
                mMetadataVersionLabel.setTextColor(getResources().getColor(R.color.design_default_color_primary_dark));
                //
            } else if (result > 0) {
                mMetadataVersionLabel.setTextColor(getResources().getColor(R.color.red));
                Util.log (ApplicationConstants.CURRENT_METADATA_VERSION + " is higher than " +serverVersion);
                ToastUtil.error("Metadata is Lower than expected");
                return  false;
            } else {

                Util.log (serverVersion + " is equal to " + serverVersion);
            }
        }

        return  true;
    }



    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }


    public void getDeviceInfo() {
       OpenMRS openMRS =OpenMRS.getInstance();
        String deviceName = Build.MODEL;  // Device name/model
        String deviceUniqueId = getDeviceUniqueId() ;      // Call method to get Wi-Fi ID
        String facility = openMRS.getLocationDisplay();  // Call another method to get facility info
       TextView textView= root.findViewById(R.id.detail);






       textView.setText(String.format("%s%s%s",facility+"\n", deviceName+"\n",  deviceUniqueId+"\n"));



    }

    // Method to get  device ID
    private String getDeviceUniqueId() {
        String m_androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if(m_androidId!=null) {
            return m_androidId;
        }
        return "Unknown";
    }


    // save the current view for report
    private void saveViewAsImage(View view) {
        // Create a bitmap from the view
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);


        String datimeCode=OpenMRS.getInstance().getOpenMRSSharedPreferences().getString("datim_code", "");
        String fileName =datimeCode+"_"+getString(R.string.app_version).split(":")[1]+"_"+getDeviceUniqueId();



        // Convert string to bytes (encoding)
        byte[] byteArray = fileName.getBytes();

        // Encode to hexadecimal
        String hexEncoded = bytesToHex(byteArray);

        // Decode the hexadecimal string back to bytes
        byte[] decodedBytes = hexToBytes(hexEncoded);
        String decodedString = new String(decodedBytes);

        // Save the bitmap to a file
        File file = new File(OpenMRSCustomHandler.createFolderVersion() + "/"+hexEncoded+".png");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(getContext(), "Report saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("SaveViewAsImage", "Error saving image", e);
            Toast.makeText(getContext(), "Fail to save report"  , Toast.LENGTH_LONG).show();
        }
    }


    // Method to convert a byte array to a hexadecimal string (encoding)
    public   String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Method to convert a hexadecimal string back to a byte array (decoding)
    public  byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }

    int  REQUEST_WRITE_STORAGE=1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Write permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Write permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}