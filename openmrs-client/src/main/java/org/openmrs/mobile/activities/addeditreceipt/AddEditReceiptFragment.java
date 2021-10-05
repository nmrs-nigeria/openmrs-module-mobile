package org.openmrs.mobile.activities.addeditreceipt;

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
import org.openmrs.mobile.activities.addeditreceipt.AddEditReceiptContract;
import org.openmrs.mobile.activities.commodity.CommodityActivity;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.Department;
import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.ItemBatch;
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

public class AddEditReceiptFragment extends ACBaseFragment<AddEditReceiptContract.Presenter> implements AddEditReceiptContract.View {

    private RelativeLayout relativeLayout;
    private LocalDate birthdate;
    private DateTime bdt;
    private ProgressBar progressBar;
    private TextInputLayout textInputLayoutReceipt;
    private EditText edoperationNumber;
    private EditText edoperationDate;
    private EditText edquantity;
    private EditText edwastage;
    private TextView operationdateerror;
    private Button datePicker;
    private Spinner mInstanceTypeSpinner;
    private TextView instanceTypeerror;
    private Spinner mDestinationSpinner;
    private TextView destinationerror;
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

    private boolean isUpdateReceipt = false;
    private Receipt updatedReceipt;
    private int PERMISSION_ID = 44;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_receipt_info, container, false);
        setHasOptionsMenu(true);
        resolveViews(root);
        addListeners();
        fillFields(mPresenter.getReceiptToUpdate());
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    @Override
    public void finishReceiptInfoActivity() {
        getActivity().finish();
    }

    @Override
    public void setErrorsVisibility(boolean receiptError) {
        // Only two dedicated text views will be visible for error messages.
        // Rest error messages will be displayed in dedicated TextInputLayouts.

    }

    @Override
    public void scrollToTop() {
        ScrollView scrollView = this.getActivity().findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, scrollView.getPaddingTop());
    }

    private void updateReceiptWithData(Receipt receipt) {
        String receipt_date = null;
        if (!ViewUtils.isEmpty(edoperationDate)) {
            String unvalidatedDate = edoperationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            bdt = dateTimeFormatter.parseDateTime(unvalidatedDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT);
            receipt_date = dateTimeFormatter.print(bdt);
            receipt.setOperationDate(receipt_date);
        }
//        Department department = new Select()
//                .from(Department.class)
//                .where("name = ?", mDepartmentSpinner.getSelectedItem().toString())
//                .executeSingle();
//        Item item = new Select()
//                .from(Item.class)
//                .where("name = ?", mItemSpinner.getSelectedItem().toString())
//                .executeSingle();
//        receipt.setDepartment(department.getUuid());
//        receipt.setItem(item.getUuid());
//        receipt.setBatchNumber(mItemBatchSpinner.getSelectedItem().toString());
//        receipt.setTestPurpose(mTestPurposeSpinner.getSelectedItem().toString());
//        receipt.setDataSystem(mDataSystemSpinner.getSelectedItem().toString());
//        receipt.setQuantity(ViewUtils.isEmpty(edquantity) ? 0 : Integer.parseInt(edquantity.getText().toString()));
//        receipt.setWastage(ViewUtils.isEmpty(edwastage) ? 0 : Integer.parseInt(edwastage.getText().toString()));

    }

    private Receipt updateReceipt(Receipt receipt) {
        updateReceiptWithData(receipt);
        return receipt;
    }


    private Receipt createReceipt() {
        Receipt receipt = new Receipt();
        updateReceiptWithData(receipt);
        return receipt;


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

    public static AddEditReceiptFragment newInstance() {
        return new AddEditReceiptFragment();
    }

    private void resolveViews(View v) {
        relativeLayout = v.findViewById(R.id.addEditRelativeLayout);
        edoperationDate = v.findViewById(R.id.operation_date);
        edoperationNumber = v.findViewById(R.id.operationNumber);
        operationdateerror= v.findViewById(R.id.operationdateerror);
        destinationerror= v.findViewById(R.id.destinationerror);
        instanceTypeerror= v.findViewById(R.id.instanceTypeerror);
        statuserror= v.findViewById(R.id.statuserror);
        commoditySourceerror= v.findViewById(R.id.commoditysourceerror);
        datasystemerror= v.findViewById(R.id.datasystemerror);
        commodityTypeerror= v.findViewById(R.id.commoditytypeerror);
        datePicker = v.findViewById(R.id.btn_datepicker);
//        textInputLayoutReceipt = v.findViewById(R.id.textInputLayoutReceipt);
        progressBar = v.findViewById(R.id.progress_bar);
        mDestinationSpinner = v.findViewById(R.id.destination);
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

    private void fillFields(final Receipt receipt) {

        if (receipt != null) {
            isUpdateReceipt = true;
            updatedReceipt = receipt;
            //Change to Update Receipt Form
            try {
//                edwastage.setText(receipt.getWastage());
//                edquantity.setText(receipt.getQuantity());
                if (StringUtils.notNull(receipt.getOperationDate()) || StringUtils.notEmpty(receipt.getOperationDate())) {
                    bdt = DateUtils.convertTimeString(receipt.getOperationDate());
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
//        List<Department> departments = new Select()
//                .distinct()
//                .from(Department.class)
//                .groupBy("name")
//                .execute();
//        List<String> department_strs = new ArrayList<String>();
//        for (Department row : departments) {
//            department_strs.add(row.getName());
//        }
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
        instance_type_strs.add("Receipt");
        instance_type_strs.add("Distribution");

        List<String> destination_strs = new ArrayList<String>();
        destination_strs.add("Main");
        destination_strs.add("Pharmacy");

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
        receiptAdapter(mInstanceTypeSpinner,instance_type_strs);
        receiptAdapter(mDestinationSpinner,destination_strs);
        receiptAdapter(mCommoditySourceSpinner,commodity_source_strs);
        receiptAdapter(mCommodityTypeSpinner,commodity_type_strs);
        receiptAdapter(mDataSystemSpinner,data_system_strs);
        receiptAdapter(mStatusSpinner,status_strs);





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


            DatePickerDialog mDatePicker = new DatePickerDialog(AddEditReceiptFragment.this.getActivity(), (datePicker, selectedYear, selectedMonth, selectedDay) -> {
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

    public void receiptAdapter(Spinner spinner, List<String> records ){
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
        if (isUpdateReceipt) {
            mPresenter.confirmUpdate(updateReceipt(updatedReceipt));
        } else {
            mPresenter.confirmRegister(createReceipt());
        }
    }



    @Override
    public void onResume(){
        super.onResume();
    }
}

