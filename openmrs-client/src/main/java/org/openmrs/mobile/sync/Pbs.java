package org.openmrs.mobile.sync;

import static org.openmrs.mobile.utilities.ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT;

import androidx.annotation.NonNull;

import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.activities.pbs.PatientBiometricSyncResponseModel;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationDTO;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.ServiceLogDAO;
import org.openmrs.mobile.security.HashMethods;
import org.openmrs.mobile.utilities.ApplicationConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class Pbs {
    private RestApi restApi;
    FingerPrintDAO dao = new FingerPrintDAO();
    FingerPrintVerificationDAO daoVerification = new FingerPrintVerificationDAO();

    public Pbs(@NonNull RestApi restApi) {
        this.restApi = restApi;
    }

    public LogResponse syncPBSAwait(String patientUUID, Long patientId, String identifier) {
        if (patientUUID != null && patientUUID != "") {
            List<PatientBiometricContract> pbs = dao.getAll(false, patientId.toString());
            List<PatientBiometricVerificationContract> pbsVerification = daoVerification.getAll(false, patientId.toString());

            if (pbs.size() ==0 && pbsVerification.size() ==0) {
                return  new LogResponse(
                                true,
                                identifier,
                                "No fingerprints",
                                "",
                                "PBS Sync"
                        );}
// make patient whom it print as not save not to sync
            List<PatientBiometricVerificationContract> confirm = daoVerification.getSinglePatientPBS( patientId );
            if(confirm.size()!= pbsVerification.size()){
                return  new LogResponse(
                        true,
                        identifier,
                        "Undecided prints",
                        "Open this patient and confirm him as recapture or replacement for base",
                        "PBS recapture Sync"
                );
            }
            //minimum prints not reached for both base and recapture
            if (pbs.size() < MINIMUM_REQUIRED_FINGERPRINT && pbsVerification.size() < MINIMUM_REQUIRED_FINGERPRINT) {
                return  new LogResponse(
                        false,
                        identifier,
                        "Minimum prints not reached",
                        "Capture more prints, do a recapture",
                        "PBS Sync"
                );

            } else {


                if (pbs.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                    PatientBiometricDTO dto = new PatientBiometricDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbs));
                    dto.setPatientUUID(patientUUID);
                    try {
                        Response<PatientBiometricSyncResponseModel> res = startSyncCaptureAwait(dto);
                        if (res.isSuccessful()) {
                            // change only the sync state to 1
                            dao.updateSync(Long.valueOf(patientId),1, false);
                            // setting void to one for all records that matches the the UUID
                             new ServiceLogDAO().set_patient_PBS_void(String.valueOf(patientId),patientUUID,1);
                            return new LogResponse(
                                    true,
                                    identifier,
                                    "Capture save to server successfully",
                                    "",
                                    "PBS Sync capture"
                            );
                        } else {
                            String err = patientUUID + " Capture  unsuccessfully " + res.errorBody().string() +
                                    "  " + res.message() + "  " + res.code() + "  " + res.body();
                            return new LogResponse(
                                    false,
                                    identifier,
                                    err,
                                    "Check connection,",
                                    "PBS Sync capture"
                            );
                        }
                    } catch (Exception e) {
                        return new LogResponse(
                                false,
                                identifier,
                                e.getMessage(),
                                "Check network connection",
                                "PBS Sync capture"
                        );
                    }

                } else if (pbsVerification.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                    PatientBiometricVerificationDTO dto = new PatientBiometricVerificationDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbsVerification));
                    dto.setPatientUUID(patientUUID);
                    try {
                        Response<PatientBiometricSyncResponseModel> res = startSyncRecaptureAwait(dto);
                        if (res.isSuccessful()) {
                            //  remove prints
                            daoVerification.deletePrint( patientId );
                            // setting void to one for all records that matches the the UUID
                            new ServiceLogDAO().set_patient_PBS_void(String.valueOf(patientId),patientUUID,1);
                                return new LogResponse(
                                    true,
                                    identifier,
                                    "Recapture save to server successfully",
                                    "",
                                    "PBS Sync recapture"
                            );
                        } else {
                            String err = patientUUID + "Recapture  unsuccessfully " + res.errorBody().string() +
                                    "  " + res.message() + "  " + res.code() + "  " + res.body();

                            return new LogResponse(
                                    false,
                                    identifier,
                                    err,
                                    "Check connection,",
                                    "PBS Sync recapture"
                            );
                        }
                    } catch (Exception e) {
                        return new LogResponse(
                                false,
                                identifier,
                                e.getMessage(),
                                "Check network connection",
                                "PBS Sync recapture"
                        );
                    }
                } else {
                    return new LogResponse(
                            false,
                            identifier,
                            "No prints found",
                            "Report this error",
                            "PBS Sync recapture"
                    );
                }
            }
        } else {
            return new LogResponse(
                    false,
                    identifier,
                    "No UUID",
                    "Try again",
                    "PBS Sync");
        }

    }


    private Response<PatientBiometricSyncResponseModel> startSyncCaptureAwait(PatientBiometricDTO PBSObj) throws IOException {
        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");
        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/SaveToDatabase";

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
            Call<PatientBiometricSyncResponseModel> call = restApi.syncPBS(url, PBSObj);
            return call.execute();

    }

    private Response<PatientBiometricSyncResponseModel> startSyncRecaptureAwait(PatientBiometricVerificationDTO PBSObj) throws IOException {

        //System.out.print(json);
        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");
        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/ReSaveFingerprintVerificationToDatabase";

            /// add hashing before syncing
            for (int bioIndex = 0; bioIndex < PBSObj.getFingerPrintList().size(); bioIndex++) {
                PatientBiometricVerificationContract b = PBSObj.getFingerPrintList().get(bioIndex);
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
}
