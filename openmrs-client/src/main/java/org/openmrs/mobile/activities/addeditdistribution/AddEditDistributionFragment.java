package org.openmrs.mobile.activities.addeditdistribution;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionContract;
import org.openmrs.mobile.activities.commodity.CommodityActivity;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.models.Department;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//import org.openmrs.mobile.activities.addeditpatient.AddEditPatientFragmentPermissionsDispatcher;

public class AddEditDistributionFragment extends ACBaseFragment<AddEditDistributionContract.Presenter> implements AddEditDistributionContract.View {

    private RelativeLayout relativeLayout;
    private LocalDate birthdate;
    private DateTime bdt;
    private ProgressBar progressBar;
    private TextInputLayout textInputLayoutDistribution;
    private EditText edoperationNumber;
    private EditText edoperationDate;
    private EditText edquantity;
    private EditText edwastage;
    private TextView operationdateerror;
    private Button datePicker;
    private Spinner mInstanceTypeSpinner;
    private TextView instanceTypeerror;
    private Spinner mDepartmentSpinner;
    private TextView departmenterror;
    private Spinner mSourceSpinner;
    private TextView sourceerror;
    private Spinner mStatusSpinner;
    private TextView statuserror;
    private Spinner mCommoditySourceSpinner;
    private TextView commoditySourceerror;
    private Spinner mCommodityTypeSpinner;
    private TextView commodityTypeerror;
    private Spinner mDataSystemSpinner;
    private TextView datasystemerror;

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

