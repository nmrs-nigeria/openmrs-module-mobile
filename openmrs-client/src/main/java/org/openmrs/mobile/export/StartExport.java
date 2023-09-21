package org.openmrs.mobile.export;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.mobile.activities.matchingpatients.MatchingPatientsActivity;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.sync.LogResponse;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.Notifier;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class StartExport {

    private RestApi restApi;
    Context context;

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
                PatientExport patientSync = new PatientExport();
            LogResponse pLogResponse = patientSync.exportBioData(patientObject, patient,"BIO_EXPORT" + patient.getUuid() + " id" + patient.getId() );

                updateNotification(i, "bio",sucess, fail, pLogResponse);
                if(!pLogResponse.isSuccess()) {
                    OpenMRSCustomHandler.writeLogToFile(pLogResponse.getFullMessage());
                }
                // Encounter EXPORT
                LogResponse eLogResponse =  EncounterExport().startExport(patientObject, patient, "ENC_EXPORT" + patient.getUuid() + " id" + patient.getId());
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


}
