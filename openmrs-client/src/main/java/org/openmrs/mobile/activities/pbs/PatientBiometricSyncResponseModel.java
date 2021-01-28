package org.openmrs.mobile.activities.pbs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PatientBiometricSyncResponseModel {
    @SerializedName("IsSuccessful")
    @Expose
    private boolean IsSuccessful;

    @SerializedName("ErrorMessage")
    @Expose
    private String ErrorMessage;

    public boolean getIsSuccessful() {
        return IsSuccessful;
    }
    public void setIsSuccessful(boolean isSuccessful) {
        this.IsSuccessful = isSuccessful;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.ErrorMessage = errorMessage;
    }
}
