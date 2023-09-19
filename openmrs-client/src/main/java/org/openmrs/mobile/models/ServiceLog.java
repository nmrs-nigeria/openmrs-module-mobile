/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ServiceLog implements Serializable {


    @SerializedName("id")
    @Expose
    private long patientId;
    @SerializedName("form_name")
    @Expose
    private String formName;
    @SerializedName("puuid")
    @Expose
    private String patientUUID;
    @SerializedName("voided")
    @Expose
    private int voided;
    @SerializedName("date_created")
    @Expose
    private String dateCreated;
    @SerializedName("visit_date")
    @Expose
    private String visitDate;


    public ServiceLog(long patientId, String formName, String patientUUID, int voided, String dateCreated, String visitDate) {
        this.patientId = patientId;
        this.formName = formName;
        this.patientUUID = patientUUID;
        this.voided = voided;
        this.dateCreated = dateCreated;
        this.visitDate = visitDate;
    }

    public ServiceLog( ) {
    }
    // Getters and Setters

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getPatientUUID() {
        return patientUUID;
    }

    public void setPatientUUID(String patientUUID) {
        this.patientUUID = patientUUID;
    }

    public int getVoided() {
        return voided;
    }

    public void setVoided(int voided) {
        this.voided = voided;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

}
