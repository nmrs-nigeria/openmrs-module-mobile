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

package org.openmrs.mobile.activities.login;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;

public class LoginActivity extends ACBaseActivity {

    Context context;
    private String[] permissions_to_enable;
    private static final int REQUEST_CODE_FOR_ALL_PERMISSIONS = 1;

    public LoginContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(!isConnected()){
            Toast.makeText(getApplicationContext(), "Not Connected! ", Toast.LENGTH_SHORT).show();
            showAlert();
        }else{
            Toast.makeText(getApplicationContext(), "Connected! ", Toast.LENGTH_SHORT).show();
        }


        requestPermission();

        // Create fragment
        LoginFragment loginFragment =
                (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.loginContentFrame);
        if (loginFragment == null) {
            loginFragment = LoginFragment.newInstance();
        }
        if (!loginFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    loginFragment, R.id.loginContentFrame);
        }

        mPresenter = new LoginPresenter(loginFragment, mOpenMRS);
    }

    private void requestPermission() {

        permissions_to_enable = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
        };

        ActivityCompat.requestPermissions(this, permissions_to_enable, REQUEST_CODE_FOR_ALL_PERMISSIONS);
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_title))
                .setIcon(R.drawable.icon_wifi_off)
                .setMessage(getString(R.string.alert_message))
                .setPositiveButton("EXIT APPLICATION", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        System.exit(0);
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }



}
