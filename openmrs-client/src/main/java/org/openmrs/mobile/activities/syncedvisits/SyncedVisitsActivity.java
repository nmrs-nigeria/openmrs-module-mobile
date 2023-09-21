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

package org.openmrs.mobile.activities.syncedvisits;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.lastviewedpatients.LastViewedPatientsActivity;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.StringUtils;

public class SyncedVisitsActivity extends ACBaseActivity {

    public SyncedVisitsPresenter mPresenter;
    private SearchView searchView;
    private String query;

    //Menu Items
    private MenuItem mAddPatientMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_patients);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Create fragment
        SyncedVisitsFragment syncedVisitsFragment =
                (SyncedVisitsFragment) getSupportFragmentManager().findFragmentById(R.id.syncedPatientsContentFrame);
        if (syncedVisitsFragment == null) {
            syncedVisitsFragment = SyncedVisitsFragment.newInstance();
        }
        if (!syncedVisitsFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    syncedVisitsFragment, R.id.syncedPatientsContentFrame);
        }

        if(savedInstanceState != null){
            query = savedInstanceState.getString(ApplicationConstants.BundleKeys.PATIENT_QUERY_BUNDLE, "");
            mPresenter = new SyncedVisitsPresenter(syncedVisitsFragment, query);
        } else {
            mPresenter = new SyncedVisitsPresenter(syncedVisitsFragment);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String query = searchView.getQuery().toString();
        outState.putString(ApplicationConstants.BundleKeys.PATIENT_QUERY_BUNDLE, query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            /*case R.id.syncbutton:
                enableAddPatient(OpenMRS.getInstance().getSyncState());
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    ToastUtil.notify("Syncing switched on, attempting to sync patients and form data");
//                    Intent i = new Intent(this, PatientService.class);
//                    this.startService(i);
//                    Intent i1 = new Intent(this, EncounterService.class);
//                    this.startService(i1);
//                }
                break;*/
            case R.id.actionAddPatients:
                Intent intent = new Intent(this, LastViewedPatientsActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                // Do nothing
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.find_locally_and_add_patients_menu, menu);

        mAddPatientMenuItem = menu.findItem(R.id.actionAddPatients);
        enableAddPatient(OpenMRS.getInstance().getSyncState());

        // Search function
        MenuItem searchMenuItem = menu.findItem(R.id.actionSearchLocal);
        searchView = (SearchView) searchMenuItem.getActionView();

        if(StringUtils.notEmpty(query)){
            searchMenuItem.expandActionView();
            searchView.setQuery(query, true);
            searchView.clearFocus();
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mPresenter.setQuery(query);
                mPresenter.updateLocalPatientsList();
                return true;
            }
        });

        return true;
    }

    private void enableAddPatient(boolean enabled) {
        int resId = enabled ? R.drawable.ic_add : R.drawable.ic_add_disabled;
        mAddPatientMenuItem.setEnabled(enabled);
        mAddPatientMenuItem.setIcon(resId);
    }

}