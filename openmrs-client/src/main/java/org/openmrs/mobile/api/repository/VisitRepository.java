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

package org.openmrs.mobile.api.repository;

import org.openmrs.mobile.api.EncounterService;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.RestServiceBuilderSpecial;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.LocationDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.listeners.retrofit.GetVisitTypeCallbackListener;
import org.openmrs.mobile.listeners.retrofit.StartVisitResponseListenerCallback;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.models.VisitType;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.activeandroid.query.Update;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class VisitRepository {

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

    public void syncVisitsData(@NonNull Patient patient) {
        syncVisitsData(patient, null);
    }

    public void syncVisitsData(@NonNull final Patient patient, @Nullable final DefaultResponseCallbackListener callbackListener) {
        Call<Results<Visit>> call = restApi.findVisitsByPatientUUID(patient.getUuid(), "custom:(uuid,location:ref,visitType:ref,startDatetime,stopDatetime:full)");
        call.enqueue(new Callback<Results<Visit>>() {
            @Override
            public void onResponse(@NonNull Call<Results<Visit>> call, @NonNull Response<Results<Visit>> response) {
                if (response.isSuccessful()) {
                    List<Visit> visits = response.body().getResults();
                    Observable.just(visits)
                            .flatMap(Observable::from)
                            .forEach(visit ->
                                    visitDAO.saveOrUpdate(visit, patient.getId())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(),
                                    error -> error.printStackTrace()
                            );
                    if (callbackListener != null) {
                        callbackListener.onResponse();
                    }
                }
                else {
                    if (callbackListener != null) {
                        callbackListener.onErrorResponse(response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Results<Visit>> call, @NonNull Throwable t) {
                if (callbackListener != null) {
                    callbackListener.onErrorResponse(t.getMessage());
                }
            }

        });
    }

    public void getVisitType(final GetVisitTypeCallbackListener callbackListener) {
        Call<Results<VisitType>> call = restApi.getVisitType();
        call.enqueue(new Callback<Results<VisitType>>() {

            @Override
            public void onResponse(@NonNull Call<Results<VisitType>> call, @NonNull Response<Results<VisitType>> response) {
                if (response.isSuccessful()) {
                    callbackListener.onGetVisitTypeResponse(response.body().getResults().get(0));
                }
                else {
                    callbackListener.onErrorResponse(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Results<VisitType>> call, @NonNull Throwable t) {
                callbackListener.onErrorResponse(t.getMessage());
            }

        });
    }

    public void syncLastVitals(final String patientUuid) {
        syncLastVitals(patientUuid, null);
    }

    public void syncLastVitals(final String patientUuid, @Nullable final DefaultResponseCallbackListener callbackListener) {
        Call<Results<Encounter>> call = restApi.getLastVitals(patientUuid, ApplicationConstants.EncounterTypes.VITALS, "full", 1,"desc");
        call.enqueue(new Callback<Results<Encounter>>() {
            @Override
            public void onResponse(@NonNull Call<Results<Encounter>> call, @NonNull Response<Results<Encounter>> response) {
                if (response.isSuccessful()) {
                    if (!response.body().getResults().isEmpty()) {
                        encounterDAO.saveLastVitalsEncounter(response.body().getResults().get(0), patientUuid);
                    }
                    if (callbackListener != null) {
                        callbackListener.onResponse();
                    }
                }
                else {
                    if (callbackListener != null) {
                        callbackListener.onErrorResponse(response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Results<Encounter>> call, @NonNull Throwable t) {
                if (callbackListener != null) {
                    callbackListener.onErrorResponse(t.getMessage());
                }
            }
        });
    }


    public void startVisit(final Patient patient, String encounterDate) {
        startVisit(patient,encounterDate, null);
    }

    public void startVisit(final Patient patient,String encounterDate, @Nullable final StartVisitResponseListenerCallback callbackListener) {
        final Visit visit = new Visit();
//        visit.setStartDatetime(DateUtils.convertTime(System.currentTimeMillis(), DateUtils.OPEN_MRS_REQUEST_FORMAT));
        visit.setStartDatetime(encounterDate);
        visit.setPatient(patient);
        visit.setLocation(locationDAO.findLocationByName(OpenMRS.getInstance().getLocation()));
        visit.setVisitType(new VisitType(null, ApplicationConstants.DEFAULT_VISIT_TYPE_UUID));

        Call<Visit> call = restApi.startVisit(visit);
        call.enqueue(new Callback<Visit>() {
            @Override
            public void onResponse(@NonNull Call<Visit> call, @NonNull Response<Visit> response) {
                if (response.isSuccessful()) {
                    Visit newVisit = response.body();
                    visitDAO.saveOrUpdate(newVisit, patient.getId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(id -> {
                                if(callbackListener != null) {
                                    callbackListener.onStartVisitResponse(id);
                                }
                            });
                }
                else {
                    if(callbackListener != null) {
                        callbackListener.onErrorResponse(response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Visit> call, @NonNull Throwable t) {
                if(callbackListener != null) {
                    callbackListener.onErrorResponse(t.getMessage());
                }
            }
        });
    }


    public void endVisitByUUID(final Visit visit) {
        endVisitByUUID(visit, null);
    }

    public void endVisitByUUID(final Visit visit, @Nullable final StartVisitResponseListenerCallback callbackListener) {
        visit.setStopDatetime(DateUtils.convertTime(System.currentTimeMillis(), DateUtils.OPEN_MRS_REQUEST_FORMAT));
        new VisitDAO().updateVisitLocally(visit, visit.getId(), visit.getPatient().getId());
        Visit test = new Visit();
        test.setStopDatetime(DateUtils.convertTime(System.currentTimeMillis(), DateUtils.OPEN_MRS_REQUEST_FORMAT));

        Call<Visit> call = restApi.endVisitByUUID(visit.getUuid(), test );

        call.enqueue(new Callback<Visit>() {
            @Override
            public void onResponse(@NonNull Call<Visit> call, @NonNull Response<Visit> response) {
                if (response.isSuccessful()) {
                    visitDAO.getVisitByID(visit.getId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(vis -> {
                                vis.setStopDatetime(response.body().getStopDatetime());
                                visitDAO.saveOrUpdate(vis, vis.getPatient().getId())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe();
                            });
                }
                else {
                    if(callbackListener != null) {
                        callbackListener.onErrorResponse(response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Visit> call, @NonNull Throwable t) {
                if(callbackListener != null) {
                    callbackListener.onErrorResponse(t.getMessage());
                }
            }
        });
    }

    public void reOpenVisitByUUID(final Visit visit) {
        reOpenVisitByUUID(visit, null);
    }

    public void reOpenVisitByUUID(final Visit visit, @Nullable final StartVisitResponseListenerCallback callbackListener) {
        Visit test = new Visit();
        test.setStopDatetime(null);
        test.setStartDatetime(visit.getStartDatetime());
        Call<Visit> call = restApiSpecial.endVisitByUUID(visit.getUuid(), test );

        call.enqueue(new Callback<Visit>() {
            @Override
            public void onResponse(@NonNull Call<Visit> call, @NonNull Response<Visit> response) {
                if (response.isSuccessful()) {
                    visitDAO.getVisitByID(visit.getId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(vis -> {
                                vis.setStopDatetime(response.body().getStopDatetime());
                                visitDAO.saveOrUpdate(vis, vis.getPatient().getId())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe();
                            });
                }
                else {
                    if(callbackListener != null) {
                        callbackListener.onErrorResponse(response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Visit> call, @NonNull Throwable t) {
                if(callbackListener != null) {
                    callbackListener.onErrorResponse(t.getMessage());
                }
            }
        });
    }

    public void syncVisit(final Patient patient, Visit visit, Encountercreate encountercreate) {
        syncVisit(patient, visit,encountercreate, null);
    }

    public void syncVisit(final Patient patient, Visit visit, Encountercreate encountercreate,@Nullable final DefaultResponseCallbackListener callbackListener) {
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
                    new Update(Encountercreate.class).set("visit = ?",newUUID).where("visit = ?",visit.getUuid()).execute();
                    new EncounterService().syncEncounter(encountercreate);
                    new VisitRepository().endVisitByUUID(new VisitDAO().getVisitByIDLocally(visit.getId()));
                    visit.setUuid(newUUID);
                    visitDAO.saveOrUpdate(visit, patient.getId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(id -> {
                                if (callbackListener != null) {
                                    callbackListener.onResponse();
                                }
                            });
                }
                else {
                    if(callbackListener != null) {
                        callbackListener.onErrorResponse(response.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Visit> call, @NonNull Throwable t) {
                if(callbackListener != null) {
                    callbackListener.onErrorResponse(t.getMessage());
                }
            }
        });
    }

    public void startVisitLocally(final Patient patient,String startDate) {
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

}
