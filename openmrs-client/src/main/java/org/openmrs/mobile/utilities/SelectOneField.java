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

public class SelectOneField implements Serializable, Parcelable {

    private String concept = null;
    private Answer chosenAnswer = null;
    private List<Answer> answerList;

    // Added
    private int id;
    private String obs = null;
    private String questionLabel;
    private String answerLabel;
    private String groupConcept;
    private String repeatConcept;

    public SelectOneField(List<Answer> answerList, String concept) {
        this.answerList = answerList;
        this.concept = concept;
        this.id = Math.abs(concept.hashCode());
    }

    //Added
    public SelectOneField(List<Answer> answerList, String concept, String obs) {
        this.answerList = answerList;
        this.concept = concept;
        this.obs = obs;
        this.id = Math.abs(concept.hashCode());
    }

    public SelectOneField(List<Answer> answerList, String concept, String repeatConcept, String obs) {
        this.answerList = answerList;
        this.concept = concept;
        this.id = Math.abs((concept + repeatConcept).hashCode());
    }


    public void setAnswer(int answerPosition) {
        if (answerPosition != -1 && answerPosition < answerList.size()) {
            chosenAnswer = answerList.get(answerPosition);
        }
        if (answerPosition == -1) {
            chosenAnswer = null;
        }
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<Answer> getAnswerList() {
        return answerList;
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

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
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

    @Override
    public int describeContents() {
        return 0;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.concept);
        dest.writeSerializable(this.chosenAnswer);
        dest.writeList(this.answerList);
        dest.writeString(this.groupConcept);
        dest.writeString(this.repeatConcept);
    }

    protected SelectOneField(Parcel in) {
        this.id = in.readInt();
        this.concept = in.readString();
        this.chosenAnswer = (Answer) in.readSerializable();
        this.answerList = new ArrayList<>();
        in.readList(this.answerList, Answer.class.getClassLoader());
        this.groupConcept = in.readString();
        this.repeatConcept = in.readString();
    }

    public static final Parcelable.Creator<SelectOneField> CREATOR = new Parcelable.Creator<SelectOneField>() {
        @Override
        public SelectOneField createFromParcel(Parcel source) {
            return new SelectOneField(source);
        }

        @Override
        public SelectOneField[] newArray(int size) {
            return new SelectOneField[size];
        }
    };
}
