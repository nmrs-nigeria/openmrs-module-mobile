package org.openmrs.mobile.activities.formprogramlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.activevisits.ActiveVisitsActivity;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientActivity;
import org.openmrs.mobile.activities.formdisplay.FormDisplayActivity;
import org.openmrs.mobile.activities.formentrypatientlist.FormEntryPatientListActivity;
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsActivity;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.ImageUtils;
import org.openmrs.mobile.utilities.ThemeUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;


public class FormProgramFragment extends ACBaseFragment<FormProgramContract.Presenter> implements FormProgramContract.View , View.OnClickListener{

    private static Boolean formCreateFlag;
    private ImageView mheifButton;
    private ImageView mhivEnrollmentButton;
    private ImageView mcareCardButton;
    private ImageView mlabButton;
    private ImageView mpharmacyButton;
    private ImageView madultinitButton;
    private ImageView mpedinitButton;
    private ImageView mtransferButton;
    private ImageView mclientRefButton;
    private ImageView martComButton;
    private ImageView mclientTracButton;
    private ImageView mancTrackingButton;
    private ImageView mpartnerButton;
    private ImageView mclientinteakeButton;
    private ImageView mdeliveryButton;
    private ImageView mmaternalButton;
    private ImageView mpmtcthtsButton;
    private ImageView mchildButton;
    private ImageView mchildRSTButton;
    private ImageView madultRSTButton;
    private RelativeLayout mhivEnrollmentView;
    private RelativeLayout mcareCardView;
    private RelativeLayout mlabView;
    private RelativeLayout mheifView;
    private RelativeLayout mpharmacyView;
    private RelativeLayout madultinitView;
    private RelativeLayout mpedinitView;
    private RelativeLayout mtransferView;
    private RelativeLayout mclientRefView;
    private RelativeLayout martComView;
    private RelativeLayout mclientTracView;
    private RelativeLayout mancTrackingView;
    private RelativeLayout mpartnerView;
    private RelativeLayout mclientinteakeView;
    private RelativeLayout mdeliveryView;
    private RelativeLayout mmaternalView;
    private RelativeLayout mpmtcthtsView;
    private RelativeLayout mchildView;
    private RelativeLayout mchildRSTView;
    private RelativeLayout madultRSTView;

    private SparseArray<Bitmap> mBitmapCache;
    private List<String> formsName = new ArrayList<>();
    public static FormProgramFragment newInstance() {

        return new FormProgramFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_form_program, container, false);
        setHasOptionsMenu(true);
        if (root != null) {
            initFragmentFields(root);
            setListeners();
        }
        // Font config
        FontsUtil.setFont(this.getActivity().findViewById(android.R.id.content));
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    private void initFragmentFields(View root) {
        mheifButton = root.findViewById(R.id.heifButton);
        mhivEnrollmentButton  = root.findViewById(R.id.hivEnrollmentButton);
        mcareCardButton  = root.findViewById(R.id.careCardButton);
        mlabButton  = root.findViewById(R.id.labButton);
        mpharmacyButton  = root.findViewById(R.id.pharmacyButton);
        madultinitButton  = root.findViewById(R.id.adultinitButton);
        mpedinitButton  = root.findViewById(R.id.pedinitButton);
        mtransferButton  = root.findViewById(R.id.transferButton);
        mclientRefButton  = root.findViewById(R.id.clientRefButton);
        martComButton  = root.findViewById(R.id.artComButton);
        mclientTracButton  = root.findViewById(R.id.clientTracButton);
        mancTrackingButton  = root.findViewById(R.id.ancTrackingButton);
        mpartnerButton  = root.findViewById(R.id.partnerButton);
        mclientinteakeButton  = root.findViewById(R.id.clientinteakeButton);
        mdeliveryButton  = root.findViewById(R.id.deliveryButton);
        mmaternalButton  = root.findViewById(R.id.maternalButton);
        mpmtcthtsButton  = root.findViewById(R.id.pmtcthtsButton);
        mchildButton  = root.findViewById(R.id.childButton);
        mchildRSTButton  = root.findViewById(R.id.childRSTButton);

        madultRSTButton  = root.findViewById(R.id.adultRSTButton);

        mhivEnrollmentView  = root.findViewById(R.id.hivEnrollmentView);
        mcareCardView  = root.findViewById(R.id.careCardView);
        mlabView  = root.findViewById(R.id.labView);
        mheifView  = root.findViewById(R.id.heifView);
        mpharmacyView  = root.findViewById(R.id.pharmacyView);
        madultinitView  = root.findViewById(R.id.adultinitView);
        mpedinitView  = root.findViewById(R.id.pedinitView);
        mtransferView  = root.findViewById(R.id.transferView);
        mclientRefView  = root.findViewById(R.id.clientRefView);
        martComView  = root.findViewById(R.id.artComView);
        mclientTracView  = root.findViewById(R.id.clientTracView);
        mancTrackingView  = root.findViewById(R.id.ancTrackingView);
        mpartnerView  = root.findViewById(R.id.partnerView);
        mclientinteakeView  = root.findViewById(R.id.clientinteakeView);
        mdeliveryView  = root.findViewById(R.id.deliveryView);
        mmaternalView  = root.findViewById(R.id.maternalView);
        mpmtcthtsView  = root.findViewById(R.id.pmtcthtsView);
        mchildView  = root.findViewById(R.id.childView);
        mchildRSTView  = root.findViewById(R.id.childRSTView);
        madultRSTView  = root.findViewById(R.id.adultRSTView);


    }

