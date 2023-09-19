package org.openmrs.mobile.activities.addeditreceipt;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.activeandroid.query.Delete;
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
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Destination;
import org.openmrs.mobile.models.Pharmacy;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.Department;
import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.ItemBatch;
import org.openmrs.mobile.models.ReceiptItem;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;

//import org.openmrs.mobile.activities.addeditpatient.AddEditPatientFragmentPermissionsDispatcher;

public class AddEditReceiptFragment extends ACBaseFragment<AddEditReceiptContract.Presenter> implements AddEditReceiptContract.View {

    private RelativeLayout relativeLayout;
    private LocalDate birthdate;
    private DateTime bdt;
    private DateTime rEdt;
    private ProgressBar progressBar;
    private TextInputLayout textInputLayoutReceipt;
    private EditText edoperationNumber;
    private EditText receiptExpirationDate;
    private EditText edoperationDate;
    private EditText edquantity;
    private EditText edwastage;
    private TextView operationdateerror;
    private Button datePicker;
    private Button receiptExpirationDatePicker;
    private Spinner receiptItemSpinner;
    private Spinner receiptItemPharmacySpinner;
    private Spinner receiptDrugTypeSpinner;
    private EditText receiptQuantity;
    private EditText receiptItemBatch;
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
    private LinearLayout linearLayoutItemLab;
    private LinearLayout linearLayoutItemPharmacy;
    private LinearLayout linearLayoutDrugType;

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
    //initialize the receipt items
    ReceiptItem receiptItemClass;
    private Receipt admReceipt;
    private ReceiptItem admReceiptItem;
    private Button addMoreBtn;
    private Button deleteCommodity;

    //Declare a List array where consumption objects will be saved for multiple encounters
    private List<Receipt> receiptMultiple;
    private List<ReceiptItem> receiptItemMultiple;
    private boolean allCompleted = true;
    List<String> commodity_type_strs = new ArrayList<String>();
    List<String> destination_strs = new ArrayList<String>();
    List<String> items = new ArrayList<String>();
    List<String> itemsPharmacy = new ArrayList<String>();
    List<String> drug_Type_strs = new ArrayList<String>();

