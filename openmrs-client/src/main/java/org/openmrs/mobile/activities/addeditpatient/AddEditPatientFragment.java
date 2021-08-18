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

package org.openmrs.mobile.activities.addeditpatient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.dialog.CameraOrGalleryPickerDialog;
import org.openmrs.mobile.activities.dialog.CustomFragmentDialog;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardActivity;
import org.openmrs.mobile.activities.patientdashboard.details.PatientPhotoActivity;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.bundle.CustomDialogBundle;
import org.openmrs.mobile.listeners.watcher.PatientBirthdateValidatorWatcher;
import org.openmrs.mobile.models.IdentifierType;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.PersonAddress;
import org.openmrs.mobile.models.PersonAttribute;
import org.openmrs.mobile.models.PersonAttributeType;
import org.openmrs.mobile.models.PersonName;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.IdGeneratorUtil;
import org.openmrs.mobile.utilities.ImageUtils;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

@RuntimePermissions
public class AddEditPatientFragment extends ACBaseFragment<AddEditPatientContract.Presenter> implements AddEditPatientContract.View {

    private RelativeLayout relativeLayout;
    private LocalDate birthdate;
    private DateTime bdt;

    private TextInputLayout firstNameTIL;
    private TextInputLayout middleNameTIL;
    private TextInputLayout lastNameTIL;
    private TextInputLayout address1TIL;
    private TextInputLayout countryTIL;
    private TextInputLayout uniqueIdTIL;
    private TextInputLayout phoneTIL;
    private TextInputLayout textInputLayoutHei;
    private TextInputLayout textInputLayoutOpenmrsCode;
    private TextInputLayout textInputLayoutHts;
    private TextInputLayout textInputLayoutArt;
    private TextInputLayout textInputLayoutAnc;


    private EditText edfname;
    private EditText edmname;
    private EditText edlname;
    private EditText eddob;
    private EditText edyr;
    private EditText edmonth;
    private EditText edaddr1;
    private EditText edaddr2;
    private AutoCompleteTextView edcity;
    private EditText edunique;
    private EditText edphonenumber;
    private AutoCompleteTextView edstate;
    private CountryCodePicker mCountryCodePicker;
    private AutoCompleteTextView edpostal;

    private EditText edart;
    private EditText edanc;
    private EditText edhts;
    private EditText edhei;
    private EditText edopenmrs;


    private CheckBox checkart;
    private CheckBox checkAnc;
    private CheckBox checkhts;
    private CheckBox checkhei;

    private RadioGroup gen;
    private ProgressBar progressBar;

    private TextView fnameerror;
    private TextView lnameerror;
    private TextView doberror;
    private TextView gendererror;
    private TextView addrerror;
    private TextView countryerror;
    private TextView uniqueerror;
    private TextView phoneerror;

    private Button datePicker;

    private DateTimeFormatter dateTimeFormatter;

    private ImageView patientImageView;

    private FloatingActionButton capturePhotoBtn;
    private Bitmap patientPhoto = null;
    private Bitmap resizedPatientPhoto = null;
    private String patientName;
    private File output = null;
    private final static int IMAGE_REQUEST = 1;
    private final static int GALLERY_IMAGE_REQUEST = 2;
    private OpenMRSLogger logger = new OpenMRSLogger();