    private void setListeners() {
        mhivEnrollmentView.setOnClickListener(this);
        mcareCardView.setOnClickListener(this);
        mlabView.setOnClickListener(this);
        mheifView.setOnClickListener(this);
        mpharmacyView.setOnClickListener(this);
        madultinitView.setOnClickListener(this);
        mpedinitView.setOnClickListener(this);
        mtransferView.setOnClickListener(this);
        mclientRefView.setOnClickListener(this);
        martComView.setOnClickListener(this);
        mclientTracView.setOnClickListener(this);
        mancTrackingView.setOnClickListener(this);
        mpartnerView.setOnClickListener(this);
        mclientinteakeView.setOnClickListener(this);
        mdeliveryView.setOnClickListener(this);
        mmaternalView.setOnClickListener(this);
        mpmtcthtsView.setOnClickListener(this);
        mchildView.setOnClickListener(this);
        mchildRSTView.setOnClickListener(this);
        madultRSTView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int position;
        switch (v.getId()) {
            case R.id.deliveryView:
                position = formsName.indexOf("Delivery register");
                mPresenter.listItemClicked(position,"Delivery register");
                break;
            case R.id.clientTracView:
                position = formsName.indexOf("Client Tracking and Termination");
                mPresenter.listItemClicked(position,"Client Tracking and Termination");
                break;
            case R.id.partnerView:
                position = formsName.indexOf("Partner Register");
                mPresenter.listItemClicked(position,"Partner Register");
                break;
            case R.id.maternalView:
                position = formsName.indexOf("Maternal Cohort Register");
                mPresenter.listItemClicked(position,"Maternal Cohort Register");
                break;
            case R.id.pmtcthtsView:
                position = formsName.indexOf("PMTCT HTS Register");
                mPresenter.listItemClicked(position,"PMTCT HTS Register");
                break;
            case R.id.childView:
                position = formsName.indexOf("Child Birth Registration");
                mPresenter.listItemClicked(position,"Child Birth Registration");
                break;
            case R.id.clientinteakeView:
                position = formsName.indexOf("Client intake form");
                mPresenter.listItemClicked(position,"Client intake form");
                break;
            case R.id.labView:
                position = formsName.indexOf("Laboratory Order and Result form");
                mPresenter.listItemClicked(position,"Laboratory Order and Result form");
                break;
            case R.id.pharmacyView:
                position = formsName.indexOf("Pharmacy Order Form");
                mPresenter.listItemClicked(position,"Pharmacy Order Form");
                break;

            case R.id.pedinitView:
                position = formsName.indexOf("Pediatric Initial Clinical Evaluation");
                mPresenter.listItemClicked(position,"Pediatric Initial Clinical Evaluation");
                break;
            case R.id.transferView:
                position = formsName.indexOf("Transfer Form");
                mPresenter.listItemClicked(position,"Transfer Form");
                break;
            case R.id.clientRefView:
                position = formsName.indexOf("Client Referral Form");
                mPresenter.listItemClicked(position,"Client Referral Form");
                break;
            case R.id.artComView:
                position = formsName.indexOf("ART Commencement Form");
                mPresenter.listItemClicked(position,"ART Commencement Form");
                break;
            case R.id.ancTrackingView:
                position = formsName.indexOf("General Antenatal Care");
                mPresenter.listItemClicked(position,"General Antenatal Care");
                break;

            case R.id.hivEnrollmentView:
                position = formsName.indexOf("HIV Enrollment");
                mPresenter.listItemClicked(position,"HIV Enrollment");
                break;
            case R.id.careCardView:
                position = formsName.indexOf("Care Card");
                mPresenter.listItemClicked(position,"Care Card");
                break;
            case R.id.heifView:
                position = formsName.indexOf("HEI (HIV Exposed Infant) Programs");
                mPresenter.listItemClicked(position,"HEI (HIV Exposed Infant) Programs");
                break;
            case R.id.adultinitView:
                position = formsName.indexOf("Adult Initial Clinical Evaluation");
                mPresenter.listItemClicked(position,"Adult Initial Clinical Evaluation");
                break;
            case R.id.adultRSTView:
                position = formsName.indexOf("Risk Stratification Adult");
                mPresenter.listItemClicked(position,"Risk Stratification Adult");
                break;
            case R.id.childRSTView:
                position = formsName.indexOf("Risk Assessment Pediatric");
                mPresenter.listItemClicked(position,"Risk Assessment Pediatric");
                break;
            default:
                // Do nothing
                break;
        }
    }



