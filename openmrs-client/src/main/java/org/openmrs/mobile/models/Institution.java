package org.openmrs.mobile.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Table(name = "institution")
public class Institution extends Model implements Serializable {

    @Column(name = "name")
    @SerializedName("name")
    @Expose
    private String name;

    @Column(name = "state")
    @SerializedName("state")
    @Expose
    private String state;

    @Column(name = "lga")
    @SerializedName("lga")
    @Expose
    private String lga;

    @Column(name = "uuid")
    @SerializedName("name")
    @Expose
    private String uuid;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLga() {
        return lga;
    }

    public void setLga(String lga) {
        this.lga = lga;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
