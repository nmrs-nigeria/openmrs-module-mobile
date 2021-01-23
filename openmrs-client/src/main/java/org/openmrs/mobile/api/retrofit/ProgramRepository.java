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

package org.openmrs.mobile.api.retrofit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;
import org.openmrs.mobile.R;
import org.openmrs.mobile.api.CustomApiCallback;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.models.ProgramEnrollment;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgramRepository {

    public void addProgram(RestApi restApi, ProgramEnrollment programEnrollment, CustomApiCallback callback) {
        if (NetworkUtils.isOnline()) {
            restApi.addProgram(programEnrollment).enqueue(new Callback<ProgramEnrollment>() {
                @Override
                public void onResponse(@NotNull Call<ProgramEnrollment> call, @NotNull Response<ProgramEnrollment> response) {
                    if (response.isSuccessful()) {
                        ToastUtil.success(OpenMRS.getInstance().getString(R.string.add_program_success_msg));
                        OpenMRS.getInstance().getOpenMRSLogger().e("Adding Program Successful " + response.raw());
                        callback.onSuccess();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ProgramEnrollment> call, @NotNull Throwable t) {
//                    ToastUtil.error(OpenMRS.getInstance().getString(R.string.add_program_failure_msg));
//                    OpenMRS.getInstance().getOpenMRSLogger().e("Failed to add program. Error:  " + t.getMessage());
                    callback.onFailure();
                }
            });
        } else {
            ToastUtil.error(OpenMRS.getInstance().getString(R.string.add_program_no_network_msg));
            OpenMRS.getInstance().getOpenMRSLogger().e("Failed to add provider. Device Offline");
            callback.onFailure();
        }
    }


}
