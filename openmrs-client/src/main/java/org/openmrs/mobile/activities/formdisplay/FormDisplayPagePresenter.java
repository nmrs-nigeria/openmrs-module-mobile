/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.mobile.activities.formdisplay;

import android.widget.LinearLayout;

import com.activeandroid.query.Select;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.bundle.FormFieldsWrapper;
import org.openmrs.mobile.models.Facility;
import org.openmrs.mobile.models.Page;
import org.openmrs.mobile.models.Question;
import org.openmrs.mobile.models.Section;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.InputField;
import org.openmrs.mobile.utilities.SelectManyFields;
import org.openmrs.mobile.utilities.SelectOneField;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FormDisplayPagePresenter extends BasePresenter implements FormDisplayContract.Presenter.PagePresenter {

    private FormDisplayContract.View.PageView mFormDisplayPageView;
    private Page mPage;
    private ArrayList<FormFieldsWrapper> mFormFieldsWrapper = null;

    private List<Page> pageList = new LinkedList<>();
    public List<List<InputField>> mInputFields = new LinkedList<>();
    public List<List<SelectOneField>> mSelectOneField = new LinkedList<>();
    public List<List<SelectManyFields>> mSelectManyFields = new LinkedList<>();
    private String encounterDate = null;
    private String personGender = null;
    public boolean isNew =  true;
    public FormDisplayPagePresenter(FormDisplayContract.View.PageView mFormPageView, Page page) {
        this.mFormDisplayPageView = mFormPageView;
        this.mPage = page;
        this.mFormDisplayPageView.setPresenter(this);
    }

    public FormDisplayPagePresenter(FormDisplayContract.View.PageView mFormPageView, Page page, String personGender) {
        this.mFormDisplayPageView = mFormPageView;
        this.mPage = page;
        this.mFormDisplayPageView.setPresenter(this);
        this.personGender = personGender;
    }

    public FormDisplayPagePresenter(FormDisplayContract.View.PageView mFormPageView, Page page, FormFieldsWrapper formFieldsWrapper) {
        this.mFormDisplayPageView = mFormPageView;
        this.mPage = page;
        this.mFormDisplayPageView.setPresenter(this);
        setViewFields(formFieldsWrapper);
    }

    public FormDisplayPagePresenter(FormDisplayContract.View.PageView mFormPageView, Page page, ArrayList<FormFieldsWrapper> formFieldsWrapper, List<Page> pageList, String encounterDate) {
        this.mFormDisplayPageView = mFormPageView;
        this.pageList = pageList;
        this.mPage = page;
        this.mFormDisplayPageView.setPresenter(this);
        this.mFormFieldsWrapper = formFieldsWrapper;
        this.encounterDate = encounterDate;
    }


    private void setViewFields(FormFieldsWrapper formFieldsWrapper) {
        if (formFieldsWrapper != null) {
            mFormDisplayPageView.setInputFields(formFieldsWrapper.getInputFields());
            mFormDisplayPageView.setSelectOneFields(formFieldsWrapper.getSelectOneFields());
            mFormDisplayPageView.setSelectManyFields(formFieldsWrapper.getSelectManyFields());
        }
    }

    @Override
    public void subscribe() {
        if (isNew) {
            if (this.mFormFieldsWrapper == null) {
                //For follow up
                List<Section> sectionList = mPage.getSections();
                for (Section section : sectionList) {
                    addSection(section);
                }
            } else {
                for (Page page : this.pageList) {
                    mInputFields.add(this.mFormFieldsWrapper.get(pageList.indexOf(page)).getInputFields());
                    mSelectOneField.add(this.mFormFieldsWrapper.get(pageList.indexOf(page)).getSelectOneFields());
                    mSelectManyFields.add(this.mFormFieldsWrapper.get(pageList.indexOf(page)).getSelectManyFields());

                }
                List<Section> sectionList = mPage.getSections();
                for (Section section : sectionList) {
                    addSection(section);
                }
            }
            isNew = false;
        }
    }

    private void addSection(Section section) {
        LinearLayout sectionLinearLayout = mFormDisplayPageView.createSectionLayout(section.getLabel());

//        if(personNames != null && section.getLabel().equals("Child Follow up Information")){
//            mFormDisplayPageView.createSpecialSelectQuestionDropdown(sectionLinearLayout, personNames);
//        }
        mFormDisplayPageView.attachSectionToView(sectionLinearLayout);
        if ((mInputFields == null || mInputFields.isEmpty()) && (mSelectOneField == null || mSelectOneField.isEmpty()) && (mSelectManyFields == null || mSelectManyFields.isEmpty())) {
            for (Question question : section.getQuestions()) {
                addQuestion(question, sectionLinearLayout);
            }
        } else {
            for (Question question : section.getQuestions()) {
                addQuestionEdit(question, sectionLinearLayout);
            }
        }

    }


    private void addQuestion(Question question, LinearLayout sectionLinearLayout) {

        if(question.getGenderSpecificConcept() != null && !question.getGenderSpecificConcept().equals(this.personGender)){
            question.setGenderSpecificConcept("hide");
        }
        if (question.getQuestionOptions().getRendering().equals("group")) {
            LinearLayout questionLinearLayout = mFormDisplayPageView.createQuestionGroupLayout(question.getLabel());
            mFormDisplayPageView.attachQuestionToSection(sectionLinearLayout, questionLinearLayout);

            for(Question subquestion:question.getQuestions()) {
                subquestion.setGroupConcept(question.getQuestionOptions().getConcept());
                subquestion.setRepeatConcept(question.getQuestionOptions().getRepeatConcept());
                addQuestion(subquestion,questionLinearLayout);
            }
        }
        if (question.getQuestionOptions().getRendering().equals("subHeading")) {
            LinearLayout questionLinearLayout = mFormDisplayPageView.createQuestionGroupLayout(question.getLabel());
            mFormDisplayPageView.attachQuestionToSection(sectionLinearLayout, questionLinearLayout);

            for(Question subquestion:question.getQuestions()) {
                subquestion.setGroupConcept(question.getQuestionOptions().getConcept());
                subquestion.setRepeatConcept(question.getQuestionOptions().getRepeatConcept());
                addQuestion(subquestion,questionLinearLayout);
            }
        }

        if(question.getQuestionOptions().getRendering().equals("number")) {
            mFormDisplayPageView.createAndAttachNumericQuestionEditText(question, sectionLinearLayout);
        }

        if (question.getQuestionOptions().getRendering().equals("select")) {
            mFormDisplayPageView.createAndAttachSelectQuestionDropdown(question, sectionLinearLayout);
        }
        if (question.getQuestionOptions().getRendering().equals("date")) {
            mFormDisplayPageView.createAndAttachDateQuestionEditText(question, sectionLinearLayout);
        }
        if (question.getQuestionOptions().getRendering().equals("radio")) {
            mFormDisplayPageView.createAndAttachSelectQuestionRadioButton(question, sectionLinearLayout);
        }
        if (question.getQuestionOptions().getRendering().equals("text") | question.getQuestionOptions().getRendering().equals("textarea")) {
            mFormDisplayPageView.createAndAttachTextQuestionEditText(question, sectionLinearLayout);
        }
        if (question.getQuestionOptions().getRendering().equals("check")) {
            mFormDisplayPageView.createAndAttachSelectQuestionCheckBox(question, sectionLinearLayout);
        }
        if (question.getQuestionOptions().getConcept().equalsIgnoreCase("de06184b-cc63-47bf-917c-b985a3a878ef")){
            List<Facility> facilities = new Select()
                    .distinct()
                    .from(Facility.class)
                    .groupBy("stateName")
                    .execute();
            mFormDisplayPageView.createAndAttachSelectQuestionDropdownStateReferredFacility(facilities,sectionLinearLayout);
            mFormDisplayPageView.createAndAttachSelectQuestionDropdownReferredFacility(sectionLinearLayout);
        }
    }

    private void addQuestionEdit(Question question, LinearLayout sectionLinearLayout) {
        question.setGenderSpecificConcept(this.personGender);
        if (question.getQuestionOptions().getRendering().equals("group")) {
            LinearLayout questionLinearLayout = mFormDisplayPageView.createQuestionGroupLayout(question.getLabel());
            mFormDisplayPageView.attachQuestionToSection(sectionLinearLayout, questionLinearLayout);

            for (Question subquestion : question.getQuestions()) {
                subquestion.setGroupConcept(question.getQuestionOptions().getConcept());
                subquestion.setRepeatConcept(question.getQuestionOptions().getRepeatConcept());
                if (question.getQuestionOptions().getRepeatConcept() == null) {
                    addQuestionEditGroup(subquestion, questionLinearLayout, question.getQuestionOptions().getConcept());
                }else{
                    addQuestionEditRepeat(subquestion, questionLinearLayout, question.getQuestionOptions().getConcept(), question.getQuestionOptions().getRepeatConcept());
                }
            }
        }
        if (question.getQuestionOptions().getRendering().equals("subHeading")) {
            LinearLayout questionLinearLayout = mFormDisplayPageView.createQuestionGroupLayout(question.getLabel());
            mFormDisplayPageView.attachQuestionToSection(sectionLinearLayout, questionLinearLayout);

            for (Question subquestion : question.getQuestions()) {
//                subquestion.setGroupConcept(question.getQuestionOptions().getConcept());
                addQuestionEdit(subquestion, questionLinearLayout);

            }
        }

        if (question.getQuestionOptions().getRendering().equals("number")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept())) {
                        mFormDisplayPageView.editAndAttachNumericQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("date")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept())) {
                        if (question.getType().equals("encounterDate")) {
                            mFormDisplayPageView.editAndAttachDateQuestionEditText(question, sectionLinearLayout, DateUtils.convertTime(DateUtils.convertTime(this.encounterDate), DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT));
                        } else {
                            mFormDisplayPageView.editAndAttachDateQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                        }
                    }
                }
            }

        }

        if (question.getQuestionOptions().getRendering().equals("text")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept())) {
                        mFormDisplayPageView.editAndAttachTextQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("select")) {
            for (List<SelectOneField> selectOneFields : mSelectOneField) {
                for (SelectOneField selectOneField : selectOneFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectOneField.getConcept())) {

                        mFormDisplayPageView.editAndAttachSelectQuestionDropdown(question, sectionLinearLayout, selectOneField);
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("check")) {

            for (List<SelectManyFields> selectManyFields : mSelectManyFields) {
                for (SelectManyFields selectManyField : selectManyFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectManyField.getConcept())) {
                        mFormDisplayPageView.editAndAttachSelectQuestionCheckBox(question, sectionLinearLayout, selectManyField);
                    }
                }
            }
        }
        if (question.getQuestionOptions().getRendering().equals("radio")) {
            for (List<SelectOneField> selectOneFields : mSelectOneField) {
                for (SelectOneField selectOneField : selectOneFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectOneField.getConcept())) {
                        mFormDisplayPageView.editAndAttachSelectQuestionRadioButton(question, sectionLinearLayout, selectOneField);
                    }
                }
            }
        }
    }

    private void addQuestionEditGroup(Question question, LinearLayout sectionLinearLayout, String groupConceptUIID) {
        question.setGenderSpecificConcept(this.personGender);
        if (question.getQuestionOptions().getRendering().equals("number")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept()) && inputField.getGroupConcept().equals(groupConceptUIID)) {
                        mFormDisplayPageView.editAndAttachNumericQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("date")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept()) && inputField.getGroupConcept().equals(groupConceptUIID)) {
                        if (question.getType().equals("encounterDate")) {
                            mFormDisplayPageView.editAndAttachDateQuestionEditText(question, sectionLinearLayout, DateUtils.convertTime(DateUtils.convertTime(this.encounterDate), DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT));
                        } else {
                            mFormDisplayPageView.editAndAttachDateQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                        }
                    }
                }
            }

        }

        if (question.getQuestionOptions().getRendering().equals("text")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept()) && inputField.getGroupConcept().equals(groupConceptUIID)) {
                        mFormDisplayPageView.editAndAttachTextQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("select")) {
            for (List<SelectOneField> selectOneFields : mSelectOneField) {
                for (SelectOneField selectOneField : selectOneFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectOneField.getConcept()) && selectOneField.getGroupConcept().equals(groupConceptUIID)) {

                        mFormDisplayPageView.editAndAttachSelectQuestionDropdown(question, sectionLinearLayout, selectOneField);
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("check")) {

            for (List<SelectManyFields> selectManyFields : mSelectManyFields) {
                for (SelectManyFields selectManyField : selectManyFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectManyField.getConcept()) && selectManyField.getGroupConcept().equals(groupConceptUIID)) {
                        mFormDisplayPageView.editAndAttachSelectQuestionCheckBox(question, sectionLinearLayout, selectManyField);
                    }
                }
            }
        }
        if (question.getQuestionOptions().getRendering().equals("radio")) {
            for (List<SelectOneField> selectOneFields : mSelectOneField) {
                for (SelectOneField selectOneField : selectOneFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectOneField.getConcept()) && selectOneField.getGroupConcept().equals(groupConceptUIID)) {
                        mFormDisplayPageView.editAndAttachSelectQuestionRadioButton(question, sectionLinearLayout, selectOneField);
                    }
                }
            }
        }
    }

    private void addQuestionEditRepeat(Question question, LinearLayout sectionLinearLayout, String groupConceptUIID, String repeatConceptUUID) {
        question.setGenderSpecificConcept(this.personGender);
        if (question.getQuestionOptions().getRendering().equals("number")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept()) && inputField.getGroupConcept().equals(groupConceptUIID) && inputField.getRepeatConcept().equals(repeatConceptUUID)) {
                        mFormDisplayPageView.editAndAttachNumericQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("date")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept()) && inputField.getGroupConcept().equals(groupConceptUIID) && inputField.getRepeatConcept().equals(repeatConceptUUID)) {
                        if (question.getType().equals("encounterDate")) {
                            mFormDisplayPageView.editAndAttachDateQuestionEditText(question, sectionLinearLayout, DateUtils.convertTime(DateUtils.convertTime(this.encounterDate), DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT));
                        } else {
                            mFormDisplayPageView.editAndAttachDateQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                        }
                    }
                }
            }

        }

        if (question.getQuestionOptions().getRendering().equals("text")) {
            for (List<InputField> inputFields : mInputFields) {
                for (InputField inputField : inputFields) {
                    if (question.getQuestionOptions().getConcept().equals(inputField.getConcept()) && inputField.getGroupConcept().equals(groupConceptUIID) && inputField.getRepeatConcept().equals(repeatConceptUUID)) {
                        mFormDisplayPageView.editAndAttachTextQuestionEditText(question, sectionLinearLayout, inputField.getValueAll());
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("select")) {
            for (List<SelectOneField> selectOneFields : mSelectOneField) {
                for (SelectOneField selectOneField : selectOneFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectOneField.getConcept()) && selectOneField.getGroupConcept().equals(groupConceptUIID) && selectOneField.getRepeatConcept().equals(repeatConceptUUID)) {

                        mFormDisplayPageView.editAndAttachSelectQuestionDropdown(question, sectionLinearLayout, selectOneField);
                    }
                }
            }

        }
        if (question.getQuestionOptions().getRendering().equals("check")) {

            for (List<SelectManyFields> selectManyFields : mSelectManyFields) {
                for (SelectManyFields selectManyField : selectManyFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectManyField.getConcept()) && selectManyField.getGroupConcept().equals(groupConceptUIID)&& selectManyField.getRepeatConcept().equals(repeatConceptUUID)) {
                        mFormDisplayPageView.editAndAttachSelectQuestionCheckBox(question, sectionLinearLayout, selectManyField);
                    }
                }
            }
        }
        if (question.getQuestionOptions().getRendering().equals("radio")) {
            for (List<SelectOneField> selectOneFields : mSelectOneField) {
                for (SelectOneField selectOneField : selectOneFields) {
                    if (question.getQuestionOptions().getConcept().equals(selectOneField.getConcept()) && selectOneField.getGroupConcept().equals(groupConceptUIID) && selectOneField.getRepeatConcept().equals(repeatConceptUUID)) {
                        mFormDisplayPageView.editAndAttachSelectQuestionRadioButton(question, sectionLinearLayout, selectOneField);
                    }
                }
            }
        }
    }

}
