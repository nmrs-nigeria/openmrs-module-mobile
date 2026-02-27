package org.openmrs.mobile.export;

import static org.openmrs.mobile.utilities.ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT;
import static org.openmrs.mobile.utilities.FormService.getFormResourceByName;

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
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.LocationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.EncounterProvider;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.IdentifierType;
import org.openmrs.mobile.models.Location;
import org.openmrs.mobile.models.Observation;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientDto;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.ProgramEnrollment;
import org.openmrs.mobile.models.Resource;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.security.HashMethods;
import org.openmrs.mobile.sync.EncounterSync;
import org.openmrs.mobile.sync.LogResponse;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.Notifier;
import org.openmrs.mobile.utilities.ObservationDeserializer;
import org.openmrs.mobile.utilities.ResourceSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;

public class FullExport {

    private File openMRSFolder;
    Context context;
    FingerPrintDAO dao = new FingerPrintDAO();
    FingerPrintVerificationDAO daoVerification = new FingerPrintVerificationDAO();
    private Gson myGson;

    public FullExport(Context context, File openMRSFolder) {
        this.context = context;
        this.openMRSFolder = openMRSFolder;

        myGson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Resource.class, new ResourceSerializer())
                .registerTypeHierarchyAdapter(Observation.class, new ObservationDeserializer())
                .create();
    }

    private void setSyncState(boolean b) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("EXPORT",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pbs_Export", b);
        editor.apply();
    }

    // get if the PBS Exporting loop is still send data
    private boolean getSyncState() {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("EXPORT",
                Activity.MODE_PRIVATE);
        return sharedPref.getBoolean("pbs_Export", false);
    }

    // variable to determine if the patient comparison is done offline or online

    private int size;

    private void updateNotification(int index, String sub,
                                    int success, int fail, LogResponse logResponse) {

        String summary = "Exporting " + index + " out of " + size +
                ". Succeeded: " + success +
                (fail == 0 ? "" : ". Failed: " + fail);
        String bContent = summary +
                (getSyncState() ? "\tRunning" : "\tCompleted     ")
                + (logResponse.isSuccess() ? "" : "\tMessage " + logResponse.getMessage());

        if (logResponse.isSuccess())
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS,
                    "NMRS Exporting", summary, bContent);
        else
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "NMRS Exporting",
                    bContent, bContent);

    }
    /*
    Start of Exporting. A background service with Context will initialize and call this function
     */



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


    // methode to syn all patient  EXPORT all the patient available
    public void starExportingPatients() {
        OpenMRS openMrs = OpenMRS.getInstance();
        Notifier.cancel(context, 1);
        Notifier.notify(context, 1, Notifier.CHANNEL_EXPORT, "NMRS Export",
                "Checking patient", null);
        PatientDAO patientDAO = new PatientDAO();
        List<Patient> patientList =  getPatientsWithUpdatedData(patientDAO.getAllPatientsLocal());

        // hold patient that should not EXPORT base on it already existing


        setSyncState(true);
        int i = 0;
        int sucess = 0;
        int fail = 0;
        size = patientList.size();
        JSONArray dataJson = new JSONArray();
        for (Patient patient : patientList) {
            i++;
            JSONObject patientObject = new JSONObject();
            // EXPORT patient
            LogResponse pLogResponse = addBioData(patientObject, patient, "BIO_EXPORT" + patient.getUuid() + " id" + patient.getId());
            updateNotification(i + 1, "bio", sucess, fail, pLogResponse);
            if (!pLogResponse.isSuccess()) {
                OpenMRSCustomHandler.writeLogToFile(pLogResponse.getFullMessage());
            }
            // Encounter EXPORT
            LogResponse eLogResponse = addEncounters(patientObject,
                    patient, "ENC_EXPORT" + patient.getUuid() + " id" + patient.getId());
            updateNotification(i, "eco", sucess, fail, eLogResponse);
            if (!eLogResponse.isSuccess()) {
                OpenMRSCustomHandler.writeLogToFile(eLogResponse.getFullMessage());
            }

            // check if already EXPORT recapture or base
            // if UUID is null get the patient again
            LogResponse pbsLogResponse = addPBS(patientObject, patient,
                    "PBS_EXPORT" + patient.getUuid() + " id" + patient.getId());
            updateNotification(i, "pbs", sucess, fail, pbsLogResponse);
            if (!pbsLogResponse.isSuccess()) {
                OpenMRSCustomHandler.writeLogToFile(pbsLogResponse.getFullMessage() + "\n\n");
            }

            //
            if (pbsLogResponse.isSuccess() && eLogResponse.isSuccess()
                    && pLogResponse.isSuccess()) {
                sucess++;
                dataJson.put(patientObject);

            } else {
                fail++;
            }
            updateNotification(i, "", sucess, fail, new LogResponse(true,
                    "", "", "", ""));

        }
        setSyncState(false);
        updateNotification(i, "", sucess, fail, new LogResponse(true,
                "", "", "", ""));


        if (dataJson.length() > 0) {
            try {

                JSONObject jsonExport = new JSONObject();
                JSONObject global = new JSONObject();
                // add location base params

                Location location = new LocationDAO().findLocationByName(OpenMRS.getInstance().getLocation());
                global.put("exportedTime", System.currentTimeMillis());
                global.put("location", openMrs.getLocation());
                global.put("locationId",location.getId());
                global.put("locationUuid",location.getUuid());
                global.put("locationName", location.getName());
                global.put("locationDescription", location.getDescription());
                global.put("locationDisplay", location.getDisplay());
                global.put("locationParentUuid", location.getParentLocationUuid());

                // bind the params
                jsonExport.put("global", global);
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
//                Util.log("Is folder "+openMRSFolder.isDirectory());
//                Util.log("  folder "+openMRSFolder.getAbsolutePath());
//                Util.log("Is file "+openMRSFolder.isFile());
                File fileCreated = new File(openMRSFolder + "/" + fileName);
//                Util.log("Is folder fileCreated "+fileCreated.isDirectory());
//                Util.log("  folder fileCreated "+fileCreated.getAbsolutePath());
//                Util.log("Is file fileCreated "+fileCreated.isFile());
                FileOutputStream fileout = new FileOutputStream(fileCreated);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                outputWriter.write(jsonExport.toString());
                outputWriter.flush();
                outputWriter.close();
                //display file saved message
                Toast.makeText(context, "File saved successfully! as " + fileName, Toast.LENGTH_LONG).show();
                OpenMRSCustomHandler.writeLogToFile(new LogResponse(
                        fail < 1, "Summary", "Exported: " + sucess + "\t Failed: " + fail + "\tTotal: " + size,
                        "If failed grater than one check the upper log for the reason", "Export").getFullMessage());
                // Start activity to Preview all similar patients

            } catch (Exception e) {
                e.printStackTrace();
                OpenMRSCustomHandler.writeLogToFile("Fail to export  " + e.getMessage());

            }
            //Log.d("TAG_NAME", dataJson.toString());

        } else {
            Toast.makeText(context, "There is no recent Data captured on this device. Please capture and export.", Toast.LENGTH_LONG).show();


        }


    }

    public @NonNull LogResponse addEncounters(JSONObject patientObject, @NonNull Patient patient, String identity) {
        LogResponse logResponse = new LogResponse(identity);
        try {
            List<Encountercreate> encountercreatelist = new Select()
                    .from(Encountercreate.class)
                    .where("patientid = ?", patient.getId())
                    .where("synced = ?", false) // case duplicate forms handle
                    .execute();
            JSONArray encountersArray = new JSONArray();

            for (final Encountercreate encountercreate : encountercreatelist) {
                try {
                    JSONObject encounterJSON = new JSONObject();
                    encountercreate.pullObslist();
                    encountercreate.setFormUuid(getFormResourceByName(encountercreate.getFormname()).getUuid());
                    // Call<Encounter> call = apiService.createEncounter(encountercreate);
                    encounterJSON.put("createEncounter", myGson.toJson(encountercreate));
                    if (!encountercreate.getSynced() ) {
                        List<EncounterProvider> encounterProviders = new ArrayList<>();
                        EncounterProvider encounterProvider = new EncounterProvider();
                        encounterProvider.setProvider("f9badd80-ab76-11e2-9e96-0800200c9a66");
                        encounterProvider.setEncounterRole("a0b03050-c99b-11e0-9572-0800200c9a66");
                        encounterProviders.add(encounterProvider);
                        encountercreate.setEncounterProviders(encounterProviders);
                        encounterJSON.put("formName", encountercreate.getFormname());

                        Long visitID = new VisitDAO().getVisitsIDByUUID(encountercreate.getVisit()).toBlocking().single();
                        if (visitID != 0) {
                            Visit visit = new VisitDAO().getVisitByIDLocally(visitID);
                            if (visit != null) {
                                encounterJSON.put("visitUuid", visit.getUuid());
                                encounterJSON.put("visitStartDateTime",visit.getStartDatetime());
                                encounterJSON.put("visitStopDateTime",visit.getStopDatetime());
                            }
                        }
                        encountersArray.put(encounterJSON);
                    }
                } catch (Exception e) {

                }

            }

            patientObject.put("encounters", encountersArray);
            logResponse.appendLogs(true, "Success", "", "addEncounters");


        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(), "", "addEncounters");
        }


        return logResponse;

    }


    protected LogResponse addBioData(JSONObject patientObject, Patient patient,
                                     @NonNull String identifier) {
        LogResponse logResponse = new LogResponse(identifier);
        try {
            patientObject.put("puuid", patient.getUuid());
            PatientDto patientDto = patient.getPatientDto();
            patientObject.put("patientDTO", myGson.toJson(patientDto, PatientDto.class));
            logResponse.appendLogs(true, "Success", "", "exportBioData");

        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(), "", "exportBioData");
        }
        return logResponse;

    }

    protected LogResponse addPBS(JSONObject patientObject, Patient patient, String identifier) {
        LogResponse logResponse = new LogResponse(identifier);
        try {
            JSONObject jsonObject = new JSONObject();
            //set pbs object init
            jsonObject.put("dataAvailable", false);
            ;
            patientObject.put("pbs", jsonObject);
            List<PatientBiometricContract> pbs = dao.getAll(false, patient.getId().toString());
            List<PatientBiometricVerificationContract> pbsVerification = daoVerification.getAll(false, patient.getId()
                    .toString());

            if (pbs.size() == 0 && pbsVerification.size() == 0) {
                logResponse.appendLogs(
                        true,
                        "No fingerprints",
                        "",
                        "PBS Export");
                return logResponse;

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
                logResponse.appendLogs(false,
                        "Minimum prints not reached", "Capture more prints, do a recapture", "PBS Export"
                );
                return logResponse;

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


                    // set values to capture
                    jsonObject = new JSONObject();
                    Gson gson = new Gson();
                    jsonObject.put("dataAvailable", true);
                    jsonObject.put("uuid", patient.getUuid());
                    jsonObject.put("base", true);
                    jsonObject.put("templates", gson.toJson(dto));
                    patientObject.put("pbs", jsonObject);
                    logResponse.appendLogs(true, "Success", "", " addPBS");


                } else if (pbsVerification.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                    PatientBiometricVerificationDTO dto = new PatientBiometricVerificationDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbsVerification));
                    dto.setPatientUUID(patient.getUuid());
                    // set values to capture
                    jsonObject = new JSONObject();
                    Gson gson = new Gson();
                    jsonObject.put("dataAvailable", true);
                    jsonObject.put("uuid", patient.getUuid());
                    jsonObject.put("base", false);
                    jsonObject.put("templates", gson.toJson(dto));
                    patientObject.put("pbs", jsonObject);
                    logResponse.appendLogs(true, "Success", "", " addPBS");


                } else {
                    logResponse.appendLogs(
                            false,
                            "No prints found",
                            "Report this error",
                            "PBS Export recapture"
                    );
                }
            }
        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(), "", " addPBS");
        }
        return logResponse;
    }

}
