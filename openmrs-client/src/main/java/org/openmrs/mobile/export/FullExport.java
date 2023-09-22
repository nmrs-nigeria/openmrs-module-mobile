package org.openmrs.mobile.export;

import static org.openmrs.mobile.utilities.ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.activeandroid.query.Select;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationDTO;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientDto;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.security.HashMethods;
import org.openmrs.mobile.sync.LogResponse;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.Notifier;

import java.util.ArrayList;
import java.util.List;

public class StartExport {

    private RestApi restApi;
    Context context;
    FingerPrintDAO dao = new FingerPrintDAO();
    FingerPrintVerificationDAO daoVerification = new FingerPrintVerificationDAO();
    public StartExport(Context context) {
        this.context = context;
        this.restApi = RestServiceBuilder.createService(RestApi.class);
    }

    private void setSyncState(boolean b) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("EXPORT",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pbs_sync", b);
        editor.apply();
    }

    // get if the PBS Exporting loop is still send data
    private boolean getSyncState() {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("EXPORT",
                Activity.MODE_PRIVATE);
        return sharedPref.getBoolean("pbs_sync", false);
    }

    // variable to determine if the patient comparison is done offline or online

  private int size;
    private void updateNotification(int index, String sub,
                                    int success, int fail, LogResponse logResponse) {

        String summary=   "Exporting "+ index +" out of "+ size+
                ". Succeeded: "+  success+
             ( fail==0?"":  ". Failed: "+fail);
        String bContent=  summary+
                 (getSyncState()?"\tRunning":"\tCompleted     "   )
                +(   logResponse.isSuccess()?"":"\tMessage "+ logResponse.getMessage())
             ;

        if(logResponse.isSuccess())
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS,
                    "NMRS Exporting",  summary, bContent);
        else
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "NMRS Exporting",
                    bContent, bContent);

    }
    /*
    Start of Exporting. A background service with Context will initialize and call this function
     */



    // methode to syn all patient  EXPORT all the patient available
    private void starExportingPatients() {
        Notifier.cancel(context, 1);
        Notifier.notify(context, 1, Notifier.CHANNEL_EXPORT, "NMRS Export",
                "Checking patient", null  );
        PatientDAO patientDAO = new PatientDAO();
        List<Patient> patientList = patientDAO.getAllPatientsLocal();
        // hold patient that should not EXPORT base on it already existing


        setSyncState(true);
        int i= 0;
        int sucess =0;
        int fail=0;
        size=patientList.size();
        JSONArray dataJson = new JSONArray();
        for (Patient patient : patientList) {
                i++;
            JSONObject patientObject= new JSONObject();
                // EXPORT patient
               
            LogResponse pLogResponse = addBioData(patientObject, patient,"BIO_EXPORT" + patient.getUuid() + " id" + patient.getId() );
                updateNotification(i, "bio",sucess, fail, pLogResponse);
                if(!pLogResponse.isSuccess()) {
                    OpenMRSCustomHandler.writeLogToFile(pLogResponse.getFullMessage());
                }
                // Encounter EXPORT
                LogResponse eLogResponse = addEncounters(patientObject,
                        patient, "ENC_EXPORT" + patient.getUuid() + " id" + patient.getId());
                updateNotification(i, "eco",sucess, fail, eLogResponse);
                if(!eLogResponse.isSuccess()) {
                    OpenMRSCustomHandler.writeLogToFile(eLogResponse.getFullMessage());
                }
                // check if already EXPORT recapture or base
                // if UUID is null get the patient again
                LogResponse    pbsLogResponse = new PbsExport().syncPBSAwait(patient.getUuid(), Long.valueOf(patient.getId()), "PBS_" + patient.getUuid() + " id" + patient.getId());
                    updateNotification(i, "pbs",sucess, fail, pbsLogResponse);
                    if(!pbsLogResponse.isSuccess()) {
                        OpenMRSCustomHandler.writeLogToFile(pbsLogResponse.getFullMessage() + "\n\n");
                    }
                //
                 if(pbsLogResponse.isSuccess()&& eLogResponse.isSuccess()
                        && pLogResponse.isSuccess()){
                    sucess++;

                }else{
                    fail++;
                }
                updateNotification(i, "",sucess, fail,new LogResponse(true,
                        "","","",""));

        }
        setSyncState(false);
        updateNotification(i, "",sucess, fail,new LogResponse(true,
                "","","",""));

        OpenMRSCustomHandler.writeLogToFile(new LogResponse(
                fail < 1, "Summary", "Synced:"+sucess+"\t Failed:"+fail+"\tTotal"+size,
                "If failed grater than one check the upper log for the reason", "Export").getFullMessage());
        // Start activity to Preview all similar patients

    }
    public @NonNull LogResponse addEncounters(JSONObject patientObject ,@NonNull Patient patient, String identity)  {
        LogResponse logResponse = new LogResponse(identity);
        try {
            List<Encountercreate> encountercreatelist = new Select()
                    .from(Encountercreate.class)
                    .where("patientid = ?", patient.getId())
                    .where("synced = ?", false) // case duplicate forms handle
                    .execute();

            Visit mVisit = null;
            if (encountercreatelist.size() < 1) {
                logResponse.appendLogs(true, "Already Export or not entered", "Check web", "Export Encounter ");
                return logResponse;
            }
            JSONArray encounters = new JSONArray();

            for (final Encountercreate encountercreate : encountercreatelist) {
                encountercreate.pullObslist();
                Gson gson = new Gson();
                encounters.put(gson.toJson(encounters));
            }

            patientObject.put("encouters", encounters);

        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(),"","exportBioData");
        }



        return logResponse;

    }


    protected  LogResponse addBioData(JSONObject patientObject ,Patient patient,
                                         @NonNull String identifier    ) {
        LogResponse logResponse = new LogResponse(identifier);
        try {
            PatientDto patientDto = patient.getPatientDto();
            Gson gson = new Gson();
            patientObject.put("bio_data",gson.toJson(patient));
            logResponse.appendLogs(false, "","","exportBioData");

        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(),"","exportBioData");
        }
        return  logResponse;

    }

    protected LogResponse  addPBS(JSONObject patientObject, String patientUUID, Long patientId, String identifier) {
        LogResponse logResponse =new LogResponse(identifier);

            List<PatientBiometricContract> pbs = dao.getAll(false, patientId.toString());
            List<PatientBiometricVerificationContract> pbsVerification = daoVerification.getAll(false, patientId.toString());

            if (pbs.size() ==0 && pbsVerification.size() ==0) {
                logResponse.appendLogs(
                        true,
                        "No fingerprints",
                        "",
                        "PBS Export");
                return   logResponse;
            }
// make patient whom it print as not save not to Export
//            List<PatientBiometricVerificationContract> confirm = daoVerification.getSinglePatientPBS( patientId );
//            if(confirm.size()!= pbsVerification.size()){
//                return  new LogResponse(
//                        true,
//                        identifier,
//                        "Undecided prints",
//                        "Open this patient and confirm him as recapture or replacement for base",
//                        "PBS recapture Export"
//                );
//            }
            //minimum prints not reached for both base and recapture
            if (pbs.size() < MINIMUM_REQUIRED_FINGERPRINT && pbsVerification.size() < MINIMUM_REQUIRED_FINGERPRINT) {
                logResponse.appendLogs(    false,
                        "Minimum prints not reached", "Capture more prints, do a recapture",  "PBS Export"
                );
                return  logResponse;

            } else { 
                if (pbs.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                    PatientBiometricDTO dto = new PatientBiometricDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbs));
                    dto.setPatientUUID(patientUUID);

                    
                    //set hashing
                    for (int bioIndex = 0; bioIndex < dto.getFingerPrintList().size(); bioIndex++) {
                        PatientBiometricContract b = dto.getFingerPrintList().get(bioIndex);
                        b.setModel(ApplicationConstants.PBS_PASSWORD_VERSION);
                        b.setManufacturer(HashMethods.getPBSHash(dto.getPatientUUID(),
                                b.getDateCreated(),
                                b.getImageQuality(),
                                b.getSerialNumber(),
                                b.getFingerPositions().toString()
                        ));
                        dto.getFingerPrintList().set(bioIndex, b);
                    }

                       try{
                    // set values to capture
                    JSONObject jsonObject = new JSONObject();
                    Gson gson = new Gson();
                    jsonObject.put("uuid", patientUUID);
                    jsonObject.put("base", true);
                    jsonObject.put("templates", gson.toJson(dto));
                    patientObject.put("pbs", jsonObject);

                       } catch (Exception e) {
                           logResponse.appendLogs(false, e.getMessage(),"","exportBioData");
                       }
                    

                } else if (pbsVerification.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                    PatientBiometricVerificationDTO dto = new PatientBiometricVerificationDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbsVerification));
                    dto.setPatientUUID(patientUUID);
                    try{
                        // set values to capture
                        JSONObject jsonObject = new JSONObject();
                        Gson gson = new Gson();
                        jsonObject.put("uuid", patientUUID);
                        jsonObject.put("base", false);
                        jsonObject.put("templates", gson.toJson(dto));
                        patientObject.put("pbs", jsonObject);

                    } catch (Exception e) {
                        logResponse.appendLogs(false, e.getMessage(),"","exportBioData");
                    }
                } else {
                     logResponse.appendLogs(
                            false,
                            "No prints found",
                            "Report this error",
                            "PBS Export recapture"
                    );
                }
            }

  return  logResponse;
    }


}
