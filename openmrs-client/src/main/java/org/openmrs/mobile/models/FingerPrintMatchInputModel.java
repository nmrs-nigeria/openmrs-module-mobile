package org.openmrs.mobile.models;

import org.openmrs.mobile.activities.pbs.PatientBiometricContract;

import java.util.List;

public class FingerPrintMatchInputModel {

    private String FingerPrintTemplate;

    private List<String> FingerPrintTemplates;

    private List<PatientBiometricContract> FingerPrintTemplateListToMatch;

    public FingerPrintMatchInputModel(String FingerPrintTemplate, List<PatientBiometricContract> FingerPrintTemplateListToMatch) {
        this.FingerPrintTemplate = FingerPrintTemplate;
        this.FingerPrintTemplateListToMatch = FingerPrintTemplateListToMatch;
    }

    public FingerPrintMatchInputModel( List<PatientBiometricContract> FingerPrintTemplateListToMatch,
                                       List<String> FingerPrintTemplates) {
        this.FingerPrintTemplateListToMatch = FingerPrintTemplateListToMatch;
        this.FingerPrintTemplates = FingerPrintTemplates;
    }



    public String getFingerPrintTemplate() {
        return FingerPrintTemplate;
    }

    public void setFingerPrintTemplate(String FingerPrintTemplate) {
        this.FingerPrintTemplate = FingerPrintTemplate;
    }

    public List<PatientBiometricContract> getFingerPrintTemplateListToMatch() {
        return FingerPrintTemplateListToMatch;
    }

    public void setFingerPrintTemplateListToMatch(List<PatientBiometricContract> FingerPrintTemplateListToMatch) {
        this.FingerPrintTemplateListToMatch = FingerPrintTemplateListToMatch;
    }

    public List<String> getFingerPrintTemplates() {
        return FingerPrintTemplates;
    }

    public void setFingerPrintTemplates(List<String> fingerPrintTemplates) {
        FingerPrintTemplates = fingerPrintTemplates;
    }
}
