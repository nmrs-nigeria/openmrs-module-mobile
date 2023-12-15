/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.mobile.api;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.activeandroid.query.Select;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.openmrs.mobile.api.repository.VisitRepository;
import org.openmrs.mobile.api.retrofit.ProgramRepository;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.listeners.retrofit.StartVisitResponseListenerCallback;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.EncounterProvider;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.ProgramEnrollment;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;

import static org.openmrs.mobile.utilities.FormService.getFormResourceByName;

public class EncounterService extends IntentService implements CustomApiCallback {

    private final RestApi apiService = RestServiceBuilder.createService(RestApi.class);
    private RestApi restApi;

    public EncounterService() {
        super("Save Encounter");
        restApi = RestServiceBuilder.createService(RestApi.class);
    }


    public void addEncounter(final Encountercreate encountercreate, String encounterDate, @Nullable DefaultResponseCallbackListener callbackListener) {

        if (NetworkUtils.isOnline()) {
            new VisitDAO().getActiveVisitByPatientId(encountercreate.getPatientId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(visit -> {
                        if (visit != null) {
                            encountercreate.setVisit(visit.getUuid());
                            if (callbackListener != null) {
                                syncEncounter(encountercreate, callbackListener);
                            } else {
                                syncEncounter(encountercreate);
                            }
                        } else {

                            startNewVisitForEncounter(encountercreate, encounterDate);
                        }
                    });
        } else
            ToastUtil.warning("No internet connection. Form data is saved locally " +
                    "and will sync when internet connection is restored. ");
    }


    public void addEncounter(final Encountercreate encountercreate, final String encounterDate) {
        addEncounter(encountercreate, encounterDate, null);
    }

    private void startNewVisitForEncounter(final Encountercreate encountercreate, String encounterDate, @Nullable final DefaultResponseCallbackListener callbackListener) {
        new VisitRepository().startVisit(new PatientDAO().findPatientByUUID(encountercreate.getPatient()), encounterDate,
                new StartVisitResponseListenerCallback() {
                    @Override
                    public void onStartVisitResponse(long id) {
                        new VisitDAO().getVisitByID(id)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(visit -> {
                                    encountercreate.setVisit(visit.getUuid());
                                    if (callbackListener != null) {
                                        syncEncounter(encountercreate, callbackListener);
                                    } else {
                                        syncEncounter(encountercreate);
                                    }
                                });
                    }

                    @Override
                    public void onResponse() {
                        // This method is intentionally empty
                        ToastUtil.error("uu");

                    }

                    @Override
                    public void onErrorResponse(String errorMessage) {
                        ToastUtil.error(errorMessage);
                    }
                });
    }

    public void startNewVisitForEncounter(final Encountercreate encountercreate, String encounterDate) {
        startNewVisitForEncounter(encountercreate, encounterDate, null);
    }

    public void syncEncounter(final Encountercreate encountercreate, @Nullable final DefaultResponseCallbackListener callbackListener) {

        if (NetworkUtils.isOnline()) {
            try {
            encountercreate.pullObslist();
            encountercreate.setFormUuid(getFormResourceByName(encountercreate.getFormname()).getUuid());
            Call<Encounter> call = apiService.createEncounter(encountercreate);
            call.enqueue(new Callback<Encounter>() {
                @Override
                public void onResponse(@NonNull Call<Encounter> call, @NonNull Response<Encounter> response) {
                    if (response.isSuccessful()) {
                        Encounter encounter = response.body();
                        linkvisit(encountercreate.getPatientId(), encountercreate.getFormname(), encounter, encountercreate);
                        encountercreate.setSynced(true);
                        encountercreate.save();
                        new VisitRepository().syncLastVitals(encountercreate.getPatient());
                        if (callbackListener != null) {
                            callbackListener.onResponse();
                        }
                    } else {
                        String error = "An error occurred.";
                        try {
                            error = response.errorBody().string();
                            Log.e("EncounterService", "onResponse: " + error);

                            JSONObject jObjError = new JSONObject(error);
                            error = jObjError.getJSONObject("error").getString("message");
                        } catch (Exception ee) {
                            error = response.errorBody().toString();
                        }

                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(error);
                        }
                    }
                }

                    @Override
                    public void onFailure(@NonNull Call<Encounter> call, @NonNull Throwable t) {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(t.getLocalizedMessage());
                        }
                    }
                });
            }catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            ToastUtil.error("Sync is off. Turn on sync to save form data.");
        }

    }

    public void syncEncounter(final Encountercreate encountercreate) {
        syncEncounter(encountercreate, null);
    }

    private void linkvisit(Long patientid, String formname, Encounter encounter, Encountercreate encountercreate) {
        VisitDAO visitDAO = new VisitDAO();
        String uuid = "";

        try {
            if (encounter != null)
                uuid = encounter.getVisit().getUuid();
        } catch (Exception e) {
            if (encountercreate != null)
                uuid = encountercreate.getVisit();
        }
        visitDAO.getVisitByUuid(uuid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(visit -> {
                    encounter.setEncounterType(new EncounterType(formname));
                    for (int i = 0; i < encountercreate.getObservations().size(); i++) {
                        encounter.getObservations().get(i).setDisplayValue
                                (encountercreate.getObservations().get(i).getValue());
                    }
                    if (visit != null) {
                        List<Encounter> encounterList = visit.getEncounters();
                        encounterList.add(encounter);
                        visitDAO.saveOrUpdate(visit, patientid)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(id ->
                                        ToastUtil.success(formname + " data saved successfully"));
                    }
                });

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (NetworkUtils.isOnline()) {
            List<Encountercreate> encountercreatelist = new Select()
                    .from(Encountercreate.class)
                    .execute();
            for (final Encountercreate encountercreate : encountercreatelist) {
                try {
                    Patient patient = new PatientDAO().findPatientByID(Long.toString(encountercreate.getPatientId()));
                    if (!encountercreate.getSynced() && null !=patient &&
                            patient.isSynced()) {
                        List<EncounterProvider> encounterProviders = new ArrayList<>();
                        EncounterProvider encounterProvider = new EncounterProvider();
                        encounterProvider.setProvider("f9badd80-ab76-11e2-9e96-0800200c9a66");
                        encounterProvider.setEncounterRole("a0b03050-c99b-11e0-9572-0800200c9a66");
                        encounterProviders.add(encounterProvider);
                        encountercreate.setEncounterProviders(encounterProviders);
                        if (encountercreate.getFormname().equals("Client intake form")) {
                            ProgramEnrollment programEnrollment = new ProgramEnrollment();
                            programEnrollment.setPatient(encountercreate.getPatient());
                            programEnrollment.setProgram("14d6f977-7952-41cd-b243-1c3bcc4a9213");
                            programEnrollment.setDateEnrolled(encountercreate.getEncounterDatetime());
                            ProgramRepository programRepository = new ProgramRepository();
                            programRepository.addProgram(restApi, programEnrollment, this);
                        }
                        if (encountercreate.getFormname().equals("HIV Enrollment")) {

                            ProgramEnrollment programEnrollment = new ProgramEnrollment();
                            programEnrollment.setPatient(encountercreate.getPatient());
                            programEnrollment.setProgram("9083deaa-f37f-44b3-9046-b87b134711a1");
                            programEnrollment.setDateEnrolled(encountercreate.getEncounterDatetime());
                            ProgramRepository programRepository = new ProgramRepository();
                            programRepository.addProgram(restApi, programEnrollment, this);
                        }
                        if (encountercreate.getFormname().equals("General Antenatal Care")) {
                            ProgramEnrollment programEnrollment = new ProgramEnrollment();
                            programEnrollment.setPatient(encountercreate.getPatient());
                            programEnrollment.setProgram("c3ae2d33-97d3-4cc4-9206-8a8160593648");
                            programEnrollment.setDateEnrolled(encountercreate.getEncounterDatetime());
                            ProgramRepository programRepository = new ProgramRepository();
                            programRepository.addProgram(restApi, programEnrollment, this);
                        }
                        if (null != encountercreate.getVisit()) {
                            new VisitDAO().getActiveVisitByUUID(encountercreate.getVisit())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(visit -> {
                                        if (visit != null) {
                                            new VisitRepository().reOpenVisitByUUID(new VisitDAO().getVisitByIDLocally(visit.getId()));
                                            encountercreate.setVisit(visit.getUuid());
                                            visit.setStopDatetime(null);
                                            new VisitDAO().updateVisitLocally(visit, visit.getId(), visit.getPatient().getId());

                                            syncEncounter(encountercreate);

                                        } else {
                                            // new VisitRepository().endVisitByUUID(new VisitDAO().getActiveVisitByUUID(encountercreate.getVisit())); //  new VisitDAO().getVisitByIDLocally(visit.getId()));
                                            startNewVisitForEncounter(encountercreate, encountercreate.getEncounterDatetime());
                                        }

                                    });
                        } else {
                            startNewVisitForEncounter(encountercreate, encountercreate.getEncounterDatetime());
                        }
                    }
                }catch (Exception e){
                    ToastUtil.error(e.toString());
                }
            }


        } else {
            ToastUtil.warning("No internet connection. Form data is saved locally " +
                    "and will sync when internet connection is restored. ");
        }
    }


    //    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (NetworkUtils.isOnline()) {
//
//            List<Encountercreate> encountercreatelist = new Select()
//                    .from(Encountercreate.class)
//                    .execute();
//
//
//            for (final Encountercreate encountercreate : encountercreatelist) {
//                if (!encountercreate.getSynced() &&
//                        new PatientDAO().findPatientByID(Long.toString(encountercreate.getPatientId())).isSynced()) {
////                    new VisitRepository().syncVisit(encountercreate.getPatient());
//                    new VisitDAO().getActiveVisitByPatientId(encountercreate.getPatientId())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(visit -> {
//                                if (visit != null) {
//                                    encountercreate.setVisit(visit.getUuid());
//                                    syncEncounter(encountercreate);
//
//                                } else {
//                                    startNewVisitForEncounter(encountercreate);
//                                }
//                            });
//                }
//            }
//
//
//        } else {
//            ToastUtil.error("No internet connection. Form data is saved locally " +
//                    "and will sync when internet connection is restored. ");
//        }
//    }
    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure() {

    }

}