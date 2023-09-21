/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.mobile.activities.formdisplay;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Spinner;

import org.intellij.lang.annotations.Identifier;
import org.joda.time.LocalDateTime;
import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.api.CustomApiCallback;
import org.openmrs.mobile.api.EncounterService;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.repository.PatientRepository;
import org.openmrs.mobile.api.repository.VisitRepository;
import org.openmrs.mobile.api.retrofit.ProgramRepository;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.LocationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.ServiceLogDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.listeners.retrofit.StartVisitResponseListenerCallback;
import org.openmrs.mobile.models.Answer;
import org.openmrs.mobile.models.EncounterProvider;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.IdentifierType;
import org.openmrs.mobile.models.Location;
import org.openmrs.mobile.models.Obscreate;
import org.openmrs.mobile.models.ObscreateLocal;
import org.openmrs.mobile.models.Obsgroup;
import org.openmrs.mobile.models.ObsgroupLocal;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.ProgramEnrollment;
import org.openmrs.mobile.models.Resource;
import org.openmrs.mobile.models.ServiceLog;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.IdGeneratorUtil;
import org.openmrs.mobile.utilities.InputField;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.RangeEditText;
import org.openmrs.mobile.utilities.SelectManyFields;
import org.openmrs.mobile.utilities.SelectOneField;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.fragment.app.Fragment;

import rx.android.schedulers.AndroidSchedulers;

import static org.openmrs.mobile.utilities.DateUtils.DATE_WITH_TIME_FORMAT;
import static org.openmrs.mobile.utilities.FormService.getFormResourceByName;

public class FormDisplayMainPresenter extends BasePresenter implements FormDisplayContract.Presenter.MainPresenter, CustomApiCallback {
    private RestApi restApi;
    private final long mPatientID;
    private final String mEncountertype;
    private final String mFormname;
    private FormDisplayContract.View.MainView mFormDisplayView;
    private Patient mPatient;
    private FormPageAdapter mPageAdapter;
    private String mEncounterDate = null;
    private String mPatientIdentifier = null;
    private LocationDAO locationDAO;
    private VisitRepository visitRepository;
    private long mEntryID = 0;
    private boolean isNegative = true;

