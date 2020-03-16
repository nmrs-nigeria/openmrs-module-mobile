/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.utilities;

import android.os.Parcel;
import android.os.Parcelable;

import org.openmrs.mobile.models.Answer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectManyFields implements Serializable, Parcelable {

    private String concept = null;
    private Answer chosenAnswer = null;
    private List<Answer> answerList;
    private List<Answer> chosenAnswerList = new ArrayList<>();
    private List<Integer> answerPositionList = new ArrayList<>();
    private String obs = null;

    private String questionLabel;
    private String answerLabel;
    private String groupConcept;
    private String repeatConcept;
    public SelectManyFields(List<Answer> answerList, String concept) {
        this.answerList = answerList;
        this.concept = concept;
    }

    public SelectManyFields(List<Answer> answerList, String concept, String obs) {
        this.answerList = answerList;
        this.concept = concept;
        this.obs = obs;
    }

    public void setAnswer(int answerPosition) {
        if (answerPosition < answerList.size()) {
            chosenAnswer = answerList.get(answerPosition);
            chosenAnswerList.add(chosenAnswer);
            answerPositionList.add(answerPosition);
        }
        if (answerPosition == -1) {
            chosenAnswer = null;
        }
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

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }
    public Answer getAnswer(int i){
        return chosenAnswerList.get(i);
    }
    public List<Answer> getChosenAnswerList() {
        return chosenAnswerList;
    }

    public void setChosenAnswerList(List<Answer> chosenAnswerList) {
        this.chosenAnswerList = chosenAnswerList;
    }

    public List<Integer> getAnswerPositionList() {
        return answerPositionList;
    }

    public void setAnswerPositionList(List<Integer> answerPositionList) {
        this.answerPositionList = answerPositionList;
    }

    public String getGroupConcept() {
        return groupConcept;
    }

    public void setGroupConcept(String groupConcept) {
        this.groupConcept = groupConcept;
    }

    public String getRepeatConcept() {
        return repeatConcept;
    }

    public void setRepeatConcept(String repeatConcept) {
        this.repeatConcept = repeatConcept;
    }

    public void setChosenAnswer(Answer chosenAnswer) {
        this.chosenAnswer = chosenAnswer;
    }

    public Answer getChosenAnswer() {
        return chosenAnswer;
    }

    public String getConcept() {
        return concept;
    }

    public int getChosenAnswerPosition() {
        return answerList.indexOf(chosenAnswer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.concept);
        dest.writeSerializable(this.chosenAnswer);
        dest.writeList(this.answerList);
        dest.writeList(this.chosenAnswerList);
        dest.writeList(this.answerPositionList);
        dest.writeString(this.obs);
//        dest.writeString(this.answerLabel);
        dest.writeString(this.questionLabel);
        dest.writeString(this.groupConcept);
        dest.writeString(this.repeatConcept);
    }

    protected SelectManyFields(Parcel in) {
        this.concept = in.readString();
        this.chosenAnswer = (Answer) in.readSerializable();
        this.answerList = new ArrayList<Answer>();
        in.readList(this.answerList, Answer.class.getClassLoader());
        in.readList(this.chosenAnswerList, Answer.class.getClassLoader());
        in.readList(this.answerPositionList, Answer.class.getClassLoader());
        this.obs = in.readString();
//        this.answerLabel = in.readString();
        this.questionLabel = in.readString();
        this.groupConcept = in.readString();
        this.repeatConcept = in.readString();
    }

    public static final Parcelable.Creator<SelectManyFields> CREATOR = new Parcelable.Creator<SelectManyFields>() {
        @Override
        public SelectManyFields createFromParcel(Parcel source) {
            return new SelectManyFields(source);
        }

        @Override
        public SelectManyFields[] newArray(int size) {
            return new SelectManyFields[size];
        }
    };
}
