package org.openmrs.mobile.api.response;

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;
import com.google.gson.reflect.TypeToken;

import org.openmrs.mobile.models.ReceiptItem;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

@Table(name = "receipt")
public class ReceiptResponse extends Model implements Serializable {
    private Gson gson=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private Type itemtype = new TypeToken<List<ReceiptItem>>(){}.getType();

    @Column(name = "operationNumber")
    @SerializedName("operationNumber")
    @Expose
    private String operationNumber;

    @Column(name = "instanceType")
    @SerializedName("instanceType")
    @Expose
    public List<String> instanceType= new ArrayList<>();


    @Column(name = "operationDate")
    @SerializedName("operationDate")
    @Expose
    private String operationDate;

    @Column(name = "destination")
    @SerializedName("destination")
    @Expose
    private String destination;

    @Column(name = "institution")
    @SerializedName("institution")
    @Expose
    protected String institution;

    @Column(name = "department")
    @SerializedName("department")
    @Expose
    protected String department;

    @Column(name = "patient")
    @SerializedName("patient")
    @Expose
    protected String patient;

    @Column(name = "disposedType")
    @SerializedName("disposedType")
    @Expose
    protected String disposedType;

    @Column(name = "adjustmentKind")
    @SerializedName("adjustmentKind")
    @Expose
    protected String adjustmentKind;

    @SerializedName("attributes")
    @Expose
    protected List<String> attributes = new ArrayList<>();

    @Column(name = "attributes")
    protected String attributesList;

    @SerializedName("items")
    @Expose
    private List<ReceiptItem> items = new ArrayList<>();

    @Column(name = "items")
    private String itemsList;

    @Column(name = "status")
    @SerializedName("status")
    @Expose
    private String status;
    @Column(name = "commoditySource")
    @SerializedName("commoditySource")
    @Expose
    private String commoditySource;
    @Column(name = "commodityType")
    @SerializedName("commodityType")
    @Expose
    private String commodityType;

    @Column(name = "dataSystem")
    @SerializedName("dataSystem")
    @Expose
    private String dataSystem;

    @Column(name = "isSynced")
    private boolean isSynced;

    public Type getItemtype() {
        return itemtype;
    }

    public void setItemtype(Type itemtype) {
        this.itemtype = itemtype;
    }

    public String getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(String operationNumber) {
        this.operationNumber = operationNumber;
    }

    public List<String> getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(List<String> instanceType) {
         instanceType = new ArrayList<String>();
    }

    public String getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(String operationDate) {
        this.operationDate = operationDate;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<ReceiptItem> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItem> items) {
        this.items = items;
    }

    public void pullItemsList() {
        this.items = gson.fromJson(this.itemsList,itemtype);
    }

    public void setItemsList() {
        this.itemsList = gson.toJson(items,itemtype);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommoditySource() {
        return commoditySource;
    }

    public void setCommoditySource(String commoditySource) {
        this.commoditySource = commoditySource;
    }

    public String getCommodityType() {
        return commodityType;
    }

    public void setCommodityType(String commodityType) {
        this.commodityType = commodityType;
    }

    public String getDataSystem() {
        return dataSystem;
    }

    public void setDataSystem(String dataSystem) {
        this.dataSystem = dataSystem;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getDisposedType() {
        return disposedType;
    }

    public void setDisposedType(String disposedType) {
        this.disposedType = disposedType;
    }

    public String getAdjustmentKind() {
        return adjustmentKind;
    }

    public void setAdjustmentKind(String adjustmentKind) {
        this.adjustmentKind = adjustmentKind;
    }

    public List<String> getAttributes() {
        List<String> attributes = new ArrayList<String>();
        return attributes;
    }

    public void setAttributes(List<String> attribute) {
        this.attributes = attribute;
    }
}

