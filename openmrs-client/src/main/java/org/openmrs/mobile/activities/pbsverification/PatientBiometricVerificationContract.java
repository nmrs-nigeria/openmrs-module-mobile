package org.openmrs.mobile.activities.pbsverification;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PatientBiometricVerificationContract implements Serializable {

    @SerializedName("manufacturer")
    @Expose
    private String Manufacturer;

    @SerializedName("model")
    @Expose
    private String Model;

    @SerializedName("serialNumber")
    @Expose
    private String SerialNumber;

    @SerializedName("imageWidth")
    @Expose
    private int ImageWidth;

    @SerializedName("imageHeight")
    @Expose
    private int ImageHeight;

    @SerializedName("imageDPI")
    @Expose
    private int ImageDPI;

    @SerializedName("imageQuality")
    @Expose
    private int ImageQuality;

    @SerializedName("image")
    @Expose
    private String Image;

    @SerializedName("imageByte")
    @Expose
    private byte[] ImageByte;

    @SerializedName("template")
    @Expose
    private String Template;

    @SerializedName("fingerPositions")
    @Expose
    private org.openmrs.mobile.activities.pbs.FingerPositions FingerPositions;

    @SerializedName("patienId")
    @Expose
    private int PatienId;


    @SerializedName("dateCreated")
    @Expose
    private String dateCreated;

    @SerializedName("biometricInfo_Id")
    @Expose
    public String BiometricInfo_Id;

    @SerializedName("creator")
    @Expose
    private Integer Creator;

    @SerializedName("errorMessage")
    @Expose
    private String ErrorMessage;

    @SerializedName("syncStatus")
    @Expose
    private int SyncStatus;


    public String getManufacturer() {
        return Manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.Manufacturer = manufacturer;
    }

    public String getModel() {
        return Model;
    }
    public void setModel(String model) { this.Model = model;  }
    public String getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(String dateCreated) {
        this.dateCreated=dateCreated;
    }
    public String getSerialNumber() {
        return SerialNumber;
    }
    public void setSerialNumber(String serialNumber) { this.SerialNumber = serialNumber;  }

    public int getImageWidth() {
        return ImageWidth;
    }
    public void setImageWidth(int imageWidth) { this.ImageWidth = imageWidth ;  }

    public int getImageHeight() {
        return ImageHeight;
    }
    public void setImageHeight(int imageHeight) { this.ImageHeight = imageHeight ;  }

    public int getImageDPI() {
        return ImageDPI;
    }
    public void setImageDPI(int imageDPI) { this.ImageDPI = imageDPI ;  }

    public int getImageQuality() {
        return ImageQuality;
    }
    public void setImageQuality(int imageQuality) { this.ImageQuality = imageQuality ;  }

    public String getImage() {
        return Image;
    }
    public void setImage(String image) { this.Image = image;  }

    public byte[] getImageByte() {
        return ImageByte;
    }
    public void setImageByte(byte[] imageByte) { this.ImageByte = imageByte;  }

    public String getTemplate() {
        return Template;
    }
    public void setTemplate(String template) { this.Template = template;  }

    public org.openmrs.mobile.activities.pbs.FingerPositions getFingerPositions() {
        return FingerPositions;
    }
    public void setFingerPositions(org.openmrs.mobile.activities.pbs.FingerPositions fingerPositions) { this.FingerPositions = fingerPositions;  }

    public int getPatienId() {
        return PatienId;
    }
    public void setPatienId(int patienId) { this.PatienId = patienId;  }

    public String getBiometricInfo_Id() {   return BiometricInfo_Id;  }
    public void setBiometricInfo_Id(String biometricInfo_Id) { this.BiometricInfo_Id = biometricInfo_Id;  }

    public Integer getCreator() {
        return Creator;
    }
    public void setCreator(Integer creator) { this.Creator = creator;  }

    public String getErrorMessage() {
        return ErrorMessage;
    }
    public void setErrorMessage(String errorMessage) { this.ErrorMessage = errorMessage;  }

    //
    public int getSyncStatus() {
        return SyncStatus;
    }
    public void setSyncStatus(int syncStatus) { this.SyncStatus = syncStatus;  }
}



