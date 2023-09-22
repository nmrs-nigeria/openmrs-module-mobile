package org.openmrs.mobile.sync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.openmrs.mobile.activities.matchingpatients.MatchingPatientsActivity;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.response.PbsServerContract;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.Notifier;
import org.openmrs.mobile.utilities.PatientAndMatchesWrapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class StartNewSync {

    private RestApi restApi;
    Context context;

    public StartNewSync(Context context) {
        this.context = context;
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

    // variable to determine if the patient comparison is done offline or online
    boolean calculatedLocally;
  private int size;
    private void updateNotification(int index, String sub,
                                    int success, int fail, LogResponse logResponse) {

        String summary=   "Syncing "+ index +" out of "+ size+
                ". Succeeded: "+  success+
             ( fail==0?"":  ". Failed: "+fail);
        String bContent=  summary+
                 (getSyncState()?"\tRunning":"\tCompleted     "   )
                +(   logResponse.isSuccess()?"":"\tMessage "+ logResponse.getMessage())
             ;

        if(logResponse.isSuccess())
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS,
                    "NMRS syncing",  summary, bContent);
        else
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "NMRS syncing",
                    bContent, bContent);

    }
    /*
    Start of syncing. A background service with Context will initialize and call this function
     */
    public void runSyncAwait() {
        Notifier.cancel(context, 1);
        Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "NMRS Sync",
                "Checking patient", null  );

        //check server biometric service is on available and put to log
        LogResponse serverResponse = new LogResponse("SERVER STATUS");

        if (NetworkUtils.isOnline()) {
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

            // this method is called even if the biometric service is not running the other detailed to sync
            if (!getSyncState()) {
                starSyncingPatients();
            } else {
                updateNotification(0,"",0,0,
                        new LogResponse(false,
                                "SYNC", "Sync already running. If this persist, click logout to see the dialog to resolve the issue " +
                                "                                        Avoid frequent btn pressing.  ",
                                "",
                                "SYNC "));
                OpenMRSCustomHandler.writeLogToFile(new LogResponse(false,
                        "SYNC", "Sync already running",
                        "if this persist, click logout to see the dialog to resolve the issue" +
                        " Avoid frequent btn pressing. ",
                        "SYNC ").getFullMessage());
            }
        } else {
            OpenMRSCustomHandler.writeLogToFile("No Network, syncing stopped");
            //
            setSyncState(false);
        }
    }


    // methode to syn all patient  sync all the patient available
    private void starSyncingPatients() {
        PatientDAO patientDAO = new PatientDAO();
        List<Patient> patientList = patientDAO.getAllPatientsLocal();
        // hold patient that should not sync base on it already existing
        PatientAndMatchesWrapper patientAndMatchesWrapper = new PatientAndMatchesWrapper();
        Pbs pbs = new Pbs(restApi);
        setSyncState(true);
        int i= 0;
        int success =0;
        int fail=0;
        size=patientList.size();
        for (Patient patient : patientList) {
            if (NetworkUtils.isOnline()) {
                i++;
                // sync patient
                PatientSync patientSync = new PatientSync(restApi);
                calculatedLocally = patientSync.syncPatient("BIO_" + patient.getUuid() + " id" + patient.getId(), patient, patientAndMatchesWrapper);
                LogResponse pLogResponse = patientSync.getSyncResponse();

                updateNotification(i, "bio",success, fail, pLogResponse);
                if(!pLogResponse.isSuccess()) {
                    OpenMRSCustomHandler.writeLogToFile(pLogResponse.getFullMessage());
                }
                // Encounter sync
                LogResponse eLogResponse = new EncounterSync().startSync(patient, "ENC_" + patient.getUuid() + " id" + patient.getId());
                updateNotification(i, "eco",success, fail, eLogResponse);
                if(!eLogResponse.isSuccess()) {
                    OpenMRSCustomHandler.writeLogToFile(eLogResponse.getFullMessage());
                }
                // check if already sync recapture or base
                // if UUID is null get the patient again
                LogResponse pbsLogResponse;
                if (patient.getUuid().trim().isEmpty()) {
                    Patient newPatient = patientDAO.findPatientByID(String.valueOf(patient.getId()));
                     pbsLogResponse = pbs.syncPBSAwait(newPatient.getUuid(),  newPatient.getId(), "PBS_" + newPatient.getUuid() + " id" + patient.getId());

                    updateNotification(i, "pbs",success, fail, pbsLogResponse);
                    if(!pbsLogResponse.isSuccess()) {
                        OpenMRSCustomHandler.writeLogToFile(pbsLogResponse.getFullMessage() + "\n\n");
                    }
                } else {
                    pbsLogResponse = pbs.syncPBSAwait(patient.getUuid(),  patient.getId(), "PBS_" + patient.getUuid() + " id" + patient.getId());
                    updateNotification(i, "pbs",success, fail, pbsLogResponse);
                    if(!pbsLogResponse.isSuccess()) {
                        OpenMRSCustomHandler.writeLogToFile(pbsLogResponse.getFullMessage() + "\n\n");
                    }

                }
                //
                 if(pbsLogResponse.isSuccess() && eLogResponse.isSuccess()
                      //  && pLogResponse.isSuccess()
                 ){
                    success++;

                }else{
                    fail++;
                }
                updateNotification(i, "",success, fail,new LogResponse(true,
                        "","","",""));
            } else {
                OpenMRSCustomHandler.writeLogToFile("No Network");
                fail++;
                break;
            }
        }
        setSyncState(false);
        updateNotification(i, "",success, fail,new LogResponse(true,
                "","","",""));

        OpenMRSCustomHandler.writeLogToFile(new LogResponse(
                fail < 1, "Summary", "Synced:"+success+"\t Failed:"+fail+"\tTotal"+size,
                "If failed grater than one check the upper log for the reason", "Export").getFullMessage());
        // Start activity to Preview all similar patients
        if (!patientAndMatchesWrapper.getMatchingPatients().isEmpty()) {
            Intent intent1 = new Intent(context.getApplicationContext(), MatchingPatientsActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra(ApplicationConstants.BundleKeys.CALCULATED_LOCALLY, calculatedLocally);
            intent1.putExtra(ApplicationConstants.BundleKeys.PATIENTS_AND_MATCHES, patientAndMatchesWrapper);
            context.startActivity(intent1);
        }
    }


}