    int operationDay = 0;
    int operationMonth = 0;
    int operationYear = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_receipt_info, container, false);
        setHasOptionsMenu(true);
        resolveViews(root);
        addListeners();
        fillFields(mPresenter.getReceiptToUpdate());

        receiptMultiple = new ArrayList<Receipt>();
        receiptItemMultiple = new ArrayList<ReceiptItem>();

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
        validate(receipt);
    }

    public void validateSubmition() {
        if (mCommodityTypeSpinner.getSelectedItem().toString().equals("Pharmacy")) {
            if (receiptItemPharmacySpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Pharmacy");
                allCompleted = false;
            }

            if (receiptDrugTypeSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Drug Type");
                allCompleted = false;
            }

        } else if (mCommodityTypeSpinner.getSelectedItem().toString().equals("Lab")) {
            if (receiptItemSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Lab");
                allCompleted = false;
            }
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select the Commodity Type");
            allCompleted = false;
        }

        if (ViewUtils.isEmpty(edoperationDate)) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please enter the Operation Date");
            allCompleted = false;
        }

        if (mDestinationSpinner.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select the Destination");
            allCompleted = false;
        }

        if (receiptItemBatch.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter Item Batch");
            allCompleted = false;
        }

        if (receiptQuantity.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Quantity");
            allCompleted = false;
        }

        if (receiptExpirationDate.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Expiration Date");
            allCompleted = false;
        }

    }

    public void validate(Receipt receipt) {
        String receipt_date = null;
        String commodityType = mCommodityTypeSpinner.getSelectedItem().toString();

        String instanceTypeReceipt = "fce0b4fc-9402-424a-aacb-f99599e51a9f";

        //initialize the receipt items
        receiptItemClass = new ReceiptItem();

        List<ReceiptItem> receiptItemClassArray = new ArrayList<ReceiptItem>();

        if (!ViewUtils.isEmpty(edoperationDate)) {
            String unvalidatedDate = edoperationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            bdt = dateTimeFormatter.parseDateTime(unvalidatedDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_COMMODITY_FORMAT);
            receipt_date = dateTimeFormatter.print(bdt);
            receipt.setOperationDate(receipt_date);
        }

        if (mDestinationSpinner.getSelectedItemPosition() > 0) {
            Destination destination = new Select()
                    .from(Destination.class)
                    .where("name = ?", mDestinationSpinner.getSelectedItem().toString())
                    .executeSingle();

            receipt.setDestination(destination.getUuid());
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select the Destination");
            allCompleted = false;
        }

        receipt.setInstanceType(instanceTypeReceipt);

        receipt.setStatus(mStatusSpinner.getSelectedItem().toString());

        receipt.setOperationNumber("WILL BE GENERATED");

        receipt.setCommoditySource(mCommoditySourceSpinner.getSelectedItem().toString());

        receipt.setCommodityType(commodityType);

        receipt.setDataSystem(mDataSystemSpinner.getSelectedItem().toString());

        if (commodityType.equals("Lab")) {
            if (receiptItemSpinner.getSelectedItemPosition() > 0) {
                Item itemSelect = new Select()
                        .from(Item.class)
                        .where("name = ?", receiptItemSpinner.getSelectedItem().toString())
                        .executeSingle();
                receiptItemClass.setItem(itemSelect.getUuid());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Lab");
                allCompleted = false;
            }
        } else if (commodityType.equals("Pharmacy")) {
            receipt.setDepartment("");
            receipt.setAdjustmentKind("");
            receipt.setPatient("");
            receipt.setDisposedType("");
            receipt.setInstitution("");
            receipt.setAttributes(null);

            if (receiptItemPharmacySpinner.getSelectedItemPosition() > 0) {
                Pharmacy pharmacySelect = new Select()
                        .from(Pharmacy.class)
                        .where("name = ?", receiptItemPharmacySpinner.getSelectedItem().toString())
                        .executeSingle();
                receiptItemClass.setItem(pharmacySelect.getUuid());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Pharmacy");
                allCompleted = false;
            }

            if (receiptDrugTypeSpinner.getSelectedItemPosition() > 0) {
                receiptItemClass.setItemDrugType(receiptDrugTypeSpinner.getSelectedItem().toString());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Drug Type");
                allCompleted = false;
            }

        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select commodity type");
            allCompleted = false;
        }

        if (receiptItemBatch.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Receipt Item Batch");
            allCompleted = false;
        } else {
            receiptItemClass.setItemBatch(receiptItemBatch.getText().toString());
        }

        if (receiptQuantity.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Receipt Quantity");
            allCompleted = false;
        } else {
            receiptItemClass.setQuantity(Integer.valueOf(receiptQuantity.getText().toString()));
        }

        if (receiptExpirationDate.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Expiration Date");
            allCompleted = false;
        } else {
            String rExpDate = receiptExpirationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            rEdt = dateTimeFormatter.parseDateTime(rExpDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_COMMODITY_FORMAT);
            String exp_date = dateTimeFormatter.print(rEdt);
            receiptItemClass.setExpiration(exp_date);


            /**
             *  String unvalidatedDate = edoperationDate.getText().toString().trim();
             *             dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
             *             bdt = dateTimeFormatter.parseDateTime(unvalidatedDate);
             *             dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT);
             *             receipt_date = dateTimeFormatter.print(bdt);
             *             receipt.setOperationDate(receipt_date);
             */
        }


        receiptItemClass.setCalculatedExpiration(true);
        //Add the items into the Array List
        receiptItemClassArray.add(receiptItemClass);

        receipt.setItems(receiptItemClassArray);

        if (allCompleted) {
            //If all vaidations are correct then save consumption
            receiptMultiple.add(receipt);
            //receiptItemMultiple.add(receiptItemClassArray);
            //clear the form fields after saving
            mDestinationSpinner.setSelection(0);
            mCommodityTypeSpinner.setSelection(0);
            receiptDrugTypeSpinner.setSelection(0);
            receiptItemPharmacySpinner.setSelection(0);
            receiptItemSpinner.setSelection(0);
            receiptQuantity.getText().clear();
            receiptItemBatch.getText().clear();
            receiptExpirationDate.getText().clear();
        }
    }

    private Receipt updateReceipt(Receipt receipt) {
        updateReceiptWithData(receipt);
        return receipt;
    }


    private List<Receipt> createReceipt() {
        Receipt receipt = new Receipt();
        updateReceiptWithData(receipt);
        return receiptMultiple;
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
        //edoperationNumber = v.findViewById(R.id.operationNumber);
        operationdateerror = v.findViewById(R.id.operationdateerror);
        destinationerror = v.findViewById(R.id.destinationerror);
        instanceTypeerror = v.findViewById(R.id.instanceTypeerror);
        statuserror = v.findViewById(R.id.statuserror);
        commoditySourceerror = v.findViewById(R.id.commoditysourceerror);
        datasystemerror = v.findViewById(R.id.datasystemerror);
        commodityTypeerror = v.findViewById(R.id.commoditytypeerror);
        datePicker = v.findViewById(R.id.btn_datepicker);
        receiptExpirationDatePicker = v.findViewById(R.id.btn_receiptExpirationDatedatepicker);
        receiptExpirationDate = v.findViewById(R.id.receiptExpirationDate);
        receiptItemSpinner = v.findViewById(R.id.receiptItem);
        receiptItemPharmacySpinner = v.findViewById(R.id.receiptItemPharmacy);
        receiptDrugTypeSpinner = v.findViewById(R.id.receiptDrugType);
        receiptQuantity = v.findViewById(R.id.receiptQuantity);
        receiptItemBatch = v.findViewById(R.id.receiptItemBatch);
//        textInputLayoutReceipt = v.findViewById(R.id.textInputLayoutReceipt);
        progressBar = v.findViewById(R.id.progress_bar);
        mDestinationSpinner = v.findViewById(R.id.destination);
//        mItemSpinner = v.findViewById(R.id.item);
        mInstanceTypeSpinner = v.findViewById(R.id.instanceType);
        mStatusSpinner = v.findViewById(R.id.status);
        mCommoditySourceSpinner = v.findViewById(R.id.commoditysource);
        mCommodityTypeSpinner = v.findViewById(R.id.commodity_type);
        mDataSystemSpinner = v.findViewById(R.id.data_system);
        addMoreBtn = v.findViewById(R.id.addMoreButton);
        deleteCommodity = v.findViewById(R.id.deleteCommodity);
        linearLayoutItemLab = v.findViewById(R.id.linearLayoutItemLab);
        linearLayoutItemPharmacy = v.findViewById(R.id.linearLayoutItemPharmacy);
        linearLayoutDrugType = v.findViewById(R.id.linearLayoutDrugType);
        edoperationDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        edoperationDate.setPadding(15, 0, 0, 15);
        FontsUtil.setFont(edoperationDate, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
    }

    private void fillFields(final Receipt receipt) {
        OpenMRSCustomHandler.showJson(receipt);
        if (receipt != null) {
            addMoreBtn.setVisibility(View.GONE);
            deleteCommodity.setVisibility(View.VISIBLE);
            isUpdateReceipt = true;
            updatedReceipt = receipt;
            List<ReceiptItem> receiptItemList = receipt.getItems();
            //Change to Update Receipt Form
            //bdt = DateUtils.convertTimeString(receipt.getOperationDate());
            edoperationDate.setText(DateUtils.convertTime(DateUtils.convertTime(receipt.getOperationDate(), DateUtils.OPEN_MRS_COMMODITY_FORMAT),
                    DateUtils.DEFAULT_DATE_FORMAT));
            receiptQuantity.setText(String.valueOf(receiptItemList.get(0).getQuantity()));

            receiptExpirationDate.setText(receiptItemList.get(0).getExpiration().replace("-", "/"));

            receiptItemBatch.setText(receiptItemList.get(0).getItemBatch());


            //get the index from the test purpose string array
            int spinner_commodity_type_Str_Position = commodity_type_strs.indexOf(receipt.getCommodityType());
            mCommodityTypeSpinner.setSelection(spinner_commodity_type_Str_Position);

            //get the index from the test purpose string array
            Destination destination = new Select()
                    .from(Destination.class)
                    .where("uuid = ?", receipt.getDestination())
                    .executeSingle();
            int spinner_destination_strs_Position = destination_strs.indexOf(destination.getName());
            mDestinationSpinner.setSelection(spinner_destination_strs_Position);



            if(receipt.getCommodityType().equals("Lab")){
                Item item = new Select()
                        .from(Item.class)
                        .where("uuid = ?", receiptItemList.get(0).getItem())
                        .executeSingle();

                //get the index from the test purpose string array
                int spinner_item_strs_Position = items.indexOf(item.getName());
                receiptItemSpinner.setSelection(spinner_item_strs_Position);
            }else if(receipt.getCommodityType().equals("Pharmacy")){
                Pharmacy pharmacy = new Select()
                        .from(Pharmacy.class)
                        .where("uuid = ?", receiptItemList.get(0).getItem())
                        .executeSingle();

                //get the index from the test purpose string array
                int spinner_item_pharmacy_strs_Position = itemsPharmacy.indexOf(pharmacy.getName());
                receiptItemPharmacySpinner.setSelection(spinner_item_pharmacy_strs_Position);
                OpenMRSCustomHandler.showJson(receiptItemPharmacySpinner);

                int spinner_item_drug_type_strs_Position = drug_Type_strs.indexOf(receiptItemList.get(0).getItemDrugType());
                receiptDrugTypeSpinner.setSelection(spinner_item_drug_type_strs_Position);
                OpenMRSCustomHandler.showJson(receiptItemPharmacySpinner);

            }else{

            }

            try {
//                edwastage.setText(receipt.getWastage());
//                edquantity.setText(receipt.getQuantity());
                if (StringUtils.notNull(receipt.getOperationDate()) || StringUtils.notEmpty(receipt.getOperationDate())) {
                    bdt = DateUtils.convertTimeString(receipt.getOperationDate());
                    edoperationDate.setText(DateUtils.convertTime(DateUtils.convertTime(bdt.toString(), DateUtils.OPEN_MRS_REQUEST_FORMAT),
                            DateUtils.DEFAULT_DATE_FORMAT));
                }
//                mDepartmentSpinner.setSelection( 2 );

            } catch (Exception e) {
                ToastUtil.error(e.toString());
            }

        }
    }

    private void addListeners() {
        List<String> instance_type_strs = new ArrayList<String>();
        instance_type_strs.add("Receipt");

        List<Destination> destinations = new Select()
                .distinct()
                .from(Destination.class)
                .groupBy("name")
                .execute();

        destination_strs.add("--Select Destination--");
        for (Destination row : destinations) {
            destination_strs.add(row.getName());
        }

        List<Item> itemList = new Select()
                .distinct()
                .from(Item.class)
                .groupBy("name")
                .execute();

        items.add("--Select Item Lab--");
        for (Item itemRow : itemList) {
            items.add(itemRow.getName());
        }

        List<Pharmacy> itemListPharmacy = new Select()
                .distinct()
                .from(Pharmacy.class)
                .groupBy("name")
                .execute();

        itemsPharmacy.add("--Select Item Pharmacy--");
        for (Pharmacy itemRow : itemListPharmacy) {
            itemsPharmacy.add(itemRow.getName());
        }

        List<String> commodity_source_strs = new ArrayList<String>();
        commodity_source_strs.add("PEPFAR");
        commodity_source_strs.add("GF");
        commodity_source_strs.add("GoN");
        commodity_source_strs.add("Other Donors");


        commodity_type_strs.add("--Select Commodity Type--");
        commodity_type_strs.add("Lab");
        commodity_type_strs.add("Pharmacy");


        drug_Type_strs.add("--Select Drug Type--");
        drug_Type_strs.add("Adult ART");
        drug_Type_strs.add("Paediatric ART");
        drug_Type_strs.add("OI Prophylaxis/Treatment");
        drug_Type_strs.add("Advanced HIV Disease Drugs");
        drug_Type_strs.add("Anti-TB Drugs");
        drug_Type_strs.add("STI");

        List<String> status_strs = new ArrayList<String>();
        status_strs.add("New");

        List<String> data_system_strs = new ArrayList<String>();
        data_system_strs.add("Mobile");
        //data_system_strs.add("Laptop");
        receiptAdapter(mInstanceTypeSpinner, instance_type_strs);
        receiptAdapter(mDestinationSpinner, destination_strs);
        receiptAdapter(mCommoditySourceSpinner, commodity_source_strs);
        receiptAdapter(mCommodityTypeSpinner, commodity_type_strs);
        receiptAdapter(mDataSystemSpinner, data_system_strs);
        receiptAdapter(mStatusSpinner, status_strs);
        receiptAdapter(receiptItemSpinner, items);
        receiptAdapter(receiptItemPharmacySpinner, itemsPharmacy);
        receiptAdapter(receiptDrugTypeSpinner, drug_Type_strs);


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
        receiptExpirationDatePicker.setBackgroundColor(Color.GRAY);

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
                //Add plus one to the selected operationdate because the expiry date needs to be a day before the operation date
                operationDay = selectedDay + 1;
                operationMonth = adjustedMonth;
                operationYear = selectedYear;
                edoperationDate.setText(selectedDay + "/" + adjustedMonth + "/" + selectedYear);
                birthdate = new LocalDate(selectedYear, adjustedMonth, selectedDay);
                bdt = birthdate.toDateTimeAtStartOfDay().toDateTime();
            }, cYear, cMonth, cDay);
            //mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            mDatePicker.setTitle(getString(R.string.date_picker_title));
            mDatePicker.show();

        });

        receiptExpirationDatePicker.setOnClickListener(v -> {
            int cYear;
            int cMonth;
            int cDay;

            if (rEdt == null) {
                Calendar currentDate = Calendar.getInstance();
                cYear = currentDate.get(Calendar.YEAR);
                cMonth = currentDate.get(Calendar.MONTH);
                cDay = currentDate.get(Calendar.DAY_OF_MONTH);
            } else {
                cYear = rEdt.getYear();
                cMonth = rEdt.getMonthOfYear() - 1;
                cDay = rEdt.getDayOfMonth();
            }


            //mExpirationDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            if(operationDay != 0) {
                DatePickerDialog mExpirationDatePicker = new DatePickerDialog(AddEditReceiptFragment.this.getActivity(), (receiptExpirationDatePicker, selectedYear, selectedMonth, selectedDay) -> {
                    int adjustedMonth = selectedMonth + 1;
                    receiptExpirationDate.setText(selectedDay + "/" + adjustedMonth + "/" + selectedYear);
                    birthdate = new LocalDate(selectedYear, adjustedMonth, selectedDay);
                    rEdt = birthdate.toDateTimeAtStartOfDay().toDateTime();
                }, cYear, cMonth, cDay);

                mExpirationDatePicker.getDatePicker().setMinDate(new DateTime(operationYear, operationMonth, operationDay, 0, 0).getMillis());
                mExpirationDatePicker.setTitle(getString(R.string.date_picker_title_expiry));
                mExpirationDatePicker.show();
            }else{
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select the Operation Date before the Expiry date");
            }

        });

        addMoreBtn.setOnClickListener(v -> {
            addMore();
        });

        deleteCommodity.setOnClickListener(v -> {
            deleteCommodity();
        });

        mCommodityTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //check for the selected position
                switch (position) {
                    case 1: //Lab
                        linearLayoutItemLab.setVisibility(View.VISIBLE);
                        linearLayoutItemPharmacy.setVisibility(View.GONE);
                        linearLayoutDrugType.setVisibility(View.GONE);
                        break;
                    case 2: //Pharmacy
                        linearLayoutItemLab.setVisibility(View.GONE);
                        linearLayoutItemPharmacy.setVisibility(View.VISIBLE);
                        linearLayoutDrugType.setVisibility(View.VISIBLE);
                        break;
                    default: //Hide all here
                        linearLayoutItemLab.setVisibility(View.GONE);
                        linearLayoutItemPharmacy.setVisibility(View.GONE);
                        linearLayoutDrugType.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    public void receiptAdapter(Spinner spinner, List<String> records) {
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
        //Validate the form fields to address those clicking the submit forms directly without going through the add more
        validateSubmition();
        if(allCompleted) {
            if (isUpdateReceipt) {
                mPresenter.confirmUpdate(updateReceipt(updatedReceipt), receiptItemClass);
            } else {
                mPresenter.confirmRegister(createReceipt(), receiptItemClass);
            }
        }
        allCompleted = true;
    }

    private void addMore() {
        admReceipt = new Receipt();
        updateReceiptWithData(admReceipt);
    }

    private void deleteCommodity(){
        mPresenter.deleteCommodity();
        //new Delete().from(ReceiptItem.class).where("receiptId = ?", lastid).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

