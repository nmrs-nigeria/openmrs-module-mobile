package org.openmrs.mobile.sync;

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


import static org.openmrs.mobile.utilities.FormService.getFormResourceByName;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.activeandroid.query.Select;

import org.openmrs.mobile.R;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.RestServiceBuilderSpecial;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.LocationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.EncounterProvider;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.ProgramEnrollment;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.models.VisitType;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class EncounterSync {

    private final RestApi apiService = RestServiceBuilder.createService(RestApi.class);
    private RestApi restApi;
    private Context context;
    public EncounterSync() {
        //this.context = ctx;
        restApi = RestServiceBuilder.createService(RestApi.class);
    }


    public Visit addEncounter(final Encountercreate encountercreate, String encounterDate, @NonNull LogResponse logResponse) {

        if (NetworkUtils.isOnline()) {
            Visit visit = new VisitDAO().getActiveVisitByPatientId(encountercreate.getPatientId()).toBlocking().single();
            if (visit != null) {
                encountercreate.setVisit(visit.getUuid());
                syncEncounter(encountercreate, logResponse);
                return visit;
            } else {
                return startNewVisitForEncounter(encountercreate, encounterDate, logResponse);
            }
        } else {
            ToastUtil.warning("No internet connection. Form data is saved locally " +
                    "and will sync when internet connection is restored. ");
            logResponse.appendLogs("Network off", "", "Add Encounter");
        }
        return null;
    }


    private Visit startNewVisitForEncounter(final Encountercreate encountercreate, String encounterDate, @NonNull LogResponse logResponse) {
        VisitRepository repository = new VisitRepository();
        Long id = repository.startVisit(new PatientDAO().findPatientByUUID(encountercreate.getPatient()),
                encounterDate, logResponse);
        if (id != null) {
            Visit visit = new VisitDAO().getVisitByID(id).toBlocking().single();
            encountercreate.setVisit(visit.getUuid());
            syncEncounter(encountercreate, logResponse);
            return visit;
        } else {
            ToastUtil.error("Error");
        }

        return null;
    }


    public void syncEncounter(final Encountercreate encountercreate, @NonNull LogResponse logResponse) {
        if (NetworkUtils.isOnline()) {
            try {
                encountercreate.pullObslist();
                encountercreate.setFormUuid(getFormResourceByName(encountercreate.getFormname()).getUuid());
                Call<Encounter> call = apiService.createEncounter(encountercreate);
                Response<Encounter> res = call.execute();
                if (res.isSuccessful()) {
                    Util.log("syncEncounter okay" + Thread.currentThread().getName());
                    Encounter encounter = res.body();
                    linkvisit(encountercreate.getPatientId(), encountercreate.getFormname(), encounter, encountercreate);
                    encountercreate.setSynced(true);
                    encountercreate.save();
                    logResponse.setSuccess(true);
                    new VisitRepository().syncLastVitals(encountercreate.getPatient(), logResponse);

                } else {
                    Util.log("syncEncounter fai");
                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "", "sync encounter");

                }
            } catch (Exception e) {
                Util.log("syncEncounter 04 " + e.getMessage() + e.toString());
                logResponse.appendLogs(e.getMessage(), "", "sync encounter");

            }

        } else {
            ToastUtil.error("Sync is off. Turn on sync to save form data.");
            logResponse.appendLogs("Offline", "Check your connection", "sync encounter");
        }

    }


    private void linkvisit(Long patientid, String formname, Encounter encounter,
                           Encountercreate encountercreate) {
        VisitDAO visitDAO = new VisitDAO();
        String uuid = "";

        try {
            if (encounter != null)
                uuid = encounter.getVisit().getUuid();
        } catch (Exception e) {
            if (encountercreate != null)
                uuid = encountercreate.getVisit();
        }
        Visit visit = visitDAO.getVisitByUuid(uuid).toBlocking().single();
        encounter.setEncounterType(new EncounterType(formname));
        for (int i = 0; i < encountercreate.getObservations().size(); i++) {
            encounter.getObservations().get(i).setDisplayValue
                    (encountercreate.getObservations().get(i).getValue());
        }
        if (visit != null) {
            List<Encounter> encounterList = visit.getEncounters();
            encounterList.add(encounter);
            visitDAO.saveOrUpdate(visit, patientid).toBlocking();
            ToastUtil.success(formname + " data saved successfully");
        }
    }

    public @NonNull LogResponse startSync(@NonNull Patient patient, String identity) {
        LogResponse logResponse = new LogResponse(identity);
        if (NetworkUtils.isOnline()) {
            List<Encountercreate> encountercreatelist = new Select()
                    .from(Encountercreate.class)
                    .where("patientid = ?", patient.getId())
                    .where("synced = ?", false) // case duplicate forms handle
                    .execute();

            Visit mVisit = null;
            if (encountercreatelist.size() < 1) {
                //Toast.makeText(null, "All Encounters synced", Toast.LENGTH_LONG);
                logResponse.appendLogs(true, "Already sync or not entered", "Check web", "Sync Encounter ");
                return logResponse;
            }
            for (final Encountercreate encountercreate : encountercreatelist) {
                try {
                    if (!encountercreate.getSynced() && patient.isSynced()) {
                        //get the encounter from the server. We are doing this to check
                        restApi.getEncounter(patient.getUuid(), encountercreate.getFormUuid(), encountercreate.getEncounterDatetime(), encountercreate.getEncounterDatetime(), "full");


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
                            addProgram(restApi, programEnrollment, logResponse);
                        }
                        if (encountercreate.getFormname().equals("HIV Enrollment")) {

                            ProgramEnrollment programEnrollment = new ProgramEnrollment();
                            programEnrollment.setPatient(encountercreate.getPatient());
                            programEnrollment.setProgram("9083deaa-f37f-44b3-9046-b87b134711a1");
                            programEnrollment.setDateEnrolled(encountercreate.getEncounterDatetime());
                            addProgram(restApi, programEnrollment, logResponse);
                        }
                        if (encountercreate.getFormname().equals("General Antenatal Care")) {
                            ProgramEnrollment programEnrollment = new ProgramEnrollment();
                            programEnrollment.setPatient(encountercreate.getPatient());
                            programEnrollment.setProgram("c3ae2d33-97d3-4cc4-9206-8a8160593648");
                            programEnrollment.setDateEnrolled(encountercreate.getEncounterDatetime());

                            addProgram(restApi, programEnrollment, logResponse);
                        }


                        if (null != encountercreate.getVisit()) {
                            Visit visit = new VisitDAO().getActiveVisitByUUID(encountercreate.getVisit())
                                    .toBlocking().single();
                            if (visit != null) {
                                Util.log(" visit id " + visit.getId());
                                mVisit = visit;
                                new VisitRepository().reOpenVisitByUUID(new VisitDAO().getVisitByIDLocally(visit.getId()), logResponse);
                                encountercreate.setVisit(visit.getUuid());
                                visit.setStopDatetime(null);
                                new VisitDAO().updateVisitLocally(visit, visit.getId(), visit.getPatient().getId());
                                syncEncounter(encountercreate, logResponse);

                            } else {
                                Visit v = startNewVisitForEncounter(encountercreate, encountercreate.getEncounterDatetime(), logResponse);
                                if (v != null) {
                                    mVisit = v;
                                }
                            }

                        } else {
                            Visit v = startNewVisitForEncounter(encountercreate, encountercreate.getEncounterDatetime(), logResponse);
                            if (v != null) {
                                mVisit = v;
                            }
                        }
                    }
                } catch (Exception e) {
                    ToastUtil.error(e.toString());
                    logResponse.appendLogs(e.getMessage(), "", "Sync Encounter ");
                }
            }

            if (mVisit != null) {
                endVisit(mVisit, logResponse);
            }
        } else {
            ToastUtil.warning("No internet connection. Form data is saved locally " +
                    "and will sync when internet connection is restored. ");
        }


        return logResponse;

    }


    private void addProgram(RestApi restApi, ProgramEnrollment programEnrollment, LogResponse logResponse) {
        if (NetworkUtils.isOnline()) {
            try {
                Response<ProgramEnrollment> res = restApi.addProgram(programEnrollment).execute();
                if (res.isSuccessful()) {
                    ToastUtil.success(OpenMRS.getInstance().getString(R.string.add_program_success_msg));
                    OpenMRS.getInstance().getOpenMRSLogger().e("Adding Program Successful " + res.raw());
                    logResponse.appendLogs("Program added", "", "Sync Encounter addProgram");
                } else {
                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "Contact HI", "Sync Encounter addProgram");

                }
            } catch (Exception e) {
                logResponse.appendLogs(e.getMessage(), "", "Sync Encounter addProgram");
                ToastUtil.error(OpenMRS.getInstance().getString(R.string.add_program_no_network_msg));
                OpenMRS.getInstance().getOpenMRSLogger().e("Failed to add provider. Device Offline");

            }
        } else {
            logResponse.appendLogs("Offline", "Please let syn complete before going offline", "Sync Encounter addProgram");
        }
    }

    public void endVisit(Visit visit, LogResponse logResponse) {
        new VisitRepository().endVisitByUUID(visit, logResponse);
    }


    class VisitRepository {

        private RestApi restApi;
        private RestApi restApiSpecial;
        private VisitDAO visitDAO;
        private LocationDAO locationDAO;
        private EncounterDAO encounterDAO;

        public VisitRepository() {
            restApi = RestServiceBuilder.createService(RestApi.class);
            visitDAO = new VisitDAO();
            locationDAO = new LocationDAO();
            encounterDAO = new EncounterDAO();
            restApiSpecial = RestServiceBuilderSpecial.createService(RestApi.class);
        }

        public VisitRepository(RestApi restApi, VisitDAO visitDAO, LocationDAO locationDAO, EncounterDAO encounterDAO) {
            this.restApi = restApi;
            this.visitDAO = visitDAO;
            this.locationDAO = locationDAO;
            this.encounterDAO = encounterDAO;
        }

        /*
                public void syncVisitsData(@NonNull Patient patient, SyncResponse syncResponse) {
                    syncVisitsData(patient, null, syncResponse);
                }

                public void syncVisitsData(@NonNull final Patient patient,
                                           @Nullable final DefaultResponseCallbackListener callbackListener,
                                           @NonNull SyncResponse syncResponse) {
                    Call<Results<Visit>> call = restApi.findVisitsByPatientUUID(patient.getUuid(), "custom:(uuid,location:ref,visitType:ref,startDatetime,stopDatetime:full)");
                    try {
                        Response<Results<Visit>> res = call.execute();
                        if (res.isSuccessful()) {
                            List<Visit> visits = res.body().getResults();
                            Observable.just(visits)
                                    .flatMap(Observable::from)
                                    .forEach(visit ->
                                                    visitDAO.saveOrUpdate(visit, patient.getId())
                                                            .observeOn(Schedulers.trampoline())
                                                            .subscribe(),
                                            error -> error.printStackTrace()
                                    );
                            if (callbackListener != null) {
                                callbackListener.onResponse();
                            }
                        } else {
                            if (callbackListener != null) {
                                callbackListener.onErrorResponse(res.message());
                            }
                            String err = "ErrorBody:" + res.errorBody().string() +
                                    "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                            syncResponse.appendLogs(err, "Contact HI", "Sync  startVisit");

                        }

                    } catch (Exception e) {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(e.getMessage());
                        }
                        syncResponse.appendLogs(e.getMessage(), " ", "sync  startVisit");
                    }

                }

                public void getVisitType(final GetVisitTypeCallbackListener callbackListener) {
                    Call<Results<VisitType>> call = restApi.getVisitType();
                    call.enqueue(new Callback<Results<VisitType>>() {

                        @Override
                        public void onResponse(@NonNull Call<Results<VisitType>> call, @NonNull Response<Results<VisitType>> response) {
                            if (response.isSuccessful()) {
                                callbackListener.onGetVisitTypeResponse(response.body().getResults().get(0));
                            } else {
                                callbackListener.onErrorResponse(response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Results<VisitType>> call, @NonNull Throwable t) {
                            callbackListener.onErrorResponse(t.getMessage());
                        }

                    });
                }

        */
        public void syncLastVitals(final String patientUuid, LogResponse logResponse) {
            Call<Results<Encounter>> call = restApi.getLastVitals(patientUuid, ApplicationConstants.EncounterTypes.VITALS, "full", 1, "desc");
            try {
                Response<Results<Encounter>> res = call.execute();
                if (res.isSuccessful()) {
                    if (!res.body().getResults().isEmpty()) {
                        encounterDAO.saveLastVitalsEncounter(res.body().getResults().get(0),
                                patientUuid);
                        logResponse.setSuccess(true);
                    }
                } else {
                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "Contact HI", "syncLastVitals getIdGenPatientIdentifier");

                }
            } catch (Exception e) {
                logResponse.appendLogs(e.getMessage(), "", "syncLastVitals getIdGenPatientIdentifier");

            }

        }


        public Long startVisit(final Patient patient, String encounterDate, @NonNull LogResponse logResponse) {
            final Visit visit = new Visit();
//        visit.setStartDatetime(DateUtils.convertTime(System.currentTimeMillis(), DateUtils.OPEN_MRS_REQUEST_FORMAT));
            visit.setStartDatetime(encounterDate);
            visit.setPatient(patient);
            visit.setLocation(locationDAO.findLocationByName(OpenMRS.getInstance().getLocation()));
            visit.setVisitType(new VisitType(null, ApplicationConstants.DEFAULT_VISIT_TYPE_UUID));

            Call<Visit> call = restApi.startVisit(visit);
            try {
                Response<Visit> res = call.execute();
                if (res.isSuccessful()) {
                    Visit newVisit = res.body();
                    return visitDAO.saveOrUpdate(newVisit, patient.getId()).toBlocking().single();

                } else {
                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "Contact HI", "Sync  startVisit");

                }

            } catch (Exception e) {

                logResponse.appendLogs(e.getMessage(), " ", "sync  startVisit");
            }

            return null;
        }


        public void endVisitByUUID(final Visit visit, LogResponse logResponse) {
            visit.setStopDatetime(DateUtils.convertTime(System.currentTimeMillis(), DateUtils.OPEN_MRS_REQUEST_FORMAT));
            new VisitDAO().updateVisitLocally(visit, visit.getId(), visit.getPatient().getId());
            Visit newVisit = new Visit();
           newVisit.setStopDatetime(DateUtils.convertTime(System.currentTimeMillis(), DateUtils.OPEN_MRS_REQUEST_FORMAT));
         if(true ) return;
            Call<Visit> call = restApi.endVisitByUUID(visit.getUuid(), newVisit);
            try {
                Response<Visit> res = call.execute();
                if (res.isSuccessful()) {
                    Visit vis = visitDAO.getVisitByID(visit.getId()).toBlocking().single();
                    vis.setStopDatetime(res.body().getStopDatetime());
                    visitDAO.saveOrUpdate(vis, vis.getPatient().getId()).toBlocking();

                } else {

                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "Contact HI", "sync  endVisitByUUID");

                }
            } catch (Exception e) {

                logResponse.appendLogs(e.getMessage(), "", "sync endVisitByUUID");

            }


        }


        public void reOpenVisitByUUID(final Visit visit, LogResponse logResponse) {
            Visit test = new Visit();
            test.setStopDatetime(null);
            test.setStartDatetime(visit.getStartDatetime());
            Call<Visit> call = restApiSpecial.endVisitByUUID(visit.getUuid(), test);
            try {
                Response<Visit> res = call.execute();
                if (res.isSuccessful()) {
                    Visit vis = visitDAO.getVisitByID(visit.getId()).toBlocking().single();
                    vis.setStopDatetime(res.body().getStopDatetime());
                    visitDAO.saveOrUpdate(vis, vis.getPatient().getId()).toBlocking().single();
                } else {
                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "", "sync  reOpenVisitByUUID");

                }
            } catch (Exception e) {
                logResponse.appendLogs(e.getMessage(), "", "reOpenVisitByUUID");

            }


        }
