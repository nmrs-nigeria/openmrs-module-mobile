package org.openmrs.mobile.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Control implements Serializable {

    @SerializedName("conditionOptions")
    @Expose
private List<Condition> conditionOptions;


    public List<Condition> getConditionOptions() {
        return conditionOptions;
    }

    public void setConditionOptions(List<Condition> conditionOptions) {
        this.conditionOptions = conditionOptions;
    }
}
