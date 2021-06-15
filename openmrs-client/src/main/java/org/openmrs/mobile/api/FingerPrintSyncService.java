package org.openmrs.mobile.api;


import com.activeandroid.app.Application;
import com.google.gson.Gson;

import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.activities.pbs.PatientBiometricSyncResponseModel;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.listeners.retrofit.GenericResponseCallbackListener;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;
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
                    } else {
                        responseCallbackListener.onErrorResponse("Some server errors occurred");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<PatientBiometricContract>> call, @NonNull Throwable t) {
                    ToastUtil.notify("Error: " + t.getMessage());
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

            Call<PatientBiometricSyncResponseModel> call = restApi.syncPBS(url, PBSObj);
            call.enqueue(new Callback<PatientBiometricSyncResponseModel>() {
                @Override
                public void onResponse(@NonNull Call<PatientBiometricSyncResponseModel> call, @NonNull retrofit2.Response<PatientBiometricSyncResponseModel> response) {
                    if (response.isSuccessful()) {
                        responseCallbackListener.onResponse(response.body());
                    } else {
                        ToastUtil.notify("Error: " + response.body());
                        responseCallbackListener.onErrorResponse(response.body());
                        System.out.print("Error occurred");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PatientBiometricSyncResponseModel> call, @NonNull Throwable t) {
                    ToastUtil.notify("Error: " + t.getMessage());
                    responseCallbackListener.onErrorResponse(t.getMessage());
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
            return 0;
        }

        ToastUtil.warning("about to sync " +PBS_DTO.size() +" finger prints to server");

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
                        syncCount[0] += 1;
                    }
                }

                @Override
                public void onErrorResponse(PatientBiometricSyncResponseModel errorMessage) {
                    if(errorMessage !=null){
                        ToastUtil.notify(errorMessage.getErrorMessage());
                    }
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    ToastUtil.notify(errorMessage);
                }
            });
        }
        return syncCount[0];
        //dao.deleteAllPrints();
    }
}

