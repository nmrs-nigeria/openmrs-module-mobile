package org.openmrs.mobile.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Condition {
    @SerializedName("when")
    @Expose
    private String when;

    @SerializedName("displayType")
    @Expose
    private String displayType;

    @SerializedName("childControl")
    @Expose
    private String childControl;

    @SerializedName("controlType")
    @Expose
    private String controlType;

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public String getChildControl() {
        return childControl;
    }

    public void setChildControl(String childControl) {
        this.childControl = childControl;
    }

    public String getControlType() {
        return controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }
}
