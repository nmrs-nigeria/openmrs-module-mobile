package org.openmrs.mobile.api;


import com.activeandroid.app.Application;
import com.google.gson.Gson;

import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.activities.pbs.PatientBiometricSyncResponseModel;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.listeners.retrofit.GenericResponseCallbackListener;
import org.openmrs.mobile.models.FingerPrintLog;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.api.response.PbsServerContract;
import org.openmrs.mobile.security.HashMethods;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;

public class FingerPrintSyncService extends Application {
    private static FingerPrintSyncService instance;
    private RestApi restApi;

    public FingerPrintSyncService() {
        this.restApi = RestServiceBuilder.createService(RestApi.class);
        // super("Sync Finger Prints");
    }

    public FingerPrintSyncService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }


    public void CheckServiceStatus(GenericResponseCallbackListener<PbsServerContract> responseCallbackListener) {
        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");

        //String url = baseUrl[0]+"://"+ baseUrl[1].replaceAll("//","") +":2018/api/Client/get";
        //"http://192.168.0.151:2018/api/Client/get"; baseUrl[1].replaceAll("//","");
        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/server";

        if (NetworkUtils.isOnline()) {
            Call<PbsServerContract> call = restApi.checkServerStatus(url);
            call.enqueue(new Callback<PbsServerContract>() {
                @Override
                public void onResponse(@NonNull Call<PbsServerContract> call, @NonNull retrofit2.Response<PbsServerContract> response) {
                             responseCallbackListener.onResponse(response.body());
                }

                @Override
                public void onFailure(@NonNull Call<PbsServerContract> call, @NonNull Throwable t) {
                  responseCallbackListener.onErrorResponse("Failed to established connection to the PBS server");
                   ToastUtil.notify("Error: " + t.getMessage());
                    OpenMRSCustomHandler.writeLogToFile("Error from check service status Message {" + t.getMessage() + "}");
                }
            });
        }
    }
 public  void  retrieveFingerLog(String patientUUID, String patientId){

     String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");

     String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/CheckForPreviousCapture";
      Call <FingerPrintLog> call = restApi.getFingerLog(url, patientUUID);
     call.enqueue(new Callback<FingerPrintLog>() {
         @Override
         public void onResponse(@NonNull Call<FingerPrintLog> call, @NonNull retrofit2.Response<FingerPrintLog> response) {
             if (response.isSuccessful()) {
                 if (response.body() != null  ) {
                  FingerPrintLog fingerPrintLog=   response.body();
                  fingerPrintLog.setPid(patientId);
                  fingerPrintLog.save();
                 }else{
                     ToastUtil.notify("Error "  );
                       Util.log("Body is null ");
                 }
             }else{
                 Util.log("Error failed download completely    ");
             }
         }
         @Override
         public void onFailure(Call<FingerPrintLog> call, Throwable t) {
             ToastUtil.notify("Error: " + t.getMessage());
             OpenMRSCustomHandler.writeLogToFile(" Error fingerprinlog" + t.getMessage() + "}");
         }
     });
 }
    public void retrieveCaptureFromServer(String patientUUID, String patientId, boolean saveTemplate) {

        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");

        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/CheckForPreviousCapture";
        //String url = "http://192.168.0.151:2018/api/FingerPrint/CheckForPreviousCapture"; //baseUrl[1].replaceAll("//","");
        Util.log("public void retrieveCaptureFromServer(String patientUUID, boolean saveTemplate)");
            Call<List<PatientBiometricContract>> call = restApi.checkForExistingPBS(url, patientUUID);
            call.enqueue(new Callback<List<PatientBiometricContract>>() {
                @Override
                public void onResponse(@NonNull Call<List<PatientBiometricContract>> call, @NonNull retrofit2.Response<List<PatientBiometricContract>> response) {
                    if (response.isSuccessful()) {
                     //   retrieveFingerLog(patientUUID, patientId);
                        if (response.body() != null && response.body().size() >= ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT) {
                           Patient patient =  new PatientDAO().findPatientByUUID(patientUUID);
                            for (PatientBiometricContract item : response.body()) {
                                if(!saveTemplate){
                                    item.setTemplate("");
                                }
                                String patientId = String.valueOf(patient.getId());
                                item.setPatienId(Integer.parseInt(patientId));
                                item.setSyncStatus(1); //set to already synced
                            }
                           // Util.log("FingerPrintDAO().saveFingerPrint(response.body())");
                            new FingerPrintDAO().saveFingerPrint(response.body());
                        }else{
                          //  Util.log("LessPrints "+response.body().size());
                        }
                    }else{
                       // Util.log("retrieveCaptureFromServer ");
                    }
                }
                @Override
                public void onFailure(Call<List<PatientBiometricContract>> call, Throwable t) {
                    ToastUtil.notify("Error: " + t.getMessage());
                    Util.log("Failure "+t.getMessage());
                    OpenMRSCustomHandler.writeLogToFile("Error from retrieve capture from server: Message {" + t.getMessage() + "}");
                }
        });
    }


    //http://localhost:2018/api/FingerPrint/CheckForPreviousCapture?PatientUUID=17faf42b-543e-435a-ba1d-95888cf4ae2e
    public void CheckForPreviousCapture(String patientUUID, GenericResponseCallbackListener<List<PatientBiometricContract>> responseCallbackListener) {
        Util.log("CheckForPreviousCapture(String patientUUID, GenericResponseCallbackListener<List<PatientBiometricContract>> responseCallbackListener) ");

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


    public void startSync(PatientBiometricDTO PBSObj, GenericResponseCallbackListener<PatientBiometricSyncResponseModel> responseCallbackListener) {

        String json = new Gson().toJson(PBSObj);
        System.out.print(json);

        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");
        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/SaveToDatabase";

        if (NetworkUtils.isOnline()) {

            /// add hashing before syncing
        for(int bioIndex=0;bioIndex<PBSObj.getFingerPrintList().size();bioIndex++){
           PatientBiometricContract b= PBSObj.getFingerPrintList().get(bioIndex);
           b.setModel(ApplicationConstants.PBS_PASSWORD_VERSION);
           b.setManufacturer(HashMethods.getPBSHash(PBSObj.getPatientUUID(),
                   b.getDateCreated(),
                   b.getImageQuality(),
                   b.getSerialNumber(),
                   b.getFingerPositions().toString()
                   ));
            PBSObj.getFingerPrintList().set(bioIndex,b );
        }


            Call<PatientBiometricSyncResponseModel> call = restApi.syncPBS(url, PBSObj);
            call.enqueue(new Callback<PatientBiometricSyncResponseModel>() {
                @Override
                public void onResponse(@NonNull Call<PatientBiometricSyncResponseModel> call, @NonNull retrofit2.Response<PatientBiometricSyncResponseModel> response) {
                    if (response.isSuccessful()) {
                       // OpenMRSCustomHandler.writeLogToFile("PBS synced successfully: " + response.message());
                        responseCallbackListener.onResponse(response.body());
                    } else {
                        try {
                            OpenMRSCustomHandler.writeLogToFile("PBS sync error: Error Code:" + response.code() + ", Error Message:" + response.message() + " Error Body:" + response.errorBody().string());
                            ToastUtil.notify("Error: " +  response.errorBody().string());
                        } catch (IOException e) {
                            OpenMRSCustomHandler.writeLogToFile("PBS  sync error: Error Code:" + response.code() + ", Error Message:" + response.message() + " Error Body:" + response.errorBody());
                            ToastUtil.notify("Error: " + response.body());
                        }
                        responseCallbackListener.onErrorResponse(response.body());
                        //System.out.print("Error occurred");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PatientBiometricSyncResponseModel> call, @NonNull Throwable t) {
                    ToastUtil.notify("Error: " + t.getMessage());
                    responseCallbackListener.onErrorResponse(t.getMessage());
                    OpenMRSCustomHandler.writeLogToFile("Error from startSync: " + t.getMessage() + ", More messages: " + t.getStackTrace());
                }
            });
        }
    }


    public int autoSyncFingerPrint() {

        ToastUtil.notify("Searching for finger prints to sync");

        FingerPrintDAO dao = new FingerPrintDAO();

        List<PatientBiometricContract> prints = dao.getAll(false,null);

        //group finger print by patient
        Map<Integer, ArrayList<PatientBiometricContract>> printsForEachPatient = new HashMap<>();
        for (PatientBiometricContract item : prints) {
            ArrayList<PatientBiometricContract> list;
            if (printsForEachPatient.containsKey(item.getPatienId())) {
                list = printsForEachPatient.get(item.getPatienId());
            } else {
                list = new ArrayList<>();
            }
            list.add(item);
            printsForEachPatient.put(item.getPatienId(), list);
        }

        PatientDAO pDAO = new PatientDAO();
        List<PatientBiometricDTO> PBS_DTO = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<PatientBiometricContract>> entry : printsForEachPatient.entrySet()) {
            PatientBiometricDTO _dto = new PatientBiometricDTO();

            Patient patient = pDAO.findPatientByID(entry.getKey().toString());
            if (patient != null) {
                _dto.setPatientUUID(patient.getUuid());
                _dto.setFingerPrintList(entry.getValue());
                PBS_DTO.add(_dto);
            }
        }

        if(PBS_DTO.size() == 0){
            ToastUtil.notify("No finger print data to sync");
            OpenMRSCustomHandler.writeLogToFile("No finger print data to sync");
            return 0;
        }

        ToastUtil.warning("about to sync " +PBS_DTO.size() +" finger prints to server");
        OpenMRSCustomHandler.writeLogToFile("about to sync " +PBS_DTO.size() + " finger prints to server");

        final int[] syncCount = {0};
        for (PatientBiometricDTO aPrint : PBS_DTO) {
            PatientBiometricContract dto = aPrint.getFingerPrintList().get(0);

            startSync(aPrint, new GenericResponseCallbackListener<PatientBiometricSyncResponseModel>() {
                @Override
                public void onResponse(PatientBiometricSyncResponseModel obj) {
                    if (obj.getIsSuccessful()) {
                        //update
                        dto.setSyncStatus(1);
                        dao.updatePatientFingerPrintSyncStatus((long) dto.getPatienId(), dto);
                        ToastUtil.success("fingerprint saved online");
                        OpenMRSCustomHandler.writeLogToFile("Fingerprint saved online. Sync successful");
                        syncCount[0] += 1;
                    }
                }

                @Override
                public void onErrorResponse(PatientBiometricSyncResponseModel errorMessage) {
                    if(errorMessage !=null){
                        ToastUtil.notify(errorMessage.getErrorMessage());
                        OpenMRSCustomHandler.writeLogToFile("Error response from Autosync: " + errorMessage.getErrorMessage() + " Error Code: " + errorMessage.getErrorCode());
                    }
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    ToastUtil.notify(errorMessage);
                    OpenMRSCustomHandler.writeLogToFile("Error response from Autosync: " + errorMessage);
                }
            });
        }
        return syncCount[0];
        //dao.deleteAllPrints();
    }
}

