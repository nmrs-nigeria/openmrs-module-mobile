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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilderCommodity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.utilities.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferRepository extends RetrofitRepository {

    private OpenMRSLogger logger;
    private RestApi restApi;

    public TransferRepository(){
        this.logger = new OpenMRSLogger();
        this.restApi = RestServiceBuilderCommodity.createService(RestApi.class);
    }





    public void syncTransfer(final Transfer transfer, @Nullable final DefaultResponseCallbackListener callbackListener) {
        if (NetworkUtils.isOnline()) {
            Call<Transfer> call = restApi.startTransfer(transfer);
            call.enqueue(new Callback<Transfer>() {
                @Override
                public void onResponse(@NonNull Call<Transfer> call, @NonNull Response<Transfer> response) {
                    if (response.isSuccessful()) {
                        Transfer newTransfer = response.body();
                        transfer.save();
                    } else {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(response.message());
                        }
                        OpenMRSCustomHandler.writeLogToFile("Failed Transfer: " + response.code() + " / " + response.message() + " / " + response.body() + " / " + response.errorBody());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Transfer> call, @NonNull Throwable t) {
                    if (callbackListener != null) {
//                        callbackListener.onErrorResponse(t.getMessage());
                        callbackListener.onResponse();
                        OpenMRSCustomHandler.writeLogToFile("Transfer Repository Failure: " + t.getMessage());
                    }
                }
            });
        }else{
            transfer.save();
            if (callbackListener != null) {
                callbackListener.onResponse();
            }
        }
    }



}
