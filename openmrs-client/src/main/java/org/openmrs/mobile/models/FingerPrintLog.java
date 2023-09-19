/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.mobile.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Table(name = "fingerprint_log")
public class FingerPrintLog extends Model implements Serializable{

    @Column(name = "puuid")
    @SerializedName("puuid")
    @Expose
    private String puuid;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Column(name = "pid")
    @SerializedName("pid")
    @Expose
    private String pid;
    @Column(name = "last_capture_date")
    @SerializedName("last_capture_date")
    @Expose
    private String lastCapturedDate;

    @Column(name = "recapture_count")
    @SerializedName("recapture_count")
    @Expose
    private int recapturedCount;
    // -1 new patient
    // 0 base capture available
    // more than zero signify recapture count

    @Column(name = "replaced_count")
    @SerializedName("replaced_count")
    @Expose
    private int replaceCount=1;
    @Column(name = "time")
    @SerializedName("time")
    @Expose
    private Long time;
    public int getReplaceCount() {
        return replaceCount;
    }

    public void setReplaceCount(int replaceCount) {
        this.replaceCount = replaceCount;
    }



    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getLastCapturedDate() {
        return lastCapturedDate;
    }

    public void setLastCapturedDate(String lastCapturedDate) {
        this.lastCapturedDate = lastCapturedDate;
    }

    public int getRecapturedCount() {
        return recapturedCount;
    }

    public void setRecapturedCount(int recapturedCount) {
        this.recapturedCount = recapturedCount;
    }




}
