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

package org.openmrs.mobile.activities.syncedpatients;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.lastviewedpatients.LastViewedPatientsActivity;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SyncedPatientsFragment extends ACBaseFragment<SyncedPatientsContract.Presenter> implements SyncedPatientsContract.View {

    // Fragment components
    private TextView mEmptyList;
    private RecyclerView mSyncedPatientRecyclerView;

    //Initialization Progress bar
    private ProgressBar mProgressBar;

    private MenuItem mAddPatientMenuItem;
private TextView totalPatientsTextView;
private  TextView dataPatientsTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_synced_patients, container, false);

        // Patient list config
        mSyncedPatientRecyclerView = root.findViewById(R.id.syncedPatientRecyclerView);
        mSyncedPatientRecyclerView.setHasFixedSize(true);
        mSyncedPatientRecyclerView.setAdapter(new SyncedPatientsRecyclerViewAdapter(this,
                new ArrayList<>()));
        mSyncedPatientRecyclerView.setVisibility(View.GONE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(root.getContext());
        mSyncedPatientRecyclerView.setLayoutManager(linearLayoutManager);

        mEmptyList = root.findViewById(R.id.emptySyncedPatientList);
        mProgressBar = root.findViewById(R.id.syncedPatientsInitialProgressBar);
        totalPatientsTextView=root.findViewById(R.id.totalPatients);
        dataPatientsTextView=root.findViewById(R.id.dataPatients);

        // Font config
        FontsUtil.setFont(this.getActivity().findViewById(android.R.id.content));

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            /*case R.id.syncbutton:
                enableAddPatient(OpenMRS.getInstance().getSyncState());
                break;*/
            case R.id.actionAddPatients:
                if (NetworkUtils.hasNetwork()) {
                    Intent intent = new Intent(getActivity(), LastViewedPatientsActivity.class);
                    startActivity(intent);
                }else {
                    NoInternetConnectionSnackbar();
                }
                break;
            default:
                // Do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is used to update data in SyncedPatientRecyclerView.
     * It creates SyncedPatientsRecyclerViewAdapter and fills it with fresh data.
     * Then new instance of SyncedPatientsRecyclerViewAdapter is set to its SyncedPatientRecyclerView.
     *
     * @param patientList new set of data
     */
    @Override
    public void updateAdapter(List<Patient> patientList) {
        SyncedPatientsRecyclerViewAdapter adapter = new SyncedPatientsRecyclerViewAdapter(this, patientList);
        adapter.notifyDataSetChanged();
        mSyncedPatientRecyclerView.setAdapter(adapter);
        totalPatientsTextView.setText(String.format("%d", patientList.size()));

        Single.fromCallable(() -> getPatientsWithUpdatedData(patientList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataPatients -> {
                    dataPatientsTextView.setText(String.format("%d", dataPatients.size()));
                }, throwable -> {
                    dataPatientsTextView.setText("Error");
                });
    }



    private List<Patient> getPatientsWithUpdatedData(List<Patient> patientList) {
        EncounterDAO encounterDAO = new EncounterDAO();
        FingerPrintDAO fingerPrintDAO = new FingerPrintDAO();
        FingerPrintVerificationDAO fingerPrintVerificationDAO = new FingerPrintVerificationDAO();
        List<Patient> newPatientList = new ArrayList<>();
        for (Patient patient : patientList) {
            Long id = patient.getId();
            boolean isEncounterSafeToDelete = encounterDAO.safeToDelete(id);
            boolean isFingerprintSafeToDelete = fingerPrintDAO.safeToDelete(id);
            boolean isFingerprintsVerificationSafeToDelete = fingerPrintVerificationDAO.safeToDelete(id);
            if (patient.isSynced()
                    && isEncounterSafeToDelete &&
                    isFingerprintSafeToDelete
                    && isFingerprintsVerificationSafeToDelete) {
                // data already sync
            } else {
                newPatientList.add(patient);
            }
        }

        return newPatientList;
    }

    @Override
    public void updateListVisibility(boolean isVisible) {
        mProgressBar.setVisibility(View.GONE);
        if (isVisible) {
            mSyncedPatientRecyclerView.setVisibility(View.VISIBLE);
            mEmptyList.setVisibility(View.GONE);
        } else {
            mSyncedPatientRecyclerView.setVisibility(View.GONE);
            mEmptyList.setVisibility(View.VISIBLE);
            mEmptyList.setText(getString(R.string.search_patient_no_results));
        }
    }

    @Override
    public void updateListVisibility(boolean isVisible, @NonNull String replacementWord) {
        if (isVisible) {
            mSyncedPatientRecyclerView.setVisibility(View.VISIBLE);
            mEmptyList.setVisibility(View.GONE);
        } else {
            mSyncedPatientRecyclerView.setVisibility(View.GONE);
            mEmptyList.setVisibility(View.VISIBLE);
            mEmptyList.setText(getString(R.string.search_patient_no_result_for_query, replacementWord));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        mAddPatientMenuItem = menu.findItem(R.id.actionAddPatients);
        enableAddPatient(OpenMRS.getInstance().getSyncState());
    }

    private void enableAddPatient(boolean enabled) {
        int resId = enabled ? R.drawable.ic_add : R.drawable.ic_add_disabled;
        mAddPatientMenuItem.setEnabled(enabled);
        mAddPatientMenuItem.setIcon(resId);
    }
    private void NoInternetConnectionSnackbar() {
        Snackbar mSnackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                R.string.snackbar_no_internet_connection, Snackbar.LENGTH_SHORT);
        View sbView = mSnackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        mSnackbar.show();
    }

    /**
     * @return New instance of SyncedPatientsFragment
     */
    public static SyncedPatientsFragment newInstance() {
        return new SyncedPatientsFragment();
    }

}
