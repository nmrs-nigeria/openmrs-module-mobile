package org.openmrs.mobile.activities.pbsverification;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PatientBiometricDTO {

    @SerializedName("patientUUID")
    @Expose
    private String PatientUUID;

    @SerializedName("fingerPrintList")
    @Expose
    private List<PatientBiometricVerificationContract> FingerPrintList;


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
    }
}

