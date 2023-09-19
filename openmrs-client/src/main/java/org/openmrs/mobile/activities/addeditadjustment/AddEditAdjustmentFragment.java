package org.openmrs.mobile.activities.addeditadjustment;

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

import com.activeandroid.query.Select;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.commodity.CommodityActivity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.models.Department;
import org.openmrs.mobile.models.Destination;
import org.openmrs.mobile.models.Adjustment;
import org.openmrs.mobile.models.AdjustmentItem;
import org.openmrs.mobile.models.AdjustmentItem;
import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.Pharmacy;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AddEditAdjustmentFragment extends ACBaseFragment<AddEditAdjustmentContract.Presenter> implements AddEditAdjustmentContract.View {

    private RelativeLayout relativeLayout;
    private LocalDate birthdate;
    private DateTime bdt;
    private DateTime rEdt;
    private ProgressBar progressBar;
    private TextInputLayout textInputLayoutAdjustment;
    private EditText edoperationNumber;
    private EditText edoperationDate;
    private EditText edquantity;
    private EditText edwastage;
    private TextView operationdateerror;
    private Button datePicker;
    private Button adjustmentExpirationDatePicker;
    private Spinner adjustmentItemSpinner;
    private Spinner adjustmentItemPharmacySpinner;
    private Spinner adjustmentDrugTypeSpinner;
    private EditText adjustmentQuantity;
    private EditText adjustmentItemBatch;
    private EditText adjustmentExpirationDate;
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

    private boolean isUpdateAdjustment = false;
    private Adjustment updatedAdjustment;
    private int PERMISSION_ID = 44;

    AdjustmentItem adjustmentItemClass;

    private Adjustment admAdjustment;
    private AdjustmentItem admAdjustmentItem;
    private Button addMoreBtn;
    private Button deleteCommodity;

    //Declare a List array where consumption objects will be saved for multiple encounters
    private List<Adjustment> adjustmentMultiple;
    private boolean allCompleted = true;
    List<String> instance_type_strs = new ArrayList<String>();
    List<String> destination_strs = new ArrayList<String>();
    List<String> items = new ArrayList<String>();
    List<String> itemsPharmacy = new ArrayList<String>();
    List<String> commodity_source_strs = new ArrayList<String>();
    List<String> commodity_type_strs = new ArrayList<String>();
    List<String> drug_Type_strs = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_adjustment_info, container, false);
        setHasOptionsMenu(true);
        resolveViews(root);
        addListeners();
        fillFields(mPresenter.getAdjustmentToUpdate());
        adjustmentMultiple = new ArrayList<Adjustment>();
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    @Override
    public void finishAdjustmentInfoActivity() {
        getActivity().finish();
    }

    @Override
    public void setErrorsVisibility(boolean adjustmentError) {
        // Only two dedicated text views will be visible for error messages.
        // Rest error messages will be displayed in dedicated TextInputLayouts.

    }

    @Override
    public void scrollToTop() {
        ScrollView scrollView = this.getActivity().findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, scrollView.getPaddingTop());
    }

    private void updateAdjustmentWithData(Adjustment adjustment) {
        validate(adjustment);
    }

    public void validateSubmition() {
        if (mCommodityTypeSpinner.getSelectedItem().toString().equals("Pharmacy")) {
            if (adjustmentItemPharmacySpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Pharmacy");
                allCompleted = false;
            }

            if (adjustmentDrugTypeSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Drug Type");
                allCompleted = false;
            }

        } else if (mCommodityTypeSpinner.getSelectedItem().toString().equals("Lab")) {
            if (adjustmentItemSpinner.getSelectedItemPosition() == 0) {
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

        if (adjustmentItemBatch.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter Item Batch");
            allCompleted = false;
        }

        if (adjustmentQuantity.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Quantity");
            allCompleted = false;
        }

        if (adjustmentExpirationDate.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Expiration Date");
            allCompleted = false;
        }

    }

    public void validate(Adjustment adjustment) {
        String adjustment_date = null;
        boolean allCompleted = true;
        String commodityType = mCommodityTypeSpinner.getSelectedItem().toString();

        String instanceTypeAdjustment = "288fd7fe-1374-4f7a-89e6-d5f1ac97d4a5";

        adjustmentItemClass = new AdjustmentItem();

        List<AdjustmentItem> adjustmentItemClassArray = new ArrayList<AdjustmentItem>();

        if (!ViewUtils.isEmpty(edoperationDate)) {
            String unvalidatedDate = edoperationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            bdt = dateTimeFormatter.parseDateTime(unvalidatedDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_COMMODITY_FORMAT);
            adjustment_date = dateTimeFormatter.print(bdt);
            adjustment.setOperationDate(adjustment_date);
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Adjustment Operation Date");
            allCompleted = false;
        }

        if (commodityType.equals("Lab")) {
            if (adjustmentItemSpinner.getSelectedItemPosition() > 0) {
                Item itemSelect = new Select()
                        .from(Item.class)
                        .where("name = ?", adjustmentItemSpinner.getSelectedItem().toString())
                        .executeSingle();
                adjustmentItemClass.setItem(itemSelect.getUuid());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Lab");
                allCompleted = false;
            }
        } else if (commodityType.equals("Pharmacy")) {
            if (adjustmentItemPharmacySpinner.getSelectedItemPosition() > 0) {
                Pharmacy pharmacySelect = new Select()
                        .from(Pharmacy.class)
                        .where("name = ?", adjustmentItemPharmacySpinner.getSelectedItem().toString())
                        .executeSingle();
                adjustmentItemClass.setItem(pharmacySelect.getUuid());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Pharmacy");
                allCompleted = false;
            }

            if (adjustmentDrugTypeSpinner.getSelectedItemPosition() > 0) {
                adjustmentItemClass.setItemDrugType(adjustmentDrugTypeSpinner.getSelectedItem().toString());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Drug Type");
                allCompleted = false;
            }

        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select commodity type");
            allCompleted = false;
        }

        if (mDestinationSpinner.getSelectedItemPosition() > 0) {
            Destination destination = new Select()
                    .from(Destination.class)
                    .where("name = ?", mDestinationSpinner.getSelectedItem().toString())
                    .executeSingle();
            adjustment.setDestination(destination.getUuid());
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select Source/Destination");
            allCompleted = false;
        }

        adjustment.setDepartment("");

        adjustment.setInstitution("");

        adjustment.setPatient("");

        adjustment.setAdjustmentKind("positive");

        adjustment.setDisposedType("");

        adjustment.setInstanceType(instanceTypeAdjustment);

        adjustment.setStatus(mStatusSpinner.getSelectedItem().toString());

        adjustment.setOperationNumber("WILL BE GENERATED");

        adjustment.setCommoditySource(mCommoditySourceSpinner.getSelectedItem().toString());

        adjustment.setCommodityType(mCommodityTypeSpinner.getSelectedItem().toString());

        adjustment.setDataSystem(mDataSystemSpinner.getSelectedItem().toString());

        adjustment.setAttributes(new ArrayList<String>());


        if (adjustmentItemBatch.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Adjustment Item Batch");
            allCompleted = false;
        } else {
            adjustmentItemClass.setItemBatch(adjustmentItemBatch.getText().toString());
        }

        if (adjustmentQuantity.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Adjustment Quantity");
            allCompleted = false;
        } else {
            adjustmentItemClass.setQuantity(Integer.valueOf(adjustmentQuantity.getText().toString()));
        }

        if (adjustmentExpirationDate.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Expiration Date");
            allCompleted = false;
        } else {
            String rExpDate = adjustmentExpirationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            rEdt = dateTimeFormatter.parseDateTime(rExpDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_COMMODITY_FORMAT);
            String exp_date = dateTimeFormatter.print(rEdt);
            adjustmentItemClass.setExpiration(exp_date);
        }


        adjustmentItemClass.setCalculatedExpiration(true);

        adjustmentItemClassArray.add(adjustmentItemClass);

        adjustment.setItems(adjustmentItemClassArray);

        if (allCompleted) {
//If all vaidations are correct then save consumption
            adjustmentMultiple.add(adjustment);
            //clear the form fields after saving
            mCommodityTypeSpinner.setSelection(0);
            adjustmentDrugTypeSpinner.setSelection(0);
            adjustmentItemPharmacySpinner.setSelection(0);
            adjustmentItemSpinner.setSelection(0);
            adjustmentQuantity.getText().clear();
            adjustmentItemBatch.getText().clear();
            adjustmentExpirationDate.getText().clear();
        }

    }

    private Adjustment updateAdjustment(Adjustment adjustment) {
        updateAdjustmentWithData(adjustment);
        return adjustment;
    }


    private List<Adjustment> createAdjustment() {
        Adjustment adjustment = new Adjustment();
        updateAdjustmentWithData(adjustment);
        return adjustmentMultiple;
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
        startActivity(intent);
    }

    @Override
    public boolean areFieldsNotEmpty() {
        return (!ViewUtils.isEmpty(edoperationDate));
    }

    public static AddEditAdjustmentFragment newInstance() {
        return new AddEditAdjustmentFragment();
    }

    private void resolveViews(View v) {
        relativeLayout = v.findViewById(R.id.addEditRelativeLayout);
        edoperationDate = v.findViewById(R.id.operation_date);
        //edoperationNumber = v.findViewById(R.id.operationNumber);
        operationdateerror = v.findViewById(R.id.operationdateerror);
        destinationerror = v.findViewById(R.id.statuserror);
        instanceTypeerror = v.findViewById(R.id.instanceTypeerror);
        statuserror = v.findViewById(R.id.statuserror);
        commoditySourceerror = v.findViewById(R.id.commoditysourceerror);
        datasystemerror = v.findViewById(R.id.datasystemerror);
        commodityTypeerror = v.findViewById(R.id.commoditytypeerror);
        datePicker = v.findViewById(R.id.btn_datepicker);
        adjustmentExpirationDatePicker = v.findViewById(R.id.btn_ExpirationDatedatepicker);
        adjustmentExpirationDate = v.findViewById(R.id.adjustmentExpirationDate);
        adjustmentItemSpinner = v.findViewById(R.id.adjustmentItem);
        adjustmentItemPharmacySpinner = v.findViewById(R.id.adjustmentItemPharmacy);
        adjustmentDrugTypeSpinner = v.findViewById(R.id.adjustmentDrugType);
        adjustmentQuantity = v.findViewById(R.id.adjustmentQuantity);
        adjustmentItemBatch = v.findViewById(R.id.adjustmentItemBatch);
        progressBar = v.findViewById(R.id.progress_bar);
        mDestinationSpinner = v.findViewById(R.id.destination);
//        mItemSpinner = v.findViewById(R.id.item);
        mInstanceTypeSpinner = v.findViewById(R.id.instanceType);
        mStatusSpinner = v.findViewById(R.id.status);
        mCommoditySourceSpinner = v.findViewById(R.id.commoditysource);
        //edoperationNumber = v.findViewById(R.id.operationNumber);
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

    private void fillFields(final Adjustment adjustment) {

        if (adjustment != null) {
            addMoreBtn.setVisibility(View.GONE);
            deleteCommodity.setVisibility(View.VISIBLE);
            isUpdateAdjustment = true;
            updatedAdjustment = adjustment;
            //Change to Update Adjustment Form
            List<AdjustmentItem> adjustmentItemList = adjustment.getItems();
            //Change to Update Adjustment Form

            edoperationDate.setText(adjustment.getOperationDate().replace("-", "/"));

            adjustmentQuantity.setText(String.valueOf(adjustmentItemList.get(0).getQuantity()));

            adjustmentExpirationDate.setText(adjustmentItemList.get(0).getExpiration().replace("-", "/"));

            adjustmentItemBatch.setText(adjustmentItemList.get(0).getItemBatch());


            //get the index from the test purpose string array
            int spinner_commodity_type_Str_Position = commodity_type_strs.indexOf(adjustment.getCommodityType());
            mCommodityTypeSpinner.setSelection(spinner_commodity_type_Str_Position);


            if(adjustment.getCommodityType().equals("Lab")){
                Item item = new Select()
                        .from(Item.class)
                        .where("uuid = ?", adjustmentItemList.get(0).getItem())
                        .executeSingle();

                //get the index from the test purpose string array
                int spinner_item_strs_Position = items.indexOf(item.getName());
                adjustmentItemSpinner.setSelection(spinner_item_strs_Position);
            }else if(adjustment.getCommodityType().equals("Pharmacy")){
                Pharmacy pharmacy = new Select()
                        .from(Pharmacy.class)
                        .where("uuid = ?", adjustmentItemList.get(0).getItem())
                        .executeSingle();

                //get the index from the test purpose string array
                int spinner_item_pharmacy_strs_Position = itemsPharmacy.indexOf(pharmacy.getName());
                adjustmentItemPharmacySpinner.setSelection(spinner_item_pharmacy_strs_Position);
                OpenMRSCustomHandler.showJson(adjustmentItemPharmacySpinner);

                int spinner_item_drug_type_strs_Position = drug_Type_strs.indexOf(adjustmentItemList.get(0).getItemDrugType());
                adjustmentDrugTypeSpinner.setSelection(spinner_item_drug_type_strs_Position);
                OpenMRSCustomHandler.showJson(adjustmentItemPharmacySpinner);

            }else{

            }

            Destination destination = new Select()
                    .from(Destination.class)
                    .where("uuid = ?", adjustment.getDestination())
                    .executeSingle();
            int spinner_destination_strs_Position = destination_strs.indexOf(destination.getName());
            mDestinationSpinner.setSelection(spinner_destination_strs_Position);

        }
    }

    private void addListeners() {

        instance_type_strs.add("Adjustment");


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


        commodity_source_strs.add("PEPFAR");
        commodity_source_strs.add("GF");
        commodity_source_strs.add("GoN");


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

        adjustmentAdapter(mInstanceTypeSpinner, instance_type_strs);
        adjustmentAdapter(mDestinationSpinner, destination_strs);
        adjustmentAdapter(mCommoditySourceSpinner, commodity_source_strs);
        adjustmentAdapter(mCommodityTypeSpinner, commodity_type_strs);
        adjustmentAdapter(mDataSystemSpinner, data_system_strs);
        adjustmentAdapter(mStatusSpinner, status_strs);
        adjustmentAdapter(adjustmentItemSpinner, items);
        adjustmentAdapter(adjustmentItemPharmacySpinner, itemsPharmacy);
        adjustmentAdapter(adjustmentDrugTypeSpinner, drug_Type_strs);


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
        adjustmentExpirationDatePicker.setBackgroundColor(Color.GRAY);

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


            DatePickerDialog mDatePicker = new DatePickerDialog(AddEditAdjustmentFragment.this.getActivity(), (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                int adjustedMonth = selectedMonth + 1;
                edoperationDate.setText(selectedDay + "/" + adjustedMonth + "/" + selectedYear);
                birthdate = new LocalDate(selectedYear, adjustedMonth, selectedDay);
                bdt = birthdate.toDateTimeAtStartOfDay().toDateTime();
            }, cYear, cMonth, cDay);
            mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            mDatePicker.setTitle(getString(R.string.date_picker_title));
            mDatePicker.show();

        });

        adjustmentExpirationDatePicker.setOnClickListener(v -> {
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


            DatePickerDialog mExpirationDatePicker = new DatePickerDialog(AddEditAdjustmentFragment.this.getActivity(), (adjustmentExpirationDatePicker, selectedYear, selectedMonth, selectedDay) -> {
                int adjustedMonth = selectedMonth + 1;
                adjustmentExpirationDate.setText(selectedDay + "/" + adjustedMonth + "/" + selectedYear);
                birthdate = new LocalDate(selectedYear, adjustedMonth, selectedDay);
                rEdt = birthdate.toDateTimeAtStartOfDay().toDateTime();
            }, cYear, cMonth, cDay);
            mExpirationDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            mExpirationDatePicker.setTitle(getString(R.string.date_picker_title_expiry));
            mExpirationDatePicker.show();

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

    public void adjustmentAdapter(Spinner spinner, List<String> records) {
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
        if (allCompleted) {
            if (isUpdateAdjustment) {
                mPresenter.confirmUpdate(updateAdjustment(updatedAdjustment), adjustmentItemClass);
            } else {
                mPresenter.confirmRegister(createAdjustment(), adjustmentItemClass);
                startCommodityDashboardActivity();
                finishAdjustmentInfoActivity();
            }
        }
        allCompleted = true;
    }

    private void addMore() {
        admAdjustment = new Adjustment();
        updateAdjustmentWithData(admAdjustment);
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

