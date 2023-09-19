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

@Table(name="InventoryStockSummaryLab")
public class InventoryStockSummaryLab extends Model implements Serializable {

    @Column(name = "uuid")
    @SerializedName("uuid")
    @Expose
    private String uuid;

    @Column(name = "name")
    @SerializedName("name")
    @Expose
    private String name;

    @Column(name = "itemBatch")
    @SerializedName("itemBatch")
    @Expose
    private String itemBatch;

    @Column(name = "itemType")
    @SerializedName("itemType")
    @Expose
    private String itemType;

    @Column(name = "expiration")
    @SerializedName("expiration")
    @Expose
    private String expiration;

    @Column(name = "quantity")
    @SerializedName("quantity")
    @Expose
    private String quantity;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getItemBatch() {
        return itemBatch;
    }

    public void setItemBatch(String itemBatch) {
        this.itemBatch = itemBatch;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
