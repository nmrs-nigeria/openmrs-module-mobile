/*Circular Viewpager indicator code obtained from:
http://www.androprogrammer.com/2015/06/view-pager-with-circular-indicator.html*/

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
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.bundle.FormFieldsWrapper;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Form;
import org.openmrs.mobile.models.Page;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FormService;
import org.openmrs.mobile.utilities.RangeEditText;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class FormDisplayActivity extends ACBaseActivity implements FormDisplayContract.View.MainView {

    private ViewPager mViewPager;
    private Button mBtnNext, mBtnFinish, mBtnPrevious;
    private int mDotsCount;
    private ImageView[] mDots;
    private Long personID = null;
    private int mStep = 1;
    private boolean isEligible = false;
    private boolean isValid = false;
    private String mMessage;
    private String formName;

    private FormDisplayContract.Presenter.MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_display);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        String valuereference = null;
        if (bundle != null) {
            valuereference = (String) bundle.get(ApplicationConstants.BundleKeys.VALUEREFERENCE);
            formName = (String) bundle.get(ApplicationConstants.BundleKeys.FORM_NAME);
            personID = (Long) bundle.get(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
            getSupportActionBar().setTitle(formName + " Form");
        }

        initViewComponents(valuereference);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        attachPresenterToFragment(fragment);
        super.onAttachFragment(fragment);
    }

    private void attachPresenterToFragment(Fragment fragment) {
        if (fragment instanceof FormDisplayPageFragment) {
            Bundle bundle = getIntent().getExtras();
            String encounterDate = null;
            String personGender = null;
            String valueRef = null;
            ArrayList<FormFieldsWrapper> formFieldsWrappers = null;
            if (bundle != null) {
                Patient patient = new PatientDAO().findPatientByID(Long.toString(personID));
                valueRef = (String) bundle.get(ApplicationConstants.BundleKeys.VALUEREFERENCE);
                formFieldsWrappers = bundle.getParcelableArrayList(ApplicationConstants.BundleKeys.FORM_FIELDS_LIST_BUNDLE);
                personGender = patient.getGender();
                encounterDate = (String) bundle.get(ApplicationConstants.BundleKeys.ENCOUNTERDATETIME);
            }
            Form form = FormService.getForm(valueRef);
            List<Page> pageList = form.getPages();
            for (Page page : pageList) {
                if (formFieldsWrappers != null) {
                    new FormDisplayPagePresenter((FormDisplayPageFragment) fragment, pageList.get(getFragmentNumber(fragment)), formFieldsWrappers, pageList, encounterDate);

                } else {
                    if (personGender != null) {
                        new FormDisplayPagePresenter((FormDisplayPageFragment) fragment, pageList.get(getFragmentNumber(fragment)), personGender);
                    } else {
                        new FormDisplayPagePresenter((FormDisplayPageFragment) fragment, pageList.get(getFragmentNumber(fragment)));
                    }
                }
            }
        }
    }

    @Override
    public void quitFormEntry() {
        finish();
    }

    @Override
    public void setPresenter(FormDisplayContract.Presenter.MainPresenter presenter) {
        this.mPresenter = presenter;
    }

    private void initViewComponents(String valueRef) {
        FormPageAdapter formPageAdapter = new FormPageAdapter(getSupportFragmentManager(), valueRef);
        LinearLayout pagerIndicator = findViewById(R.id.viewPagerCountDots);

        mBtnNext = findViewById(R.id.btn_next);
        mBtnPrevious = findViewById(R.id.btn_previous);
        mBtnFinish = findViewById(R.id.btn_finish);
//        mChronometer = (Chronometer) getView().findViewById(R.id.chronometer);
        mBtnNext.setOnClickListener(view -> mViewPager.setCurrentItem(mViewPager.getCurrentItem() + getmStep()));
        mBtnPrevious.setOnClickListener(view -> mViewPager.setCurrentItem(mViewPager.getCurrentItem() - getmStep()));

        mBtnFinish.setOnClickListener(view -> mPresenter.createEncounter(isEligible,isValid,mMessage));
        mViewPager = findViewById(R.id.container);


        mViewPager.setAdapter(formPageAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mDotsCount; i++) {
                    mDots[i].setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.nonselecteditem_dot));
                }
                mDots[position].setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.selecteditem_dot));

                Spinner spinner = findViewById(customHashCode("1fb46619-abcd-405a-81c9-3c9018473729"));

                // Consider this when it causes skip for other form
                if (spinner != null) {
                    spinner.setEnabled(false);
                    Spinner spinnerScreening = (Spinner) findViewById(customHashCode("b75f5803-34e9-46ea-bd41-c174670a13c7"));
                    Spinner spinnerConfirmatory = (Spinner) findViewById(customHashCode("1183a13b-cc22-43df-8c57-fefe7acebd3e"));
                    Spinner spinnerBreaker = (Spinner) findViewById(customHashCode("2e46d331-a956-49a2-b9eb-f74b8946fa08"));
                    String textScreening = spinnerScreening.getSelectedItem().toString();
                    String textConfirmatory = spinnerConfirmatory.getSelectedItem().toString();
                    String textBreaker = spinnerBreaker.getSelectedItem().toString();
                    if (StringUtils.notEmpty(textScreening) && textScreening.equals("Non-reactive")) {
                        spinner.setSelection(2);
                        setmStep(1);
                    } else if (StringUtils.notEmpty(textScreening) && textScreening.equals("Reactive")) {
                        spinner.setSelection(0);
                        if (StringUtils.notEmpty(textConfirmatory) && textConfirmatory.equals("Reactive")) {
                            spinner.setSelection(1);
                        } else if (StringUtils.notEmpty(textConfirmatory) && textConfirmatory.equals("Non-reactive")) {
                            spinner.setSelection(0);
                            if (StringUtils.notEmpty(textBreaker) && textBreaker.equals("Reactive")) {
                                spinner.setSelection(1);
                                setmStep(1);
                            } else if (StringUtils.notEmpty(textBreaker) && textBreaker.equals("Non-reactive")) {
                                spinner.setSelection(2);
                            }

                        }


                    }

                }

                if (position + 1 == mDotsCount) {
                    mBtnNext.setVisibility(View.GONE);
                    mBtnPrevious.setVisibility(View.VISIBLE);
                    mBtnFinish.setVisibility(View.VISIBLE);
                    RangeEditText visitDateEditText = findViewById(customHashCode("6bcaf85b-8504-4c7f-b510-a50436236b80"));
                    if (visitDateEditText != null && !ViewUtils.isEmpty(visitDateEditText)){
                        isValid = true;
                    }
                } else if (position == 0) {
                    mBtnNext.setVisibility(View.VISIBLE);
                    mBtnPrevious.setVisibility(View.GONE);
                    mBtnFinish.setVisibility(View.GONE);
                    if (formName.equals("Client intake form")) {
                        setmStep(1);
                    }

                }else if (position == 1){
                    setmStep(1);
                    mBtnPrevious.setVisibility(View.VISIBLE);
                    RangeEditText visitDateEditText = findViewById(customHashCode("6bcaf85b-8504-4c7f-b510-a50436236b80"));
                    if (visitDateEditText != null && !ViewUtils.isEmpty(visitDateEditText)){
                        isValid = true;
                    }
                }
                else {
                    mBtnNext.setVisibility(View.VISIBLE);
                    mBtnPrevious.setVisibility(View.VISIBLE);
                    mBtnFinish.setVisibility(View.GONE);
                }

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // This method is intentionally empty
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // This method is intentionally empty
            }
        });

        mPresenter = new FormDisplayMainPresenter(this, getIntent().getExtras(), (FormPageAdapter) mViewPager.getAdapter());

        // Set page indicators:
        mDotsCount = formPageAdapter.getCount();
        mDots = new ImageView[mDotsCount];
        for (int i = 0; i < mDotsCount; i++) {
            mDots[i] = new ImageView(this);
            mDots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.nonselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            pagerIndicator.addView(mDots[i], params);
        }
        mDots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selecteditem_dot));
        if (mDotsCount == 1) {
            mBtnNext.setVisibility(View.GONE);
            mBtnPrevious.setVisibility(View.GONE);
            mBtnFinish.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void enableSubmitButton(boolean enabled) {
        mBtnFinish.setEnabled(enabled);
    }

    @Override
    public void showToast(String errorMessage) {
        ToastUtil.error(errorMessage);
    }

    private int getFragmentNumber(Fragment fragment) {
        String fragmentTag = fragment.getTag();
        String[] parts = fragmentTag.split(":");
        return Integer.parseInt(parts[3]);
    }

    public int getmStep() {
        return mStep;
    }

    public void setmStep(int mStep) {
        this.mStep = mStep;
    }

    private int customHashCode(String inputString) {
//        int hashCode = inputString.hashCode();
//        if (hashCode < 1) {
//            String temp = Integer.toString(hashCode).substring(1);
//            return Integer.parseInt(temp);
//        }
        return Math.abs(inputString.hashCode());
    }

    public boolean isEligible() {
        return isEligible;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getmMessage() {
        return mMessage;
    }

    public void setmMessage(String mMessage) {
        this.mMessage = mMessage;
    }
}