    @Override
    public void showFormList(String[] forms, String programName, List<String> formName, boolean isElig, boolean isEnrol, boolean isFTime, boolean isCompl, boolean isPos, boolean isClient, boolean isAnc, boolean isPmtctHts, boolean isAncPos) {
        formsName = formName;
        FormProgramActivity fPActivity = (FormProgramActivity) getActivity();
        for (String form : forms){
            switch (programName){
                case "PMTCT":
                    switch (form) {
                        case "Delivery register":
                            assert fPActivity != null;
                            if (fPActivity.isFirstTimeANC()){
                                mdeliveryView.setVisibility(View.GONE);
                            }else{
                                if(fPActivity.isAncPositive() || isAncPos){
                                    mdeliveryView.setVisibility(View.VISIBLE);
                                }else{
                                    mdeliveryView.setVisibility(View.GONE);
                                }

                            }
                            break;
                        case "General Antenatal Care":
                            assert fPActivity != null;
                            if (fPActivity.isFirstTimeANC()){
                                if (isAnc){
                                    mancTrackingView.setVisibility(View.GONE);
                                }else{
                                    mancTrackingView.setVisibility(View.VISIBLE);
                                }

                            }else{
                                mancTrackingView.setVisibility(View.GONE);
                            }
                            break;
//                        case "Client Referral Form":
//                            assert fPActivity != null;
//                            if (fPActivity.isFirstTimeANC()){
//                                if (isAnc && fPActivity.isFirstTimePMTCTHts()) {
//                                    mclientRefView.setVisibility(View.VISIBLE);
//                                }else{
//                                    mclientRefView.setVisibility(View.GONE);
//                                }
//                            }else{
//                                mclientRefView.setVisibility(View.GONE);
//                            }
//                            break;
                        case "Partner Register":
                            assert fPActivity != null;
                            if (fPActivity.isFirstTimeANC()){
                                mpartnerView.setVisibility(View.GONE);
                            }else{
                                if(!fPActivity.isFirstTimePMTCTHts() || isPmtctHts){
                                    mpartnerView.setVisibility(View.VISIBLE);
                                }else{
                                    mpartnerView.setVisibility(View.GONE);
                                }

                            }
                            break;
//                        case "Pharmacy Order Form":
//                            assert fPActivity != null;
//                            if (fPActivity.isFirstTimeANC()){
//                                if (isAnc) {
//                                    mpharmacyView.setVisibility(View.VISIBLE);
//                                }else{
//                                    mpharmacyView.setVisibility(View.GONE);
//                                }
//                            }else{
//                                mpharmacyView.setVisibility(View.VISIBLE);
//                            }
//                            break;
                        case "PMTCT HTS Register":
                            assert fPActivity != null;
                            if (fPActivity.isFirstTimeANC() && !isAnc){
                                mpmtcthtsView.setVisibility(View.GONE);
                            }else{
                                if (isAnc || !isPmtctHts) {
                                    mpmtcthtsView.setVisibility(View.VISIBLE);
                                }else{
                                    mpmtcthtsView.setVisibility(View.GONE);
                                }
                            }
                            break;
                        case "Maternal Cohort Register":
                            assert fPActivity != null;
                            if (fPActivity.isFirstTimeANC()){
                                mmaternalView.setVisibility(View.GONE);
                            }else{
                                if(fPActivity.isAncPositive() || isAncPos){
                                    mmaternalView.setVisibility(View.VISIBLE);
                                }else{
                                    mmaternalView.setVisibility(View.GONE);
                                }

                            }

                            break;
                        default:
                            // Do nothing
                            break;
                    }
                    break;
                    //End of PMTCT
                case "HTS":
                    switch (form){
                        case "Client intake form":
                            if (fPActivity.isFirstTime()){
                                if(isElig) {
                                    mclientinteakeView.setVisibility(View.VISIBLE);
                                }else{
                                    mclientinteakeView.setVisibility(View.GONE);
                                }
                            }
                            else{
                                if (isElig){
                                    if (fPActivity.isClientExist() || isClient ) {
                                        mclientinteakeView.setVisibility(View.GONE);
                                    }else{
                                        mclientinteakeView.setVisibility(View.VISIBLE);
                                    }
                                }else{
                                    if (fPActivity.isEligible()){
                                        if (fPActivity.isClientExist() || isClient ) {
                                            mclientinteakeView.setVisibility(View.GONE);
                                        }else{
                                            mclientinteakeView.setVisibility(View.VISIBLE);
                                        }
                                    }else{
                                        mclientinteakeView.setVisibility(View.GONE);
                                    }
                                }
                            }
                            break;
                        case "Risk Assessment Pediatric":

                            if (fPActivity.isFirstTime()) {

                                if(isElig || !isFTime) {
                                    mchildRSTView.setVisibility(View.GONE);
                                }else{
                                    mchildRSTView.setVisibility(View.VISIBLE);
                                }
                            }else{
                                mchildRSTView.setVisibility(View.GONE);
                            }
                            break;
                        case "Risk Stratification Adult":

                            if (fPActivity.isFirstTime()) {
                                if(isElig || !isFTime) {
                                    madultRSTView.setVisibility(View.GONE);
                                }else{
                                    madultRSTView.setVisibility(View.VISIBLE);
                                }
                            }else{
                                madultRSTView.setVisibility(View.GONE);
                            }
                            break;
                        case "HIV Enrollment":
                            if (fPActivity.isFirstTime() && !isElig ) {
                                mhivEnrollmentView.setVisibility(View.GONE);
                            }else{
                                if(fPActivity.isEligible() || isElig) {
                                    if(fPActivity.isEnrolled() || isEnrol) {
                                        mhivEnrollmentView.setVisibility(View.GONE);
                                    }else{
                                        if(isPos || fPActivity.isPositive())
                                        mhivEnrollmentView.setVisibility(View.VISIBLE);
                                        else mhivEnrollmentView.setVisibility(View.GONE);

                                    }
                                }
                            }
                            break;
                        case "Client Referral Form":
                            if (fPActivity.isFirstTime() && !isElig) {
                                mclientRefView.setVisibility(View.GONE);
                            }else{
                                if(fPActivity.isEligible() || isElig) {
                                    if(fPActivity.isEnrolled() || isEnrol) {
                                        mclientRefView.setVisibility(View.GONE);
                                    }else{
                                        if(isPos || fPActivity.isPositive())
                                        mclientRefView.setVisibility(View.VISIBLE);
                                        else mclientRefView.setVisibility(View.GONE);
                                    }
                                }
                            }
                            break;
                        case "Pharmacy Order Form":
                            if (((fPActivity.isFirstTime() || !fPActivity.isEnrolled()) && !isEnrol)) {
                                mpharmacyView.setVisibility(View.GONE);
                            }else{
                                if((fPActivity.isEligible() || isEnrol) && !fPActivity.isCompleted()) {
                                    if(!isCompl) {
                                        mpharmacyView.setVisibility(View.VISIBLE);
                                    }else{
                                        mpharmacyView.setVisibility(View.GONE);
                                    }
                                }else{
                                    mpharmacyView.setVisibility(View.GONE);
                                }
                            }
                            break;
                        default:
                            // Do nothing
                            break;
                    }
                    break;
    ///end HTS form



                case "HEI":
                    switch (form){
                        case "Child Birth Registration":
                            mchildView.setVisibility(View.VISIBLE);
                            break;
                        case "Child Follow Up":
                            mheifView.setVisibility(View.VISIBLE);
                            break;
                        default:
                            // Do nothing
                            break;
                    }
                    break;

                    ///End HEI(highly exposed infant
                case "ART":
                    switch (form) {
                        case "Adult Initial Clinical Evaluation":
                            madultinitView.setVisibility(View.GONE);
                            break;
                        case "ART Commencement Form":
                            martComView.setVisibility(View.GONE);
                            break;
                        case "Care Card":
                            mcareCardView.setVisibility(View.GONE);
                            break;
                        case "Client Referral Form":
                            mclientRefView.setVisibility(View.GONE);
                            break;
                        case "Risk Stratification Adult":
                            madultRSTView.setVisibility(View.GONE);
                            break;
                        case "Pharmacy Order Form":
                            mpharmacyView.setVisibility(View.VISIBLE);
                            break;
                        case "Client Tracking and Termination":
                            mclientTracView.setVisibility(View.GONE);
                            break;
                        case "HIV Enrollment":
                            mhivEnrollmentView.setVisibility(View.GONE);
                            break;
                        case "Laboratory Order and Result form":
                            mlabView.setVisibility(View.VISIBLE);
                            break;
                        case "Pediatric Initial Clinical Evaluation":
                            mpedinitView.setVisibility(View.GONE);
                            break;
                        default:
                            // Do nothing
                            break;
                    }
                    break;
                    // End of ART

                default:
                    // Do nothing
                    break;

            }

        }
    }



