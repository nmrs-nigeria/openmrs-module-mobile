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

import com.activeandroid.query.Update;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilderCommodity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;
import org.openmrs.mobile.utilities.NetworkUtils;

import java.lang.reflect.Modifier;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptRepository extends RetrofitRepository {

    private OpenMRSLogger logger;
    private RestApi restApi;

    public ReceiptRepository(){
        this.logger = new OpenMRSLogger();
        this.restApi = RestServiceBuilderCommodity.createService(RestApi.class);
    }





    public void syncReceipt(final Receipt receipt, @Nullable final DefaultResponseCallbackListener callbackListener) {
        if (NetworkUtils.isOnline()) {
            Call<Receipt> call = restApi.startReceipt(receipt);
            call.enqueue(new Callback<Receipt>() {
                @Override
                public void onResponse(@NonNull Call<Receipt> call, @NonNull Response<Receipt> response) {
                    try{
                        //Receipt newReceipt = response.body();
                        if (response.isSuccessful()) {
                            OpenMRSCustomHandler.writeLogToFile("Receipt Sync is successful");
                            receipt.setSynced(true);
                            long lastInsertReceipt = receipt.save();
                            new Update(ReceiptItem.class)
                                    .set("isSynced = 1")
                                    .where("receiptId = ?", lastInsertReceipt)
                                    .execute();
                        } else {
                            if (callbackListener != null) {
                                callbackListener.onErrorResponse(response.message());
                            }
                            OpenMRSCustomHandler.writeLogToFile("Failed Receipt: " + response.code() + " / " + response.message() + " / " + response.body() + " / " + response.errorBody());
                        }
                    }catch (Exception e){
                        OpenMRSCustomHandler.writeLogToFile(e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Receipt> call, @NonNull Throwable t) {
                    OpenMRSCustomHandler.writeLogToFile("The response code is = " + t.getMessage());
                    if (callbackListener != null) {
                       callbackListener.onErrorResponse(t.getMessage());
                       // callbackListener.onResponse();

                    }
                }
            });
        }else{
            if (callbackListener != null) {
                callbackListener.onResponse();
            }
        }
    }



}
