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
import org.openmrs.mobile.databases.Util;
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

            if (pbs.size() == 0 && pbsVerification.size() == 0) {
                LogResponse log = new LogResponse(
                        true,
                        identifier,
                        "No fingerprints",
                        "",
                        "PBS Sync"
                );
                //Additional log precise
                log.addSimpleLogs(new SimpleLog("PBS", true, false));
                return log;
            }
// make patient whom it print as not save not to sync
            //           List<PatientBiometricVerificationContract> confirm = daoVerification.getSinglePatientPBS(patientId);
//            if (confirm.size() != pbsVerification.size()) {
//                LogResponse log = new LogResponse(
//                        true,
//                        identifier,
//                        "Undecided prints",
//                        "Open this patient and confirm him as recapture or replacement for base",
//                        "PBS recapture Sync"
//                );
//                //Additional log precise
//                log.addSimpleLogs(new SimpleLog("Undecided prints", true, false));
//                return log;
//            }
            //minimum prints not reached for both base and recapture
            if (pbs.size() < MINIMUM_REQUIRED_FINGERPRINT && pbsVerification.size() < MINIMUM_REQUIRED_FINGERPRINT) {
                LogResponse log =
                        new LogResponse(
                                false,
                                identifier,
                                "Minimum prints not reached",
                                "Capture more prints, do a recapture",
                                "PBS Sync"
                        );
                //Additional log precise
                log.addSimpleLogs(new SimpleLog("PBS", false, "Minimum prints not reach", true, pbsVerification.size() > 1 ? pbs.size() : pbs.size()));
                return log;

            } else {
                if (pbs.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                    PatientBiometricDTO dto = new PatientBiometricDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbs));
                    dto.setPatientUUID(patientUUID);
                    try {
                        Response<PatientBiometricSyncResponseModel> res = startSyncCaptureAwait(dto);

                        if (res.isSuccessful()) {
                            // change only the sync state to 1
                            dao.updateSync(Long.valueOf(patientId), 1, false);
                            // setting void to one for all records that matches the the UUID
                            new ServiceLogDAO().set_patient_PBS_void(String.valueOf(patientId), patientUUID, 1);
                            LogResponse log = new LogResponse(
                                    true,
                                    identifier,
                                    "Capture save to server successfully",
                                    "",
                                    "PBS Sync capture"
                            );

                            //Additional log precise


                            log.addSimpleLogs(new SimpleLog("PBS-Base", true, getServerMessage(res), true, pbs.size()));
                            return log;
                        } else {
                            String err = patientUUID + " Capture  unsuccessfully " + res.errorBody().string() +
                                    "  " + res.message() + "  " + res.code() + "  " + res.body();
                            LogResponse log = new LogResponse(
                                    false,
                                    identifier,
                                    err,
                                    "Check connection,",
                                    "PBS Sync capture"
                            );
                            //Additional log precise
                            log.addSimpleLogs(new SimpleLog("PBS-Base", false, err, true, pbs.size()));
                            return log;
                        }
                    } catch (Exception e) {
                        LogResponse log = new LogResponse(
                                false,
                                identifier,
                                e.getMessage(),
                                "Check network connection",
                                "PBS Sync capture"
                        );
                        //Additional log precise
                        log.addSimpleLogs(new SimpleLog("PBS-Base", false, e.getMessage(), true, pbs.size()));
                        return log;
                    }

                } else if (pbsVerification.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                    PatientBiometricVerificationDTO dto = new PatientBiometricVerificationDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbsVerification));
                    dto.setPatientUUID(patientUUID);


                    try {

                        Response<PatientBiometricSyncResponseModel> res = dto.isReplaceBase() ? startSyncReplaceBaseAwait(dto) :
                                startSyncRecaptureAwait(dto);
                        if (res.isSuccessful()) {

                            //  remove prints
                            daoVerification.deletePrint(patientId);
                            // setting void to one for all records that matches the the UUID
                            new ServiceLogDAO().set_patient_PBS_void(String.valueOf(patientId), patientUUID, 1);

                            LogResponse log = new LogResponse(
                                    true,
                                    identifier,
                                    "Recapture save to server successfully",
                                    "",
                                    "PBS Sync recapture"
                            );


                            //Additional log precise
                            log.addSimpleLogs(new SimpleLog(dto.isReplaceBase() ? "PBS-Replace base" : "PBS-Recapture", true, getServerMessage(res), true, pbsVerification.size()));
                            return log;
                        } else {
                            String err = patientUUID + "Recapture  unsuccessfully " + res.errorBody().string() +
                                    "  " + res.message() + "  " + res.code() + "  " + res.body();

                            Util.log(err);
                            LogResponse log = new LogResponse(
                                    false,
                                    identifier,
                                    err,
                                    "Check connection,",
                                    "PBS Sync recapture"
                            );

                            //Additional log precise
                            log.addSimpleLogs(new SimpleLog(dto.isReplaceBase() ? "PBS-Replace base" : "PBS-Recapture", false, err, true, pbsVerification.size()));
                            return log;
                        }
                    } catch (Exception e) {
                        LogResponse log = new LogResponse(
                                false,
                                identifier,
                                e.getMessage(),
                                "Check network connection",
                                "PBS Sync recapture"
                        );
                        //Additional log precise
                        log.addSimpleLogs(new SimpleLog(dto.isReplaceBase() ? "PBS-Replace base" : "PBS-Recapture", false, e.getMessage(), true, pbsVerification.size()));
                        return log;
                    }
                } else {
                    LogResponse log = new LogResponse(
                            false,
                            identifier,
                            "No prints found",
                            "Report this error",
                            "PBS Sync recapture"
                    );
                    //Additional log precise
                    log.addSimpleLogs(new SimpleLog("PBS-Recapture", false, "No prints found: Report this error", false));
                    return log;
                }
            }
        } else {
            LogResponse log = new LogResponse(
                    false,
                    identifier,
                    "No UUID",
                    "Try again",
                    "PBS Sync");
            //Additional log precise
            log.addSimpleLogs(new SimpleLog("PBS", false, "No UUID"));
            return log;
        }

    }

    private String getServerMessage(Response<PatientBiometricSyncResponseModel> res) {
        String message = "Server Message Exception";
        try {
            message = res.body().getErrorMessage();
            if (message == null) {
                message = "";
            } else {
                message = message.replaceAll("Error!", "");
            }
        } catch (Exception ignored) {
        }
        return message;
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
        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/ReSaveFingerprintVerificationToDatabaseMobile";
        //  Util.log("URL " + url);
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


    private Response<PatientBiometricSyncResponseModel> startSyncReplaceBaseAwait(PatientBiometricVerificationDTO PBSObj) throws IOException {
        String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");
        String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/api/FingerPrint/replaceBaseWithRecaptureMobile";

        /// add hashing before syncing
        //Util.log("StartSyncReplaceBaseAwait URL " + url);
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
