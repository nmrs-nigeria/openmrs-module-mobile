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
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Table(name = "receipt_item")
public class ReceiptItem extends Model implements Serializable {
    @SerializedName("calculatedExpiration")
    @Expose
    private String calculatedExpiration;
    @SerializedName("item")
    @Expose
    private String item;
    @SerializedName("quantity")
    @Expose
    private Integer quantity;
    @SerializedName("itemBatch")
    @Expose
    private String itemBatch;
    @SerializedName("expiration")
    @Expose
    private String expiration;

    public String getCalculatedExpiration() {
        return calculatedExpiration;
    }

    public void setCalculatedExpiration(String calculatedExpiration) {
        this.calculatedExpiration = calculatedExpiration;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getItemBatch() {
        return itemBatch;
    }

    public void setItemBatch(String itemBatch) {
        this.itemBatch = itemBatch;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
}
