package org.openmrs.mobile.activities.formprogramlist;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.FormResource;
import org.openmrs.mobile.utilities.FormService;
import org.openmrs.mobile.utilities.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FormProgramPresenter extends BasePresenter implements FormProgramContract.Presenter {
    private static String[] formsStringArray = null;

    private FormProgramContract.View view;
    private Long patientId;
    private String programName;
    private List<FormResource> formResourceList;
    private EncounterDAO encounterDAO;
    private boolean isEligible;
    private boolean isEnrolled;
    private boolean isFirstTime;
    private boolean isCompleted;
    private boolean isPositive = false;
    private boolean isAncPositive = false;
    private boolean isClientExist = false;
    private boolean isAncExist = false;
    private boolean isPmtctHtsExist = false;


    public FormProgramPresenter(FormProgramContract.View view, long patientId, String programName) {
        this.view = view;
        this.view.setPresenter(this);
        this.patientId = patientId;
        this.programName = programName;
        this.encounterDAO = new EncounterDAO();
    }

    public FormProgramPresenter(FormProgramContract.View view, long patientId, EncounterDAO encounterDAO) {
        this.view = view;
        this.view.setPresenter(this);
        this.patientId = patientId;
        this.encounterDAO = encounterDAO;
    }

    @Override
    public void subscribe() {
        loadFormResourceList();
        view.bindDrawableResources();
    }

    @Override
    public void loadFormResourceList() {
        formResourceList = new ArrayList<>();
        List<String> formName = new ArrayList<>();
        List<FormResource> allFormResourcesList = FormService.getFormResourceList();
        for (FormResource formResource : allFormResourcesList) {
            List<FormResource> valueRef = formResource.getResourceList();
            String valueRefString = null;

            for (FormResource resource : valueRef) {
                if (resource.getName().equals("json")) {
                    valueRefString = resource.getValueReference();
                }
            }
            // Get the forms (REMEMBER to set default if there is issue - get reference from the old)
            if (!StringUtils.isBlank(valueRefString)) {
                formResourceList.add(formResource);
            }

        }

        int size = formResourceList.size();
        formsStringArray = new String[size];
        for (int i = 0; i < size; i++) {
            formsStringArray[i] = formResourceList.get(i).getName();
            formName.add(formResourceList.get(i).getName());
        }

        List<Encountercreate> encountercreateListAdult = new VisitDAO().getLocalEncounterByPatientIDStr(this.patientId,"Risk Assessment Pediatric");
        List<Encountercreate> encountercreateListChild = new VisitDAO().getLocalEncounterByPatientIDStr(this.patientId,"Risk Stratification Adult");
        if(encountercreateListAdult.isEmpty() && encountercreateListChild.isEmpty()){
            this.isFirstTime = true;
        }else{
            this.isFirstTime = false;
        }

        List<Encountercreate> encountercreateListEligibleChild = new VisitDAO().getLocalEncounterByPatientIDEligible(this.patientId, "Risk Assessment Pediatric","Yes");
        List<Encountercreate> encountercreateListEligibleAdult = new VisitDAO().getLocalEncounterByPatientIDEligible(this.patientId, "Risk Stratification Adult","Yes");

        if(encountercreateListEligibleChild.isEmpty() && encountercreateListEligibleAdult.isEmpty()){
            isEligible = false;
        }else{
            isEligible = true;
        }

//        if(encountercreateListEligibleAdult.isEmpty()){
//            isEligible = false;
//        }else{
//            isEligible = true;
//        }

        List<Encountercreate> encountercreateAncList = new VisitDAO().getLocalEncounterByPatientIDEligible(this.patientId, "PMTCT HTS Register","Yes");
        if(encountercreateAncList.isEmpty()){
            isAncPositive = false;
        }else{
            isAncPositive = true;
        }

        List<Encountercreate> encountercreateList = new VisitDAO().getLocalEncounterByPatientIDEligible(this.patientId, "Client intake form","Yes");
        if(encountercreateList.isEmpty()){
            isPositive = false;
        }else{
            isPositive = true;
        }

        List<Encountercreate> encountercreateListClientExist = new VisitDAO().getLocalEncounterByPatientIDStr(this.patientId, "Client intake form");
        if(encountercreateListClientExist.isEmpty()){
            isClientExist = false;
        }else{
            isClientExist = true;
        }

        List<Encountercreate> encountercreateListEnrolled = new VisitDAO().getLocalEncounterByPatientIDStr(this.patientId, "HIV Enrollment");
        if(encountercreateListEnrolled.isEmpty()){
            isEnrolled = false;
        }else{
            isEnrolled = true;
        }

        List<Encountercreate> encountercreateListCompleted = new VisitDAO().getLocalEncounterByPatientIDStr(this.patientId, "Pharmacy Order Form");
        if(encountercreateListCompleted.isEmpty()){
            isCompleted = false;
        }else{
            isCompleted = true;
        }

        List<Encountercreate> encountercreateGenCareA= new VisitDAO().getLocalEncounterByPatientIDStr(this.patientId,"General Antenatal Care");
        if(encountercreateGenCareA.isEmpty()){
            isAncExist = false;
        }else{
            isAncExist = true;
        }
        List<Encountercreate> encountercreatePmtctHts= new VisitDAO().getLocalEncounterByPatientIDStr(this.patientId,"PMTCT HTS Register");
        if(encountercreatePmtctHts.isEmpty()){
            isPmtctHtsExist = false;
        }else{
            isPmtctHtsExist = true;
        }
        view.showFormList(formsStringArray,programName,formName,isEligible,isEnrolled,isFirstTime,isCompleted,isPositive,isClientExist, isAncExist,isPmtctHtsExist, isAncPositive);
    }

    @Override
    public void listItemClicked(int position, String formName) {
        List<FormResource> valueRef = formResourceList.get(position).getResourceList();
        String valueRefString = null;
        for (FormResource resource : valueRef) {
            if (resource.getName().equals("json"))
                valueRefString = resource.getValueReference();
        }
        EncounterType encType = new EncounterType();
        String e_type = formsStringArray[position];
        if (e_type.equals("Risk Assessment Pediatric")) {
            encType = encounterDAO.getEncounterTypeByFormName("Risk Stratification Pediatrics");
        }else{
            encType = encounterDAO.getEncounterTypeByFormName(formsStringArray[position]);
        }
        if (encType != null) {
            String encounterType = encType.getUuid();
            view.startFormDisplayActivity(formName, patientId, valueRefString, encounterType);
        } else {
            view.showError("There is no encounter type called " + formName);
        }
    }

    public boolean isEligible() {
        return isEligible;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }

    public boolean isEnrolled() {
        return isEnrolled;
    }

    public void setEnrolled(boolean enrolled) {
        isEnrolled = enrolled;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isAncExist() {
        return isAncExist;
    }

    public void setAncExist(boolean ancExist) {
        isAncExist = ancExist;
    }

    public boolean isPmtctHtsExist() {
        return isPmtctHtsExist;
    }

    public void setPmtctHtsExist(boolean pmtctHtsExist) {
        isPmtctHtsExist = pmtctHtsExist;
    }
}
