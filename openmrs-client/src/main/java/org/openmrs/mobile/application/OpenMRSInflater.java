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

package org.openmrs.mobile.application;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.utilities.FontsUtil;


public class OpenMRSInflater {
    private LayoutInflater mInflater;

    public OpenMRSInflater(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    public ViewGroup addKeyValueStringViewGroup(ViewGroup parentLayout, String label, String data) {
        View view = mInflater.inflate(R.layout.row_key_value_data, null, false);
        TextView labelText = view.findViewById(R.id.keyValueDataRowTextLabel);
        if (label.contains(":")) {
            labelText.setText(label.substring(0, label.indexOf(':')));
        } else {
            labelText.setText(label);
        }
//        FontsUtil.setFont(labelText, FontsUtil.OpenFonts.OPEN_SANS_SEMIBOLD);
        FontsUtil.setFont(labelText, FontsUtil.OpenFonts.OPEN_SANS_BOLD);

        TextView dataText = view.findViewById(R.id.keyValueDataRowTextData);
        dataText.setText(data);
        FontsUtil.setFont(dataText, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
        parentLayout.addView(view);
        return parentLayout;
    }
    public ViewGroup addKeyValueStringView(ViewGroup parentLayout, String label, String data) {
        View view = mInflater.inflate(R.layout.row_key_value_data, null, false);
        TextView labelText = view.findViewById(R.id.keyValueDataRowTextLabel);
        if (label.contains(":")) {
            labelText.setText(label.substring(0, label.indexOf(':')));
        } else {
            labelText.setText(label);
        }
//        FontsUtil.setFont(labelText, FontsUtil.OpenFonts.OPEN_SANS_SEMIBOLD);
        FontsUtil.setFont(labelText, FontsUtil.OpenFonts.OPEN_SANS_REGULAR);

        TextView dataText = view.findViewById(R.id.keyValueDataRowTextData);
        dataText.setText(data);
        FontsUtil.setFont(dataText, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
        parentLayout.addView(view);
        return parentLayout;
    }

    public ViewGroup addSingleStringView(ViewGroup parentLayout, String label) {
        View view = mInflater.inflate(R.layout.row_single_text_data, null, false);
        TextView labelText = view.findViewById(R.id.singleTextRowLabelText);
        labelText.setText(label);
        // FontsUtil.setFont(labelText, FontsUtil.OpenFonts.OPEN_SANS_SEMIBOLD);
        FontsUtil.setFont(labelText, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
        parentLayout.addView(view);
        return parentLayout;
    }

}
