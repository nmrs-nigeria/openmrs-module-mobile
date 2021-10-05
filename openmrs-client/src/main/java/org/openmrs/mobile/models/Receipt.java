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

@Table(name = "receipt")
public class Receipt extends Model implements Serializable {
    private Gson gson=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private Type itemtype = new TypeToken<List<ReceiptItem>>(){}.getType();

    @Column(name = "operationNumber")
    @SerializedName("operationNumber")
    @Expose
    private String operationNumber;
    @Column(name = "instanceType")
    @SerializedName("instanceType")
    @Expose
    private String instanceType;
    @Column(name = "operationDate")
    @SerializedName("operationDate")
    @Expose
    private String operationDate;
    @Column(name = "destination")
    @SerializedName("destination")
    @Expose
    private String destination;

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

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
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
}
