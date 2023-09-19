package org.openmrs.mobile.activities.addedittransfer;

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
import android.widget.AutoCompleteTextView;
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
import org.openmrs.mobile.models.Institution;
import org.openmrs.mobile.models.InventoryStockSummaryLab;
import org.openmrs.mobile.models.InventoryStockSummaryPharmacy;
import org.openmrs.mobile.models.TransferItem;
import org.openmrs.mobile.models.Facility;
import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.Pharmacy;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.models.TransferItem;
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

public class AddEditTransferFragment extends ACBaseFragment<AddEditTransferContract.Presenter> implements AddEditTransferContract.View {

    private RelativeLayout relativeLayout;
    private LocalDate birthdate;
    private DateTime bdt;
    private DateTime rEdt;
    private ProgressBar progressBar;
    private TextInputLayout textInputLayoutTransfer;
    private EditText edoperationNumber;
    private EditText transferExpirationDate;
    private EditText edoperationDate;
    private EditText edquantity;
    private EditText edwastage;
    private TextView operationdateerror;
    private Button datePicker;
    private Button transferExpirationDatePicker;
    private Spinner transferItemSpinner;
    private Spinner transferItemPharmacySpinner;
    private Spinner transferDrugTypeSpinner;
    private EditText transferQuantity;
    private Spinner transferItemBatchLabSpinner;
    private Spinner transferItemBatchPharmacySpinner;
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
    private Spinner mSourceSpinner;
    private AutoCompleteTextView mInstitutionAutoComplete;
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

    private boolean isUpdateTransfer = false;
    private Transfer updatedTransfer;
    private int PERMISSION_ID = 44;
    //initialize the transfer items
    TransferItem transferItemClass;

    private Transfer admTransfer;
    private TransferItem admTransferItem;
    private Button addMoreBtn;
    private Button deleteCommodity;