/*
        public void syncVisit(final Patient patient, Visit visit, Encountercreate encountercreate) {
            syncVisit(patient, visit, encountercreate, null);
        }

        public void syncVisit(final Patient patient, Visit visit, Encountercreate encountercreate, @Nullable final DefaultResponseCallbackListener callbackListener) {
            visit.setLocation(locationDAO.findLocationByName(OpenMRS.getInstance().getLocation()));
            visit.setVisitType(new VisitType(null, OpenMRS.getInstance().getVisitTypeUUID()));
            Call<Visit> call = restApi.startVisit(visit);
            call.enqueue(new Callback<Visit>() {
                @Override
                public void onResponse(@NonNull Call<Visit> call, @NonNull Response<Visit> response) {
                    if (response.isSuccessful()) {
                        Visit newVisit = response.body();
                        String newUUID = newVisit.getUuid();
                        encountercreate.setVisit(newUUID);
                        new Update(Encountercreate.class).set("visit = ?", newUUID).where("visit = ?", visit.getUuid()).execute();
                        new EncounterService().syncEncounter(encountercreate);
                        new org.openmrs.mobile.api.repository.VisitRepository().endVisitByUUID(new VisitDAO().getVisitByIDLocally(visit.getId()));
                        visit.setUuid(newUUID);
                        visitDAO.saveOrUpdate(visit, patient.getId())
                                .observeOn(Schedulers.trampoline())
                                .subscribe(id -> {
                                    if (callbackListener != null) {
                                        callbackListener.onResponse();
                                    }
                                });
                    } else {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(response.message());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Visit> call, @NonNull Throwable t) {
                    if (callbackListener != null) {
                        callbackListener.onErrorResponse(t.getMessage());
                    }
                }
            });
        }

        public void startVisitLocally(final Patient patient, String startDate) {
            final Visit visit = new Visit();
            final String uuid = UUID.randomUUID().toString().replace("-", "");
            visit.setStartDatetime(DateUtils.convertTime(DateUtils.convertTime(startDate), DateUtils.OPEN_MRS_REQUEST_FORMAT));
            visit.setPatient(patient);
            String facility = OpenMRS.getInstance().getLocation();
            visit.setLocation(locationDAO.findLocationByName(facility));
            visit.setUuid(uuid);
            String display = "Facility Visit @ " + facility + " - " + DateUtils.convertTime(DateUtils.convertTime(startDate), DateUtils.DATE_WITH_TIME_FORMAT);
            visit.setDisplay(display);
            visit.setVisitType(new VisitType(null, OpenMRS.getInstance().getVisitTypeUUID()));
            visitDAO.saveVisitLocally(visit, patient.getId());
        }
*/
    }


}