    private boolean isUpdateDistribution = false;
    private Distribution updatedDistribution;
    private int PERMISSION_ID = 44;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_distribution_info, container, false);
        setHasOptionsMenu(true);
        resolveViews(root);
        addListeners();
        fillFields(mPresenter.getDistributionToUpdate());
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    @Override
    public void finishDistributionInfoActivity() {
        getActivity().finish();
    }

    @Override
    public void setErrorsVisibility(boolean distributionError) {
        // Only two dedicated text views will be visible for error messages.
        // Rest error messages will be displayed in dedicated TextInputLayouts.

    }

    @Override
    public void scrollToTop() {
        ScrollView scrollView = this.getActivity().findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, scrollView.getPaddingTop());
    }

    private void updateDistributionWithData(Distribution distribution) {
        String distribution_date = null;
        if (!ViewUtils.isEmpty(edoperationDate)) {
            String unvalidatedDate = edoperationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            bdt = dateTimeFormatter.parseDateTime(unvalidatedDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT);
            distribution_date = dateTimeFormatter.print(bdt);
            distribution.setOperationDate(distribution_date);
        }
//        Department department = new Select()
//                .from(Department.class)
//                .where("name = ?", mDepartmentSpinner.getSelectedItem().toString())
//                .executeSingle();
//        Item item = new Select()
//                .from(Item.class)
//                .where("name = ?", mItemSpinner.getSelectedItem().toString())
//                .executeSingle();
//        distribution.setDepartment(department.getUuid());
//        distribution.setItem(item.getUuid());
//        distribution.setBatchNumber(mItemBatchSpinner.getSelectedItem().toString());
//        distribution.setTestPurpose(mTestPurposeSpinner.getSelectedItem().toString());
//        distribution.setDataSystem(mDataSystemSpinner.getSelectedItem().toString());
//        distribution.setQuantity(ViewUtils.isEmpty(edquantity) ? 0 : Integer.parseInt(edquantity.getText().toString()));
//        distribution.setWastage(ViewUtils.isEmpty(edwastage) ? 0 : Integer.parseInt(edwastage.getText().toString()));

    }

    private Distribution updateDistribution(Distribution distribution) {
        updateDistributionWithData(distribution);
        return distribution;
    }


    private Distribution createDistribution() {
        Distribution distribution = new Distribution();
        updateDistributionWithData(distribution);
        return distribution;


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
    public void startCommodityDashboardActivity() {
        Intent intent = new Intent(getActivity(), CommodityActivity.class);
//        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patient.getId());
        startActivity(intent);
    }
//
//    @Override
//    public void showUpgradeRegistrationModuleInfo() {
//        ToastUtil.notifyLong(getResources().getString(R.string.registration_core_info));
//    }
//
    @Override
    public boolean areFieldsNotEmpty() {
        return (!ViewUtils.isEmpty(edoperationDate));
    }

    public static AddEditDistributionFragment newInstance() {
        return new AddEditDistributionFragment();
    }

    private void resolveViews(View v) {
        relativeLayout = v.findViewById(R.id.addEditRelativeLayout);
        edoperationDate = v.findViewById(R.id.operation_date);
        edoperationNumber = v.findViewById(R.id.operationNumber);
        operationdateerror= v.findViewById(R.id.operationdateerror);
        departmenterror= v.findViewById(R.id.departmenterror);
        sourceerror= v.findViewById(R.id.statuserror);
        instanceTypeerror= v.findViewById(R.id.instanceTypeerror);
        statuserror= v.findViewById(R.id.statuserror);
        commoditySourceerror= v.findViewById(R.id.commoditysourceerror);
        datasystemerror= v.findViewById(R.id.datasystemerror);
        commodityTypeerror= v.findViewById(R.id.commoditytypeerror);
        datePicker = v.findViewById(R.id.btn_datepicker);
//        textInputLayoutDistribution = v.findViewById(R.id.textInputLayoutDistribution);
        progressBar = v.findViewById(R.id.progress_bar);
        mDepartmentSpinner = v.findViewById(R.id.department);
        mSourceSpinner = v.findViewById(R.id.source);
//        mItemSpinner = v.findViewById(R.id.item);
        mInstanceTypeSpinner = v.findViewById(R.id.instanceType);
        mStatusSpinner = v.findViewById(R.id.status);
        mCommoditySourceSpinner= v.findViewById(R.id.commoditysource);
        edoperationNumber = v.findViewById(R.id.operationNumber);
        mCommodityTypeSpinner= v.findViewById(R.id.commodity_type);
        mDataSystemSpinner= v.findViewById(R.id.data_system);
//        FontsUtil.setFont(edwastage, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
//        edquantity.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//        edquantity.setPadding(15, 0, 0, 15);
//        FontsUtil.setFont(edquantity, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
        edoperationDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        edoperationDate.setPadding(15, 0, 0, 15);
        FontsUtil.setFont(edoperationDate, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
    }

    private void fillFields(final Distribution distribution) {

        if (distribution != null) {
            isUpdateDistribution = true;
            updatedDistribution = distribution;
            //Change to Update Distribution Form
            try {
//                edwastage.setText(distribution.getWastage());
//                edquantity.setText(distribution.getQuantity());
                if (StringUtils.notNull(distribution.getOperationDate()) || StringUtils.notEmpty(distribution.getOperationDate())) {
                    bdt = DateUtils.convertTimeString(distribution.getOperationDate());
                    edoperationDate.setText(DateUtils.convertTime(DateUtils.convertTime(bdt.toString(), DateUtils.OPEN_MRS_REQUEST_FORMAT),
                            DateUtils.DEFAULT_DATE_FORMAT));
                }
//                mDepartmentSpinner.setSelection( 2 );

            } catch (Exception e){
                ToastUtil.error(e.toString());
            }
        }
    }

    private void addListeners() {
        List<Department> departments = new Select()
                .distinct()
                .from(Department.class)
                .groupBy("name")
                .execute();
        List<String> department_strs = new ArrayList<String>();
        for (Department row : departments) {
            department_strs.add(row.getName());
        }
//
//        List<Item> items = new Select()
//                .distinct()
//                .from(Item.class)
//                .groupBy("name")
//                .execute();
//        List<String> item_strs = new ArrayList<String>();
//        for (Item row : items) {
//            item_strs.add(row.getName());
//        }
//
//        List<ItemBatch> item_batches = new Select()
//                .distinct()
//                .from(ItemBatch.class)
//                .groupBy("name")
//                .execute();
//        List<String> item_batch_strs = new ArrayList<String>();
//        for (ItemBatch row : item_batches) {
//            item_batch_strs.add(row.getName());
//        }
        List<String> instance_type_strs = new ArrayList<String>();
        instance_type_strs.add("Distribution");
        instance_type_strs.add("Distribution");

        List<String> source_strs = new ArrayList<String>();
        source_strs.add("Main");
        source_strs.add("Pharmacy");

        List<String> commodity_source_strs = new ArrayList<String>();
        commodity_source_strs.add("PEPFAR");
        commodity_source_strs.add("GF");
        commodity_source_strs.add("GoN");

        List<String> commodity_type_strs = new ArrayList<String>();
        commodity_type_strs.add("New");

        List<String> status_strs = new ArrayList<String>();
        status_strs.add("New");

        List<String> data_system_strs = new ArrayList<String>();
        data_system_strs.add("Mobile");
        data_system_strs.add("Laptop");
        distributionAdapter(mInstanceTypeSpinner,instance_type_strs);
        distributionAdapter(mSourceSpinner,source_strs);
        distributionAdapter(mCommoditySourceSpinner,commodity_source_strs);
        distributionAdapter(mCommodityTypeSpinner,commodity_type_strs);
        distributionAdapter(mDataSystemSpinner,data_system_strs);
        distributionAdapter(mStatusSpinner,status_strs);
        distributionAdapter(mDepartmentSpinner,department_strs);





        edoperationDate.setClickable(true);
        edoperationDate.addTextChangedListener(new TextWatcher() {
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


            DatePickerDialog mDatePicker = new DatePickerDialog(AddEditDistributionFragment.this.getActivity(), (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                int adjustedMonth = selectedMonth + 1;
                edoperationDate.setText(selectedDay + "/" + adjustedMonth + "/" + selectedYear);
                birthdate = new LocalDate(selectedYear, adjustedMonth, selectedDay);
                bdt = birthdate.toDateTimeAtStartOfDay().toDateTime();
            }, cYear, cMonth, cDay);
            mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            mDatePicker.setTitle(getString(R.string.date_picker_title));
            mDatePicker.show();

        });

    }

    public void distributionAdapter(Spinner spinner, List<String> records ){
        ArrayAdapter arrayAdapter_department = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, records) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setPadding(5, 20, 0, 0);
                FontsUtil.setFont(text, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                return view;
            }
        };
        arrayAdapter_department.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter_department);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selection = (String) parent.getItemAtPosition(position);
                String item = parent.getItemAtPosition(position).toString();

            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        if (isUpdateDistribution) {
            mPresenter.confirmUpdate(updateDistribution(updatedDistribution));
        } else {
            mPresenter.confirmRegister(createDistribution());
        }
    }



    @Override
    public void onResume(){
        super.onResume();
    }
}

