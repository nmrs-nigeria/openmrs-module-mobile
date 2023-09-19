package org.openmrs.mobile.api;


import androidx.annotation.NonNull;

import com.activeandroid.app.Application;
import com.google.gson.Gson;

import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.activities.pbs.PatientBiometricSyncResponseModel;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationDTO;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.listeners.retrofit.GenericResponseCallbackListener;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.security.HashMethods;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class FingerPrintVerificationSyncService extends Application {
    private static FingerPrintVerificationSyncService instance;
    private RestApi restApi;

    public FingerPrintVerificationSyncService() {
        this.restApi = RestServiceBuilder.createService(RestApi.class);
        // super("Sync Finger Prints");
    }

    public FingerPrintVerificationSyncService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

/*
    public void CheckServiceStatus(GenericResponseCallbackListener<String> responseCallbackListener) {
        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");

        //String url = baseUrl[0]+"://"+ baseUrl[1].replaceAll("//","") +":2018/api/Client/get";
        //"http://192.168.0.151:2018/api/Client/get"; baseUrl[1].replaceAll("//","");
        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/server";

        if (NetworkUtils.isOnline()) {

            Call<String> call = restApi.genericRequest(url);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                    responseCallbackListener.onResponse(response.body());
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    ToastUtil.notify("Error: " + t.getMessage());
                    OpenMRSCustomHandler.writeLogToFile("Error from check service status Message {" + t.getMessage() + "}");
                }
            });
        }
    }

    public void retrieveCaptureFromServer(String patientUUID, boolean saveTemplate) {

        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");

        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/CheckForPreviousCapture";
        //String url = "http://192.168.0.151:2018/api/FingerPrint/CheckForPreviousCapture"; //baseUrl[1].replaceAll("//","");

            Call<List<PatientBiometricContract>> call = restApi.checkForExistingPBS(url, patientUUID);
            call.enqueue(new Callback<List<PatientBiometricContract>>() {
                @Override
                public void onResponse(@NonNull Call<List<PatientBiometricContract>> call, @NonNull retrofit2.Response<List<PatientBiometricContract>> response) {
                    if (response.isSuccessful()) {

                        if (response.body() != null && response.body().size() >= 6) {
                           Patient patient =  new PatientDAO().findPatientByUUID(patientUUID);
                            for (PatientBiometricContract item : response.body()) {
                                if(!saveTemplate){
                                    item.setTemplate("");
                                }
                                String patientId = String.valueOf(patient.getId());
                                item.setPatienId(Integer.parseInt(patientId));
                                item.setSyncStatus(1); //set to already synced
                            }
                            new FingerPrintDAO().saveFingerPrint(response.body());
                        }
                    }
                }
                @Override
                public void onFailure(Call<List<PatientBiometricContract>> call, Throwable t) {
                    ToastUtil.notify("Error: " + t.getMessage());
                    OpenMRSCustomHandler.writeLogToFile("Error from retrieve capture from server: Message {" + t.getMessage() + "}");
                }
        });
    }




    //http://localhost:2018/api/FingerPrint/CheckForPreviousCapture?PatientUUID=17faf42b-543e-435a-ba1d-95888cf4ae2e
    public void CheckForPreviousCapture(String patientUUID, GenericResponseCallbackListener<List<PatientBiometricContract>> responseCallbackListener) {

        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");

        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/CheckForPreviousCapture";
        //String url = "http://192.168.0.151:2018/api/FingerPrint/CheckForPreviousCapture"; //baseUrl[1].replaceAll("//","");

        if (NetworkUtils.isOnline()) {

            Call<List<PatientBiometricContract>> call = restApi.checkForExistingPBS(url, patientUUID);
            call.enqueue(new Callback<List<PatientBiometricContract>>() {
                @Override
                public void onResponse(@NonNull Call<List<PatientBiometricContract>> call, @NonNull retrofit2.Response<List<PatientBiometricContract>> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null)
                            responseCallbackListener.onResponse(response.body());
                        //OpenMRSCustomHandler.writeLogToFile("Check Previous Capture Online Success: " + response.body());
                    } else {
                        responseCallbackListener.onErrorResponse("Some server errors occurred");
                        OpenMRSCustomHandler.writeLogToFile("Check Previous Capture Online Error: Some server errors occurred. Code:" + response.code() + " Message:" + response.raw() + " ::: " + response.errorBody());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<PatientBiometricContract>> call, @NonNull Throwable t) {
                    ToastUtil.notify("Error: " + t.getMessage());
                    OpenMRSCustomHandler.writeLogToFile("Error from check for previous capture: Message {" + t.getMessage() + "}");
                }
            });
        }
    }

*/
    public void startSync(PatientBiometricVerificationDTO PBSObj, GenericResponseCallbackListener<PatientBiometricSyncResponseModel> responseCallbackListener) {

        String json = new Gson().toJson(PBSObj);
        //System.out.print(json);
        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");
        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/ReSaveFingerprintVerificationToDatabase";

        if (NetworkUtils.isOnline()) {

            /// add hashing before syncing
            for(int bioIndex=0;bioIndex<PBSObj.getFingerPrintList().size();bioIndex++){
                PatientBiometricVerificationContract b= PBSObj.getFingerPrintList().get(bioIndex);
                b.setModel(ApplicationConstants.PBS_PASSWORD_VERSION);
                b.setManufacturer(HashMethods.getPBSHash(PBSObj.getPatientUUID(),
                        b.getDateCreated(),
                        b.getImageQuality(),
                        b.getSerialNumber(),
                        b.getFingerPositions().toString()
                ));
                PBSObj.getFingerPrintList().set(bioIndex,b );
            }

          //  Util.log("URL of the file "+url);
            Call<PatientBiometricSyncResponseModel> call = restApi.syncPBS(url, PBSObj);
            call.enqueue(new Callback<PatientBiometricSyncResponseModel>() {
                @Override
                public void onResponse(@NonNull Call<PatientBiometricSyncResponseModel> call, @NonNull retrofit2.Response<PatientBiometricSyncResponseModel> response) {
                    if (response.isSuccessful()) {
                       // OpenMRSCustomHandler.writeLogToFile("PBS Recapture synced successfully: " + response.message());
                        responseCallbackListener.onResponse(response.body());
                    } else {
                        try {
                            OpenMRSCustomHandler.writeLogToFile("PBS Recapture sync error: Error Code:" + response.code() + ", Error Message:" + response.message() + " Error Body:" + response.errorBody().string());
                            ToastUtil.notify("Error: " +  response.errorBody().string());
                        } catch (IOException e) {
                            OpenMRSCustomHandler.writeLogToFile("PBS Recapture sync error: Error Code:" + response.code() + ", Error Message:" + response.message() + " Error Body:" + response.errorBody());
                            ToastUtil.notify("Error: " + response.body());
                        }
                           responseCallbackListener.onErrorResponse(response.body());
                       // System.out.print("Error occurred");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PatientBiometricSyncResponseModel> call, @NonNull Throwable t) {
                    ToastUtil.notify("Error: " + t.getMessage());
                    responseCallbackListener.onErrorResponse(t.getMessage());
                    OpenMRSCustomHandler.writeLogToFile("Error from Recapture startSync: " + t.getMessage() + ", More messages: " + t.getStackTrace());
                }
            });
        }
    }


}