    private boolean isUpdatePatient = false;
    private Patient updatedPatient;
    private int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;
    private String mLattitude = null;
    private String mLongitude = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_patient_info, container, false);
        setHasOptionsMenu(true);
        // Get location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        getLastLocation();
        resolveViews(root);
        hideEditText();
        addListeners();
        fillFields(mPresenter.getPatientToUpdate());
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    @Override
    public void finishPatientInfoActivity() {
        getActivity().finish();
    }

    @Override
    public void setErrorsVisibility(boolean givenNameError,
                                    boolean familyNameError,
                                    boolean dayOfBirthError,
                                    boolean addressError,
                                    boolean countryError,
                                    boolean genderError,
                                    boolean countryNull,
                                    boolean stateError,
                                    boolean cityError,
                                    boolean postalError) {
        // Only two dedicated text views will be visible for error messages.
        // Rest error messages will be displayed in dedicated TextInputLayouts.
        if (dayOfBirthError) {
            doberror.setVisibility(View.VISIBLE);

            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            String minimumDate = DateTime.now().minusYears(
                    ApplicationConstants.RegisterPatientRequirements.MAX_PATIENT_AGE)
                    .toString(dateTimeFormatter);
            String maximumDate = DateTime.now().toString(dateTimeFormatter);

            doberror.setText(getString(R.string.dob_error, minimumDate, maximumDate));
        } else {
            doberror.setVisibility(View.GONE);
        }

        if (genderError) {
            gendererror.setVisibility(View.VISIBLE);
        } else {
            gendererror.setVisibility(View.GONE);
        }
    }

    @Override
    public void scrollToTop() {
        ScrollView scrollView = this.getActivity().findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, scrollView.getPaddingTop());
    }


    private Patient updatePatientWithData(Patient patient) {
        String emptyError = getString(R.string.emptyerror);

        // Validate address
        if (ViewUtils.isEmpty(edaddr1)
                && ViewUtils.isEmpty(edaddr2)
                && ViewUtils.isEmpty(edcity)
                && ViewUtils.isEmpty(edpostal)
                && ViewUtils.isCountryCodePickerEmpty(mCountryCodePicker)
                && ViewUtils.isEmpty(edstate)) {

            addrerror.setText(R.string.atleastone);
            address1TIL.setErrorEnabled(true);
            address1TIL.setError(getString(R.string.atleastone));
        } else if (!ViewUtils.validateText(ViewUtils.getInput(edaddr1), ViewUtils.ILLEGAL_ADDRESS_CHARACTERS)
                || !ViewUtils.validateText(ViewUtils.getInput(edaddr2), ViewUtils.ILLEGAL_ADDRESS_CHARACTERS)) {

            addrerror.setText(getString(R.string.addr_invalid_error));
            address1TIL.setErrorEnabled(true);
            address1TIL.setError(getString(R.string.addr_invalid_error));
        } else {
            address1TIL.setErrorEnabled(false);
        }

        // Add address
        PersonAddress address = new PersonAddress();
        address.setAddress1(ViewUtils.getInput(edaddr1));
        address.setAddress2(ViewUtils.getInput(edaddr2));
        address.setCityVillage(ViewUtils.getInput(edcity));
        address.setPostalCode(ViewUtils.getInput(edpostal));
        address.setCountry(mCountryCodePicker.getSelectedCountryName());
        address.setStateProvince(ViewUtils.getInput(edstate));
        address.setLatitude(mLattitude);
        address.setLongitude(mLongitude);
        address.setPreferred(true);

        List<PersonAddress> addresses = new ArrayList<>();
        addresses.add(address);
        patient.setAddresses(addresses);

        // Validate names
        String givenNameEmpty = getString(R.string.fname_empty_error);
        // Invalid characters for given name only
        String givenNameError = getString(R.string.fname_invalid_error);
        // Invalid characters for the middle name
        String middleNameError = getString(R.string.midname_invalid_error);
        // Invalid family name
        String familyNameError = getString(R.string.lname_invalid_error);

        // First name validation
        if (ViewUtils.isEmpty(edfname)) {
            fnameerror.setText(emptyError);
            firstNameTIL.setErrorEnabled(true);
            firstNameTIL.setError(emptyError);
        } else if (!ViewUtils.validateText(ViewUtils.getInput(edfname), ViewUtils.ILLEGAL_CHARACTERS)) {
            lnameerror.setText(familyNameError);
            firstNameTIL.setErrorEnabled(true);
            firstNameTIL.setError(givenNameError);
        } else {
            firstNameTIL.setErrorEnabled(false);
        }

        // Middle name validation (can be empty)
        if (!ViewUtils.validateText(ViewUtils.getInput(edmname), ViewUtils.ILLEGAL_CHARACTERS)) {
            lnameerror.setText(familyNameError);
            middleNameTIL.setErrorEnabled(true);
            middleNameTIL.setError(middleNameError);
        } else {
            middleNameTIL.setErrorEnabled(false);
        }

        // Family name validation
        if (ViewUtils.isEmpty(edlname)) {
            lnameerror.setText(emptyError);
            lastNameTIL.setErrorEnabled(true);
            lastNameTIL.setError(emptyError);
        } else if (!ViewUtils.validateText(ViewUtils.getInput(edlname), ViewUtils.ILLEGAL_CHARACTERS)) {
            lnameerror.setText(familyNameError);
            lastNameTIL.setErrorEnabled(true);
            lastNameTIL.setError(familyNameError);
        } else {
            lastNameTIL.setErrorEnabled(false);
        }

        // Unique ID validation
//        if (ViewUtils.isEmpty(edunique)) {
//            uniqueerror.setText(emptyError);
//            uniqueIdTIL.setErrorEnabled(true);
//            uniqueIdTIL.setError(emptyError);
//        } else if (!ViewUtils.validateText(ViewUtils.getInput(edunique), ViewUtils.ILLEGAL_CHARACTERS)) {
//            uniqueerror.setText(familyNameError);
//            uniqueIdTIL.setErrorEnabled(true);
//            uniqueIdTIL.setError(familyNameError);
//        } else {
//            uniqueIdTIL.setErrorEnabled(false);
//        }
//        patient.se
        // Add names
        PersonName name = new PersonName();
        name.setFamilyName(ViewUtils.getInput(edlname));
        name.setGivenName(ViewUtils.getInput(edfname));
        name.setMiddleName(ViewUtils.getInput(edmname));

        List<PersonName> names = new ArrayList<>();
        names.add(name);
        patient.setNames(names);

        // Add Phone Number
        if (!ViewUtils.isEmpty(edphonenumber)) {
            PersonAttribute personAttribute = new PersonAttribute();
            PersonAttributeType personAttributeType = new PersonAttributeType();
            personAttributeType.setDisplay("Telephone Number");
            personAttributeType.setUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");
            personAttribute.setAttributeType(personAttributeType);
            personAttribute.setValue(ViewUtils.getInput(edphonenumber));

            List<PersonAttribute> pAttributes = new ArrayList<>();
            pAttributes.add(personAttribute);
            patient.setAttributes(pAttributes);
        }

        // Add gender
        String[] genderChoices = {"M", "F"};
        int index = gen.indexOfChild(getActivity().findViewById(gen.getCheckedRadioButtonId()));
        if (index != -1) {
            patient.setGender(genderChoices[index]);
        } else {
            patient.setGender(null);
        }

        // Add birthdate
        String birthdate = null;
        if (ViewUtils.isEmpty(eddob)) {
            if (!StringUtils.isBlank(ViewUtils.getInput(edyr)) || !StringUtils.isBlank(ViewUtils.getInput(edmonth))) {
                dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT);

                int yeardiff = ViewUtils.isEmpty(edyr) ? 0 : Integer.parseInt(edyr.getText().toString());
                int mondiff = ViewUtils.isEmpty(edmonth) ? 0 : Integer.parseInt(edmonth.getText().toString());
                LocalDate now = new LocalDate();
                bdt = now.toDateTimeAtStartOfDay().toDateTime();
                bdt = bdt.minusYears(yeardiff);
                bdt = bdt.minusMonths(mondiff);
                patient.setBirthdateEstimated(true);
                birthdate = dateTimeFormatter.print(bdt);
            }
        } else {
            String unvalidatedDate = eddob.getText().toString().trim();

            DateTime minDateOfBirth = DateTime.now().minusYears(
                    ApplicationConstants.RegisterPatientRequirements.MAX_PATIENT_AGE);
            DateTime maxDateOfBirth = DateTime.now();

            if (DateUtils.validateDate(unvalidatedDate, minDateOfBirth, maxDateOfBirth)) {
                dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
                bdt = dateTimeFormatter.parseDateTime(unvalidatedDate);

                dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT);
                birthdate = dateTimeFormatter.print(bdt);
            }
        }
        patient.setBirthdate(birthdate);

        if (patientPhoto != null)
            patient.setPhoto(patientPhoto);

        return patient;
    }


    private Patient createPatient() {
        Patient patient = new Patient();
        List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
        if (!ViewUtils.isEmpty(edunique)){
            final PatientIdentifier patientIdentifier = new PatientIdentifier();
            patientIdentifier.setIdentifier(ViewUtils.getInput(edunique));
            IdentifierType identifierType = new IdentifierType("Hospital Number");
            patientIdentifier.setDisplay("Hospital Number");
            patientIdentifier.setIdentifierType(identifierType);
            identifiers.add(patientIdentifier);
        }else {
            final PatientIdentifier patientIdentifier = new PatientIdentifier();
            patientIdentifier.setIdentifier(IdGeneratorUtil.getAlphaNumericString(10));
            IdentifierType identifierType = new IdentifierType("Hospital Number");
            patientIdentifier.setDisplay("Hospital Number");
            patientIdentifier.setIdentifierType(identifierType);
            identifiers.add(patientIdentifier);
        }
//        if (!ViewUtils.isEmpty(edanc)){
//            PatientIdentifier patientIdentifier = new PatientIdentifier();
//            patientIdentifier.setIdentifier(ViewUtils.getInput(edanc));
//            IdentifierType identifierType = new IdentifierType("ANC Number");
//            patientIdentifier.setDisplay("ANC Number");
//            patientIdentifier.setIdentifierType(identifierType);
//            identifiers.add(patientIdentifier);
//        }
//        if (!ViewUtils.isEmpty(edart)){
//            PatientIdentifier patientIdentifier = new PatientIdentifier();
//            patientIdentifier.setIdentifier(ViewUtils.getInput(edart));
//            IdentifierType identifierType = new IdentifierType("ART Number");
//            patientIdentifier.setDisplay("ART Number");
//            patientIdentifier.setIdentifierType(identifierType);
//            identifiers.add(patientIdentifier);
//        }
//        if (!ViewUtils.isEmpty(edhts)){
//            PatientIdentifier patientIdentifier = new PatientIdentifier();
//            patientIdentifier.setIdentifier(ViewUtils.getInput(edhts));
//            IdentifierType identifierType = new IdentifierType("HIV testing Id (Client Code)");
//            patientIdentifier.setDisplay("HIV testing Id (Client Code)");
//            patientIdentifier.setIdentifierType(identifierType);
//            identifiers.add(patientIdentifier);
//        }
//        if (!ViewUtils.isEmpty(edhei)){
//            PatientIdentifier patientIdentifier = new PatientIdentifier();
//            patientIdentifier.setIdentifier(ViewUtils.getInput(edhei));
//            IdentifierType identifierType = new IdentifierType("Exposed Infant Id");
//            patientIdentifier.setDisplay("Exposed Infant Id");
//            patientIdentifier.setIdentifierType(identifierType);
//            identifiers.add(patientIdentifier);
//        }
        patient.setIdentifiers(identifiers);
        updatePatientWithData(patient);
        patient.setUuid(" ");

        return patient;


    }

    private Patient updatePatient(Patient patient) {
        List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
        if (!ViewUtils.isEmpty(edunique)){
            final PatientIdentifier patientIdentifier = new PatientIdentifier();
            patientIdentifier.setIdentifier(ViewUtils.getInput(edunique));
            IdentifierType identifierType = new IdentifierType("Hospital Number");
            patientIdentifier.setIdentifierType(identifierType);
            patientIdentifier.setDisplay("Hospital Number");
            identifiers.add(patientIdentifier);
        }
        if (!ViewUtils.isEmpty(edopenmrs)){
            final PatientIdentifier patientIdentifier = new PatientIdentifier();
            patientIdentifier.setIdentifier(ViewUtils.getInput(edopenmrs));
            IdentifierType identifierType = new IdentifierType("OpenMRS ID");
            patientIdentifier.setIdentifierType(identifierType);
            patientIdentifier.setDisplay("OpenMRS ID");
            identifiers.add(patientIdentifier);
        }
//        if (!ViewUtils.isEmpty(edanc)){
//            PatientIdentifier patientIdentifier = new PatientIdentifier();
//            patientIdentifier.setIdentifier(ViewUtils.getInput(edanc));
//            IdentifierType identifierType = new IdentifierType("ANC Number");
//            patientIdentifier.setIdentifierType(identifierType);
//            patientIdentifier.setDisplay("ANC Number");
//            identifiers.add(patientIdentifier);
//        }
//        if (!ViewUtils.isEmpty(edart)){
//            PatientIdentifier patientIdentifier = new PatientIdentifier();
//            patientIdentifier.setIdentifier(ViewUtils.getInput(edart));
//            IdentifierType identifierType = new IdentifierType("ART Number");
//            patientIdentifier.setIdentifierType(identifierType);
//            patientIdentifier.setDisplay("ART Number");
//            identifiers.add(patientIdentifier);
//        }
//        if (!ViewUtils.isEmpty(edhts)){
//            PatientIdentifier patientIdentifier = new PatientIdentifier();
//            patientIdentifier.setIdentifier(ViewUtils.getInput(edhts));
//            IdentifierType identifierType = new IdentifierType("HIV testing Id (Client Code)");
//            patientIdentifier.setIdentifierType(identifierType);
//            patientIdentifier.setDisplay("HIV testing Id (Client Code)");
//            identifiers.add(patientIdentifier);
//        }
//        if (!ViewUtils.isEmpty(edhei)){
//            PatientIdentifier patientIdentifier = new PatientIdentifier();
//            patientIdentifier.setIdentifier(ViewUtils.getInput(edhei));
//            IdentifierType identifierType = new IdentifierType("Exposed Infant Id");
//            patientIdentifier.setIdentifierType(identifierType);
//            patientIdentifier.setDisplay("Exposed Infant Id");
//            identifiers.add(patientIdentifier);
//        }
        patient.setIdentifiers(identifiers);
        return updatePatientWithData(patient);
    }

    @Override
    public void hideSoftKeys() {
        View view = this.getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(this.getActivity());
        }
        InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void setProgressBarVisibility(boolean visibility) {
        progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showSimilarPatientDialog(List<Patient> patients, Patient newPatient) {
        setProgressBarVisibility(false);
        CustomDialogBundle similarPatientsDialog = new CustomDialogBundle();
        similarPatientsDialog.setTitleViewMessage(getString(R.string.similar_patients_dialog_title));
        similarPatientsDialog.setRightButtonText(getString(R.string.dialog_button_register_new));
        similarPatientsDialog.setRightButtonAction(CustomFragmentDialog.OnClickAction.REGISTER_PATIENT);
        similarPatientsDialog.setLeftButtonText(getString(R.string.dialog_button_cancel));
        similarPatientsDialog.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        similarPatientsDialog.setPatientsList(patients);
        similarPatientsDialog.setNewPatient(newPatient);
        ((AddEditPatientActivity) Objects.requireNonNull(this.getActivity())).createAndShowDialog(similarPatientsDialog, ApplicationConstants.DialogTAG.SIMILAR_PATIENTS_TAG);
    }

    @Override
    public void startPatientDashbordActivity(Patient patient) {
        Intent intent = new Intent(getActivity(), PatientDashboardActivity.class);
        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patient.getId());
        startActivity(intent);
    }

    @Override
    public void showUpgradeRegistrationModuleInfo() {
        ToastUtil.notifyLong(getResources().getString(R.string.registration_core_info));
    }

    @Override
    public boolean areFieldsNotEmpty() {
        return (!ViewUtils.isEmpty(edfname) ||
                (!ViewUtils.isEmpty(edmname)) ||
                (!ViewUtils.isEmpty(edlname)) ||
//                (!ViewUtils.isEmpty(edunique)) ||
                (!ViewUtils.isEmpty(eddob)) ||
                (!ViewUtils.isEmpty(edyr)) ||
                (!ViewUtils.isEmpty(edaddr1)) ||
                (!ViewUtils.isEmpty(edaddr2)) ||
                (!ViewUtils.isEmpty(edcity)) ||
                (!ViewUtils.isEmpty(edstate)) ||
                (!ViewUtils.isCountryCodePickerEmpty(mCountryCodePicker)) ||
                (!ViewUtils.isEmpty(edpostal)));
    }

    public static AddEditPatientFragment newInstance() {
        return new AddEditPatientFragment();
    }

    private void resolveViews(View v) {
        relativeLayout = v.findViewById(R.id.addEditRelativeLayout);
        edfname = v.findViewById(R.id.firstname);
        edmname = v.findViewById(R.id.middlename);
        edlname = v.findViewById(R.id.surname);
        edunique = v.findViewById(R.id.uniqueid);
        eddob = v.findViewById(R.id.dob);
        edyr = v.findViewById(R.id.estyr);
        edmonth = v.findViewById(R.id.estmonth);
        edaddr1 = v.findViewById(R.id.addr1);
        edaddr2 = v.findViewById(R.id.addr2);
        edcity = v.findViewById(R.id.city);
        edstate = v.findViewById(R.id.state);
        mCountryCodePicker=v.findViewById(R.id.ccp);
        edpostal = v.findViewById(R.id.postal);
        edanc = v.findViewById(R.id.anc);
        edart = v.findViewById(R.id.art);
        edhei = v.findViewById(R.id.hei);
        edopenmrs= v.findViewById(R.id.openmrsCode);
        edhts = v.findViewById(R.id.hts);
        edphonenumber = v.findViewById(R.id.phonenumber);

        checkAnc=v.findViewById(R.id.checkAnc);
        checkart=v.findViewById(R.id.checkArt);
        checkhts=v.findViewById(R.id.checkHts);
        checkhei=v.findViewById(R.id.checkHei);

        gen = v.findViewById(R.id.gender);
        progressBar = v.findViewById(R.id.progress_bar);

        fnameerror = v.findViewById(R.id.fnameerror);
        lnameerror = v.findViewById(R.id.lnameerror);
        doberror = v.findViewById(R.id.doberror);
        gendererror = v.findViewById(R.id.gendererror);
        addrerror = v.findViewById(R.id.addrerror);
        countryerror = v.findViewById(R.id.countryerror);
        uniqueerror = v.findViewById(R.id.uniqueiderror);
        phoneerror = v.findViewById(R.id.phoneerror);

        datePicker = v.findViewById(R.id.btn_datepicker);
        capturePhotoBtn = v.findViewById(R.id.capture_photo);
        patientImageView = v.findViewById(R.id.patientPhoto);

        firstNameTIL = v.findViewById(R.id.textInputLayoutFirstName);
        middleNameTIL = v.findViewById(R.id.textInputLayoutMiddlename);
        lastNameTIL = v.findViewById(R.id.textInputLayoutSurname);
        address1TIL = v.findViewById(R.id.textInputLayoutAddress);
        countryTIL = v.findViewById(R.id.textInputLayoutCountry);
        uniqueIdTIL = v.findViewById(R.id.textInputLayoutUniqueId);

        textInputLayoutHei = v.findViewById(R.id.textInputLayoutHei);
        textInputLayoutOpenmrsCode = v.findViewById(R.id.textInputLayoutOpenmrsCode);
        textInputLayoutHts = v.findViewById(R.id.textInputLayoutHts);
        textInputLayoutAnc = v.findViewById(R.id.textInputLayoutAnc);
        textInputLayoutArt = v.findViewById(R.id.textInputLayoutArt);
    }

    private void fillFields(final Patient patient) {
        if (patient != null) {
            //Change to Update Patient Form
            try {
                String updatePatientStr = getResources().getString(R.string.action_update_patient_data);
                this.getActivity().setTitle(updatePatientStr);

                isUpdatePatient = true;
                updatedPatient = patient;

                edfname.setText(patient.getName().getGivenName());
                edmname.setText(patient.getName().getMiddleName());
                edlname.setText(patient.getName().getFamilyName());

                patientName = patient.getName().getNameString();

                if (StringUtils.notNull(patient.getBirthdate()) || StringUtils.notEmpty(patient.getBirthdate())) {
                    bdt = DateUtils.convertTimeString(patient.getBirthdate());
                    eddob.setText(DateUtils.convertTime(DateUtils.convertTime(bdt.toString(), DateUtils.OPEN_MRS_REQUEST_FORMAT),
                            DateUtils.DEFAULT_DATE_FORMAT));
                }

                if (("M").equals(patient.getGender())) {
                    gen.check(R.id.male);
                } else if (("F").equals(patient.getGender())) {
                    gen.check(R.id.female);
                }

                edaddr1.setText(patient.getAddress().getAddress1());
                edaddr2.setText(patient.getAddress().getAddress2());
                edcity.setText(patient.getAddress().getCityVillage());
                edstate.setText(patient.getAddress().getStateProvince());
                edpostal.setText(patient.getAddress().getPostalCode());
                edphonenumber.setText(patient.getAttribute().getValue());

                for (PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
                    if (patientIdentifier.getDisplay() != null && patientIdentifier.getDisplay().equals("Hospital Number")) {
                        edunique.setText(patientIdentifier.getIdentifier());
                    } else if (patientIdentifier.getDisplay() != null && patientIdentifier.getDisplay().equals("ANC Number")) {
                        edanc.setText(patientIdentifier.getIdentifier());
                        checkAnc.setChecked(true);
                    } else if (patientIdentifier.getDisplay() != null && patientIdentifier.getDisplay().equals("ART Number")) {
                        edart.setText(patientIdentifier.getIdentifier());
                        checkart.setChecked(true);
                    } else if (patientIdentifier.getDisplay() != null && patientIdentifier.getDisplay().equals("Exposed Infant Id")) {
                        edhei.setText(patientIdentifier.getIdentifier());
                        checkhei.setChecked(true);
                    } else if (patientIdentifier.getDisplay() != null && patientIdentifier.getDisplay().equals("HIV testing Id (Client Code)")) {
                        edhts.setText(patientIdentifier.getIdentifier());
                        checkhts.setChecked(true);
                    }else if (patientIdentifier.getDisplay() != null && patientIdentifier.getDisplay().equals("OpenMRS ID")) {
                        edopenmrs.setText(patientIdentifier.getIdentifier());
                    }
                }

                if (patient.getPhoto() != null) {
                    patientPhoto = patient.getPhoto();
                    resizedPatientPhoto = patient.getResizedPhoto();
                    patientImageView.setImageBitmap(resizedPatientPhoto);
                }
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }
        }
    }

    private void hideEditText(){
        textInputLayoutAnc.setVisibility(View.GONE);
        textInputLayoutArt.setVisibility(View.GONE);
        textInputLayoutHts.setVisibility(View.GONE);
        textInputLayoutHei.setVisibility(View.GONE);
        textInputLayoutOpenmrsCode.setVisibility(View.GONE);
    }
    private void addSuggestionsToCities() {
        String country_name = mCountryCodePicker.getSelectedCountryName();
        country_name = country_name.replace("(", "");
        country_name = country_name.replace(")", "");
        country_name = country_name.replace(" ", "");
        country_name = country_name.replace("-", "_");
        country_name = country_name.replace(".", "");
        country_name = country_name.replace("'", "");
        int resourceId = this.getResources().getIdentifier(country_name.toLowerCase(), "array", getContext().getPackageName());
        if (resourceId != 0) {
            String[] states = getContext().getResources().getStringArray(resourceId);
            ArrayAdapter<String> state_adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, states);
            edstate.setAdapter(state_adapter);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AddEditPatientFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private void addListeners() {
        gen.setOnCheckedChangeListener((radioGroup, checkedId) -> gendererror.setVisibility(View.GONE));
        edstate.setOnFocusChangeListener((view, hasFocus) -> addSuggestionsToCities());

        edstate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                String selection = (String)parent.getItemAtPosition(position);
                int resourceId = getActivity().getResources().getIdentifier(selection.toLowerCase(), "array", getContext().getPackageName());
                if (resourceId != 0) {
                    String[] lgas = getContext().getResources().getStringArray(resourceId);
                    ArrayAdapter<String> lga_adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_dropdown_item_1line, lgas);
                    edcity.setAdapter(lga_adapter);
                }
            }
        });
        edcity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                String selection = (String)parent.getItemAtPosition(position);
                selection = "lga_" + selection.toLowerCase().replace("'","").replace(" ", "_").replace("-","_").replace("/","_");
                int resourceId = getActivity().getResources().getIdentifier(selection, "array", getContext().getPackageName());
                if (resourceId != 0) {
                    String[] wards = getContext().getResources().getStringArray(resourceId);
                    ArrayAdapter<String> ward_adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_dropdown_item_1line, wards);
                    edpostal.setAdapter(ward_adapter);
                }
            }
        });
        eddob.setClickable(true);
        eddob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Only needs afterTextChanged method from TextWacher
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Only needs afterTextChanged method from TextWacher
            }

            @Override
            public void afterTextChanged(Editable s) {
                // If a considerable amount of text is filled in eddob, then remove 'Estimated age' fields.
                if (s.length() >= 8) {
                    edmonth.getText().clear();
                    edyr.getText().clear();
                }
            }

        });

        datePicker.setBackgroundColor(Color.GRAY);

        datePicker.setOnClickListener(v -> {
            int cYear;
            int cMonth;
            int cDay;

            if (bdt == null) {
                Calendar currentDate = Calendar.getInstance();
                cYear = currentDate.get(Calendar.YEAR);
                cMonth = currentDate.get(Calendar.MONTH);
                cDay = currentDate.get(Calendar.DAY_OF_MONTH);
            } else {
                cYear = bdt.getYear();
                cMonth = bdt.getMonthOfYear() - 1;
                cDay = bdt.getDayOfMonth();
            }

            edmonth.getText().clear();
            edyr.getText().clear();

            DatePickerDialog mDatePicker = new DatePickerDialog(AddEditPatientFragment.this.getActivity(), (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                int adjustedMonth = selectedMonth + 1;
                eddob.setText(selectedDay + "/" + adjustedMonth + "/" + selectedYear);
                birthdate = new LocalDate(selectedYear, adjustedMonth, selectedDay);
                bdt = birthdate.toDateTimeAtStartOfDay().toDateTime();
            }, cYear, cMonth, cDay);
            mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            mDatePicker.setTitle(getString(R.string.date_picker_title));
            mDatePicker.show();

        });

        capturePhotoBtn.setOnClickListener(view -> {

            CameraOrGalleryPickerDialog dialog = CameraOrGalleryPickerDialog.getInstance(
                    (dialog1, which) -> {
                                if (which == 0) {
                                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                    StrictMode.setVmPolicy(builder.build());
                                    AddEditPatientFragmentPermissionsDispatcher.capturePhotoWithCheck(AddEditPatientFragment.this);
                                } else if (which == 1) {
                                    Intent i;
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                                        i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    else
                                        i = new Intent(Intent.ACTION_GET_CONTENT);
                                    i.addCategory(Intent.CATEGORY_OPENABLE);
                                    i.setType("image/*");
                                    startActivityForResult(i, GALLERY_IMAGE_REQUEST);
                                } else {
                                    patientImageView.setImageResource(R.drawable.ic_person_grey_500_48dp);
                                    patientImageView.invalidate();
                                    patientPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ic_person_grey_500_48dp);
                                }
                            }
                        );
                dialog.show(getChildFragmentManager(), null);
            }
        );


        patientImageView.setOnClickListener(view -> {
            if (output != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(output), "image/jpeg");
                startActivity(i);
            } else if (patientPhoto != null) {
                Intent intent = new Intent(getContext(), PatientPhotoActivity.class);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                patientPhoto.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
                intent.putExtra("photo", byteArrayOutputStream.toByteArray());
                intent.putExtra("name", patientName);
                startActivity(intent);

            }
        });

        TextWatcher textWatcher = new PatientBirthdateValidatorWatcher(eddob, edmonth, edyr);
        edmonth.addTextChangedListener(textWatcher);
        edyr.addTextChangedListener(textWatcher);

        checkhts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkhts.isChecked()){
                    textInputLayoutHts.setVisibility(View.VISIBLE);
                }else {
                    textInputLayoutHts.setVisibility(View.GONE);
                }
            }
        });
        checkart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkart.isChecked()){
                    textInputLayoutArt.setVisibility(View.VISIBLE);
                }else {
                    textInputLayoutArt.setVisibility(View.GONE);
                }
            }
        });
        checkAnc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkAnc.isChecked()){
                    textInputLayoutAnc.setVisibility(View.VISIBLE);
                }else {
                    textInputLayoutAnc.setVisibility(View.GONE);
                }
            }
        });
        checkhei.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkhei.isChecked()){
                    textInputLayoutHei.setVisibility(View.VISIBLE);
                }else {
                    textInputLayoutHei.setVisibility(View.GONE);
                }
            }
        });
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            output = new File(dir, getUniqueImageFileName());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
            startActivityForResult(takePictureIntent, IMAGE_REQUEST);
        }
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.permission_camera_rationale)
                .setPositiveButton(R.string.button_allow, (dialog, which) -> request.proceed())
                .setNegativeButton(R.string.button_deny, (dialog, button) -> request.cancel())
                .show();
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void showDeniedForCamera() {
        createSnackbarLong(R.string.permission_camera_denied)
                .show();
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void showNeverAskForCamera() {
        createSnackbarLong(R.string.permission_camera_neverask)
                .show();
    }

    private Snackbar createSnackbarLong(int stringId) {
        Snackbar snackbar = Snackbar.make(relativeLayout, stringId, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        return snackbar;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                patientPhoto = getResizedPortraitImage(output.getPath());
                Bitmap bitmap = ThumbnailUtils.extractThumbnail(patientPhoto, patientImageView.getWidth(), patientImageView.getHeight());
                patientImageView.setImageBitmap(bitmap);
                patientImageView.invalidate();
            } else {
                output = null;
            }
        } else if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            try {
                ParcelFileDescriptor parcelFileDescriptor =
                        getActivity().getContentResolver().openFileDescriptor(data.getData(), "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();

                patientPhoto = image;
                Bitmap bitmap = ThumbnailUtils.extractThumbnail(patientPhoto, patientImageView.getWidth(), patientImageView.getHeight());
                patientImageView.setImageBitmap(bitmap);
                patientImageView.invalidate();
            } catch (Exception e) {
                logger.e("Error getting image from gallery.", e);
            }
        }
    }

    private String getUniqueImageFileName() {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return timeStamp + "_" + ".jpg";
    }

    private Bitmap getResizedPortraitImage(String imagePath) {
        Bitmap portraitImg;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap photo = BitmapFactory.decodeFile(output.getPath(), options);
        float rotateAngle;
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateAngle = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateAngle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateAngle = 90;
                    break;
                default:
                    rotateAngle = 0;
                    break;
            }
            portraitImg = ImageUtils.rotateImage(photo, rotateAngle);
        } catch (IOException e) {
            logger.e(e.getMessage());
            portraitImg = photo;
        }
        return ImageUtils.resizePhoto(portraitImg);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.submit_done_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.actionSubmit:
                submitAction();
                return true;
            default:
                // Do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitAction() {
        if (isUpdatePatient) {
            mPresenter.confirmUpdate(updatePatient(updatedPatient));
        } else {
            mPresenter.confirmRegister(createPatient());
        }
    }
    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<android.location.Location>() {
                            @Override
                            public void onComplete(@NonNull Task<android.location.Location> task) {
                                android.location.Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    mLattitude = location.getLatitude()+"";
                                    mLongitude = location.getLongitude()+"";
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this.getActivity(), "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            mLattitude = mLastLocation.getLatitude()+"";
            mLongitude = mLastLocation.getLongitude()+"";
        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this.getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager)this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_ID) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLastLocation();
//            }
//        }
//    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }
}
