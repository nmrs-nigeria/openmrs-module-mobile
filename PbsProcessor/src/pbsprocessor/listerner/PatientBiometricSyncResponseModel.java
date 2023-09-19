package pbsprocessor.listerner;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PatientBiometricSyncResponseModel {
    @SerializedName("IsSuccessful")
    @Expose
    private boolean IsSuccessful;

    @SerializedName("ErrorMessage")
    @Expose
    private String ErrorMessage;

    @SerializedName("ErrorCode")
    @Expose
    private Integer ErrorCode;

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

    public Integer getErrorCode() { return ErrorCode; }
    public void setErrorCode(int errorCode) { this.ErrorCode = errorCode; }
}
