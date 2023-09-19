package org.openmrs.mobile.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Table(name = "distribution_item")
public class DistributionItem extends Model implements Serializable {

    @Column(name = "distributionId")
    @SerializedName("distributionId")
    private long distributionId;

    @Column(name = "calculatedExpiration")
    @SerializedName("calculatedExpiration")
    @Expose
    private Boolean calculatedExpiration;

    @Column(name = "item")
    @SerializedName("item")
    @Expose
    private String item;

    @Column(name = "quantity")
    @SerializedName("quantity")
    @Expose
    private Integer quantity;

    @Column(name = "itemBatch")
    @SerializedName("itemBatch")
    @Expose
    private String itemBatch;

    @Column(name = "expiration")
    @SerializedName("expiration")
    @Expose
    private String expiration;

    @Column(name = "itemDrugType")
    @SerializedName("itemDrugType")
    @Expose
    private String itemDrugType;

    @Column(name = "isSynced")
    private boolean isSynced;

    public void setDistributionId(long distributionId){
        this.distributionId = distributionId;
    }

    public long getDistributionId(){
        return distributionId;
    }

    public Boolean getCalculatedExpiration() {
        return calculatedExpiration;
    }

    public void setCalculatedExpiration(Boolean calculatedExpiration) {
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

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public String getItemDrugType() {
        return itemDrugType;
    }

    public void setItemDrugType(String itemDrugType) {
        this.itemDrugType = itemDrugType;
    }
}
