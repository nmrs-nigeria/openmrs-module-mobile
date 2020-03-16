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

package org.openmrs.mobile.listeners.watcher;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.openmrs.mobile.R;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import static com.activeandroid.Cache.getContext;

public class NumericValidatorWatcher implements TextWatcher {

    private EditText edText;
    private Double min;
    private Double max;

    public NumericValidatorWatcher(EditText editText, Double max, Double min) {
        this.edText = editText;
        this.max = max;
        this.min = min;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // This method is intentionally empty
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // This method is intentionally empty
    }

    @Override
    public void afterTextChanged(Editable editable) {
//        edText.getText().clear();
        if(!editable.toString().matches("")) {
            if (StringUtils.notEmpty(editable.toString()) && Double.parseDouble(editable.toString()) > max || Double.parseDouble(editable.toString()) < min) {
                ToastUtil.error("Ths entry must be between " + min + " and " + max);
                this.edText.setTextColor(getContext().getResources().getColor(R.color.red));
            }else{
                this.edText.setTextColor(getContext().getResources().getColor(R.color.black));
            }
        }else{
            this.edText.setTextColor(getContext().getResources().getColor(R.color.black));
        }
    }
}
