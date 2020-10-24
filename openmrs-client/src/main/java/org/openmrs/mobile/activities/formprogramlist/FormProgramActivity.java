package org.openmrs.mobile.activities.formprogramlist;


import android.os.Bundle;
import android.view.Menu;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.LogOutTimerUtil;

import java.util.List;

public class FormProgramActivity extends ACBaseActivity implements LogOutTimerUtil.LogOutListener{
    private String programName = null;
    private boolean isFirstTime = false;
    private boolean isEnrolled = false;
    private boolean isEligible = false;
    private boolean isCompleted = false;
    private boolean isPositive = false;
    private boolean isClientExist = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_program);

        // Create fragment
        FormProgramFragment formProgramFragment =
                (FormProgramFragment) getSupportFragmentManager().findFragmentById(R.id.formProgrameContentFrame);
        if (formProgramFragment == null) {
            formProgramFragment = FormProgramFragment.newInstance();
        }
        if (!formProgramFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    formProgramFragment, R.id.formProgrameContentFrame);
        }

        Bundle bundle = getIntent().getExtras();
        String mPatientID = "";

        if(bundle != null)
        {
            mPatientID = bundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
            programName = bundle.getString(ApplicationConstants.BundleKeys.PATIENT_PROGRAM);
        }
        List<Encountercreate> encountercreateList = new VisitDAO().getLocalEncounterByPatientIDEligible(Long.parseLong(mPatientID), "Client intake form","Yes");
        if(encountercreateList.isEmpty()){
            this.isPositive = false;
        }else{
            this.isPositive = true;
        }
        List<Encountercreate> encountercreateListAdult = new VisitDAO().getLocalEncounterByPatientIDStr(Long.parseLong(mPatientID),"Risk Assessment Pediatric");
        List<Encountercreate> encountercreateListChild = new VisitDAO().getLocalEncounterByPatientIDStr(Long.parseLong(mPatientID),"Risk Stratification Adult");
        if(encountercreateListAdult.isEmpty() && encountercreateListChild.isEmpty()){
            this.isFirstTime = true;
        }else{
            this.isFirstTime = false;
        }

        List<Encountercreate> encountercreateGenCare= new VisitDAO().getLocalEncounterByPatientIDStr(Long.parseLong(mPatientID),"General Antenatal Care");
        if(encountercreateGenCare.isEmpty()){
            this.isFirstTime = true;
        }else{
            this.isFirstTime = false;
        }



        List<Encountercreate> encountercreateListEligibleChild = new VisitDAO().getLocalEncounterByPatientIDEligible(Long.parseLong(mPatientID), "Risk Assessment Pediatric","Yes");
        List<Encountercreate> encountercreateListEligibleAdult = new VisitDAO().getLocalEncounterByPatientIDEligible(Long.parseLong(mPatientID), "Risk Stratification Adult","Yes");
        if(encountercreateListEligibleChild.isEmpty()){
            isEligible = false;
        }else{
            isEligible = true;
        }
        List<Encountercreate> encountercreateListClientExist = new VisitDAO().getLocalEncounterByPatientIDStr(Long.parseLong(mPatientID), "Client intake form");
        if(encountercreateListClientExist.isEmpty()){
            isClientExist = false;
        }else{
            isClientExist = true;
        }

        List<Encountercreate> encountercreateListEnrolled = new VisitDAO().getLocalEncounterByPatientIDStr(Long.parseLong(mPatientID), "HIV Enrollment");
        if(encountercreateListEnrolled.isEmpty()){
            this.isEnrolled = false;
        }else{
            this.isEnrolled = true;
        }

        List<Encountercreate> encountercreateListCompleted = new VisitDAO().getLocalEncounterByPatientIDStr(Long.parseLong(mPatientID), "Pharmacy Order Form");
        if(encountercreateListCompleted.isEmpty()){
            this.isCompleted = false;
        }else{
            this.isCompleted = true;
        }


        // Create the presenter
        new FormProgramPresenter(formProgramFragment, Long.parseLong(mPatientID),programName);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public boolean isEnrolled() {
        return isEnrolled;
    }

    public boolean isEligible() {
        return isEligible;
    }

    public void setEnrolled(boolean enrolled) {
        isEnrolled = enrolled;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }

    public boolean isClientExist() {
        return isClientExist;
    }

    public void setClientExist(boolean clientExist) {
        isClientExist = clientExist;
    }
    @Override
    protected void onStart() {
        super.onStart();
        LogOutTimerUtil.startLogoutTimer(this, this);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        LogOutTimerUtil.startLogoutTimer(this, this);
    }

    @Override
    public void doLogout() {
        logout();
    }
}
