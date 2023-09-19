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

@Table(name="transfer")
public class Transfer extends Model implements Serializable {
    private Gson gson=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private Type itemtype = new TypeToken<List<TransferItem>>(){}.getType();

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

    @Column(name = "source")
    @SerializedName("source")
    @Expose
    private String source;

    @Column(name = "destination")
    @SerializedName("destination")
    @Expose
    private String destination;

    @Column(name = "institution")
    @SerializedName("institution")
    @Expose
    private String institution;

    @Column(name = "department")
    @SerializedName("department")
    @Expose
    private String department;

    @SerializedName("items")
    @Expose
    private List<TransferItem> items = new ArrayList<>();

    @Column(name = "items")
    private String itemsList;

    @Column(name = "status")
    @SerializedName("status")
    @Expose
    private String status;

    @Column(name = "patient")
    @SerializedName("patient")
    @Expose
    private String patient;

    @Column(name = "disposedType")
    @SerializedName("disposedType")
    @Expose
    private String disposedType;

    @Column(name = "adjustmentKind")
    @SerializedName("adjustmentKind")
    @Expose
    private String adjustmentKind;

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

    @Column(name = "attributes")
    @SerializedName("attributes")
    @Expose
    private List<String> attributes = new ArrayList<>();

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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

    public List<TransferItem> getItems() {
        return items;
    }

    public void setItems(List<TransferItem> items) {
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

    public List<String> getAttributes() {
        List<String> attributes = new ArrayList<String>();
        return attributes;
    }

    public void setAttributes(List<String> attribute) {
        this.attributes = attribute;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
