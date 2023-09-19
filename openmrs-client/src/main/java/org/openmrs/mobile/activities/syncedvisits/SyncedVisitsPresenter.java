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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.activeandroid.query.Select;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.dao.PatientBiometricJoinDAO;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FilterUtil;
import org.openmrs.mobile.utilities.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SyncedVisitsPresenter extends BasePresenter implements SyncedVisitsContract.Presenter {

    // View
    @NonNull
    private final SyncedVisitsContract.View syncedVisitsView;
    private PatientDAO patientDAO;


    // Query for data filtering
    @Nullable
    private String mQuery;

    public void deletePatient(Patient mPatient) {
        new PatientDAO().deletePatient(mPatient.getId());
        addSubscription(new VisitDAO().deleteVisitsByPatientId(mPatient.getId())
                .observeOn(Schedulers.io())
                .subscribe());
    }

    public SyncedVisitsPresenter(@NonNull SyncedVisitsContract.View syncedVisitsView, String mQuery) {
        this.syncedVisitsView = syncedVisitsView;
        this.syncedVisitsView.setPresenter(this);
        this.mQuery = mQuery;
        this.patientDAO = new PatientDAO();
    }

    public SyncedVisitsPresenter(@NonNull SyncedVisitsContract.View syncedVisitsView) {
        this.patientDAO = new PatientDAO();
        this.syncedVisitsView = syncedVisitsView;
        this.syncedVisitsView.setPresenter(this);
    }

    public SyncedVisitsPresenter(@NonNull SyncedVisitsContract.View syncedVisitsView, PatientDAO patientDAO) {
        this.patientDAO= patientDAO;
        this.syncedVisitsView = syncedVisitsView;
        this.syncedVisitsView.setPresenter(this);
    }

    /**
     * Used to display initial data on activity trigger
     */
    @Override
    public void subscribe() {
        updateLocalPatientsList();
    }

    /**
     * Sets query used to filter (used by Activity's ActionBar)
     */
    @Override
    public void setQuery(String query) {
        mQuery = query;
    }

    /**
     * Used to update local patients list
     * It handles search events and replaces View's data to display
     */
    @Override
    public void updateLocalPatientsList() {
        addSubscription(patientDAO.getAllPatients()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(patientList -> {
                    boolean isFiltering = StringUtils.notNull(mQuery) && !mQuery.isEmpty();

                    if (isFiltering) {
                        patientList = FilterUtil.getPatientsFilteredByQuery(patientList, mQuery);
                        if (patientList.isEmpty()) {
                            syncedVisitsView.updateListVisibility(false, mQuery);
                        } else {
                            syncedVisitsView.updateListVisibility(true);
                        }
                    } else {
                        if (patientList.isEmpty()) {
                            syncedVisitsView.updateListVisibility(false);
                        } else {
                            syncedVisitsView.updateListVisibility(true);
                        }
                    }

                    // all patients with biometric as map
                    Map<Long,Patient>  patientsWithPrints =  new  PatientBiometricJoinDAO().getPatientWithPBS_();
                    List<Patient> filteredList = new ArrayList<>();
                            patientList = FilterUtil.getPatientsFilteredByQuery(patientList);
                            // patient list is the list of all patients in the local database when filter is not applied
                    for (Patient patient : patientList) {
                        Patient p= patientsWithPrints.get(patient.getId());
                        List<Encountercreate> encountersSynced = new Select()
                                .from(Encountercreate.class)
                                .where("patientid = ? AND synced = 0", patient.getId())
                                .execute();

                        if (!encountersSynced.isEmpty() || !patient.isSynced()){
                            if(p!=null) {
                                // check if PBS not synced and PBS data reached minimum requirement add PBS flag
                                if(!p.isFingerprintFullSync()&&  p.getFingerprintCount()>=ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT
                                ) {
                                    p.setDisplayPBS("PBS");
                                }
                                p.setDisplay("Pending");
                                filteredList.add(p);
                            }else {
                                patient.setDisplay("Pending");
                                filteredList.add(patient);
                            }
                        }else{
                            if(p!=null) {
                                // check if PBS not synced and PBS data reached minimum requirement
                                if(!p.isFingerprintFullSync()&&
                                        p.getFingerprintCount()>=ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT
                                ) {
                                    p.setDisplay("");
                                    p.setDisplayPBS("PBS");
                                    filteredList.add(p);
                                }
                            }
                        }


                    }
                    syncedVisitsView.updateAdapter(filteredList);
                }));

    }

}
