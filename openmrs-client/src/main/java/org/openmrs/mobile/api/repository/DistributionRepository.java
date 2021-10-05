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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilderCommodity;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.utilities.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DistributionRepository extends RetrofitRepository {

    private OpenMRSLogger logger;
    private RestApi restApi;

    public DistributionRepository(){
        this.logger = new OpenMRSLogger();
        this.restApi = RestServiceBuilderCommodity.createService(RestApi.class);
    }





    public void syncDistribution(final Distribution distribution, @Nullable final DefaultResponseCallbackListener callbackListener) {
        if (NetworkUtils.isOnline()) {
            Call<Distribution> call = restApi.startDistribution(distribution);
            call.enqueue(new Callback<Distribution>() {
                @Override
                public void onResponse(@NonNull Call<Distribution> call, @NonNull Response<Distribution> response) {
                    if (response.isSuccessful()) {
                        Distribution newDistribution = response.body();
//                        distribution.setSynced(true);
                        distribution.save();
                    } else {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(response.message());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Distribution> call, @NonNull Throwable t) {
                    if (callbackListener != null) {
//                        callbackListener.onErrorResponse(t.getMessage());
                        callbackListener.onResponse();

                    }
                }
            });
        }else{
            distribution.save();
            if (callbackListener != null) {
                callbackListener.onResponse();
            }
        }
    }



}
