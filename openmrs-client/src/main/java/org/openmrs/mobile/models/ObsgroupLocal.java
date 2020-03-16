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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ObsgroupLocal implements Serializable {

    @SerializedName("concept")
    @Expose
    private String concept;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("questionLabel")
    @Expose
    private String questionLabel;

    @SerializedName("answerLabel")
    @Expose
    private String answerLabel;

    @SerializedName("repeatConcept")
    @Expose
    private String repeatConcept;



    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
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

    public String getRepeatConcept() {
        return repeatConcept;
    }

    public void setRepeatConcept(String repeatConcept) {
        this.repeatConcept = repeatConcept;
    }
}
