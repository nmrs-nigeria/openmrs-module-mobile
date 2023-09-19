package pbsprocessor.api;


import pbsprocessor.OpenMRS;
import pbsprocessor.api.retrofit.RestApi;
import pbsprocessor.api.retrofit.RestServiceBuilder;
import pbsprocessor.listerner.GenericResponseCallbackListener;
import pbsprocessor.listerner.PatientBiometricSyncResponseModel;
import pbsprocessor.listerner.PbsServerContract;
import pbsprocessor.model.PatientBiometricContract;
import pbsprocessor.model.PatientBiometricDTO;
import pbsprocessor.util.ApplicationConstants;
import pbsprocessor.util.HashMethods;
import pbsprocessor.util.OpenMRSCustomHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class   SyncService {

    private RestApi restApi;

    public SyncService() {
        this.restApi = RestServiceBuilder.createService(RestApi.class);
        // super("Sync Finger Prints");
    }


    public Response<PatientBiometricSyncResponseModel> startSync(PatientBiometricDTO PBSObj,
                                                                 String url
    ) throws IOException {
        System.out.println("URL: " + url);
        /// add hashing before syncing
        for (int bioIndex = 0; bioIndex < PBSObj.getFingerPrintList().size(); bioIndex++) {
            PatientBiometricContract b = PBSObj.getFingerPrintList().get(bioIndex);
            b.setModel(ApplicationConstants.PBS_PASSWORD_VERSION);
            b.setManufacturer(HashMethods.getPBSHash(PBSObj.getPatientUUID(),
                    b.getDateCreated(),
                    b.getImageQuality(),
                    b.getSerialNumber(),
                    b.getFingerPositions().toString()
            ));
            PBSObj.getFingerPrintList().set(bioIndex, b);
        }

        //  Util.log("URL of the file "+url);
        Call<PatientBiometricSyncResponseModel> call = restApi.syncPBS(url, PBSObj);
        return call.execute();
    }


    public Response<PbsServerContract> CheckServiceStatus() throws IOException {
        Call<PbsServerContract> call = restApi.checkServerStatus(OpenMRS.getInstance().getUrlServerStatus());
      return   call.execute();
    }

}



