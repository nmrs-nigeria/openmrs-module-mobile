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

@Table(name = "obscreatelocal")
public class ObscreateLocal extends Model implements Serializable {

    @SerializedName("person")
    @Expose
    private String person;
    @SerializedName("obsDatetime")
    @Expose
    private String obsDatetime;
    @SerializedName("concept")
    @Expose
    private String concept;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("encounter")
    @Expose
    private String encounter;

    @SerializedName("questionLabel")
    @Expose
    private String questionLabel;

    @SerializedName("answerLabel")
    @Expose
    private String answerLabel;

    @SerializedName("groupMembers")
    @Expose
    private List<ObsgroupLocal> groupMembers = new ArrayList<>();

    @SerializedName("repeatConcept")
    @Expose
    private String repeatConcept;


    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getObsDatetime() {
        return obsDatetime;
    }

    public void setObsDatetime(String obsDatetime) {
        this.obsDatetime = obsDatetime;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getEncounter() {
        return encounter;
    }

    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getQuestionLabel() {
        return questionLabel;
    }

    public void setQuestionLabel(String questionLabel) {
        this.questionLabel = questionLabel;
    }

    public String getAnswerLabel() {
        return answerLabel;
    }

    public void setAnswerLabel(String answerLabel) {
        this.answerLabel = answerLabel;
    }

    public List<ObsgroupLocal> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<ObsgroupLocal> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getRepeatConcept() {
        return repeatConcept;
    }

    public void setRepeatConcept(String repeatConcept) {
        this.repeatConcept = repeatConcept;
    }
}
