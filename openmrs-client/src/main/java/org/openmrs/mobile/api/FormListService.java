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

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;

import org.openmrs.mobile.api.response.PbsServerContract;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.FormResource;
import org.openmrs.mobile.models.Module;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormListService extends IntentService {
    private final RestApi apiService = RestServiceBuilder.createService(RestApi.class);
    private List<FormResource> formresourcelist;

    public FormListService() {
        super("Sync Form List");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Util.log("Form service started");
        if(NetworkUtils.isOnline()) {
            Util.log("Form service requesting");
            Call<Results<FormResource>> call = apiService.getForms();
            call.enqueue(new Callback<Results<FormResource>>() {

                @Override
                public void onResponse(@NonNull Call<Results<FormResource>> call, @NonNull Response<Results<FormResource>> response) {
                    if (response.isSuccessful()) {
                        new Delete().from(FormResource.class).execute();
                        formresourcelist=response.body().getResults();

                        int size=formresourcelist.size();
                        ActiveAndroid.beginTransaction();
                        try {
                            for (int i = 0; i < size; i++)
                            {
                                formresourcelist.get(i).setResourcelist();
                                formresourcelist.get(i).save();
                            }
                            ActiveAndroid.setTransactionSuccessful();

                        }catch (Exception e){
                            Util.log("Save forms error "+e.getMessage());
                        } finally {
                            ActiveAndroid.endTransaction();
                        }

                    }

                }

                @Override
                public void onFailure(@NonNull Call<Results<FormResource>> call, @NonNull Throwable t) {
                    ToastUtil.error(t.getMessage());
                }
            });

            Call<Results<EncounterType>> call2 = apiService.getEncounterTypes();
            call2.enqueue(new Callback<Results<EncounterType>>() {
                @Override
                public void onResponse(@NonNull Call<Results<EncounterType>> call, @NonNull Response<Results<EncounterType>> response) {
                    if (response.isSuccessful()) {
                        new Delete().from(EncounterType.class).execute();
                        Results<EncounterType> encountertypelist = response.body();
                            for (EncounterType enctype : encountertypelist.getResults())
                                enctype.save();

                        Util.log("FORM_LIST_SERVICE:  Forms type loaded");

                    }

                }

                @Override
                public void onFailure(@NonNull Call<Results<EncounterType>> call, @NonNull Throwable t) {
                    ToastUtil.error(t.getMessage());

                }
            });


/*
Save the current modules
 */
            Call<Results<Module>>  callModule = apiService.getModules(ApplicationConstants.API.FULL);
            callModule.enqueue(new Callback<Results<Module>>() {
                @Override
                public void onResponse(@NonNull Call<Results<Module>> call, @NonNull Response<Results<Module>> response) {
                    if (response.isSuccessful()) {
                        for (Module module:response.body().getResults())
                            if(ApplicationConstants.UserKeys.METADATA_VERSION_UUID.equals(module.getUuid())) {
                                OpenMRS.getInstance().setMetadataVersion(module.getVersion()+"/"+module.getStarted());
                                break;
                            }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Results<Module>> call, @NonNull Throwable t) {

                }
            });

/*
Save the pbs
 */
            String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");
            String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/server";
            Call<PbsServerContract>  callPBSserver = apiService.checkServerStatus(url);
            callPBSserver.enqueue(new Callback<PbsServerContract>() {
                @Override
                public void onResponse(@NonNull Call<PbsServerContract> call, @NonNull Response<PbsServerContract> response) {
                    if (response.isSuccessful()) {
                                OpenMRS.getInstance().setPBSserverVersion(response.body().getAppVersion()+"/"+ DateUtils.getCurrentDateTime());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PbsServerContract> call, @NonNull Throwable t) {

                }
            });




        }



    }

}