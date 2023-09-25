package org.openmrs.mobile.export;

import static org.openmrs.mobile.utilities.ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationDTO;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.security.HashMethods;
import org.openmrs.mobile.sync.LogResponse;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.Notifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FullExport {

    private  File openMRSFolder;
    Context context;
    FingerPrintDAO dao = new FingerPrintDAO();
    FingerPrintVerificationDAO daoVerification = new FingerPrintVerificationDAO();
    public FullExport(Context context, File openMRSFolder) {
        this.context = context;
        this.openMRSFolder=openMRSFolder;
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
    public void starExportingPatients() {
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
                updateNotification(i+1, "bio",sucess, fail, pLogResponse);
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
            LogResponse vLogResponse  = addVisits(patientObject,
                    patient, "ENC_EXPORT" + patient.getUuid() + " id" + patient.getId());
                // check if already EXPORT recapture or base
                // if UUID is null get the patient again
                LogResponse    pbsLogResponse = addPBS(patientObject,  patient ,
                        "PBS_EXPORT" + patient.getUuid() + " id" + patient.getId());
                    updateNotification(i, "pbs",sucess, fail, pbsLogResponse);
                    if(!pbsLogResponse.isSuccess()) {
                        OpenMRSCustomHandler.writeLogToFile(pbsLogResponse.getFullMessage() + "\n\n");
                    }
                //
                 if(pbsLogResponse.isSuccess()&& eLogResponse.isSuccess()
                        && pLogResponse.isSuccess()){
                    sucess++;
                    dataJson.put(patientObject);

                }else{
                    fail++;
                }
                updateNotification(i, "",sucess, fail,new LogResponse(true,
                        "","","",""));

        }
        setSyncState(false);
        updateNotification(i, "",sucess, fail,new LogResponse(true,
                "","","",""));


        if (dataJson.length() > 0) {
            try {

                JSONObject jsonExport = new JSONObject();
                jsonExport.put("global","");
                jsonExport.put("data", dataJson);
                Date date = new Date();
                // your date
                // Choose time zone in which you want to interpret your Date
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Lagos"));
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);

                Long tsLong = System.currentTimeMillis() / 1000;
                String timestamp = tsLong.toString();

                //Generate the file name for the day
                String fileName = "PBS-NMRS-" + day + "-" + month + "-" + year + "-" + timestamp + ".txt";
                File fileCreated = new File(openMRSFolder + "/" + fileName);
                FileOutputStream fileout = new FileOutputStream(fileCreated);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                outputWriter.write(jsonExport.toString());
                outputWriter.flush();
                outputWriter.close();
                //display file saved message
                Toast.makeText(context, "File saved successfully! as " + fileName, Toast.LENGTH_LONG).show();
                OpenMRSCustomHandler.writeLogToFile(new LogResponse(
                        fail < 1, "Summary", "Synced:"+sucess+"\t Failed:"+fail+"\tTotal"+size,
                        "If failed grater than one check the upper log for the reason", "Export").getFullMessage());
                // Start activity to Preview all similar patients

            } catch (Exception e) {
                e.printStackTrace();
                OpenMRSCustomHandler.writeLogToFile("Fail to export  " + e.getMessage());

            }
            //Log.d("TAG_NAME", dataJson.toString());

        } else {
            Toast.makeText(context, "There is no recent Data captured on this device. Please capture and export." , Toast.LENGTH_LONG).show();


        }




    }
    public @NonNull LogResponse addEncounters(JSONObject patientObject ,@NonNull Patient patient, String identity)  {
        LogResponse logResponse = new LogResponse(identity);
        try {
            List<Encountercreate> encountercreatelist = new Select()
                    .from(Encountercreate.class)
                    .where("patientid = ?", patient.getId())
                    .where("synced = ?", false) // case duplicate forms handle
                    .execute();


            JSONArray encounters = new JSONArray();

            for (final Encountercreate encountercreate : encountercreatelist) {
                 encountercreate.pullObslist();
                //encountercreate.setObslist();
               // encountercreate.setObslistLocal();


                JSONObject jsonObject = new JSONObject();
                   jsonObject.put( "visit",encountercreate.getVisit() );
                jsonObject.put( "patient",encountercreate.getPatient() );
                jsonObject.put( "patientid",encountercreate.getPatient() );
                jsonObject.put( "encounterType",encountercreate.getEncounterType() );
                jsonObject.put( "form",encountercreate.getFormUuid() );
                jsonObject.put( "formname",encountercreate.getFormname() );
                jsonObject.put( "obs",encountercreate.getObslist() );
                jsonObject.put( "encounterDatetime",encountercreate.getEncounterDatetime() );
                jsonObject.put( "location",encountercreate.getLocation() );
                jsonObject.put( "identifier",encountercreate.getIdentifier() );
                jsonObject.put( "identifierType",encountercreate.getIdentifier() );
                jsonObject.put( "obsLocal",encountercreate.getObslistLocal() );
                encounters.put(jsonObject);
            }

            patientObject.put("encounters", encounters);
            logResponse.appendLogs(true, "Success","","addEncounters");


        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(),"","addEncounters");
        }



        return logResponse;

    }


    protected  LogResponse addBioData(JSONObject patientObject ,Patient patient,
                                         @NonNull String identifier    ) {
        LogResponse logResponse = new LogResponse(identifier);
        try {
            Gson gson = new Gson();
           JSONObject jsonObject = new JSONObject();
           jsonObject.put("id",patient.getId());
            jsonObject.put("patientUuid",patient.getUuid());
            jsonObject.put("birthdate",patient.getPerson().getBirthdate());
            jsonObject.put("address", gson.toJson(patient.getPerson().getAddresses()));
            jsonObject.put("attribute",gson.toJson(patient.getAttribute()));

            jsonObject.put("familyName",patient.getPerson().getName().getFamilyName());
            jsonObject.put("middleName",patient.getPerson().getName().getMiddleName());

            patientObject.put("bio_data",jsonObject );
            logResponse.appendLogs(true, "Success","","exportBioData");

        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(),"","exportBioData");
        }
        return  logResponse;

    }
    protected  LogResponse addVisits(JSONObject patientObject ,Patient patient,
                                      @NonNull String identifier    ) {
        LogResponse logResponse = new LogResponse(identifier);
        List<Visit> visits = new VisitDAO().getVisitsByPatientID(patient.getId()).toBlocking().single();
        try {
            JSONArray jsonArray =new JSONArray();
            for (Visit visit : visits){
                Gson gson = new Gson();
                jsonArray.put(gson.toJson(visit));
            }
            patientObject.put("visits",jsonArray);
            logResponse.appendLogs(true, "","","addVisits");

        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(),"","addVisits");
        }
        return  logResponse;

    }
    protected LogResponse  addPBS(JSONObject patientObject, Patient patient, String identifier) {
        LogResponse logResponse =new LogResponse(identifier);

            List<PatientBiometricContract> pbs = dao.getAll(false, patient.getId().toString());
            List<PatientBiometricVerificationContract> pbsVerification = daoVerification.getAll(false, patient.getId()
                    .toString());

            if (pbs.size() ==0 && pbsVerification.size() ==0) {
                try {
                    patientObject.put("pbs", new JSONArray());
                    logResponse.appendLogs(
                            true,
                            "No fingerprints",
                            "",
                            "PBS Export");
                    return logResponse;
                }catch (Exception e){
                    return logResponse;
                }
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
                    dto.setPatientUUID(patient.getUuid());

                    
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
                    jsonObject.put("uuid", patient.getUuid());
                    jsonObject.put("base", true);
                    jsonObject.put("templates", gson.toJson(dto));
                    patientObject.put("pbs", jsonObject);
                           logResponse.appendLogs(true, "Success",""," addPBS");

                       } catch (Exception e) {
                           logResponse.appendLogs(false, e.getMessage(),""," addPBS");
                       }
                    

                } else if (pbsVerification.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                    PatientBiometricVerificationDTO dto = new PatientBiometricVerificationDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbsVerification));
                    dto.setPatientUUID(patient.getUuid());
                    try{
                        // set values to capture
                        JSONObject jsonObject = new JSONObject();
                        Gson gson = new Gson();
                        jsonObject.put("uuid", patient.getUuid());
                        jsonObject.put("base", false);
                        jsonObject.put("templates", gson.toJson(dto));
                        patientObject.put("pbs", jsonObject);
                        logResponse.appendLogs(true, "Success",""," addPBS");


                    } catch (Exception e) {
                        logResponse.appendLogs(false, e.getMessage(),""," addPBS");
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
