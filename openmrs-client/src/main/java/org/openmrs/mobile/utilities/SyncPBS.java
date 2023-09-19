package org.openmrs.mobile.utilities;

import static android.app.PendingIntent.getActivity;

import static org.openmrs.mobile.utilities.ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.activities.pbs.PatientBiometricSyncResponseModel;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationDTO;
import org.openmrs.mobile.api.FingerPrintSyncService;
import org.openmrs.mobile.api.FingerPrintVerificationSyncService;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.PatientBiometricJoinDAO;
import org.openmrs.mobile.dao.ServiceLogDAO;
import org.openmrs.mobile.listeners.retrofit.GenericResponseCallbackListener;
import org.openmrs.mobile.models.Patient;

import java.util.ArrayList;
import java.util.List;
/*
Sync PBS class Sync PBS data to the server
It required context for notification

 */
public class SyncPBS {
    boolean stop=false;
    private int  attemptPatientsCount= 0;
    private   int partiallySync= 0;
    private int alreadySync=0;
    private int syncCount=0;
    private int totalPBS=0;
    private int  countFewerPBS_Patients=0;// patient with PBS that are not enough
    private   int failCount =0;
    private Context context;
    public SyncPBS(@NonNull Context context) {
        this.context=context;
    }
    //update notification or new using same channel ID created earlier.
    // Print summary PBS data sync after after a patient.
    private void updateNotification(String errorMessage) {
        /*
        countFewerPBS_Patients disable
        Patients that finger prints are not fully captured, the patient will not be acknowledge as one for syncing
         */
        String summary=   "Syncing "+(attemptPatientsCount-countFewerPBS_Patients)+" out of "+(totalPBS-countFewerPBS_Patients)+
                " Total synced "+(alreadySync+syncCount)+
               " Failed Sync"+failCount;//+" Minimum Prints not reached "+countFewerPBS_Patients;;
        String bContent= "Syncing "+(attemptPatientsCount-countFewerPBS_Patients)+" out of "+(totalPBS-countFewerPBS_Patients)+
                "\nSynced "+syncCount+ //" Already Synced "+alreadySync+
                 " Total synced "+(alreadySync+syncCount)+
                "\nFailed Sync "+failCount;//+" Minimum Prints not reached "+countFewerPBS_Patients;

        String  content =partiallySync<1? bContent:
                bContent+" Inconsistent Upload "+partiallySync;
        if(errorMessage==null)
         Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "PBS Sync",  summary, content);
        else
            Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "PBS Sync",
                    errorMessage+"\n"+summary, errorMessage+"\n"+content);
        // sync ended
        if(!getSyncState()) {
            // complete log
             updateLog();
        }
    }
    private void updateLog() {
        // complete log
        OpenMRSCustomHandler.writeLogToFile( "Syncing "+attemptPatientsCount+" out of "+totalPBS+
                "\nSynced "+syncCount+ //" Already Synced "+alreadySync+
                " Total synced "+(alreadySync+syncCount)+
                "\nFailed Sync "+failCount+" Minimum Prints not reached "+countFewerPBS_Patients
        );
    }

    // Syncing start when PBS available
    public String  startSync() {
        //Get preference  key value of the PBS syncing status.
        if(getSyncState()) {
            return "Already syncing";
        }
        setSyncState(true);
        Notifier.notify(context, 1, Notifier.CHANNEL_SYNC_PBS, "PBS Sync", "Checking PBS data", null);
        PatientBiometricJoinDAO patientBioDAO = new PatientBiometricJoinDAO();
        List<Patient>  patients= patientBioDAO.getPatientWithPBS();
        totalPBS= patients.size();
        // OpenMRSCustomHandler.writeLogToFile(OpenMRSCustomHandler.showJson(patients,""));
        // Util.log(OpenMRSCustomHandler.showJson(patients,""));
        if(patients.size()<1) {
            updateNotification("NO data PBS data found");
        }
        boolean nextSync=true;
        for(Patient patient:patients){
            attemptPatientsCount++;
            if(stop){
               updateNotification("Manually Stop");
            }else {
                //check if patient is already sync
                if (patient.isFingerprintFullSync() || patient.isFingerprintPartiallySync()) {
                    if (patient.isFingerprintPartiallySync()) {
                        partiallySync++;
                    } else {
                        alreadySync++;// record for already sync
                    }
                } else {
                    // syncing only for patient who have UUID and their print reaches the minimum required
                    if (patient.getFingerprintCount()>= MINIMUM_REQUIRED_FINGERPRINT  ) {
                        if( ( patient.getUuid()!=null&&!patient.getUuid().trim().equals(""))) {
                            if (nextSync) {
                                nextSync = syncPatientAndContinue(patient.getId(), patient.getUuid());
                            } else {
                                break;
                            }
                        }else{
                            OpenMRSCustomHandler.writeLogToFile(OpenMRSCustomHandler.showJson(patient,""));
                        }
                    } else {
                        //few prints or no UUID on the patient
                        countFewerPBS_Patients++;
                    }
                }
            }
        }

        setSyncState(false);
        updateNotification("Completed");
        return  "Completed";
    }


