package org.openmrs.mobile.activities.pbsverification;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.openmrs.mobile.activities.pbs.PatientBiometricContract;

import java.util.ArrayList;
import java.util.List;

public class PatientBiometricVerificationDTO {

    @SerializedName("patientUUID")
    @Expose
    private String PatientUUID;

    @SerializedName("fingerPrintList")
    @Expose
    private List<PatientBiometricVerificationContract> FingerPrintList;

    public boolean isReplaceBase() {
        return isReplaceBase;
    }

    private boolean isReplaceBase=false;


    public String getPatientUUID() {
        return PatientUUID;
    }
    public void setPatientUUID(String patientUUID) {
        this.PatientUUID = patientUUID;
    }

    public List<PatientBiometricVerificationContract> getFingerPrintList() {
        return FingerPrintList;
    }
    public void setFingerPrintList(ArrayList<PatientBiometricVerificationContract> fingerPrintList)   {
        this.FingerPrintList = fingerPrintList;
        // search and see if any of the recapture have   replace base flag greater than 0 and set the DT that it is requesting to replace the base.
        for (PatientBiometricVerificationContract c:fingerPrintList){
            if(c.getReplaceBase()>0){
                this.isReplaceBase=true;
                break;
            }
        }
    }
}

