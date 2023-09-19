package org.openmrs.mobile.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Table(name = "destination")
public class Destination extends Model implements Serializable {

    @Column(name = "name")
    @SerializedName("name")
    @Expose
    public String name;

    @Column(name = "uuid")
    @SerializedName("uuid")
    @Expose
    public String uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}