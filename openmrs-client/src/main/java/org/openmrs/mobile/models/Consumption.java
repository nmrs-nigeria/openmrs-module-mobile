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

import android.graphics.Bitmap;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.openmrs.mobile.utilities.ImageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Table(name = "consumption")
public class Consumption extends Model implements Serializable {

    @Column(name = "consumptionDate")
    @SerializedName("consumptionDate")
    @Expose
    private String consumptionDate;
    @Column(name = "item")
    @SerializedName("item")
    @Expose
    private String item;
    @Column(name = "department")
    @SerializedName("department")
    @Expose
    private String department;
    @Column(name = "batchNumber")
    @SerializedName("batchNumber")
    @Expose
    private String batchNumber;
    @Column(name = "testPurpose")
    @SerializedName("testPurpose")
    @Expose
    private String testPurpose;
    @Column(name = "dataSystem")
    @SerializedName("dataSystem")
    @Expose
    private String dataSystem;
    @Column(name = "name")
    @SerializedName("name")
    @Expose
    private String name;
    @Column(name = "retired")
//    @SerializedName("description")
//    @Expose
//    private String description;
    @SerializedName("retired")
    @Expose
    private String retired;
    @Column(name = "quantity")
    @SerializedName("quantity")
    @Expose
    private Integer quantity;
    @Column(name = "wastage")
    @SerializedName("wastage")
    @Expose
    private Integer wastage;

    @Column(name = "isSynced")
    private boolean isSynced;


    public String getConsumptionDate() {
        return consumptionDate;
    }

    public void setConsumptionDate(String consumptionDate) {
        this.consumptionDate = consumptionDate;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getTestPurpose() {
        return testPurpose;
    }

    public void setTestPurpose(String testPurpose) {
        this.testPurpose = testPurpose;
    }

    public String getDataSystem() {
        return dataSystem;
    }

    public void setDataSystem(String dataSystem) {
        this.dataSystem = dataSystem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }

    public String getRetired() {
        return retired;
    }

    public void setRetired(String retired) {
        this.retired = retired;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getWastage() {
        return wastage;
    }

    public void setWastage(Integer wastage) {
        this.wastage = wastage;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

}