    //Declare a List array where consumption objects will be saved for multiple encounters
    private List<Transfer> transferMultiple;
    private boolean allCompleted = true;
    List<String> source_strs = new ArrayList<String>();
    List<String> instance_type_strs = new ArrayList<String>();
    List<String> itemsPharmacy = new ArrayList<String>();
    List<String> commodity_source_strs = new ArrayList<String>();
    List<String> commodity_type_strs = new ArrayList<String>();
    List<String> drug_Type_strs = new ArrayList<String>();
    List<String> items = new ArrayList<String>();
    List<String> itemsBatchLab = new ArrayList<String>();
    List<String> itemsBatchPharmacy = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transfer_info, container, false);
        setHasOptionsMenu(true);
        resolveViews(root);
        addListeners();
        fillFields(mPresenter.getTransferToUpdate());

        transferMultiple = new ArrayList<Transfer>();

        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    @Override
    public void finishTransferInfoActivity() {
        getActivity().finish();
    }


    @Override
    public void setErrorsVisibility(boolean transferError) {
        // Only two dedicated text views will be visible for error messages.
        // Rest error messages will be displayed in dedicated TextInputLayouts.

    }

    @Override
    public void scrollToTop() {
        ScrollView scrollView = this.getActivity().findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, scrollView.getPaddingTop());
    }

    private void updateTransferWithData(Transfer transfer) {
        validate(transfer);
    }

    public void validateSubmition() {
        if (mCommodityTypeSpinner.getSelectedItem().toString().equals("Pharmacy")) {
            if (transferItemPharmacySpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Pharmacy");
                allCompleted = false;
            }

            if (transferDrugTypeSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Drug Type");
                allCompleted = false;
            }

            if (transferItemBatchPharmacySpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Batch Pharmacy");
                allCompleted = false;
            }

        } else if (mCommodityTypeSpinner.getSelectedItem().toString().equals("Lab")) {
            if (transferItemSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Lab");
                allCompleted = false;
            }

            if (transferItemBatchLabSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Batch Lab");
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

        if (mSourceSpinner.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select the Destination");
            allCompleted = false;
        }

        if (transferQuantity.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Quantity");
            allCompleted = false;
        }

        if (transferExpirationDate.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Expiration Date");
            allCompleted = false;
        }

    }

    public void validate(Transfer transfer) {
        String transfer_date = null;
        boolean allCompleted = true;
        String commodityType = mCommodityTypeSpinner.getSelectedItem().toString();

        String instanceTypeTransfer = "db40707f-9175-4199-8df2-a5702f41ec7d";

        //initialize the transfer items
        transferItemClass = new TransferItem();

        List<TransferItem> transferItemClassArray = new ArrayList<TransferItem>();

        if (!ViewUtils.isEmpty(edoperationDate)) {
            String unvalidatedDate = edoperationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            bdt = dateTimeFormatter.parseDateTime(unvalidatedDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT);
            transfer_date = dateTimeFormatter.print(bdt);
            transfer.setOperationDate(transfer_date);
        }

        //Set the destination to empty
        transfer.setDestination("");

        transfer.setInstanceType(instanceTypeTransfer);

        transfer.setStatus(mStatusSpinner.getSelectedItem().toString());

        transfer.setOperationNumber("WILL BE GENERATED");

        transfer.setPatient("");

        transfer.setAdjustmentKind("");

        List<String> attributes = new ArrayList<>();
        transfer.setAttributes(attributes);

        if (commodityType.equals("Lab")) {
            if (transferItemSpinner.getSelectedItemPosition() > 0) {
                Item itemSelect = new Select()
                        .from(Item.class)
                        .where("name = ?", transferItemSpinner.getSelectedItem().toString())
                        .executeSingle();
                transferItemClass.setItem(itemSelect.getUuid());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Lab");
                allCompleted = false;
            }

            if (transferItemBatchLabSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select the Transfer Item Batch for Lab");
                allCompleted = false;
            } else {
                transferItemClass.setItemBatch(transferItemBatchLabSpinner.getSelectedItem().toString());
            }
        } else if (commodityType.equals("Pharmacy")) {
            if (transferItemPharmacySpinner.getSelectedItemPosition() > 0) {
                Pharmacy pharmacySelect = new Select()
                        .from(Pharmacy.class)
                        .where("name = ?", transferItemPharmacySpinner.getSelectedItem().toString())
                        .executeSingle();
                transferItemClass.setItem(pharmacySelect.getUuid());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Pharmacy");
                allCompleted = false;
            }

            if (transferDrugTypeSpinner.getSelectedItemPosition() > 0) {
                transferItemClass.setItemDrugType(transferDrugTypeSpinner.getSelectedItem().toString());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Drug Type");
                allCompleted = false;
            }

            if (transferItemBatchPharmacySpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select the Transfer Item Batch for Pharmacy");
                allCompleted = false;
            } else {
                transferItemClass.setItemBatch(transferItemBatchPharmacySpinner.getSelectedItem().toString());
            }

        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select commodity type");
            allCompleted = false;
        }

        if (mSourceSpinner.getSelectedItemPosition() > 0) {
            Destination destination = new Select()
                    .from(Destination.class)
                    .where("name = ?", mSourceSpinner.getSelectedItem().toString())
                    .executeSingle();
            transfer.setSource(destination.getUuid());
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select Source/Destination");
            allCompleted = false;
        }

        if (!mInstitutionAutoComplete.getText().toString().isEmpty()) {
            Facility facility = new Select()
                    .from(Facility.class)
                    .where("facilityName = ?", mInstitutionAutoComplete.getText().toString())
                    .executeSingle();
            transfer.setInstitution(facility.getFacilityCode());
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select Institution");
            allCompleted = false;
        }

        transfer.setDepartment("");

        transfer.setDisposedType("");

        transfer.setCommoditySource(mCommoditySourceSpinner.getSelectedItem().toString());

        transfer.setCommodityType(mCommodityTypeSpinner.getSelectedItem().toString());

        transfer.setDataSystem(mDataSystemSpinner.getSelectedItem().toString());


        if (transferQuantity.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Transfer Quantity");
            allCompleted = false;
        } else {
            transferItemClass.setQuantity(Integer.valueOf(transferQuantity.getText().toString()));
        }

        if (transferExpirationDate.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Expiration Date");
            allCompleted = false;
        } else {
            String rExpDate = transferExpirationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            rEdt = dateTimeFormatter.parseDateTime(rExpDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_COMMODITY_FORMAT);
            String exp_date = dateTimeFormatter.print(rEdt);
            transferItemClass.setExpiration(exp_date);
        }


        transferItemClass.setCalculatedExpiration(true);

        transferItemClassArray.add(transferItemClass);

        transfer.setItems(transferItemClassArray);

        if (allCompleted) {
//If all vaidations are correct then save consumption
            transferMultiple.add(transfer);
            //clear the form fields after saving
            mInstitutionAutoComplete.getText().clear();
            mCommodityTypeSpinner.setSelection(0);
            transferDrugTypeSpinner.setSelection(0);
            transferItemPharmacySpinner.setSelection(0);
            transferItemSpinner.setSelection(0);
            transferQuantity.getText().clear();
            transferItemBatchPharmacySpinner.setSelection(0);
            transferItemBatchLabSpinner.setSelection(0);
            transferExpirationDate.getText().clear();
        }
    }

    private Transfer updateTransfer(Transfer transfer) {
        updateTransferWithData(transfer);
        return transfer;
    }


    private List<Transfer> createTransfer() {
        Transfer transfer = new Transfer();
        updateTransferWithData(transfer);
        return transferMultiple;
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

    public static AddEditTransferFragment newInstance() {
        return new AddEditTransferFragment();
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
        transferExpirationDatePicker = v.findViewById(R.id.btn_ExpirationDatedatepicker);
        transferExpirationDate = v.findViewById(R.id.transferExpirationDate);
        transferItemSpinner = v.findViewById(R.id.transferItem);
        transferItemPharmacySpinner = v.findViewById(R.id.transferItemPharmacy);
        transferDrugTypeSpinner = v.findViewById(R.id.transferDrugType);
        transferQuantity = v.findViewById(R.id.transferQuantity);
        transferItemBatchLabSpinner = v.findViewById(R.id.transferItemBatchLab);
        transferItemBatchPharmacySpinner = v.findViewById(R.id.transferItemBatchPharmacy);
//        textInputLayoutTransfer = v.findViewById(R.id.textInputLayoutTransfer);
        progressBar = v.findViewById(R.id.progress_bar);
//        mItemSpinner = v.findViewById(R.id.item);
        mInstanceTypeSpinner = v.findViewById(R.id.instanceType);
        mStatusSpinner = v.findViewById(R.id.status);
        mCommoditySourceSpinner = v.findViewById(R.id.commoditysource);
        mCommodityTypeSpinner = v.findViewById(R.id.commodity_type);
        mDataSystemSpinner = v.findViewById(R.id.data_system);
        mSourceSpinner = v.findViewById(R.id.source);
        mInstitutionAutoComplete = v.findViewById(R.id.institution);
        addMoreBtn = v.findViewById(R.id.addMoreButton);
        deleteCommodity = v.findViewById(R.id.deleteCommodity);
        linearLayoutItemLab = v.findViewById(R.id.linearLayoutItemLab);
        linearLayoutItemPharmacy = v.findViewById(R.id.linearLayoutItemPharmacy);
        linearLayoutDrugType = v.findViewById(R.id.linearLayoutDrugType);
        edoperationDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        edoperationDate.setPadding(15, 0, 0, 15);
        FontsUtil.setFont(edoperationDate, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
    }

    private void fillFields(final Transfer transfer) {

        if (transfer != null) {
            addMoreBtn.setVisibility(View.GONE);
            deleteCommodity.setVisibility(View.VISIBLE);
            isUpdateTransfer = true;
            updatedTransfer = transfer;
            //Change to Update Transfer Form
            List<TransferItem> transferItemList = transfer.getItems();
            //Change to Update Transfer Form

            edoperationDate.setText(DateUtils.convertTime(DateUtils.convertTime(transfer.getOperationDate(), DateUtils.OPEN_MRS_REQUEST_FORMAT),
                    DateUtils.DEFAULT_DATE_FORMAT));

            transferQuantity.setText(String.valueOf(transferItemList.get(0).getQuantity()));

            transferExpirationDate.setText(transferItemList.get(0).getExpiration().replace("-", "/"));


            Facility facility = new Select()
                    .from(Facility.class)
                    .where("facilityCode = ?", transfer.getInstitution())
                    .executeSingle();
            mInstitutionAutoComplete.setText(facility.getFacilityName());

            //get the index from the test purpose string array
            int spinner_commodity_type_Str_Position = commodity_type_strs.indexOf(transfer.getCommodityType());
            mCommodityTypeSpinner.setSelection(spinner_commodity_type_Str_Position);


            if (transfer.getCommodityType().equals("Lab")) {
                Item item = new Select()
                        .from(Item.class)
                        .where("uuid = ?", transferItemList.get(0).getItem())
                        .executeSingle();

                //get the index from the test purpose string array
                int spinner_item_strs_Position = items.indexOf(item.getName());
                transferItemSpinner.setSelection(spinner_item_strs_Position);

                //Item Batch Lab Selection
                InventoryStockSummaryLab inventoryStockSummaryLab = new Select()
                        .from(InventoryStockSummaryLab.class)
                        .where("itemBatch = ?", transferItemList.get(0).getItemBatch()).executeSingle();
                int spinner_itemBatch_strs_position = itemsBatchLab.indexOf(inventoryStockSummaryLab.getItemBatch());
                transferItemBatchLabSpinner.setSelection(spinner_itemBatch_strs_position);
            } else if (transfer.getCommodityType().equals("Pharmacy")) {
                Pharmacy pharmacy = new Select()
                        .from(Pharmacy.class)
                        .where("uuid = ?", transferItemList.get(0).getItem())
                        .executeSingle();

                //get the index from the test purpose string array
                int spinner_item_pharmacy_strs_Position = itemsPharmacy.indexOf(pharmacy.getName());
                transferItemPharmacySpinner.setSelection(spinner_item_pharmacy_strs_Position);
                OpenMRSCustomHandler.showJson(transferItemPharmacySpinner);

                int spinner_item_drug_type_strs_Position = drug_Type_strs.indexOf(transferItemList.get(0).getItemDrugType());
                transferDrugTypeSpinner.setSelection(spinner_item_drug_type_strs_Position);
                OpenMRSCustomHandler.showJson(transferItemPharmacySpinner);

                //Item Batch Pharmacy Selection
                InventoryStockSummaryPharmacy inventoryStockSummaryPharmacy = new Select()
                        .from(InventoryStockSummaryPharmacy.class)
                        .where("itemBatch = ?", transferItemList.get(0).getItemBatch()).executeSingle();
                int spinner_itemBatch_phar_strs_position = itemsBatchPharmacy.indexOf(inventoryStockSummaryPharmacy.getItemBatch());
                transferItemBatchPharmacySpinner.setSelection(spinner_itemBatch_phar_strs_position);
            } else {

            }
            //get the index from the test purpose string array
            Destination destination = new Select()
                    .from(Destination.class)
                    .where("uuid = ?", transfer.getSource())
                    .executeSingle();
            int spinner_source_str_Position = source_strs.indexOf(destination.getName());
            mSourceSpinner.setSelection(spinner_source_str_Position);

        }
    }


    private void addListeners() {
        List<String> institution_strs = new ArrayList<String>();
        List<Facility> institutions = new Select().from(Facility.class).execute();
        for (Facility facility : institutions) {
            institution_strs.add(facility.getFacilityName());
        }

        ArrayAdapter<String> institutionAutoComp = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, institution_strs);

        mInstitutionAutoComplete.setAdapter(institutionAutoComp);
        mInstitutionAutoComplete.setThreshold(1);


        List<Destination> destinations = new Select()
                .distinct()
                .from(Destination.class)
                .groupBy("name")
                .execute();
        source_strs.add("--Select Destination--");
        for (Destination row : destinations) {
            source_strs.add(row.getName());
        }


        instance_type_strs.add("Transfer");


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

        List<InventoryStockSummaryLab> itemBatchListLab = new Select().distinct().from(InventoryStockSummaryLab.class).execute();
        itemsBatchLab.add("--Select Item Batch Lab--");
        for(InventoryStockSummaryLab inLabRow : itemBatchListLab){
            itemsBatchLab.add(inLabRow.getItemBatch());
        }

        List<InventoryStockSummaryPharmacy> itemBatchListPharmacy = new Select().distinct().from(InventoryStockSummaryPharmacy.class).execute();
        itemsBatchPharmacy.add("--Select Item Batch Pharmacy--");
        for(InventoryStockSummaryPharmacy inPharmacyRow : itemBatchListPharmacy){
            itemsBatchPharmacy.add(inPharmacyRow.getItemBatch());
        }

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

        transferAdapter(mInstanceTypeSpinner, instance_type_strs);
        transferAdapter(mSourceSpinner, source_strs);
        transferAdapter(mCommoditySourceSpinner, commodity_source_strs);
        transferAdapter(mCommodityTypeSpinner, commodity_type_strs);
        transferAdapter(mDataSystemSpinner, data_system_strs);
        transferAdapter(mStatusSpinner, status_strs);
        transferAdapter(transferItemSpinner, items);
        transferAdapter(transferItemPharmacySpinner, itemsPharmacy);
        transferAdapter(transferDrugTypeSpinner, drug_Type_strs);
        transferAdapter(transferItemBatchLabSpinner, itemsBatchLab);
        transferAdapter(transferItemBatchPharmacySpinner, itemsBatchPharmacy);


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
        transferExpirationDatePicker.setBackgroundColor(Color.GRAY);

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


            DatePickerDialog mDatePicker = new DatePickerDialog(AddEditTransferFragment.this.getActivity(), (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                int adjustedMonth = selectedMonth + 1;
                edoperationDate.setText(selectedDay + "/" + adjustedMonth + "/" + selectedYear);
                birthdate = new LocalDate(selectedYear, adjustedMonth, selectedDay);
                bdt = birthdate.toDateTimeAtStartOfDay().toDateTime();
            }, cYear, cMonth, cDay);
            mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            mDatePicker.setTitle(getString(R.string.date_picker_title));
            mDatePicker.show();

        });

        transferExpirationDatePicker.setOnClickListener(v -> {
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


            DatePickerDialog mExpirationDatePicker = new DatePickerDialog(AddEditTransferFragment.this.getActivity(), (transferExpirationDatePicker, selectedYear, selectedMonth, selectedDay) -> {
                int adjustedMonth = selectedMonth + 1;
                transferExpirationDate.setText(selectedDay + "/" + adjustedMonth + "/" + selectedYear);
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

                        transferItemBatchLabSpinner.setVisibility(View.VISIBLE);
                        transferItemBatchPharmacySpinner.setVisibility(View.GONE);
                        break;
                    case 2: //Pharmacy
                        linearLayoutItemLab.setVisibility(View.GONE);
                        linearLayoutItemPharmacy.setVisibility(View.VISIBLE);
                        linearLayoutDrugType.setVisibility(View.VISIBLE);

                        transferItemBatchLabSpinner.setVisibility(View.GONE);
                        transferItemBatchPharmacySpinner.setVisibility(View.VISIBLE);
                        break;
                    default: //Hide all here
                        linearLayoutItemLab.setVisibility(View.GONE);
                        linearLayoutItemPharmacy.setVisibility(View.GONE);
                        linearLayoutDrugType.setVisibility(View.GONE);

                        transferItemBatchLabSpinner.setVisibility(View.GONE);
                        transferItemBatchPharmacySpinner.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

    }

    public void transferAdapter(Spinner spinner, List<String> records) {
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
            if (isUpdateTransfer) {
                mPresenter.confirmUpdate(updateTransfer(updatedTransfer), transferItemClass);
            } else {
                mPresenter.confirmRegister(createTransfer(), transferItemClass);
            }
        }
        allCompleted = true;
    }

    private void addMore() {
        admTransfer = new Transfer();
        updateTransferWithData(admTransfer);
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