//  Set the state true is syncing is completed
    private void setSyncState(boolean b) {
        SharedPreferences sharedPref =  context.getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pbs_sync", b);
        editor.apply();
    }
    // get if the PBS syncing loop is still send data
    private boolean getSyncState() {
        SharedPreferences sharedPref =  context.getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
       return  sharedPref.getBoolean("pbs_sync", false);
    }

 // Sync a patient whom ID and UUID are available. Return true for non network error
    private boolean syncPatientAndContinue(Long patientId , String patientUUID) {
        try{
            FingerPrintDAO dao = new FingerPrintDAO();
            FingerPrintVerificationDAO daoVerification = new FingerPrintVerificationDAO();
            //check UUID is valid
            if (patientUUID != null&& patientUUID!="") {
                List<PatientBiometricContract> pbs = dao.getAll(false, patientId.toString());

                List<PatientBiometricVerificationContract> pbsVerification = daoVerification.getAll(false, patientId.toString());

                if(pbs.size()<MINIMUM_REQUIRED_FINGERPRINT && pbsVerification.size()<MINIMUM_REQUIRED_FINGERPRINT) {
                    countFewerPBS_Patients++;
                    return  true ; //  continue
                }

                // Pbs capture
                if(pbs.size()>=MINIMUM_REQUIRED_FINGERPRINT){
                    PatientBiometricDTO dto = new PatientBiometricDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbs));
                    dto.setPatientUUID(patientUUID);
                    new FingerPrintSyncService().startSync(dto, new GenericResponseCallbackListener<PatientBiometricSyncResponseModel>() {
                        @Override
                        public void onResponse(PatientBiometricSyncResponseModel obj) {
                            if(obj !=null && obj.getIsSuccessful()){

                                updateNotification(obj.getErrorMessage());
                                PatientBiometricContract _temp =  pbs.get(0);
                                _temp.setSyncStatus(1);
                                _temp.setTemplate("");
                                dao.updatePatientFingerPrintSyncStatus(Long.valueOf(patientId), _temp);
                                syncCount++;
                                // setting void to one for all records that matches the the UUID
                                new ServiceLogDAO().set_patient_PBS_void(String.valueOf(patientId),patientUUID,1);
                                updateNotification(null);
                                OpenMRSCustomHandler.writeLogToFile("PBS auto capture synced successfully: " + patientId);

                            } else{
                                OpenMRSCustomHandler.writeLogToFile("PBS auto capture synced unsuccessfully: " + patientId+"  ");
                                failCount++;
                                updateNotification("An error occurred while saving prints on the server." );
                            }
                        }

                        @Override
                        public void onErrorResponse(PatientBiometricSyncResponseModel errorMessage) {

                            failCount++;
                            if(errorMessage !=null) {
                                updateNotification("An error occurred while saving prints on the server. " + errorMessage.getErrorMessage());
                                OpenMRSCustomHandler.writeLogToFile("PBS auto capture synced unsuccessfully: " + patientId+" "+errorMessage.getErrorMessage());

                            }
                            else {
                                OpenMRSCustomHandler.writeLogToFile("PBS auto capture synced unsuccessfully: " + patientId);

                                updateNotification("An error occurred while saving prints on the server.");
                            }
                        }

                        @Override
                        public void onErrorResponse(String errorMessage) {
                            failCount++;
                            updateNotification(errorMessage);
                        }
                    });
                }

                // verification
                if(pbsVerification.size()>=MINIMUM_REQUIRED_FINGERPRINT){
                    PatientBiometricVerificationDTO dto = new PatientBiometricVerificationDTO();
                    dto.setFingerPrintList(new ArrayList<>(pbsVerification));
                    dto.setPatientUUID(patientUUID);
                    new FingerPrintVerificationSyncService().startSync(dto, new GenericResponseCallbackListener<PatientBiometricSyncResponseModel>() {
                        @Override
                        public void onResponse(PatientBiometricSyncResponseModel obj) {
                            if(obj !=null && obj.getIsSuccessful()){
                                // remove for another recapture to take place
                                daoVerification.deletePrint(Long.valueOf(patientId));
                                syncCount++;
                                updateNotification(null);
                                OpenMRSCustomHandler.writeLogToFile("PBS auto recapture synced  successfully: " + patientId);
                                // setting void to one for all records that matches the the UUID
                                new ServiceLogDAO().set_patient_PBS_void(String.valueOf(patientId),patientUUID,1);
                            } else{
                                OpenMRSCustomHandler.writeLogToFile("PBS auto recapture synced unsuccessfully: " + patientId);
                                failCount++;
                                updateNotification("An error occurred while saving prints on the server." );
                            }
                        }

                        @Override
                        public void onErrorResponse(PatientBiometricSyncResponseModel errorMessage) {
                           failCount++;
                            if(errorMessage !=null) {
                                OpenMRSCustomHandler.writeLogToFile("PBS auto recapture synced unsuccessfully: " + patientId+" "+errorMessage.getErrorMessage());

                                updateNotification("An error occurred while saving prints on the server. " + errorMessage.getErrorMessage());
                            } else {
                                updateNotification("An error occurred while saving prints on the server.");
                                OpenMRSCustomHandler.writeLogToFile("PBS auto recapture synced unsuccessfully: " + patientId);

                            }
                        }

                        @Override
                        public void onErrorResponse(String errorMessage) {
                            failCount++;
                            updateNotification(errorMessage);
                        }
                    });
                }

            } else {
                updateNotification("You are offline");
                return  false;
            }
        }catch (Exception ex) {
            failCount++;
            updateNotification(ex.getMessage());
        }

        return  true;
    }
}
