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
import java.util.List;

public class QuestionOptions implements Serializable {
    @SerializedName("control")
    @Expose
    private Control control;

    //links to the control that it depends on for dynamic value
    @SerializedName("depend")
    @Expose
    private String depend;

    @SerializedName("repeatConcept")
    @Expose
    private String repeatConcept;

    @SerializedName("rendering")
    @Expose
    private String rendering;

    @SerializedName("concept")
    @Expose
    private String concept;

    // For numeric values
    @SerializedName("max")
    @Expose
    private String max;

    // For numeric values
    @SerializedName("min")
    @Expose
    private String min;

    // For numeric values
    @SerializedName("allowDecimal")
    @Expose
    private boolean allowDecimal;

    // For select radio boxes
    @SerializedName("answers")
    @Expose
    private List<Answer> answers;

    @SerializedName("forWhichGenderName")
    @Expose
    private String forWhichGenderName;

    /**
     * 
     * @return
     *     The rendering
     */
    public String getRendering() {
        return rendering;
    }

    /**
     * 
     * @param rendering
     *     The rendering
     */
    public void setRendering(String rendering) {
        this.rendering = rendering;
    }

    /**
     * 
     * @return
     *     The concept
     */
    public String getConcept() {
        return concept;
    }

    /**
     * 
     * @param concept
     *     The concept
     */
    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public boolean isAllowDecimal() {
        return allowDecimal;
    }

    public void setAllowDecimal(boolean allowDecimal) {
        this.allowDecimal = allowDecimal;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public String getDepend() {
        return depend;
    }

    public void setDepend(String depend) {
        this.depend = depend;
    }

    public String getRepeatConcept() {
        return repeatConcept;
    }

    public void setRepeatConcept(String repeatConcept) {
        this.repeatConcept = repeatConcept;
    }

    public String getForWhichGenderName() {
        return forWhichGenderName;
    }

    public void setForWhichGenderName(String forWhichGenderName) {
        this.forWhichGenderName = forWhichGenderName;
    }
}
