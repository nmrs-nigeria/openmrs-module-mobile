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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.activeandroid.query.Select;

import org.jdeferred.android.AndroidDeferredManager;
import org.openmrs.mobile.api.EncounterService;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.RestServiceBuilderCommodity;
import org.openmrs.mobile.api.promise.SimpleDeferredObject;
import org.openmrs.mobile.api.promise.SimplePromise;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.listeners.retrofit.DownloadPatientCallbackListener;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.IdGenPatientIdentifiers;
import org.openmrs.mobile.models.IdentifierType;
import org.openmrs.mobile.models.Location;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientDto;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.PatientPhoto;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.models.VisitType;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;

public class ConsumptionRepository extends RetrofitRepository {

    private OpenMRSLogger logger;
    private RestApi restApi;

    public ConsumptionRepository(){
        this.logger = new OpenMRSLogger();
        this.restApi = RestServiceBuilderCommodity.createService(RestApi.class);
    }





    public void syncConsumption(final Consumption consumption, @Nullable final DefaultResponseCallbackListener callbackListener) {
        if (NetworkUtils.isOnline()) {
            Call<Consumption> call = restApi.startConsumption(consumption);
            call.enqueue(new Callback<Consumption>() {
                @Override
                public void onResponse(@NonNull Call<Consumption> call, @NonNull Response<Consumption> response) {
                    if (response.isSuccessful()) {
                        Consumption newConsumption = response.body();
                        consumption.setSynced(true);
                        consumption.save();
                    } else {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(response.message());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Consumption> call, @NonNull Throwable t) {
                    if (callbackListener != null) {
//                        callbackListener.onErrorResponse(t.getMessage());
                        callbackListener.onResponse();

                    }
                }
            });
        }else{
            consumption.save();
            if (callbackListener != null) {
                callbackListener.onResponse();
            }
        }
    }



}
