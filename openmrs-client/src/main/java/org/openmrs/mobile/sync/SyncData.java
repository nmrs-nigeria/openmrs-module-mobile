package org.openmrs.mobile.sync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.matchingpatients.MatchingPatientsActivity;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.response.PbsServerContract;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientDto;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.PersonAddress;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.Notifier;
import org.openmrs.mobile.utilities.PatientAndMatchesWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class SyncData {
    private RestApi restApi;
    Context context;
    String appVersion = "";

    public SyncData(Context context) {
        this.context = context;
        this.appVersion = context.getString(R.string.app_version);
        this.restApi = RestServiceBuilder.createService(RestApi.class);
    }

    private void setSyncState(boolean b) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pbs_sync", b);
        editor.apply();
    }

    // get if the PBS syncing loop is still send data
    private boolean getSyncState() {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        return sharedPref.getBoolean("pbs_sync", false);
    }

    private void setSyncCounter(int syncCounter) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("pbs_sync_counter", syncCounter);
        editor.apply();
    }

    // get if the PBS syncing loop is still send data
    private int getSyncCounter() {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        return sharedPref.getInt("pbs_sync_counter", 0);
    }

    // variable to determine if the patient comparison is done offline or online
    boolean calculatedLocally;
    private int size;

    private void updateNotification(int index, String sub,
                                    int success, int fail, LogResponse logResponse) {

        index++;
        String summary = "Syncing " + index + " out of " + size +
                ". Succeeded: " + success +
                (fail == 0 ? "" : ". Failed: " + fail);
        String bContent = summary +
                (getSyncState() ? "\tRunning" : "\tCompleted     ")
                + (logResponse.isSuccess() ? "" : "\tMessage " + logResponse.getMessage());

        if (logResponse.isSuccess())
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS,
                    "NMRS syncing", summary, bContent);
        else
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "NMRS syncing",
                    bContent, bContent);

    }

    /*
    Start of syncing. A background service with Context will initialize and call this function
     */
    public void runSyncAwait(boolean fullSync) {
        //check server biometric service is on available and put to log

        LogResponse serverResponse = new LogResponse("SERVER STATUS");

        if (NetworkUtils.isOnline()) {
            setSyncState(true);
            //updateNotification(i, "bio",sucess, fail, pLogResponse);
            try {
                String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");
                String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/server";
                Call<PbsServerContract> call = restApi.checkServerStatus(url);
                Response<PbsServerContract> response = call.execute();
                if (response.isSuccessful()) {
                    serverResponse.appendLogs(true, "BIOMETRIC SERVICE OKAY", "", "");
                } else {
                    String err = "errorBody:" + response.errorBody().string() +
                            "  Message:" + response.message() + "  Code:" + response.code() + "  Body:" + response.body();
                    serverResponse.appendLogs(err, "BIOMETRIC SERVICE Fail 2, try again ", "syn patients->  check status");
                }
            } catch (Exception e) {
                serverResponse.appendLogs(e.getMessage(), "Check connection, and biometric services and the port number is open for external connection", "syn patients-> check status ");
            } finally {
                OpenMRSCustomHandler.writeLogToFile(serverResponse.getFullMessage());
            }

            // this method is called even if the biometric service is not running. We still need to sync other patient data
            startSyncingPatients(fullSync);


        } else {
            //  Toast.makeText(context.getApplicationContext(), "Were are offline", Toast.LENGTH_LONG).show();

            OpenMRSCustomHandler.writeLogToFile("No Network, syncing stopped");
            //
            setSyncState(false);
        }
    }


    // methode to syn all patient  sync all the patient available
    private void startSyncingPatients(boolean fullSync) {

        Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "NMRS Sync",
                "Checking patients", null);
        PatientDAO patientDAO = new PatientDAO();

        List<Patient> patientList =
                fullSync ?
                        patientDAO.getAllPatientsLocal() :
                        getPatientsWithUpdatedData(patientDAO.getAllPatientsLocal());


        // hold patient that should not sync base on it already existing
        PatientAndMatchesWrapper patientAndMatchesWrapper = new PatientAndMatchesWrapper();
        Pbs pbs = new Pbs(restApi);

        int sucess = 0;
        int fail = 0;
        size = patientList.size();

        ExcelHelperXLS excelHelperXLS = null;
        try {
            //"yyyyMMdd_HH-mm-ss"
            excelHelperXLS = new ExcelHelperXLS("yyyyMMdd");
            if (!excelHelperXLS.open()) {
                excelHelperXLS = null;
                OpenMRSCustomHandler.writeLogToFile("Failed to init excel");
            }
        } catch (Exception e) {
            excelHelperXLS = null;
            OpenMRSCustomHandler.writeLogToFile("Failed to init excel: " + e);
        }
        //loop throught the list patient availabel on the local db
        int initIndex = getSyncCounter();
        if (initIndex >= patientList.size()) {
            initIndex = 0;
        }
        for (int i = initIndex; i < patientList.size(); i++) {
            Patient patient = patientList.get(i); // assign patient
            PatientLog patientLog = new PatientLog();
            try {
                if (NetworkUtils.isOnline()) {

                    // sync patient
                    PatientSync patientSync = new PatientSync(restApi);
                    calculatedLocally = patientSync.syncPatient("BIO_" + patient.getUuid() + " id" + patient.getId(), patient, patientAndMatchesWrapper);
                    LogResponse pLogResponse = patientSync.getSyncResponse();

                    updateNotification(i, "bio", sucess, fail, pLogResponse);
                    if (!pLogResponse.isSuccess()) {
                        OpenMRSCustomHandler.writeLogToFile(pLogResponse.getFullMessage());
                    } else {
                        // ToastUtil.notify(size + " Patient: Patient " + i + " Demographic Data Synced Succesful");
                        // Util.log(size + " Patient: Patient " + i + " Demographic Data Synced Succesful");
                        // Toast.makeText(context.getApplicationContext(), size + " Patient: Patient " + i + " Demographic Data Synced Succesful", Toast.LENGTH_LONG).show();
                    }


                    LogResponse eLogResponse;
                    LogResponse pbsLogResponse;
                    // if UUID is null or empty get the patient again
                    if (patient.getUuid() == null || patient.getUuid().trim().isEmpty()) {

                        Patient newPatient = patientDAO.findPatientByID(String.valueOf(patient.getId()));
                        // Encounter sync
                        eLogResponse = new EncounterSync().startSync(newPatient, "ENC_" + newPatient.getUuid() + " id" + newPatient.getId());
                        updateNotification(i, "eco", sucess, fail, eLogResponse);
                        if (!eLogResponse.isSuccess()) {
                            OpenMRSCustomHandler.writeLogToFile(eLogResponse.getFullMessage());
                        } else {
                            //   Toast.makeText(context.getApplicationContext(), size + " Patient: Patient " + i + " Encounter Data Synced Succesful", Toast.LENGTH_LONG).show();
                        }
                        //Sync PBS
                        pbsLogResponse = pbs.syncPBSAwait(newPatient.getUuid(), newPatient.getId(), "PBS_" + newPatient.getUuid() + " id" + patient.getId());

                        updateNotification(i, "pbs", sucess, fail, pbsLogResponse);
                        if (!pbsLogResponse.isSuccess()) {
                            OpenMRSCustomHandler.writeLogToFile(pbsLogResponse.getFullMessage() + "\n\n");
                        } else {
                            // Toast.makeText(context.getApplicationContext(), size + " Patient: Patient " + i + " PBS Data Synced Succesful", Toast.LENGTH_LONG).show();
                        }


                        setPatientLog(patientLog, newPatient, pLogResponse, eLogResponse, pbsLogResponse, true);


                    } else {
                        // old patients that have UUID
                        // Encounter sync
                        eLogResponse = new EncounterSync().startSync(patient, "ENC_" + patient.getUuid() + " id" + patient.getId());
                        updateNotification(i, "eco", sucess, fail, eLogResponse);
                        if (!eLogResponse.isSuccess()) {
                            OpenMRSCustomHandler.writeLogToFile(eLogResponse.getFullMessage());
                        } else {
                            //  Toast.makeText(context.getApplicationContext(), size + " Patient: Patient " + i + " Encounter Data Synced Succesful", Toast.LENGTH_LONG).show();
                        }
                        //Sync PBS
                        pbsLogResponse = pbs.syncPBSAwait(patient.getUuid(), Long.valueOf(patient.getId()), "PBS_" + patient.getUuid() + " id" + patient.getId());
                        updateNotification(i, "pbs", sucess, fail, pbsLogResponse);
                        if (!pbsLogResponse.isSuccess()) {
                            OpenMRSCustomHandler.writeLogToFile(pbsLogResponse.getFullMessage() + "\n\n");
                        } else {
                            //  Toast.makeText(context.getApplicationContext(), size + " Patient: Patient " + i + " PBS Data Synced Succesful", Toast.LENGTH_LONG).show();
                        }
                        setPatientLog(patientLog, patient, pLogResponse, eLogResponse, pbsLogResponse, false);

                    }
                    //set counter
                    setSyncCounter(i);
                    if (pbsLogResponse.isSuccess() && eLogResponse.isSuccess()
                            && pLogResponse.isSuccess()) {
                        // Toast.makeText(context, size + " Patient: Patient " + i + " Succesfully Synced", Toast.LENGTH_LONG).show();
                        sucess++;

                    } else {
                        fail++;
                    }
                    updateNotification(i, "", sucess, fail, new LogResponse(true,
                            "", "", "", ""));


                    if (excelHelperXLS != null) {
                        excelHelperXLS.addPatientLog(patientLog, pbsLogResponse.isSuccess() && eLogResponse.isSuccess()
                                && pLogResponse.isSuccess()
                        );
                    }
                } else {
                    OpenMRSCustomHandler.writeLogToFile("No Network");
                    fail++;
                    break;
                }

            } catch (Exception e) {
                // Unexpected error occurred, reset counter
                setSyncCounter(0);
                OpenMRSCustomHandler.writeLogToFile(new LogResponse(
                        false, "Patient_Exception" + patient.getUuid() + " id" + patient.getId(),
                        "Exception occurs in syncing patient. " + e,
                        "Report to developer(HI)").getFullMessage());
                fail++;
            }
        }
        // Toast.makeText(context, "Completed", Toast.LENGTH_LONG).show();
        setSyncState(false);

        try {
            if (excelHelperXLS != null) {
                excelHelperXLS.save();
                OpenMRSCustomHandler.writeLogToFile("Save line list to: " + excelHelperXLS.getFilePath());
            }
        } catch (Exception e) {
            OpenMRSCustomHandler.writeLogToFile("Failed to save line list: " + e);
        }

        updateNotification(getSyncCounter(), "", sucess, fail, new LogResponse(true,
                "", "", "", ""));

        OpenMRSCustomHandler.writeLogToFile(new LogResponse(
                fail < 1, "Summary", "Synced:" + sucess + "\t Failed:" + fail + "\tTotal" + size,
                "If failed grater than one check the upper log for the reason", "Export").getFullMessage());
        // Start activity to Preview all similar patients
        if (!patientAndMatchesWrapper.getMatchingPatients().isEmpty()) {
            Intent intent1 = new Intent(context.getApplicationContext(), MatchingPatientsActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra(ApplicationConstants.BundleKeys.CALCULATED_LOCALLY, calculatedLocally);
            intent1.putExtra(ApplicationConstants.BundleKeys.PATIENTS_AND_MATCHES, patientAndMatchesWrapper);
            context.startActivity(intent1);
        }

        //reset counter
        if (getSyncCounter() + 1 >= patientList.size()) {
            setSyncCounter(0);
        }

        if (!fullSync && patientList.isEmpty()) {
            Notifier.notifyWithAction(context, 1, Notifier.CHANNEL_SYNC_PBS, "NMRS Sync",
                    "Completed. No new data found.", null);

        }
    }

    /**
     * Updating patientLog of a patient.  Also sync new coordinate for existing patient
     *
     * @param patientLog     pass by reference to be update by this method .
     * @param patient        Patient currently syncing
     * @param pLogResponse   Logs from syncing  patient bio data
     * @param eLogResponse   Logs from syncing forms for this patient
     * @param pbsLogResponse Logs from  syncing  PBS for this patient
     * @param isNewPatient   True when it is new patient  else False
     * @return void.
     */
    private void setPatientLog(PatientLog patientLog, Patient patient,
                               LogResponse pLogResponse,
                               LogResponse eLogResponse, LogResponse pbsLogResponse, boolean isNewPatient) {
        //
        String[] arr;
        String errorLog = "";
        arr = SimpleLog.createSimpleLogValueBio(pLogResponse.getSimpleLogs());
        String bioLog = arr[0];
        errorLog += arr[1]+"\n";

        arr = SimpleLog.createSimpleLogValuePBS(pbsLogResponse.getSimpleLogs());
        String pbsBaseLog = arr[0];
        errorLog = errorLog + arr[1]+"\n";;


        arr = SimpleLog.createSimpleLogValueForms(eLogResponse.getSimpleLogs());
        String formsLog = arr[0];
        errorLog = errorLog + arr[1]+"\n";;


        patientLog.setIdentifier(patient.getIdentifier().getIdentifier());
        patientLog.setName(patient.getName().getNameString());
        patientLog.setDob(patient.getPerson().getBirthdate());
        patientLog.setGender(patient.getGender());
        patientLog.setLatitude(patient.getAddress().getLatitude());
        patientLog.setLongitude(patient.getAddress().getLongitude());
        patientLog.setBio(bioLog);
        patientLog.setPbs(pbsBaseLog);
        patientLog.setForms(formsLog);
        patientLog.setUuid(patient.getUuid());
        patientLog.setMobileVersion(appVersion);
        patientLog.setErrorLog(errorLog);
        patientLog.setIdentifier(patient.getIdentifier().getIdentifier());

        if (!isNewPatient) {
            try {
                Response<PatientDto> patientResponse = restApi.getPatientByUUID(patient.getUuid(), ApplicationConstants.API.FULL).execute();

                if (patientResponse.isSuccessful()) {
                    Patient patientWeb = patientResponse.body().getPatient();
                    if (patientWeb != null) {
                        PersonAddress personAddress = patient.getAddress();
                        if (personAddress != null) {
                            String preferredAddressUuid = patientWeb.getAddress().getUuid();
                            Response<PersonAddress> updateRes = restApi.updatePersonAddress(personAddress, patient.getUuid(), preferredAddressUuid, ApplicationConstants.API.FULL).execute();
                            if (updateRes.isSuccessful()) {
                                patientLog.setGeoCoordinateSync(true);
                            } else {
                                Util.log("  Address web update FAILED  " + updateRes.code() + updateRes.message());
                            }
                        } else {
                            Util.log("  Address no found ");
                        }
                        if (patientLog.getIdentifier() == null) {
                            patientLog.setIdentifier(patientWeb.getIdentifier().getIdentifier());
                        }
                    } else {
                        Util.log("  Failed to retrieve the patient Body");
                    }


                } else {
                    Util.log("  Failed to retrieve the patient");
                }
            } catch (Exception e) {
                Util.log("Error Updating location " + e);
            }
        }


    }

    private void appendIdentifierToLog(PatientLog patientLog, JsonObject patientJson) {
        JsonArray identifiersArray = patientJson.getAsJsonArray("identifiers");

        // List to store maps
        List<Map<String, String>> listOfMaps = new ArrayList<>();

        // Iterate through identifiers array
        for (JsonElement identifierElement : identifiersArray) {
            JsonObject identifierObject = identifierElement.getAsJsonObject();
            String display = identifierObject.get("display").getAsString();
            String identifier = identifierObject.get("identifier").getAsString();

            // Create map and add to list
            Map<String, String> map = new HashMap<>();
            map.put("display", display);
            map.put("identifier", identifier);

            listOfMaps.add(map);
        }

        // Display the list of maps
//        for (Map<String, String> map : listOfMaps) {
//            System.out.println(map);
//        }

        patientLog.setIdentifier(listOfMaps.toString());

    }

    private List<Patient> getPatientsWithUpdatedData(List<Patient> patientList) {
        EncounterDAO encounterDAO = new EncounterDAO();
        FingerPrintDAO fingerPrintDAO = new FingerPrintDAO();
        FingerPrintVerificationDAO fingerPrintVerificationDAO = new FingerPrintVerificationDAO();
        List<Patient> newPatientList = new ArrayList<>();
        for (Patient patient : patientList) {
            Long id = patient.getId();
            boolean isEncounterSafeToDelete = encounterDAO.safeToDelete(id);
            boolean isFingerprintSafeToDelete = fingerPrintDAO.safeToDelete(id);
            boolean isFingerprintsVerificationSafeToDelete = fingerPrintVerificationDAO.safeToDelete(id);
            if (patient.isSynced()
                    && isEncounterSafeToDelete &&
                    isFingerprintSafeToDelete
                    && isFingerprintsVerificationSafeToDelete) {
                // data already sync
            } else {
                newPatientList.add(patient);
            }
        }

        return newPatientList;
    }


}