    @Override
    public void startFormDisplayActivity(String formName, Long patientId, String valueRefString, String encounterType) {
        Intent intent = new Intent(getContext(), FormDisplayActivity.class);
        intent.putExtra(ApplicationConstants.BundleKeys.FORM_NAME, formName);
        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patientId);
        intent.putExtra(ApplicationConstants.BundleKeys.VALUEREFERENCE, valueRefString);
        intent.putExtra(ApplicationConstants.BundleKeys.ENCOUNTERTYPE, encounterType);
        startActivity(intent);
    }


    /**
     * Unbinds drawable resources
     */
    private void unbindDrawableResources() {
        if (null != mBitmapCache) {
            for (int i = 0; i < mBitmapCache.size(); i++) {
                Bitmap bitmap = mBitmapCache.valueAt(i);
                bitmap.recycle();
            }
        }
    }

    private void createImageBitmap(Integer key, ViewGroup.LayoutParams layoutParams) {
        if (mBitmapCache.get(key) == null) {
            mBitmapCache.put(key, ImageUtils.decodeBitmapFromResource(getResources(), key,
                    layoutParams.width, layoutParams.height));
        }
    }


    /**
     * Binds drawable resource to ImageView
     *
     * @param imageView  ImageView to bind resource to
     * @param drawableId id of drawable resource (for example R.id.somePicture);
     */
    private void bindDrawableResource(ImageView imageView, int drawableId) {
        mBitmapCache = new SparseArray<>();
        if (getView() != null) {
            createImageBitmap(drawableId, imageView.getLayoutParams());
            imageView.setImageBitmap(mBitmapCache.get(drawableId));
        }
    }

    @Override
    public void showError(String message) {
        ToastUtil.error(message);
    }

    public void bindDrawableResources() {
        bindDrawableResource(mmaternalButton, R.drawable.ico_vitals);
        bindDrawableResource(mheifButton, R.drawable.ico_vitals);
        bindDrawableResource(mhivEnrollmentButton, R.drawable.web);
        bindDrawableResource(mcareCardButton, R.drawable.ico_vitals);
        bindDrawableResource(mlabButton, R.drawable.microscope);
        bindDrawableResource(mpharmacyButton, R.drawable.tools_and_utensils);
        bindDrawableResource(madultinitButton, R.drawable.ico_vitals);

        bindDrawableResource(mpedinitButton, R.drawable.ico_vitals);
        bindDrawableResource(mtransferButton, R.drawable.ico_vitals);
        bindDrawableResource(mclientRefButton, R.drawable.arrow);
        bindDrawableResource(martComButton, R.drawable.ico_vitals);

        bindDrawableResource(mclientTracButton, R.drawable.ico_vitals);
        bindDrawableResource(mancTrackingButton, R.drawable.ico_vitals);
        bindDrawableResource(mpartnerButton, R.drawable.couple);
        bindDrawableResource(mclientinteakeButton, R.drawable.medical_client_intake);
        bindDrawableResource(mchildRSTButton, R.drawable.healthcare_and_medical);
        bindDrawableResource(madultRSTButton, R.drawable.adult);


        bindDrawableResource(mdeliveryButton, R.drawable.teddy_bear);
        bindDrawableResource(mmaternalButton, R.drawable.holidays);
        bindDrawableResource(mpmtcthtsButton, R.drawable.healthcare);
        bindDrawableResource(mchildButton, R.drawable.ico_vitals);
        changeColorOfDashboardIcons();
        if (ThemeUtils.isDarkModeActivated()) {
            changeColorOfDashboardIcons();
        }
    }

    private void changeColorOfDashboardIcons() {
        final int greenColorResId = R.color.green;
        final int redColorResId = R.color.light_red;
        final int purpleColorResId = R.color.dark_purple;
//        final int blueColorResId = R.color.snooper_blue;
        ImageUtils.changeImageViewTint(getContext(), mdeliveryButton, purpleColorResId);
        ImageUtils.changeImageViewTint(getContext(), mclientinteakeButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), mmaternalButton, purpleColorResId);
        ImageUtils.changeImageViewTint(getContext(), mpmtcthtsButton, purpleColorResId);
        ImageUtils.changeImageViewTint(getContext(), mhivEnrollmentButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), mclientRefButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), mchildRSTButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), madultRSTButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), mpharmacyButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), mlabButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), mpartnerButton, purpleColorResId);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.submit_done_menu, menu);
    }




}