    public FormDisplayMainPresenter(FormDisplayContract.View.MainView mFormDisplayView, Bundle bundle, FormPageAdapter mPageAdapter) {
        this.mFormDisplayView = mFormDisplayView;
        this.mPatientID = (long) bundle.get(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
        this.mPatient = new PatientDAO().findPatientByID(Long.toString(mPatientID));
        this.mEncountertype = (String) bundle.get(ApplicationConstants.BundleKeys.ENCOUNTERTYPE);
        this.mFormname = (String) bundle.get(ApplicationConstants.BundleKeys.FORM_NAME);
        this.mPageAdapter = mPageAdapter;
        locationDAO = new LocationDAO();
        this.visitRepository = new VisitRepository();
        if (bundle.get(ApplicationConstants.BundleKeys.ENTRIES_ID) != null) {
            this.mEntryID = (long) bundle.get(ApplicationConstants.BundleKeys.ENTRIES_ID);
        }
        mFormDisplayView.setPresenter(this);
        restApi = RestServiceBuilder.createService(RestApi.class);


    }

    @Override
    public void subscribe() {
        // This method is intentionally empty
    }

    @Override
    public void createEncounter(boolean isEligible, boolean isValid, boolean isValidPatientIdentifier, String mMessage) {

        if (!isValid) {
            ToastUtil.error("Please ensure you enter the visit date or other compulsory fields. ");
            return;
        }
        if (!isValidPatientIdentifier) {
            ToastUtil.error("Please ensure you enter the patient identifier or other compulsory fields. ");
            return;
        }
        List<InputField> inputFields = new ArrayList<>();
        List<SelectOneField> radioGroupFields = new ArrayList<>();
        List<SelectManyFields> selectManyFields = new ArrayList<>();
        List<String> obsGroupList = new ArrayList<>();
        List<List<Obsgroup>> dataList = new ArrayList<List<Obsgroup>>();
        List<List<ObsgroupLocal>> dataListLocal = new ArrayList<List<ObsgroupLocal>>();


        mFormDisplayView.enableSubmitButton(false);

        Encountercreate encountercreate = new Encountercreate();

        if (mEntryID != 0) {
            Encountercreate.delete(Encountercreate.class, mEntryID);
        }
        encountercreate.setPatient(mPatient.getUuid());
        encountercreate.setEncounterType(mEncountertype);

        List<Obscreate> observations = new ArrayList<>();
        List<ObscreateLocal> observationsLocal = new ArrayList<>();
        List<EncounterProvider> encounterProviders = new ArrayList<>();


        SparseArray<Fragment> activefrag = mPageAdapter.getRegisteredFragments();
        boolean valid = true;
        int ct = activefrag.size();
        for (int i = 0; i < activefrag.size(); i++) {
            FormDisplayPageFragment formPageFragment = (FormDisplayPageFragment) activefrag.get(i);
            if (formPageFragment != null) {
                if (!formPageFragment.checkInputFields()) {
                    valid = false;
                    break;
                }
                inputFields.addAll(formPageFragment.getInputFields());
                radioGroupFields.addAll(formPageFragment.getSelectOneFields());
                selectManyFields.addAll(formPageFragment.getSelectManyFields());
            }
        }

        if (valid) {
            for (InputField input : inputFields) {
                if (input.getObs().equals("encounterDate")) {
                    if (input.getValueAll().isEmpty()) {
                        return;
                    } else {
                        this.mEncounterDate = DateUtils.convertTime(DateUtils.convertTime(input.getValueAll()), DateUtils.OPEN_MRS_REQUEST_FORMAT);
                    }
                }
                if (input.getObs().equals("patientIdentifier")) {
                    this.mPatientIdentifier = input.getValueAll();

                }
                if (input.getGroupConcept() != null && !obsGroupList.contains(input.getGroupConcept())) {
                    obsGroupList.add(input.getGroupConcept());
                }
                if (!input.getValueAll().isEmpty() && !input.getValueAll().equals("")) {
                    if (input.getGroupConcept() == null) {
                        Obscreate obscreate = new Obscreate();
                        obscreate.setConcept(input.getConcept());
                        obscreate.setValue(String.valueOf(input.getValueAll()));
                        obscreate.setObsDatetime(mEncounterDate);
                        obscreate.setPerson(mPatient.getUuid());
//                        observations.add(obscreate);
                        observations.add(obscreate);

                        ObscreateLocal obscreateLocal = new ObscreateLocal();
                        obscreateLocal.setConcept(input.getConcept());
                        obscreateLocal.setQuestionLabel(input.getQuestionLabel());
                        obscreateLocal.setValue(String.valueOf(input.getValueAll()));
                        obscreateLocal.setAnswerLabel(String.valueOf(input.getValueAll()));
                        obscreateLocal.setObsDatetime(mEncounterDate);
                        obscreateLocal.setPerson(mPatient.getUuid());
                        observationsLocal.add(obscreateLocal);
                    }
                }

            }
            for (SelectOneField radioGroupField : radioGroupFields) {
                if (radioGroupField.getGroupConcept() != null && radioGroupField.getChosenAnswer() != null && !obsGroupList.contains(radioGroupField.getGroupConcept())) {
                    obsGroupList.add(radioGroupField.getGroupConcept());
                }
                if (radioGroupField.getChosenAnswer() != null && radioGroupField.getChosenAnswer().getConcept() != null) {
                    if (radioGroupField.getGroupConcept() == null) {
                        Obscreate obscreate = new Obscreate();
                        obscreate.setConcept(radioGroupField.getConcept());
                        obscreate.setValue(radioGroupField.getChosenAnswer().getConcept());
                        obscreate.setObsDatetime(mEncounterDate);
                        obscreate.setPerson(mPatient.getUuid());
                        observations.add(obscreate);

                        ObscreateLocal obscreateLocal = new ObscreateLocal();
                        obscreateLocal.setConcept(radioGroupField.getConcept());
                        obscreateLocal.setQuestionLabel(radioGroupField.getQuestionLabel());
                        obscreateLocal.setValue(radioGroupField.getChosenAnswer().getConcept());
                        obscreateLocal.setAnswerLabel(radioGroupField.getChosenAnswer().getLabel());
                        obscreateLocal.setObsDatetime(mEncounterDate);
                        obscreateLocal.setPerson(mPatient.getUuid());
                        observationsLocal.add(obscreateLocal);
                    }
                }
            }
            for (SelectManyFields selectManyField : selectManyFields) {
                if (selectManyField.getGroupConcept() != null && !obsGroupList.contains(selectManyField.getGroupConcept())) {
                    obsGroupList.add(selectManyField.getGroupConcept());
                }
                if (selectManyField.getChosenAnswerList().size() > 0 && selectManyField.getObs().equals("obs")) {
                    if (selectManyField.getGroupConcept() == null) {
                        for (Answer answer : selectManyField.getChosenAnswerList()) {
                            Obscreate obscreate = new Obscreate();
                            ObscreateLocal obscreateLocal = new ObscreateLocal();
                            obscreate.setConcept(selectManyField.getConcept());
                            obscreateLocal.setConcept(selectManyField.getConcept());
                            obscreateLocal.setQuestionLabel(selectManyField.getQuestionLabel());
                            // select many id is concept id
                            obscreate.setValue(answer.getConcept());
                            obscreateLocal.setValue(answer.getConcept());
                            obscreateLocal.setAnswerLabel(answer.getLabel());
                            obscreate.setObsDatetime(mEncounterDate);
                            obscreateLocal.setObsDatetime(mEncounterDate);
                            obscreate.setPerson(mPatient.getUuid());
                            obscreateLocal.setPerson(mPatient.getUuid());
                            observations.add(obscreate);
                            observationsLocal.add(obscreateLocal);
                        }
                    }

                }
            }

            if (!obsGroupList.isEmpty()) {
                for (int i = 1; i <= obsGroupList.size(); i++) {
                    List<Obsgroup> tempList = new ArrayList<Obsgroup>();
                    List<ObsgroupLocal> tempListLocal = new ArrayList<ObsgroupLocal>();
                    dataList.add(tempList);
                    dataListLocal.add(tempListLocal);
                }
                for (InputField input : inputFields) {
                    if (!input.getValueAll().isEmpty() && !input.getValueAll().equals("")) {
                        if (input.getGroupConcept() != null) {
                            Obsgroup obsgroup = new Obsgroup();
                            ObsgroupLocal obsgroupLocal = new ObsgroupLocal();
                            obsgroup.setConcept(input.getConcept());
                            obsgroup.setValue(String.valueOf(input.getValueAll()));
                            obsgroupLocal.setConcept(input.getConcept());
                            obsgroupLocal.setAnswerLabel(String.valueOf(input.getValueAll()));
                            obsgroupLocal.setQuestionLabel(input.getQuestionLabel());
                            obsgroupLocal.setValue(String.valueOf(input.getValueAll()));
                            obsgroupLocal.setRepeatConcept(input.getRepeatConcept());
                            dataList.get(obsGroupList.indexOf(input.getGroupConcept())).add(obsgroup);
                            dataListLocal.get(obsGroupList.indexOf(input.getGroupConcept())).add(obsgroupLocal);
                        }
                    }
                }
                for (SelectOneField radioGroupField : radioGroupFields) {
                    if (radioGroupField.getChosenAnswer() != null && radioGroupField.getChosenAnswer().getConcept() != null) {
                        if (radioGroupField.getGroupConcept() != null) {
                            Obsgroup obsgroup = new Obsgroup();
                            ObsgroupLocal obsgroupLocal = new ObsgroupLocal();
                            obsgroup.setConcept(radioGroupField.getConcept());
                            obsgroup.setValue(radioGroupField.getChosenAnswer().getConcept());
                            obsgroupLocal.setConcept(radioGroupField.getConcept());
                            obsgroupLocal.setValue(radioGroupField.getChosenAnswer().getConcept());
                            obsgroupLocal.setQuestionLabel(radioGroupField.getQuestionLabel());
                            obsgroupLocal.setAnswerLabel(radioGroupField.getChosenAnswer().getLabel());
                            if (radioGroupField.getRepeatConcept() != null) {
                                obsgroupLocal.setRepeatConcept(radioGroupField.getRepeatConcept());
                            }
                            dataList.get(obsGroupList.indexOf(radioGroupField.getGroupConcept())).add(obsgroup);
                            dataListLocal.get(obsGroupList.indexOf(radioGroupField.getGroupConcept())).add(obsgroupLocal);
                        }
                    }
                }
                for (SelectManyFields selectManyField : selectManyFields) {
                    if (selectManyField.getChosenAnswerList().size() > 0 && selectManyField.getObs().equals("obs")) {
                        if (selectManyField.getGroupConcept() != null) {
                            for (Answer answer : selectManyField.getChosenAnswerList()) {
                                Obsgroup obsgroup = new Obsgroup();
                                ObsgroupLocal obsgroupLocal = new ObsgroupLocal();
                                obsgroup.setConcept(selectManyField.getConcept());
                                obsgroup.setValue(answer.getConcept());
                                obsgroupLocal.setConcept(selectManyField.getConcept());
                                obsgroupLocal.setValue(answer.getConcept());
                                obsgroupLocal.setAnswerLabel(answer.getLabel());
                                obsgroupLocal.setQuestionLabel(selectManyField.getQuestionLabel());
                                dataList.get(obsGroupList.indexOf(selectManyField.getGroupConcept())).add(obsgroup);
                                dataListLocal.get(obsGroupList.indexOf(selectManyField.getGroupConcept())).add(obsgroupLocal);
                            }
                        }
                    }
                }

                // Form the observations
                for (int i = 0; i < obsGroupList.size(); i++) {
                    if (!dataList.get(i).isEmpty()) {
                        Obscreate obscreate = new Obscreate();
                        obscreate.setConcept(obsGroupList.get(i));
                        obscreate.setObsDatetime(mEncounterDate);
                        obscreate.setPerson(mPatient.getUuid());
                        obscreate.setGroupMembers(dataList.get(i));
                        observations.add(obscreate);

                    }
                    if (!dataListLocal.get(i).isEmpty()) {
                        ObscreateLocal obscreateLocal = new ObscreateLocal();
                        obscreateLocal.setGroupMembers(dataListLocal.get(i));
                        obscreateLocal.setConcept(obsGroupList.get(i));
                        obscreateLocal.setObsDatetime(mEncounterDate);
                        obscreateLocal.setPerson(mPatient.getUuid());
                        observationsLocal.add(obscreateLocal);

                    }
                }
            }
//            if (mFormname.equals("Risk Stratification Adult")){
//                encountercreate.setIdentifier(IdGeneratorUtil.getIdentifierGenerated()+mPatientID);
//                encountercreate.setIdentifierType("HIV testing Id (Client Code)");
//            }
//            if (mFormname.equals("Risk Assessment Pediatric")){
//                encountercreate.setIdentifier(IdGeneratorUtil.getIdentifierGenerated()+mPatientID);
//                encountercreate.setIdentifierType("HIV testing Id (Client Code)");
//            }
            if (mFormname.equals("HIV Enrollment")) {
                encountercreate.setIdentifier(mPatientIdentifier);
                encountercreate.setIdentifierType("ART Number");
            }
            if (mFormname.equals("General Antenatal Care")) {
                encountercreate.setIdentifier(mPatientIdentifier);
                encountercreate.setIdentifierType("ANC Number");
            }
            if (mFormname.equals("Client intake form")) {
                encountercreate.setIdentifier(mPatientIdentifier);
                encountercreate.setIdentifierType("HIV testing Id (Client Code)");
                if (isNegative) {
                    Obscreate obscreate = new Obscreate();
                    obscreate.setConcept("c26ab267-c014-4bc8-97b1-c6c5b08a2e93");
                    obscreate.setValue(mEncounterDate);
                    obscreate.setObsDatetime(mEncounterDate);
                    obscreate.setPerson(mPatient.getUuid());
//                        observations.add(obscreate);
                    observations.add(obscreate);

                    ObscreateLocal obscreateLocal = new ObscreateLocal();
                    obscreateLocal.setConcept("c26ab267-c014-4bc8-97b1-c6c5b08a2e93");
                    obscreateLocal.setQuestionLabel("Signature Date");
                    obscreateLocal.setValue(String.valueOf(mEncounterDate));
                    obscreateLocal.setAnswerLabel(String.valueOf(mEncounterDate));
                    obscreateLocal.setObsDatetime(mEncounterDate);
                    obscreateLocal.setPerson(mPatient.getUuid());
                    observationsLocal.add(obscreateLocal);
                }
            }
            EncounterProvider encounterProvider = new EncounterProvider();
            encounterProvider.setProvider("f9badd80-ab76-11e2-9e96-0800200c9a66");
            encounterProvider.setEncounterRole("a0b03050-c99b-11e0-9572-0800200c9a66");
            encounterProviders.add(encounterProvider);
            encountercreate.setEncounterProviders(encounterProviders);
            encountercreate.setObservations(observations);
            encountercreate.setObservationsLocal(observationsLocal);
            encountercreate.setFormname(mFormname);
            encountercreate.setPatientId(mPatientID);
            encountercreate.setFormUuid(getFormResourceByName(mFormname).getUuid());
            encountercreate.setObslist();
            encountercreate.setObslistLocal();

            if (isEligible) {
                encountercreate.setEligible("Yes");
            } else {
                encountercreate.setEligible("No");
            }
            if (mEncounterDate != null) {
                encountercreate.setEncounterDatetime(mEncounterDate);
            }
            encountercreate.setLocation((locationDAO.findLocationByName(OpenMRS.getInstance().getLocation())).getUuid());
            //Util.logEncounter(encountercreate);
           /*OpenMRSCustomHandler.writeLogToFile(OpenMRSCustomHandler.showJson(encountercreate,""));
             Util.log("UUID " +encountercreate.getFormUuid());
            Util.log("name " +mFormname);*/
            //Testing to to be removed the if
            //if(true)return;
            encountercreate.save();


            // if the form is lab or pharmacy order form  for PBS recapture to pick date log the
            if (mFormname.equals("Laboratory Order and Result form") || mFormname.equals("Pharmacy Order Form")) {
                ServiceLog _servicelog = new ServiceLog();
                String patientUUID = new PatientDAO().getPatientUUID(String.valueOf(mPatientID));
                    String todayDate = DateUtils.convertTime(DateUtils.convertTime(DateUtils.getCurrentDateTime()), DateUtils.OPEN_MRS_PBS_DATE_FORMAT);
                    _servicelog.setPatientId(mPatientID);
                    _servicelog.setVoided(0);
                    _servicelog.setDateCreated(todayDate);
                    _servicelog.setVisitDate(mEncounterDate);
                    _servicelog.setPatientUUID(patientUUID==null?"":patientUUID);
                    _servicelog.setFormName(mFormname);
                    new ServiceLogDAO().saveServiceLog(_servicelog);

            } else {
                Util.log("Not supported form");
            }


            // get if this patient has a visit locally on this date
            Long visitId = new VisitDAO().getVisitsIDByDate(mPatientID, mEncounterDate);
            //We do not want to do anything online automatically again
            //if (!NetworkUtils.isOnline()) { // offline case
                if (visitId == 0) { // visit not found  offline case
                    visitRepository.startVisitLocally(mPatient, mEncounterDate);// start new visit
                    Long visitIdNow = new VisitDAO().getVisitsIDByDate(mPatientID, mEncounterDate);// get the  new visit ID
                    Visit visit = new VisitDAO().getVisitByIDLocally(visitIdNow);  // get the new Visit by ID
                    encountercreate.setVisit(visit.getUuid()); // set visit UUID
                    encountercreate.save();  //  save to update the encounter

                } else {  // visit found offline
                    Visit visit = new VisitDAO().getVisitByIDLocally(visitId); // get the visit
                    encountercreate.setVisit(visit.getUuid());//  set visit UUID
                    encountercreate.save(); // update the  encounter
                }

            mFormDisplayView.enableSubmitButton(true);
            mFormDisplayView.quitFormEntry();

            /*} else {  // online case
                if (visitId != 0) {  // visit found locally and server is online
                    Visit visit = new VisitDAO().getVisitByIDLocally(visitId);
                    visitRepository.reOpenVisitByUUID(new VisitDAO().getVisitByIDLocally(visit.getId()));
                    encountercreate.setVisit(visit.getUuid());
                    visit.setStopDatetime(null);
                    new VisitDAO().updateVisitLocally(visit, visit.getId(), visit.getPatient().getId());
                    encountercreate.save();
                } else {
                    Visit visit = new VisitDAO().getActiveLocalVisitByPatientId(mPatientID);
                    if (visit != null) {
                        visitRepository.endVisitByUUID(new VisitDAO().getVisitByIDLocally(visit.getId()));
                        encountercreate.setVisit(visit.getUuid());
                        encountercreate.save();
                    }

                }
            }*/


            /*if (!mPatient.isSynced()) {
                PatientIdentifier identifier = mPatient.getIdentifier();
                //I commented this out. It appends the patient id to the patient identifier while syncing
                //identifier.setIdentifier(identifier.getIdentifier()+mPatientID);
                identifier.setIdentifier(identifier.getIdentifier());
                List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
                identifiers.add(identifier);
                if (mFormname.equals("Client intake form")) {
                    PatientIdentifier patientIdentifier = new PatientIdentifier();
                    patientIdentifier.setIdentifier(mPatientIdentifier);
                    IdentifierType identifierType = new IdentifierType("HIV testing Id (Client Code)");
                    patientIdentifier.setDisplay("HIV testing Id (Client Code)");
                    patientIdentifier.setIdentifierType(identifierType);
                    identifiers.add(patientIdentifier);
                }
                if (mFormname.equals("HIV Enrollment")) {
                    PatientIdentifier patientIdentifier = new PatientIdentifier();
                    patientIdentifier.setIdentifier(this.mPatientIdentifier);
                    IdentifierType identifierType = new IdentifierType("ART Number");
                    patientIdentifier.setDisplay("ART Number");
                    patientIdentifier.setIdentifierType(identifierType);
                    identifiers.add(patientIdentifier);
                }
                if (mFormname.equals("General Antenatal Care")) {
                    PatientIdentifier patientIdentifier = new PatientIdentifier();
                    patientIdentifier.setIdentifier(this.mPatientIdentifier);
                    IdentifierType identifierType = new IdentifierType("ANC Number");
                    patientIdentifier.setDisplay("ANC Number");
                    patientIdentifier.setIdentifierType(identifierType);
                    identifiers.add(patientIdentifier);
                }
                mPatient.setIdentifiers(identifiers);
                mPatient.addEncounters(encountercreate.getId());
                new PatientDAO().updatePatient(mPatient.getId(), mPatient);
                ToastUtil.warning("Patient not yet synced. Form data is saved locally " +
                        "and will sync when internet connection is restored. ");
                mFormDisplayView.enableSubmitButton(true);
                mFormDisplayView.quitFormEntry();*/
            /*} else {
                PatientIdentifier identifier = mPatient.getIdentifier();
                identifier.setIdentifier(identifier.getIdentifier() + mPatientID);
                List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
                identifiers.add(identifier);
//                if (mFormname.equals("Risk Stratification Adult") || mFormname.equals("Risk Assessment Pediatric")) {
                if (mFormname.equals("Client intake form")) {
                    PatientIdentifier patientIdentifier = new PatientIdentifier();
//                    patientIdentifier.setIdentifier(IdGeneratorUtil.getIdentifierGenerated()+mPatientID);
                    patientIdentifier.setIdentifier(this.mPatientIdentifier);
                    IdentifierType identifierType = new IdentifierType("HIV testing Id (Client Code)");
                    patientIdentifier.setDisplay("HIV testing Id (Client Code)");
                    patientIdentifier.setIdentifierType(identifierType);
                    identifiers.add(patientIdentifier);
                    mPatient.setIdentifiers(identifiers);
              /*      PatientRepository patientRepository = new PatientRepository();
                    patientRepository.updatePatient(mPatient, new DefaultResponseCallbackListener() {
                        @Override
                        public void onResponse() {
//                            ToastUtil.success("Patient identifier successfully synchronized.");
                        }

                        @Override
                        public void onErrorResponse(String errorMessage) {
//                            ToastUtil.error("Patient identifier synchronization not successful.");

                        }
                    });
                    ProgramEnrollment programEnrollment = new ProgramEnrollment();
                    programEnrollment.setPatient(mPatient.getUuid());
                    programEnrollment.setProgram("14d6f977-7952-41cd-b243-1c3bcc4a9213");
                    programEnrollment.setDateEnrolled(mEncounterDate);
                    ProgramRepository programRepository = new ProgramRepository();
                    programRepository.addProgram(restApi, programEnrollment, this);
                }
                if (mFormname.equals("HIV Enrollment")) {
                    PatientIdentifier patientIdentifier = new PatientIdentifier();
                    patientIdentifier.setIdentifier(this.mPatientIdentifier);
                    IdentifierType identifierType = new IdentifierType("ART Number");
                    patientIdentifier.setDisplay("ART Number");
                    patientIdentifier.setIdentifierType(identifierType);
                    identifiers.add(patientIdentifier);
                    mPatient.setIdentifiers(identifiers);
                    PatientRepository patientRepository = new PatientRepository();
                    patientRepository.updatePatient(mPatient, new DefaultResponseCallbackListener() {
                        @Override
                        public void onResponse() {
//                            ToastUtil.success("Patient identifier successfully synchronized.");
                        }

                        @Override
                        public void onErrorResponse(String errorMessage) {
//                            ToastUtil.error("Patient identifier synchronization not successful.");

                        }
                    });
                    ProgramEnrollment programEnrollment = new ProgramEnrollment();
                    programEnrollment.setPatient(mPatient.getUuid());
                    programEnrollment.setProgram("9083deaa-f37f-44b3-9046-b87b134711a1");
                    programEnrollment.setDateEnrolled(mEncounterDate);
                    ProgramRepository programRepository = new ProgramRepository();
                    programRepository.addProgram(restApi, programEnrollment, this);
                }
                if (mFormname.equals("General Antenatal Care")) {
                    PatientIdentifier patientIdentifier = new PatientIdentifier();
                    patientIdentifier.setIdentifier(this.mPatientIdentifier);
                    IdentifierType identifierType = new IdentifierType("ANC Number");
                    patientIdentifier.setDisplay("ANC Number");
                    patientIdentifier.setIdentifierType(identifierType);
                    identifiers.add(patientIdentifier);
                    mPatient.setIdentifiers(identifiers);
                    PatientRepository patientRepository = new PatientRepository();
                    patientRepository.updatePatient(mPatient, new DefaultResponseCallbackListener() {
                        @Override
                        public void onResponse() {
//                            ToastUtil.success("Patient identifier successfully synchronized.");
                        }

                        @Override
                        public void onErrorResponse(String errorMessage) {
//                            ToastUtil.error("Patient identifier synchronization not successful.");

                        }
                    });
                    ProgramEnrollment programEnrollment = new ProgramEnrollment();
                    programEnrollment.setPatient(mPatient.getUuid());
                    programEnrollment.setProgram("c3ae2d33-97d3-4cc4-9206-8a8160593648");
                    programEnrollment.setDateEnrolled(mEncounterDate);
                    ProgramRepository programRepository = new ProgramRepository();
                    programRepository.addProgram(restApi, programEnrollment, this);
                }
                new EncounterService().addEncounter(encountercreate, mEncounterDate, new DefaultResponseCallbackListener() {
                    @Override
                    public void onResponse() {
                        mFormDisplayView.enableSubmitButton(true);
                    }

                    @Override
                    public void onErrorResponse(String errorMessage) {
                        mFormDisplayView.showToast(errorMessage);
                        Util.log(errorMessage);
                        mFormDisplayView.enableSubmitButton(true);
                    }
                });

                mFormDisplayView.quitFormEntry();
            }*/
        } else {
            mFormDisplayView.enableSubmitButton(true);
        }
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure() {

    }
}
