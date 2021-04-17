package org.openmrs.mobile.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EncounterProvider {
    @SerializedName("provider")
    @Expose
    private String provider;

    @SerializedName("encounterRole")
    @Expose
    private String encounterRole;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEncounterRole() {
        return encounterRole;
    }

    public void setEncounterRole(String encounterRole) {
        this.encounterRole = encounterRole;
    }
}
