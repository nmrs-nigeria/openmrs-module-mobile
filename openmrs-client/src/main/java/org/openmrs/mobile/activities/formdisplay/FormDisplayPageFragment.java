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

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.formprogramlist.FormProgramActivity;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.bundle.FormFieldsWrapper;
import org.openmrs.mobile.listeners.watcher.NumericValidatorWatcher;
import org.openmrs.mobile.models.Answer;
import org.openmrs.mobile.models.Condition;
import org.openmrs.mobile.models.Control;
import org.openmrs.mobile.models.Question;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.InputField;
import org.openmrs.mobile.utilities.RangeEditText;
import org.openmrs.mobile.utilities.SelectManyFields;
import org.openmrs.mobile.utilities.SelectOneField;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FormDisplayPageFragment extends ACBaseFragment<FormDisplayContract.Presenter.PagePresenter> implements FormDisplayContract.View.PageView {

    private List<InputField> inputFields = new ArrayList<>();
    private List<SelectOneField> selectOneFields = new ArrayList<>();
    private List<SelectManyFields> selectManyFields = new ArrayList<>();
    private LinearLayout mParent;
    boolean shouldMoveOne = false;
    boolean shouldMoveThirteen = false;
    Map<Integer, Integer> idManager = new HashMap<Integer, Integer>();
    String regexStr = "^[1-9]\\d*(\\.\\d+)?$";
    private static final String LABEL_ID_SALT = "UNIQUE_LABEL";
    private static final String LABEL_ID_CHECK = "CHECK_LABEL";
    private static final String CONTROL_TYPE_TEXT = "text";
    private static final String CONTROL_TYPE_DATE = "date";
    private static final String CONTROL_TYPE_SELECT = "select";
    private static final String CONTROL_TYPE_CHECK = "check";
    private static final String CONTROL_TYPE_RADIO = "radio";
    private static final String CONTROL_TYPE_SECTION = "section";

    public static FormDisplayPageFragment newInstance() {
        return new FormDisplayPageFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_form_display, container, false);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mParent = root.findViewById(R.id.sectionContainer);
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        FormFieldsWrapper formFieldsWrapper = new FormFieldsWrapper(getInputFields(), getSelectOneFields(), getSelectManyFields());
        outState.putSerializable(ApplicationConstants.BundleKeys.FORM_FIELDS_BUNDLE, formFieldsWrapper);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            FormFieldsWrapper formFieldsWrapper = (FormFieldsWrapper) savedInstanceState.getSerializable(ApplicationConstants.BundleKeys.FORM_FIELDS_BUNDLE);
            inputFields = formFieldsWrapper.getInputFields();

            for (InputField field : inputFields) {
                View v = getActivity().findViewById(field.getId());
                if (v != null && v instanceof DiscreteSeekBar) {
                    DiscreteSeekBar sb = (DiscreteSeekBar) v;
                    sb.setProgress(field.getValue().intValue());
                }
                if (field.isRed()) {
                    RangeEditText ed = getActivity().findViewById(field.getId());
                    ed.setTextColor(ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
                }
            }
            selectOneFields = formFieldsWrapper.getSelectOneFields();
        }
    }

    @Override
    public void attachSectionToView(LinearLayout linearLayout) {
        mParent.addView(linearLayout);
    }

    @Override
    public void attachQuestionToSection(LinearLayout section, LinearLayout question) {
        section.addView(question);
    }


    @Override
    public void createAndAttachNumericQuestionEditText(Question question, LinearLayout sectionLinearLayout) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RangeEditText ed = new RangeEditText(getActivity());
        FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
        if (question.getRepeatConcept() == null) {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getType());
            field.setGroupConcept(question.getGroupConcept());
            field.setQuestionLabel(question.getLabel());
            InputField inputField = getInputField(field.getConcept());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                if (question.getRequired() != null) {
                    if (question.getRequired().equals("yes")) {
                        field.setRequired(true);
                    }
                }
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            if (question.getRepeatConcept() == null) {
                vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            } else {
                vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            }
            sectionLinearLayout.addView(vv);


            if ((question.getQuestionOptions().getMax() != null) && (!(question.getQuestionOptions().isAllowDecimal()))) {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setLowerlimit(Double.parseDouble(question.getQuestionOptions().getMax()));
                ed.setUpperlimit(Double.parseDouble(question.getQuestionOptions().getMin()));
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                ed.setPadding(15, 0, 0, 15);
                if (question.getQuestionOptions().isAllowDecimal()) {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                ed.setId(field.getId());
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
//            ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                TextWatcher textWatcher = new NumericValidatorWatcher(ed, Double.parseDouble(question.getQuestionOptions().getMax()), Double.parseDouble(question.getQuestionOptions().getMin()));
                ed.addTextChangedListener(textWatcher);
                sectionLinearLayout.addView(ed, layoutParams);
            } else {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setLowerlimit(-1.0);
                ed.setUpperlimit(-1.0);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                ed.setPadding(15, 0, 0, 10);
                if (question.getQuestionOptions().isAllowDecimal()) {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                ed.setId(field.getId());
//                ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                sectionLinearLayout.addView(ed, layoutParams);
            }
            if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
                ed.setVisibility(View.VISIBLE);
                vv.setVisibility(View.VISIBLE);
            } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
                ed.setVisibility(View.GONE);
                vv.setVisibility(View.GONE);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        } else {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getRepeatConcept(), question.getType());
            field.setGroupConcept(question.getGroupConcept());
            field.setQuestionLabel(question.getLabel());
            field.setRepeatConcept(question.getRepeatConcept());
            InputField inputField = getInputField(field.getConcept(), field.getId());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                if (question.getRequired() != null) {
                    if (question.getRequired().equals("yes")) {
                        field.setRequired(true);
                    }
                }
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            if (question.getRepeatConcept() == null) {
                vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            } else {
                vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            }
            sectionLinearLayout.addView(vv);


            if ((question.getQuestionOptions().getMax() != null) && (!(question.getQuestionOptions().isAllowDecimal()))) {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setLowerlimit(Double.parseDouble(question.getQuestionOptions().getMax()));
                ed.setUpperlimit(Double.parseDouble(question.getQuestionOptions().getMin()));
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                ed.setPadding(15, 0, 0, 10);
                if (question.getQuestionOptions().isAllowDecimal()) {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                ed.setId(field.getId());
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
//            ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                TextWatcher textWatcher = new NumericValidatorWatcher(ed, Double.parseDouble(question.getQuestionOptions().getMax()), Double.parseDouble(question.getQuestionOptions().getMin()));
                ed.addTextChangedListener(textWatcher);
                sectionLinearLayout.addView(ed, layoutParams);
            } else {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setLowerlimit(-1.0);
                ed.setUpperlimit(-1.0);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                ed.setPadding(15, 0, 0, 10);
                if (question.getQuestionOptions().isAllowDecimal()) {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                ed.setId(field.getId());
//                ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                sectionLinearLayout.addView(ed, layoutParams);
            }
            if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
                ed.setVisibility(View.VISIBLE);
                vv.setVisibility(View.VISIBLE);
            } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
                ed.setVisibility(View.GONE);
                vv.setVisibility(View.GONE);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        }


    }

    @Override
    public void createAndAttachDateQuestionEditText(Question question, LinearLayout sectionLinearLayout) {
        Calendar myCalendar = Calendar.getInstance();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RangeEditText ed = new RangeEditText(getActivity());
        if (question.getRepeatConcept() == null) {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            InputField inputField = getInputField(field.getConcept());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                if (question.getRequired() != null) {
                    if (question.getRequired().equals("yes")) {
                        field.setRequired(true);
                    }
                }
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);
            //        sectionLinearLayout.addView(generateTextView(question.getLabel()));
            ed.setClickable(true);
            ed.setFocusable(false);
            ed.setName(question.getLabel());
            //        ed.setHint(question.getLabel());
            ed.setInputType(InputType.TYPE_CLASS_DATETIME);
            ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            //        ed.setTypeface(Typeface.DEFAULT_BOLD);
            ed.setId(field.getId());
            ed.setPadding(15, 0, 0, 10);
            //        ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
            FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
            sectionLinearLayout.addView(ed, layoutParams);

            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel(ed, myCalendar);
                }

            };
            ed.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                    mDatePicker.show();
                }
            });

            if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
                ed.setVisibility(View.VISIBLE);
                vv.setVisibility(View.VISIBLE);
            } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
                ed.setVisibility(View.GONE);
                vv.setVisibility(View.GONE);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        } else {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getRepeatConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            field.setRepeatConcept(question.getRepeatConcept());
            InputField inputField = getInputField(field.getConcept());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                if (question.getRequired() != null) {
                    if (question.getRequired().equals("yes")) {
                        field.setRequired(true);
                    }
                }
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);
            //        sectionLinearLayout.addView(generateTextView(question.getLabel()));
            //        ed.setEnabled(false);
            ed.setClickable(true);
            ed.setFocusable(false);
            ed.setName(question.getLabel());
            //        ed.setHint(question.getLabel());
            ed.setInputType(InputType.TYPE_CLASS_DATETIME);
            ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            //        ed.setTypeface(Typeface.DEFAULT_BOLD);
            ed.setId(field.getId());
            ed.setPadding(15, 0, 0, 10);
            //        ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
            FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
            sectionLinearLayout.addView(ed, layoutParams);

            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel(ed, myCalendar);
                }

            };
            ed.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                    mDatePicker.show();
                }
            });

            if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
                ed.setVisibility(View.VISIBLE);
                vv.setVisibility(View.VISIBLE);
            } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
                ed.setVisibility(View.GONE);
                vv.setVisibility(View.GONE);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        }

    }

    @Override
    public void createAndAttachTextQuestionEditText(Question question, LinearLayout sectionLinearLayout) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RangeEditText ed = new RangeEditText(getActivity());
        if (question.getRepeatConcept() == null) {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            InputField inputField = getInputField(field.getConcept());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                if (question.getRequired() != null) {
                    if (question.getRequired().equals("yes")) {
                        field.setRequired(true);
                    }
                }
                inputFields.add(field);
            }
            //        sectionLinearLayout.addView(generateTextView(question.getLabel()));
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);

            if (question.getQuestionOptions().getRendering().equals("text")) {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setInputType(InputType.TYPE_CLASS_TEXT);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                //            ed.setTypeface(Typeface.DEFAULT_BOLD);
                ed.setPadding(15, 0, 0, 10);
                ed.setId(field.getId());
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                sectionLinearLayout.addView(ed, layoutParams);
            } else {
                ed.setName(question.getLabel());
                ed.setSingleLine(false);
                //            ed.setHint(question.getLabel());
                ed.setInputType(InputType.TYPE_CLASS_DATETIME);
                ed.setId(field.getId());
                sectionLinearLayout.addView(ed, layoutParams);
            }
            if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
                ed.setVisibility(View.VISIBLE);
                vv.setVisibility(View.VISIBLE);
            } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
                ed.setVisibility(View.GONE);
                vv.setVisibility(View.GONE);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }

            }
        } else {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getRepeatConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            field.setRepeatConcept(question.getRepeatConcept());
//            InputField inputField = getInputField(field.getConcept());
            InputField inputField = getInputField(field.getConcept(), field.getId());

            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                if (question.getRequired() != null) {
                    if (question.getRequired().equals("yes")) {
                        field.setRequired(true);
                    }
                }
                inputFields.add(field);
            }
            //        sectionLinearLayout.addView(generateTextView(question.getLabel()));
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);

            if (question.getQuestionOptions().getRendering().equals("text")) {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setInputType(InputType.TYPE_CLASS_TEXT);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                //            ed.setTypeface(Typeface.DEFAULT_BOLD);
                ed.setPadding(15, 0, 0, 10);
                ed.setId(field.getId());
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                sectionLinearLayout.addView(ed, layoutParams);
            } else {
                ed.setName(question.getLabel());
                ed.setSingleLine(false);
                //            ed.setHint(question.getLabel());
                ed.setInputType(InputType.TYPE_CLASS_DATETIME);
                ed.setId(field.getId());
                sectionLinearLayout.addView(ed, layoutParams);
            }
            if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
                ed.setVisibility(View.VISIBLE);
                vv.setVisibility(View.VISIBLE);
            } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
                ed.setVisibility(View.GONE);
                vv.setVisibility(View.GONE);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }

            }
        }
    }


    private void updateLabel(EditText ed, Calendar cal) {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        ed.setText(sdf.format(cal.getTime()));
    }

    private View generateTextView(String text) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 20, 0, 10);
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    public InputField getInputField(String concept) {
        for (InputField inputField : inputFields) {
            if (concept.equals(inputField.getConcept())) {
                return inputField;
            }
        }
        return null;
    }

    public InputField getInputField(String concept, int hashConcept) {
        for (InputField inputField : inputFields) {
            if (concept.equals(inputField.getConcept()) && hashConcept == inputField.getId()) {
                return inputField;
            }
        }
        return null;
    }

    public SelectOneField getSelectOneField(String concept) {
        for (SelectOneField selectOneField : selectOneFields) {
            if (concept.equals(selectOneField.getConcept())) {
                return selectOneField;
            }
        }
        return null;
    }

    public SelectOneField getSelectOneField(String concept, int hashConcept) {
        for (SelectOneField selectOneField : selectOneFields) {
            if (concept.equals(selectOneField.getConcept()) && hashConcept == selectOneField.getId()) {
                return selectOneField;
            }
        }
        return null;
    }

    @Override
    public void createAndAttachSelectQuestionDropdown(Question question, LinearLayout sectionLinearLayout) {
        if (question.getRepeatConcept() == null) {
            TextView textView = new TextView(getActivity());
            textView.setPadding(10, 20, 0, 0);
            textView.setText(question.getLabel());
            textView.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            Spinner spinner = (Spinner) getActivity().getLayoutInflater().inflate(R.layout.form_dropdown, null);
            LinearLayout questionLinearLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams questionLinearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            questionLinearLayout.setOrientation(LinearLayout.VERTICAL);
            questionLinearLayoutParams.gravity = Gravity.START;
            questionLinearLayout.setLayoutParams(questionLinearLayoutParams);

            List<String> answerLabels = new ArrayList<>();
            answerLabels.add("");
            for (Answer answer : question.getQuestionOptions().getAnswers()) {
                answerLabels.add(answer.getLabel());
            }

            SelectOneField spinnerField = new SelectOneField(question.getQuestionOptions().getAnswers(),
                    question.getQuestionOptions().getConcept(), question.getType());

            spinnerField.setQuestionLabel(question.getLabel());
            spinnerField.setGroupConcept(question.getGroupConcept());
            spinner.setId(customHashCode(question.getQuestionOptions().getConcept()));

            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, answerLabels) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    FontsUtil.setFont(text, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                    return view;
                }
            };
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);

            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


            questionLinearLayout.addView(textView);
            questionLinearLayout.addView(spinner);
            FontsUtil.setFont(questionLinearLayout, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.setLayoutParams(linearLayoutParams);
            sectionLinearLayout.addView(questionLinearLayout);

            SelectOneField selectOneField = getSelectOneField(spinnerField.getConcept());
            if (selectOneField != null) {
                spinner.setSelection(selectOneField.getChosenAnswerPosition());
                setOnItemSelectedListener(spinner, selectOneField, question, 0);
            } else {
                setOnItemSelectedListener(spinner, spinnerField, question, -1);
                selectOneFields.add(spinnerField);
            }

            if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
                spinner.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
            } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
                spinner.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
            }

            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    spinner.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
            }
        } else {
            TextView textView = new TextView(getActivity());
            textView.setPadding(10, 20, 0, 0);
            textView.setText(question.getLabel());
            textView.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            Spinner spinner = (Spinner) getActivity().getLayoutInflater().inflate(R.layout.form_dropdown, null);
            LinearLayout questionLinearLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams questionLinearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            questionLinearLayout.setOrientation(LinearLayout.VERTICAL);
            questionLinearLayoutParams.gravity = Gravity.START;
            questionLinearLayout.setLayoutParams(questionLinearLayoutParams);

            List<String> answerLabels = new ArrayList<>();
            answerLabels.add("");
            for (Answer answer : question.getQuestionOptions().getAnswers()) {
                answerLabels.add(answer.getLabel());
            }

            SelectOneField spinnerField = new SelectOneField(question.getQuestionOptions().getAnswers(),
                    question.getQuestionOptions().getConcept(), question.getRepeatConcept(), question.getType());

            spinnerField.setQuestionLabel(question.getLabel());
            spinnerField.setGroupConcept(question.getGroupConcept());
            spinnerField.setRepeatConcept(question.getRepeatConcept());
            spinner.setId(customHashCode(question.getQuestionOptions().getConcept() + question.getRepeatConcept()));

            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, answerLabels) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    FontsUtil.setFont(text, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                    return view;
                }
            };
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);

            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


            questionLinearLayout.addView(textView);
            questionLinearLayout.addView(spinner);
            FontsUtil.setFont(questionLinearLayout, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.setLayoutParams(linearLayoutParams);
            sectionLinearLayout.addView(questionLinearLayout);

            SelectOneField selectOneField = getSelectOneField(spinnerField.getConcept(), spinnerField.getId());
            if (selectOneField != null) {
                spinner.setSelection(selectOneField.getChosenAnswerPosition());
                setOnItemSelectedListener(spinner, selectOneField, question, 0);
            } else {
                setOnItemSelectedListener(spinner, spinnerField, question, -1);
                selectOneFields.add(spinnerField);
            }

            if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
                spinner.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
            } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
                spinner.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
            }

            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    spinner.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
            }




        }
    }

    @Override
    public void createAndAttachSelectQuestionCheckBox(Question question, LinearLayout sectionLinearLayout) {
        TextView textView = new TextView(getActivity());
        textView.setPadding(10, 10, 0, 10);
        textView.setText(question.getLabel());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        FontsUtil.setFont(textView, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
        textView.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
        final LinearLayout linearLayoutCheckBox = new LinearLayout(getActivity());
        linearLayoutCheckBox.setOrientation(LinearLayout.VERTICAL);
        linearLayoutCheckBox.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_CHECK));
        SelectManyFields checkField = new SelectManyFields(question.getQuestionOptions().getAnswers(),
                question.getQuestionOptions().getConcept(), question.getType());
        checkField.setQuestionLabel(question.getLabel());
        checkField.setGroupConcept(question.getGroupConcept());
        int i = 0;

        for (Answer answer : question.getQuestionOptions().getAnswers()) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(answer.getLabel());
            checkBox.setId(customHashCode(answer.getConcept()));
            FontsUtil.setFont(checkBox, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCheckboxClicked(v, checkField, checkBox.getId());
                }
            });
            linearLayoutCheckBox.addView(checkBox);
            idManager.put(customHashCode(answer.getConcept()), i);
            i++;
        }


        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        sectionLinearLayout.addView(textView);
        sectionLinearLayout.addView(linearLayoutCheckBox);

        sectionLinearLayout.setLayoutParams(linearLayoutParams);

        SelectManyFields selectManyField = getSelectManyFields(checkField.getConcept());
        if (selectManyField != null) {
            if (selectManyField.getChosenAnswerPosition() > 0) {
                for (int pos : selectManyField.getAnswerPositionList()) {
                    Answer answer = selectManyField.getAnswer(pos);
                    CheckBox checkBox = new CheckBox(getActivity());
                    checkBox.setText(answer.getLabel());
                    checkBox.setId(pos);
                    checkBox.setChecked(true);
                    onCheckboxSelectedClicked(checkBox, selectManyField, pos);
                }

            }
        } else {
            selectManyFields.add(checkField);
        }

        if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("show")) {
            linearLayoutCheckBox.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else if (question.getGenderSpecificConcept() != null && question.getGenderSpecificConcept().equals("hide")) {
            linearLayoutCheckBox.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }

        if (question.getDefaultDisplay() != null) {
            if (question.getDefaultDisplay().equals("hide")) {
                linearLayoutCheckBox.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
            }
        }
    }

    private void setOnItemSelectedListener(Spinner spinner, final SelectOneField spinnerField, Question question, int mPosition) {

        if (mPosition != -1) {
            spinnerField.setAnswer(mPosition);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    spinnerField.setAnswer(i - 1);
                }

                if (StringUtils.notEmpty(question.getChildControl())) {
                    triggerDependantControl(spinnerField.getChosenAnswer(), question.getChildControl());
                }
                FormDisplayActivity fdActivity = (FormDisplayActivity) getActivity();

                // Check if the test result is positive or negative in client intake form
                if (spinnerField.getConcept().equals("1fb46619-abcd-405a-81c9-3c9018473729")) {
                    if (spinnerField.getChosenAnswer()!= null && spinnerField.getChosenAnswer().getLabel().equals("Positive")) {
                        fdActivity.setmStep(1);
                        fdActivity.setEligible(true);
                    }else if(spinnerField.getChosenAnswer()!= null && spinnerField.getChosenAnswer().getLabel().equals("Negative")){
                        fdActivity.setmStep(6);
                        fdActivity.setEligible(false);

                    }

                }
                // Check if the pharmacy order form regimen line is adult or child
                if (spinnerField.getConcept().equals("91bf2c14-1677-4c7f-be1b-99a2b64231b4") && spinnerField.getChosenAnswer()!= null) {
                    Spinner spinner = (Spinner) mParent.findViewById(customHashCode("718864d2-dd9b-4210-80fe-21e6f8dcbb14"));
                    String text = spinner.getSelectedItem().toString();
                    Spinner spinnerFirstLineAdult= mParent.findViewById(customHashCode("164506AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
                    TextView textView =  mParent.findViewById(customHashCode("164506AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + LABEL_ID_SALT ));
                    Spinner spinnerSecondLineAdult= mParent.findViewById(customHashCode("164513AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
                    TextView textViewSecond =  mParent.findViewById(customHashCode("164513AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + LABEL_ID_SALT ));
                    Spinner spinnerThirdLineAdult= mParent.findViewById(customHashCode("73fbac92-4663-43c1-ad89-5fe0bc2e52c7"));
                    TextView textViewThird =  mParent.findViewById(customHashCode("73fbac92-4663-43c1-ad89-5fe0bc2e52c7" + LABEL_ID_SALT ));

                    Spinner spinnerFirstLineChild= mParent.findViewById(customHashCode("164507AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
                    TextView textViewChild =  mParent.findViewById(customHashCode("164507AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + LABEL_ID_SALT ));
                    Spinner spinnerSecondLineChild= mParent.findViewById(customHashCode("164514AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
                    TextView textViewSecondChild =  mParent.findViewById(customHashCode("164514AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + LABEL_ID_SALT ));
                    Spinner spinnerThirdLineChild= mParent.findViewById(customHashCode("032e80e0-ed50-4e88-8ce3-7a2dfa40d0ae"));
                    TextView textViewThirdChild =  mParent.findViewById(customHashCode("032e80e0-ed50-4e88-8ce3-7a2dfa40d0ae" + LABEL_ID_SALT ));
                    if (text.equals("Adult")){
                        spinnerFirstLineChild.setVisibility(View.GONE);
                        textViewChild.setVisibility(View.GONE);
                        spinnerSecondLineChild.setVisibility(View.GONE);
                        textViewSecondChild.setVisibility(View.GONE);
                        spinnerThirdLineChild.setVisibility(View.GONE);
                        textViewThirdChild.setVisibility(View.GONE);
                        if(spinnerField.getChosenAnswer()!= null && spinnerField.getChosenAnswer().getLabel().equals("First Line")) {
                            spinnerFirstLineAdult.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            spinnerSecondLineAdult.setVisibility(View.GONE);
                            textViewSecond.setVisibility(View.GONE);
                            spinnerThirdLineAdult.setVisibility(View.GONE);
                            textViewThird.setVisibility(View.GONE);
                        }
                        if(spinnerField.getChosenAnswer()!= null && spinnerField.getChosenAnswer().getLabel().equals("Second Line")) {
                            spinnerSecondLineAdult.setVisibility(View.VISIBLE);
                            textViewSecond.setVisibility(View.VISIBLE);
                            spinnerFirstLineAdult.setVisibility(View.GONE);
                            textView.setVisibility(View.GONE);
                            spinnerThirdLineAdult.setVisibility(View.GONE);
                            textViewThird.setVisibility(View.GONE);
                        }
                        if(spinnerField.getChosenAnswer()!= null && spinnerField.getChosenAnswer().getLabel().equals("Salvage")) {
                            spinnerThirdLineAdult.setVisibility(View.VISIBLE);
                            textViewThird.setVisibility(View.VISIBLE);
                            spinnerFirstLineAdult.setVisibility(View.GONE);
                            textView.setVisibility(View.GONE);
                            spinnerSecondLineAdult.setVisibility(View.GONE);
                            textViewSecond.setVisibility(View.GONE);
                        }
                    } else if(text.equals("Child")){
                        spinnerFirstLineAdult.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        spinnerSecondLineAdult.setVisibility(View.GONE);
                        textViewSecond.setVisibility(View.GONE);
                        spinnerThirdLineAdult.setVisibility(View.GONE);
                        textViewThird.setVisibility(View.GONE);
                        if(spinnerField.getChosenAnswer()!= null && spinnerField.getChosenAnswer().getLabel().equals("First Line")) {
                            spinnerFirstLineChild.setVisibility(View.VISIBLE);
                            textViewChild.setVisibility(View.VISIBLE);
                            spinnerSecondLineChild.setVisibility(View.GONE);
                            textViewSecondChild.setVisibility(View.GONE);
                            spinnerThirdLineChild.setVisibility(View.GONE);
                            textViewThirdChild.setVisibility(View.GONE);
                        }
                        if(spinnerField.getChosenAnswer()!= null && spinnerField.getChosenAnswer().getLabel().equals("Second Line")) {
                            spinnerSecondLineChild.setVisibility(View.VISIBLE);
                            textViewSecondChild.setVisibility(View.VISIBLE);
                            spinnerFirstLineChild.setVisibility(View.GONE);
                            textViewChild.setVisibility(View.GONE);
                            spinnerThirdLineChild.setVisibility(View.GONE);
                            textViewThirdChild.setVisibility(View.GONE);
                        }
                        if(spinnerField.getChosenAnswer()!= null && spinnerField.getChosenAnswer().getLabel().equals("Salvage")) {
                            spinnerThirdLineChild.setVisibility(View.VISIBLE);
                            textViewThirdChild.setVisibility(View.VISIBLE);
                            spinnerFirstLineChild.setVisibility(View.GONE);
                            textViewChild.setVisibility(View.GONE);
                            spinnerSecondLineChild.setVisibility(View.GONE);
                            textViewSecondChild.setVisibility(View.GONE);
                        }
                    }
                }
//
                if (spinnerField.getConcept().equals("46320a3c-72cf-48b6-ad32-a4ce6912de91") ||
                        spinnerField.getConcept().equals("6c4ebfeb-f613-493a-993a-ff5efa012e6d") ||
                        spinnerField.getConcept().equals("0183f460-8798-46d3-8a9d-f92569b4d73c") ||
                        spinnerField.getConcept().equals("828b885c-c191-44d3-9f63-b1d1ec8e7457") ||
                        spinnerField.getConcept().equals("8f82fdc8-a062-46ac-8070-f7601a017f64") ||
                        spinnerField.getConcept().equals("023fb1ae-c41e-4fc3-b512-7a47e29f77b1") ||
                        spinnerField.getConcept().equals("ea49fa12-cfea-4178-863c-eefe97051cb1") ||
                        spinnerField.getConcept().equals("8dda2a65-c030-4e35-8ad1-942543047e26") ||
                        spinnerField.getConcept().equals("26605b5d-127d-4e1f-98af-1f537c6a3b48")){

                    if (spinnerField.getChosenAnswer() != null && (spinnerField.getChosenAnswer().getLabel().equals("Positive") || spinnerField.getChosenAnswer().getLabel().equals("Yes"))) {
                        shouldMoveOne = true;

                    }
                }
                if (spinnerField.getConcept().equals("cacc7904-58f6-4c71-8525-2e663021a73b") ||
                        spinnerField.getConcept().equals("ecb19ce0-4b7f-4092-a168-69030f37f326") ||
                        spinnerField.getConcept().equals("b58a1127-1862-438e-9069-ad3e79955cfa") ||
                        spinnerField.getConcept().equals("1c733781-71e5-4d3b-99d6-0cfdddc3281c") ||
                        spinnerField.getConcept().equals("3b027b15-1b39-41e3-8341-2f2f1f747fc5") ||
                        spinnerField.getConcept().equals("30909867-d0cd-47f7-9cca-14896c92fd4d") ||
                        spinnerField.getConcept().equals("8dda2a65-c030-4e35-8ad1-942543047e26")){

                    if (spinnerField.getChosenAnswer() != null && (spinnerField.getChosenAnswer().getLabel().equals("Positive") || spinnerField.getChosenAnswer().getLabel().equals("Yes"))) {
                        shouldMoveOne = true;
                    }
                }
                if (spinnerField.getConcept().equals("26605b5d-127d-4e1f-98af-1f537c6a3b48")){
                    Spinner spinner = (Spinner) mParent.findViewById(customHashCode("26605b5d-127d-4e1f-98af-1f537c6a3b48"));
                    String text = spinner.getSelectedItem().toString();
                    if (StringUtils.notEmpty(text)){
                        fdActivity.setValid(true);
                    }
                }
                if (spinnerField.getConcept().equals("8dda2a65-c030-4e35-8ad1-942543047e26")){
                    Spinner spinner = (Spinner) mParent.findViewById(customHashCode("8dda2a65-c030-4e35-8ad1-942543047e26"));
                    String text = spinner.getSelectedItem().toString();
                    if (StringUtils.notEmpty(text)){
                        fdActivity.setValid(true);
                    }
                }
                if (shouldMoveOne) {
                    fdActivity.setEligible(true);
                }


                if (question.getQuestionOptions().getControl() != null) {
                    if (spinnerField.getChosenAnswer() != null && spinnerField.getChosenAnswer().getLabel() != null) {
                        showHideControl(question.getQuestionOptions().getControl(), spinnerField.getChosenAnswer());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinnerField.setAnswer(-1);
            }
        });
    }

    private void triggerDependantControl(Answer choosenAnswer, String childControlConcept) {

        List<String> answerLabels = new ArrayList<>();
        for (String answer : choosenAnswer.getChildLabels()) {
            answerLabels.add(answer);
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, answerLabels);

        Spinner childSpinner = (Spinner) mParent.findViewById(customHashCode(childControlConcept));
        if (childSpinner != null) {

            childSpinner.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private void showHideControl(Control control, Answer answer) {
        for (Condition conditionOptions : control.getConditionOptions()) {
            if (conditionOptions.getControlType().equals(CONTROL_TYPE_SELECT)) {
                Spinner childSpinner = (Spinner) mParent.findViewById(customHashCode(conditionOptions.getChildControl()));
                TextView label = (TextView) mParent.findViewById(customHashCode(conditionOptions.getChildControl() + LABEL_ID_SALT));

                if (childSpinner != null) {
                    childSpinner.setSelected(false);
                    if (conditionOptions.getDisplayType().equals("show") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        childSpinner.setVisibility(View.VISIBLE);
                        if (label != null) {
                            label.setVisibility(View.VISIBLE);
                        }

                    } else if (conditionOptions.getDisplayType().equals("hide") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        childSpinner.setVisibility(View.GONE);
                        if (label != null) {
                            label.setVisibility(View.GONE);
                        }
                    }
                }
            } else if (conditionOptions.getControlType().equals(CONTROL_TYPE_CHECK)) {
                LinearLayout childCheckBox = (LinearLayout) mParent.findViewById(customHashCode(conditionOptions.getChildControl() + LABEL_ID_CHECK));
                TextView label = (TextView) mParent.findViewById(customHashCode(conditionOptions.getChildControl() + LABEL_ID_SALT));

                if (childCheckBox != null) {
                    if (conditionOptions.getDisplayType().equals("show") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        childCheckBox.setVisibility(View.VISIBLE);
                        if (label != null) {
                            label.setVisibility(View.VISIBLE);
                        }

                    } else if (conditionOptions.getDisplayType().equals("hide") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        childCheckBox.setVisibility(View.GONE);
                        if (label != null) {
                            label.setVisibility(View.GONE);
                        }
                    }
                }
            } else if (conditionOptions.getControlType().equals(CONTROL_TYPE_SECTION)) {
                LinearLayout linearLayout = (LinearLayout) mParent.findViewById(customHashCode(conditionOptions.getChildControl()));

                if (linearLayout != null) {
                    if (conditionOptions.getDisplayType().equals("show") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        linearLayout.setVisibility(View.VISIBLE);

                    } else if (conditionOptions.getDisplayType().equals("hide") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        linearLayout.setVisibility(View.GONE);
                    }
                }
            } else if (conditionOptions.getControlType().equals(CONTROL_TYPE_RADIO)) {
                RadioGroup childRadioButton = (RadioGroup) mParent.findViewById(customHashCode(conditionOptions.getChildControl()));
                TextView label = (TextView) mParent.findViewById(customHashCode(conditionOptions.getChildControl() + LABEL_ID_SALT));

                if (childRadioButton != null) {
                    if (conditionOptions.getDisplayType().equals("show") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        childRadioButton.setVisibility(View.VISIBLE);
                        if (label != null) {
                            label.setVisibility(View.VISIBLE);
                        }

                    } else if (conditionOptions.getDisplayType().equals("hide") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        childRadioButton.setVisibility(View.GONE);
                        if (label != null) {
                            label.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                RangeEditText childRangeEditText = (RangeEditText) mParent.findViewById(customHashCode(conditionOptions.getChildControl()));
                TextView label = (TextView) mParent.findViewById(customHashCode(conditionOptions.getChildControl() + LABEL_ID_SALT));

                if (childRangeEditText != null) {
                    if (conditionOptions.getDisplayType().equals("show") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        childRangeEditText.setVisibility(View.VISIBLE);
                        if (label != null) {
                            label.setVisibility(View.VISIBLE);
                        }

                    } else if (conditionOptions.getDisplayType().equals("hide") && answer.getLabel().equals(conditionOptions.getWhen())) {
                        childRangeEditText.setVisibility(View.GONE);
                        if (label != null) {
                            label.setVisibility(View.GONE);
                        }
                    }
                }
            }


        }
    }

    @Override
    public void createAndAttachSelectQuestionRadioButton(Question question, LinearLayout sectionLinearLayout) {
        TextView textView = new TextView(getActivity());
        textView.setPadding(20, 0, 0, 0);
        textView.setText(question.getLabel());

        RadioGroup radioGroup = new RadioGroup(getActivity());


        for (Answer answer : question.getQuestionOptions().getAnswers()) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(answer.getLabel());
            radioGroup.addView(radioButton);
        }

        SelectOneField radioGroupField = new SelectOneField(question.getQuestionOptions().getAnswers(),
                question.getQuestionOptions().getConcept());

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        radioGroupField.setQuestionLabel(question.getLabel());
        radioGroupField.setGroupConcept(question.getGroupConcept());
        sectionLinearLayout.addView(textView);
        sectionLinearLayout.addView(radioGroup);

        sectionLinearLayout.setLayoutParams(linearLayoutParams);

        SelectOneField selectOneField = getSelectOneField(radioGroupField.getConcept());
        if (selectOneField != null) {
            if (selectOneField.getChosenAnswerPosition() != -1) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(selectOneField.getChosenAnswerPosition());
                radioButton.setChecked(true);
            }
            setOnCheckedChangeListener(radioGroup, selectOneField);
        } else {
            setOnCheckedChangeListener(radioGroup, radioGroupField);
            selectOneFields.add(radioGroupField);
        }
    }

    @Override
    public void editAndAttachSelectQuestionCheckBox(Question question, LinearLayout sectionLinearLayout, SelectManyFields selectManyField) {
        TextView textView = new TextView(getActivity());
        textView.setPadding(10, 10, 0, 10);
        textView.setText(question.getLabel());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        FontsUtil.setFont(textView, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
        final LinearLayout linearLayoutCheckBox = new LinearLayout(getActivity());
        linearLayoutCheckBox.setOrientation(LinearLayout.VERTICAL);

        if (selectManyField != null) {
            selectManyField.setGroupConcept(question.getGroupConcept());
            int j = 0;
            for (Answer answer : question.getQuestionOptions().getAnswers()) {
                CheckBox checkBox = new CheckBox(getActivity());
                Answer ans = getAnswer(selectManyField.getChosenAnswerList(), answer.getConcept());
                if (ans != null) {
                    checkBox.setText(ans.getLabel());
                    checkBox.setId(customHashCode(ans.getConcept()));
                    checkBox.setChecked(true);
                    FontsUtil.setFont(checkBox, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onCheckboxClicked(v, selectManyField, checkBox.getId());
                        }
                    });
                } else {
                    checkBox.setText(answer.getLabel());
                    checkBox.setId(customHashCode(answer.getConcept()));
                    FontsUtil.setFont(checkBox, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onCheckboxClicked(v, selectManyField, checkBox.getId());
                        }
                    });
                }
                linearLayoutCheckBox.addView(checkBox);
                idManager.put(customHashCode(answer.getConcept()), j);
                j++;
            }
            selectManyFields.add(selectManyField);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            sectionLinearLayout.addView(textView);
            sectionLinearLayout.addView(linearLayoutCheckBox);

            sectionLinearLayout.setLayoutParams(linearLayoutParams);
        }
    }

    @Override
    public void editAndAttachSelectQuestionRadioButton(Question question, LinearLayout sectionLinearLayout, SelectOneField radioField) {
        TextView textView = new TextView(getActivity());
//        textView.setPadding(20,0,0,0);
        textView.setText(question.getLabel());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        RadioGroup radioGroup = new RadioGroup(getActivity());


        for (Answer answer : question.getQuestionOptions().getAnswers()) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(answer.getLabel());
            radioGroup.addView(radioButton);
        }

        SelectOneField radioGroupField = new SelectOneField(question.getQuestionOptions().getAnswers(),
                question.getQuestionOptions().getConcept(), question.getType());
        radioGroupField.setGroupConcept(question.getGroupConcept());
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        sectionLinearLayout.addView(textView);
        sectionLinearLayout.addView(radioGroup);

        sectionLinearLayout.setLayoutParams(linearLayoutParams);

//        SelectOneField selectOneField = getSelectOneField(radioGroupField.getConcept());
        if (radioField != null) {
            if (radioField.getChosenAnswer() != null) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(getIndexRadio(question.getQuestionOptions().getAnswers(), radioField.getChosenAnswer().getConcept()));
                radioButton.setChecked(true);
            }
            setOnCheckedChangeListener(radioGroup, radioField);
        } else {

            setOnCheckedChangeListener(radioGroup, radioGroupField);
            selectOneFields.add(radioGroupField);
        }
    }

    @Override
    public void editAndAttachSelectQuestionDropdown(Question question, LinearLayout sectionLinearLayout, SelectOneField selectOne) {
        if (question.getRepeatConcept() == null) {
            TextView textView = new TextView(getActivity());
            textView.setPadding(10, 20, 0, 0);
            textView.setText(question.getLabel());
            textView.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            Spinner spinner = (Spinner) getActivity().getLayoutInflater().inflate(R.layout.form_dropdown, null);

            LinearLayout questionLinearLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams questionLinearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            questionLinearLayout.setOrientation(LinearLayout.VERTICAL);
            questionLinearLayoutParams.gravity = Gravity.START;
            questionLinearLayout.setLayoutParams(questionLinearLayoutParams);

            List<String> answerLabels = new ArrayList<>();
            answerLabels.add("");
            for (Answer answer : question.getQuestionOptions().getAnswers()) {
                answerLabels.add(answer.getLabel());
            }

            SelectOneField spinnerField = new SelectOneField(question.getQuestionOptions().getAnswers(),
                    question.getQuestionOptions().getConcept(), question.getType());

            spinner.setId(customHashCode(question.getQuestionOptions().getConcept()));
            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, answerLabels) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    FontsUtil.setFont(text, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                    return view;
                }
            };
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
            //        spinner.setSelection(getIndex(spinner, myValue));
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            questionLinearLayout.addView(textView);
            questionLinearLayout.addView(spinner);
            FontsUtil.setFont(questionLinearLayout, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);

            sectionLinearLayout.setLayoutParams(linearLayoutParams);
            sectionLinearLayout.addView(questionLinearLayout);

            if (selectOne != null) {
                Answer answer = selectOne.getChosenAnswer();
                List<Answer> answers = selectOne.getAnswerList();
                spinner.setSelection(getIndex(spinner, answer.getLabel()));
                if (answer.getLabel() == null) {
                    setOnItemSelectedListener(spinner, selectOne, question, -1);
                } else {
                    setOnItemSelectedListener(spinner, selectOne, question, getIndex(spinner, answer.getLabel()));
                }
                selectOne.setQuestionLabel(question.getLabel());
                selectOne.setObs("obs");
                selectOne.setGroupConcept(question.getGroupConcept());
                selectOneFields.add(selectOne);

            } else {
                setOnItemSelectedListener(spinner, spinnerField, question, -1);
                spinnerField.setGroupConcept(question.getGroupConcept());
                selectOneFields.add(spinnerField);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    spinner.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
            }
        } else {
            TextView textView = new TextView(getActivity());
            textView.setPadding(10, 20, 0, 0);
            textView.setText(question.getLabel());
            textView.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            Spinner spinner = (Spinner) getActivity().getLayoutInflater().inflate(R.layout.form_dropdown, null);

            LinearLayout questionLinearLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams questionLinearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            questionLinearLayout.setOrientation(LinearLayout.VERTICAL);
            questionLinearLayoutParams.gravity = Gravity.START;
            questionLinearLayout.setLayoutParams(questionLinearLayoutParams);

            List<String> answerLabels = new ArrayList<>();
            answerLabels.add("");
            for (Answer answer : question.getQuestionOptions().getAnswers()) {
                answerLabels.add(answer.getLabel());
            }

            SelectOneField spinnerField = new SelectOneField(question.getQuestionOptions().getAnswers(),
                    question.getQuestionOptions().getConcept(), question.getRepeatConcept(), question.getType());

            spinner.setId(customHashCode(question.getQuestionOptions().getConcept() + question.getRepeatConcept()));
            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, answerLabels) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    FontsUtil.setFont(text, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                    return view;
                }
            };
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
//        spinner.setSelection(getIndex(spinner, myValue));
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            questionLinearLayout.addView(textView);
            questionLinearLayout.addView(spinner);
            FontsUtil.setFont(questionLinearLayout, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);

            sectionLinearLayout.setLayoutParams(linearLayoutParams);
            sectionLinearLayout.addView(questionLinearLayout);

            if (selectOne != null) {
                Answer answer = selectOne.getChosenAnswer();
                List<Answer> answers = selectOne.getAnswerList();
                spinner.setSelection(getIndex(spinner, answer.getLabel()));
                if (answer.getLabel() == null) {
                    setOnItemSelectedListener(spinner, selectOne, question, -1);
                } else {
                    setOnItemSelectedListener(spinner, selectOne, question, getIndex(spinner, answer.getLabel()));
                }
                selectOne.setQuestionLabel(question.getLabel());
                selectOne.setObs("obs");
                selectOne.setGroupConcept(question.getGroupConcept());
                selectOne.setRepeatConcept(question.getRepeatConcept());

                selectOneFields.add(selectOne);

            } else {
                setOnItemSelectedListener(spinner, spinnerField, question, -1);
                spinnerField.setGroupConcept(question.getGroupConcept());
                spinnerField.setRepeatConcept(question.getRepeatConcept());

                selectOneFields.add(spinnerField);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    spinner.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void editAndAttachNumericQuestionEditText(Question question, LinearLayout sectionLinearLayout, String ans) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RangeEditText ed = new RangeEditText(getActivity());
        if (question.getRepeatConcept() == null) {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            InputField inputField = getInputField(field.getConcept());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);

            if (ans != null) {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setText(ans);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                ed.setPadding(15, 0, 0, 10);
                ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                sectionLinearLayout.addView(ed, layoutParams);
            } else {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setLowerlimit(-1.0);
                ed.setUpperlimit(-1.0);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                ed.setPadding(15, 0, 0, 10);
                if (question.getQuestionOptions().isAllowDecimal()) {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                //            ed.setId(field.getId());
                ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                sectionLinearLayout.addView(ed, layoutParams);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        } else {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getRepeatConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            field.setRepeatConcept(question.getRepeatConcept());
            InputField inputField = getInputField(field.getConcept(), field.getId());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);

            if (ans != null) {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setText(ans);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                ed.setPadding(15, 0, 0, 10);
//                ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                ed.setId(field.getId());
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                sectionLinearLayout.addView(ed, layoutParams);
            } else {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                ed.setLowerlimit(-1.0);
                ed.setUpperlimit(-1.0);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                ed.setPadding(15, 0, 0, 10);
                if (question.getQuestionOptions().isAllowDecimal()) {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                ed.setId(field.getId());
//                ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                sectionLinearLayout.addView(ed, layoutParams);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void editAndAttachDateQuestionEditText(Question question, LinearLayout sectionLinearLayout, String ans) {
        Calendar myCalendar = Calendar.getInstance();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RangeEditText ed = new RangeEditText(getActivity());
        if (question.getRepeatConcept() == null) {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            InputField inputField = getInputField(field.getConcept());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);
//        sectionLinearLayout.addView(generateTextView(question.getLabel()));
//        ed.setEnabled(false);
            ed.setClickable(true);
            ed.setFocusable(false);
            ed.setName(question.getLabel());
//        ed.setHint(question.getLabel());
            ed.setText(ans);
            ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            ed.setInputType(InputType.TYPE_CLASS_DATETIME);
            ed.setPadding(15, 0, 0, 10);
            ed.setId(field.getId());
            FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
            sectionLinearLayout.addView(ed, layoutParams);

            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel(ed, myCalendar);
                }

            };
            ed.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(getActivity(), date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        } else {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getRepeatConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            field.setRepeatConcept(question.getRepeatConcept());
            InputField inputField = getInputField(field.getConcept(), field.getId());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);
//        sectionLinearLayout.addView(generateTextView(question.getLabel()));
//        ed.setEnabled(false);
            ed.setClickable(true);
            ed.setFocusable(false);
            ed.setName(question.getLabel());
//        ed.setHint(question.getLabel());
            ed.setText(ans);
            ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            ed.setInputType(InputType.TYPE_CLASS_DATETIME);
            ed.setPadding(15, 0, 0, 10);
            ed.setId(field.getId());
            FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
            sectionLinearLayout.addView(ed, layoutParams);

            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel(ed, myCalendar);
                }

            };
            ed.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(getActivity(), date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        }

    }

    @Override
    public void editAndAttachTextQuestionEditText(Question question, LinearLayout sectionLinearLayout, String ans) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RangeEditText ed = new RangeEditText(getActivity());
        if (question.getRepeatConcept() == null) {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            InputField inputField = getInputField(field.getConcept());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);
            //        sectionLinearLayout.addView(generateTextView(question.getLabel()));

            if (question.getQuestionOptions().getRendering().equals("text")) {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
                //            ed.setHint(question.getLabel());
                ed.setText(ans);
                ed.setInputType(InputType.TYPE_CLASS_TEXT);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                ed.setPadding(15, 0, 0, 10);
                ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);

                sectionLinearLayout.addView(ed, layoutParams);
            } else {
                ed.setName(question.getLabel());
                ed.setSingleLine(false);
                //            ed.setHint(question.getLabel());
                ed.setInputType(InputType.TYPE_CLASS_DATETIME);
                ed.setId(customHashCode(question.getQuestionOptions().getConcept()));
                sectionLinearLayout.addView(ed, layoutParams);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        } else {
            InputField field = new InputField(question.getQuestionOptions().getConcept(), question.getRepeatConcept(), question.getType());
            field.setQuestionLabel(question.getLabel());
            field.setGroupConcept(question.getGroupConcept());
            field.setRepeatConcept(question.getRepeatConcept());
            InputField inputField = getInputField(field.getConcept(), field.getId());
            if (inputField != null) {
                inputField.setId(field.getId());
            } else {
                field.setConcept(question.getQuestionOptions().getConcept());
                inputFields.add(field);
            }
            View vv = generateTextView(question.getLabel());
            vv.setId(customHashCode(question.getQuestionOptions().getConcept() + LABEL_ID_SALT + question.getRepeatConcept()));
            FontsUtil.setFont(vv, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
            sectionLinearLayout.addView(vv);
//        sectionLinearLayout.addView(generateTextView(question.getLabel()));

            if (question.getQuestionOptions().getRendering().equals("text")) {
                ed.setName(question.getLabel());
                ed.setSingleLine(true);
//            ed.setHint(question.getLabel());
                ed.setText(ans);
                ed.setInputType(InputType.TYPE_CLASS_TEXT);
                ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                ed.setPadding(15, 0, 0, 10);
                ed.setId(field.getId());
                FontsUtil.setFont(ed, FontsUtil.OpenFonts.OPEN_SANS_BOLD);

                sectionLinearLayout.addView(ed, layoutParams);
            } else {
                ed.setName(question.getLabel());
                ed.setSingleLine(false);
//            ed.setHint(question.getLabel());
                ed.setInputType(InputType.TYPE_CLASS_DATETIME);
                ed.setId(field.getId());
                sectionLinearLayout.addView(ed, layoutParams);
            }
            if (question.getDefaultDisplay() != null) {
                if (question.getDefaultDisplay().equals("hide")) {
                    ed.setVisibility(View.GONE);
                    vv.setVisibility(View.GONE);
                }
            }
        }

    }


    private static Answer getAnswer(List<Answer> answers, String conceptUuid) {
        for (Answer answer : answers) {
            if (answer.getConcept().equals(conceptUuid)) {
                return answer;
            }
        }
        return null;
    }

    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }

        return 0;
    }

    private int getIndexRadio(List<Answer> answers, String myString) {
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i).getConcept().equalsIgnoreCase(myString)) {
                return i;
            }
        }

        return 0;
    }


    private void setOnCheckedChangeListener(RadioGroup radioGroup, final SelectOneField radioGroupField) {
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            View radioButton = radioGroup1.findViewById(i);
            int idx = radioGroup1.indexOfChild(radioButton);
            radioGroupField.setAnswer(idx);
        });
    }

    @Override
    public LinearLayout createQuestionGroupLayout(String questionLabel) {
        LinearLayout questionLinearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams layoutParams = getAndAdjustLinearLayoutParams(questionLinearLayout);

        TextView tv = new TextView(getActivity());
        tv.setText(questionLabel);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary));
        FontsUtil.setFont(tv, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
        questionLinearLayout.addView(tv, layoutParams);

        return questionLinearLayout;
    }

    @Override
    public LinearLayout createSectionLayout(String sectionLabel) {
        LinearLayout sectionLinearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams layoutParams = getAndAdjustLinearLayoutParams(sectionLinearLayout);

        TextView tv = new TextView(getActivity());
        tv.setText(sectionLabel);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary));
        FontsUtil.setFont(tv, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
        sectionLinearLayout.addView(tv, layoutParams);
        sectionLinearLayout.setId(customHashCode(sectionLabel));

        return sectionLinearLayout;
    }


    private LinearLayout.LayoutParams getAndAdjustLinearLayoutParams(LinearLayout linearLayout) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        Resources r = getActivity().getResources();
        float pxLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        float pxTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        float pxRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        layoutParams.setMargins(Math.round(pxLeftMargin), Math.round(pxTopMargin), Math.round(pxRightMargin), Math.round(pxBottomMargin));

        return layoutParams;
    }

    @Override
    public List<SelectOneField> getSelectOneFields() {
        return selectOneFields;
    }

    public SelectManyFields getSelectManyFields(String concept) {
        for (SelectManyFields selectManyField : selectManyFields) {
            if (concept.equals(selectManyField.getConcept())) {
                return selectManyField;
            }
        }
        return null;
    }

    public void onCheckboxClicked(View view, SelectManyFields checkField, int i) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            checkField.setAnswer(idManager.get(i));
        }
    }

    public void onCheckboxSelectedClicked(CheckBox checkBox, SelectManyFields checkField, int i) {
        // Is the view now checked?
        boolean checked = checkBox.isChecked();
        if (checked) {
            checkField.setAnswer(i);
        }
    }

    @Override
    public List<InputField> getInputFields() {
        for (InputField field : inputFields) {
            try {
                RangeEditText ed = getActivity().findViewById(field.getId());
                if (!isEmpty(ed)) {
                    if (ed.getText().toString().trim().matches(regexStr)) {
                        field.setValue(Double.parseDouble(ed.getText().toString()));
                        field.setValueAll(ed.getText().toString());
                        field.setAnswerLabel(ed.getText().toString());
                        boolean isRed = (ed.getCurrentTextColor() == ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
                        field.setIsRed(isRed);
                    } else {
                        field.setValueAll(ed.getText().toString().trim());
                        field.setAnswerLabel(ed.getText().toString());
                    }
                } else {
                    field.setValue(-1.0);
                    field.setValueAll("");
                }
            } catch (ClassCastException e) {
                DiscreteSeekBar dsb = getActivity().findViewById(field.getId());
                field.setValue((double) dsb.getProgress());
            }
        }

        return inputFields;
    }

    @Override
    public void setInputFields(List<InputField> inputFields) {
        this.inputFields = inputFields;
    }

    @Override
    public List<SelectManyFields> getSelectManyFields() {
        return selectManyFields;
    }

    @Override
    public void setSelectManyFields(List<SelectManyFields> selectManyFields) {
        this.selectManyFields = selectManyFields;
    }

    @Override
    public void setSelectOneFields(List<SelectOneField> selectOneFields) {
        this.selectOneFields = selectOneFields;
    }

    public boolean checkInputFields() {
//        boolean allEmpty = true;
//        boolean valid=true;
//        List<Boolean> empties = new ArrayList<>();
//        for (InputField field:inputFields) {
//            try {
//                RangeEditText ed = getActivity().findViewById(field.getId());
//            if (!isEmpty(ed)) {
//                allEmpty = false;
//                if (ed.getText().toString().charAt(0) != '.') {
//                    Double inp = Double.parseDouble(ed.getText().toString());
//                    if (ed.getUpperlimit() != -1.0 && ed.getUpperlimit() != -1.0 && (ed.getUpperlimit() < inp || ed.getLowerlimit() > inp)) {
//                        ed.setTextColor(ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
//                        valid = false;
//                    }
//                }
//                else {
//                    ed.setTextColor(ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
//                    valid = false;
//                }
//            }}
//            catch (ClassCastException e){
//                DiscreteSeekBar dsb = getActivity().findViewById(field.getId());
//                if (dsb.getProgress() > dsb.getMin()) {
//                    allEmpty = false;
//                }
//            }
//        }
//
//        for (SelectOneField radioGroupField : selectOneFields) {
//            if (radioGroupField.getChosenAnswer() != null) {
//                allEmpty = false;
//            }
//        }
//
//        if (allEmpty) {
//            ToastUtil.error("All fields cannot be empty");
//            return false;
//        }
//        return valid;
        boolean allEmpty = true;
        boolean valid = true;
        List<Boolean> empties = new ArrayList<>();
        for (InputField field : inputFields) {
            try {
                RangeEditText ed = (RangeEditText) getActivity().findViewById(field.getId());
                if (field.isRequired()) {
                    if (!isEmpty(ed)) {
//                        allEmpty = false;
                        empties.add(false);
                        if (ed.getText().toString().trim().matches(regexStr)) {
                            Double inp = Double.parseDouble(ed.getText().toString());
                            if (ed.getUpperlimit() != -1.0 && ed.getUpperlimit() != -1.0 && (ed.getUpperlimit() > inp || ed.getLowerlimit() < inp)) {
                                ed.setTextColor(ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
                                valid = false;

                            }
                        }
                    } else {
//                        allEmpty = true;
                        empties.add(true);
                    }
                } else {
//                    allEmpty = false;
                    empties.add(false);
                }
            } catch (ClassCastException e) {
                DiscreteSeekBar dsb = (DiscreteSeekBar) getActivity().findViewById(field.getId());
                if (dsb.getProgress() > dsb.getMin()) {
//                    allEmpty = false;
                    empties.add(false);
                }
            }
        }

        for (SelectOneField radioGroupField : selectOneFields) {
            if (radioGroupField.getChosenAnswer() != null) {
//                allEmpty = false;
                empties.add(false);
            }
        }

        for (SelectManyFields selectManyField : selectManyFields) {
            if (selectManyField.getChosenAnswerList().size() > 0) {
//                allEmpty = false;
                empties.add(false);
            }
        }
        if (empties.contains(true)) {
            ToastUtil.error("All fields cannot be empty");
            return false;
        }
        return valid;
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    private int customHashCode(String inputString) {
//        int hashCode = inputString.hashCode();
//        if (hashCode < 1) {
//            String temp = Integer.toString(hashCode).substring(1);
//            return Integer.parseInt(temp);
//        }
        return Math.abs(inputString.hashCode());
    }
}
