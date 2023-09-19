/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package pbsprocessor.api.retrofit;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import io.swagger.v3.oas.models.responses.ApiResponse;
import okhttp3.ResponseBody;
import org.json.simple.JSONObject;
import pbsprocessor.listerner.PatientBiometricSyncResponseModel;
import pbsprocessor.listerner.PbsServerContract;
import pbsprocessor.model.PatientBiometricContract;
import pbsprocessor.model.PatientBiometricDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface RestApi {


    @GET()
    Call<PbsServerContract> checkServerStatus(@Url String url);
    @POST()
    Call<PatientBiometricSyncResponseModel> syncPBS(@Url String url, @Body PatientBiometricDTO pbsDTO);

// add post for verification syncing

    @GET()
    Call<List<PatientBiometricContract>> checkForExistingPBS(@Url String url, @Query("PatientUUID") String patientUUID);
    @GET("visit")
    Call< JSONObject> findVisitsByPatientUUID(@Query("patient") String patientUUID,


                                              @Query("v") String representation);
    @GET("encounter")
    Call<JSONObject> getLastVitals(@Query("patient") String patientUUID,

                                           @Query("encounterType") String encounterType,
                                           @Query("v") String representation,
                                           @Query("limit") int limit,
                                           @Query("order") String order);

    @GET("patient/{uuid}")
    Call<JsonElement> getPatientByUUID(@Path("uuid") String uuid,
                                       @Query("v") String representation);
}
