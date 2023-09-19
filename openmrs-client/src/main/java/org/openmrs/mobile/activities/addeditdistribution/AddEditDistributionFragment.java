package org.openmrs.mobile.activities.addeditdistribution;

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
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.field.OffsetDateTimeField;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionContract;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionFragment;
import org.openmrs.mobile.activities.commodity.CommodityActivity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.models.Department;
import org.openmrs.mobile.models.Destination;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.models.InventoryStockSummaryLab;
import org.openmrs.mobile.models.InventoryStockSummaryPharmacy;
import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.Pharmacy;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

//import org.openmrs.mobile.activities.addeditpatient.AddEditPatientFragmentPermissionsDispatcher;

public class AddEditDistributionFragment extends ACBaseFragment<AddEditDistributionContract.Presenter> implements AddEditDistributionContract.View {

    private RelativeLayout relativeLayout;
    private LocalDate birthdate;
    private DateTime bdt;
    private DateTime rEdt;
    private ProgressBar progressBar;
    private TextInputLayout textInputLayoutDistribution;
    private EditText edoperationNumber;
    private EditText edoperationDate;
    private EditText edquantity;
    private EditText edwastage;
    private TextView operationdateerror;
    private Button datePicker;
    private Spinner distributionItemLabSpinner;
    private Spinner distributionItemPharmacySpinner;
    private Spinner distributionDrugTypeSpinner;
    private EditText distributionQuantity;
    private Spinner distributionItemBatchLabSpinner;
    private Spinner distributionItemBatchPharmacySpinner;
    private Spinner distributionExpirationDateSpinner;
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

    private boolean isUpdateDistribution = false;
    private Distribution updatedDistribution;
    private int PERMISSION_ID = 44;

    DistributionItem distributionItemClass;
    private Distribution admDistribution;
    private DistributionItem admDistibutionItem;
    private Button addMoreBtn;
    private Button deleteCommodity;

    //Declare a List array where consumption objects will be saved for multiple encounters
    private List<Distribution> distributionMultiple;
    private boolean allCompleted = true;
    List<String> commodity_type_strs = new ArrayList<String>();
    List<String> itemsLab = new ArrayList<String>();
    List<String> itemsBatchLab = new ArrayList<String>();
    List<String> itemsBatchPharmacy = new ArrayList<String>();
    List<String> itemsPharmacy = new ArrayList<String>();
    List<String> expirationDate_strs = new ArrayList<>();
    List<String> drug_Type_strs = new ArrayList<String>();
    List<String> department_strs = new ArrayList<String>();
    List<String> source_strs = new ArrayList<String>();
    String selectedItemLab = "";
    String selectedItemPharmacy = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_distribution_info, container, false);
        setHasOptionsMenu(true);
        resolveViews(root);
        addListeners();
        fillFields(mPresenter.getDistributionToUpdate());

        distributionMultiple = new ArrayList<Distribution>();

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

    public void validateSubmition() {
        if (mCommodityTypeSpinner.getSelectedItem().toString().equals("Pharmacy")) {
            if (distributionItemPharmacySpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Pharmacy");
                allCompleted = false;
            }

            if (distributionDrugTypeSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Drug Type");
                allCompleted = false;
            }

            if (distributionItemBatchPharmacySpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Batch Pharmacy");
                allCompleted = false;
            }

        } else if (mCommodityTypeSpinner.getSelectedItem().toString().equals("Lab")) {
            if (distributionItemLabSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Lab");
                allCompleted = false;
            }

            if (distributionItemBatchLabSpinner.getSelectedItemPosition() == 0) {
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


        if (distributionQuantity.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Quantity");
            allCompleted = false;
        }

        if (distributionExpirationDateSpinner.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Expiration Date");
            allCompleted = false;
        }

    }

    private void updateDistributionWithData(Distribution distribution) {
        validate(distribution);
    }

    public void validate(Distribution distribution) {
        String distribution_date = null;
        boolean allCompleted = true;
        String commodityType = mCommodityTypeSpinner.getSelectedItem().toString();

        String instanceTypeDistribution = "c264f34b-c795-4576-9928-454d1fa20e09";

        distributionItemClass = new DistributionItem();

        List<DistributionItem> distributionItemClassArray = new ArrayList<DistributionItem>();

        if (!ViewUtils.isEmpty(edoperationDate)) {
            String unvalidatedDate = edoperationDate.getText().toString().trim();
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
            bdt = dateTimeFormatter.parseDateTime(unvalidatedDate);
            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_COMMODITY_FORMAT);
            distribution_date = dateTimeFormatter.print(bdt);
            distribution.setOperationDate(distribution_date);
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Distribution Operation Date");
            allCompleted = false;
        }

        if (mSourceSpinner.getSelectedItemPosition() > 0) {
            Destination destination = new Select()
                    .from(Destination.class)
                    .where("name = ?", mSourceSpinner.getSelectedItem().toString())
                    .executeSingle();

            distribution.setSource(destination.getUuid());
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select Source/Destination");
            allCompleted = false;
        }

        if (mDepartmentSpinner.getSelectedItemPosition() > 0) {
            Department department = new Select()
                    .from(Department.class)
                    .where("name = ?", mDepartmentSpinner.getSelectedItem().toString())
                    .executeSingle();
            distribution.setDepartment(department.getUuid());
        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select Department");
            allCompleted = false;
        }

        distribution.setInstanceType(instanceTypeDistribution);

        distribution.setStatus(mStatusSpinner.getSelectedItem().toString());

        distribution.setOperationNumber("WILL BE GENERATED");

        distribution.setCommoditySource(mCommoditySourceSpinner.getSelectedItem().toString());

        distribution.setCommodityType(mCommodityTypeSpinner.getSelectedItem().toString());

        distribution.setDataSystem(mDataSystemSpinner.getSelectedItem().toString());

        if (commodityType.equals("Lab")) {
            if (distributionItemLabSpinner.getSelectedItemPosition() > 0) {
                Item itemSelect = new Select()
                        .from(Item.class)
                        .where("name = ?", distributionItemLabSpinner.getSelectedItem().toString())
                        .executeSingle();
                distributionItemClass.setItem(itemSelect.getUuid());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Lab");
                allCompleted = false;
            }

            if (distributionItemBatchLabSpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Distribution Item Batch Lab");
                allCompleted = false;
            } else {
                distributionItemClass.setItemBatch(distributionItemBatchLabSpinner.getSelectedItem().toString());
            }

        } else if (commodityType.equals("Pharmacy")) {
            distribution.setDestination("");
            distribution.setAdjustmentKind("");
            distribution.setPatient("");
            distribution.setDisposedType("");
            distribution.setInstitution("");
            distribution.setAttributes(null);

            if (distributionItemPharmacySpinner.getSelectedItemPosition() > 0) {
                Pharmacy pharmacySelect = new Select()
                        .from(Pharmacy.class)
                        .where("name = ?", distributionItemPharmacySpinner.getSelectedItem().toString())
                        .executeSingle();
                distributionItemClass.setItem(pharmacySelect.getUuid());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Item Pharmacy");
                allCompleted = false;
            }

            if (distributionDrugTypeSpinner.getSelectedItemPosition() > 0) {
                distributionItemClass.setItemDrugType(distributionDrugTypeSpinner.getSelectedItem().toString());
            } else {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Select Drug Type");
                allCompleted = false;
            }


            if (distributionItemBatchPharmacySpinner.getSelectedItemPosition() == 0) {
                OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Distribution Item Batch Pharmacy");
                allCompleted = false;
            } else {
                distributionItemClass.setItemBatch(distributionItemBatchPharmacySpinner.getSelectedItem().toString());
            }

        } else {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select commodity type");
            allCompleted = false;
        }

        if (distributionQuantity.getText().toString().isEmpty()) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Enter the Distribution Quantity");
            allCompleted = false;
        } else {
            distributionItemClass.setQuantity(Integer.valueOf(distributionQuantity.getText().toString()));
        }

        if (distributionExpirationDateSpinner.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Select the Expiration Date");
            allCompleted = false;
        } else {
//            String rExpDate = distributionExpirationDate.getText().toString().trim();
//            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);
//            rEdt = dateTimeFormatter.parseDateTime(rExpDate);
//            dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_COMMODITY_FORMAT);
//            String exp_date = dateTimeFormatter.print(rEdt);
//            distributionItemClass.setExpiration(exp_date);
        }


        distributionItemClass.setCalculatedExpiration(true);

        distributionItemClassArray.add(distributionItemClass);

        distribution.setItems(distributionItemClassArray);

        if (allCompleted) {
//If all vaidations are correct then save consumption
            distributionMultiple.add(distribution);
            //clear the form fields after saving
            mCommodityTypeSpinner.setSelection(0);
            distributionDrugTypeSpinner.setSelection(0);
            distributionItemPharmacySpinner.setSelection(0);
            distributionItemLabSpinner.setSelection(0);
            distributionQuantity.getText().clear();
            distributionItemBatchLabSpinner.setSelection(0);
            distributionItemBatchPharmacySpinner.setSelection(0);
            distributionExpirationDateSpinner.setSelection(0);
        }
    }

    private Distribution updateDistribution(Distribution distribution) {
        updateDistributionWithData(distribution);
        return distribution;
    }


    private List<Distribution> createDistribution() {
        Distribution distribution = new Distribution();
        updateDistributionWithData(distribution);
        return distributionMultiple;
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
        //edoperationNumber = v.findViewById(R.id.operationNumber);
        operationdateerror = v.findViewById(R.id.operationdateerror);
        departmenterror = v.findViewById(R.id.departmenterror);
        sourceerror = v.findViewById(R.id.statuserror);
        instanceTypeerror = v.findViewById(R.id.instanceTypeerror);
        statuserror = v.findViewById(R.id.statuserror);
        commoditySourceerror = v.findViewById(R.id.commoditysourceerror);
        datasystemerror = v.findViewById(R.id.datasystemerror);
        commodityTypeerror = v.findViewById(R.id.commoditytypeerror);
        datePicker = v.findViewById(R.id.btn_datepicker);
        distributionExpirationDateSpinner = v.findViewById(R.id.distributionExpirationDate);
        distributionItemLabSpinner = v.findViewById(R.id.distributionItem);
        distributionItemPharmacySpinner = v.findViewById(R.id.distributionItemPharmacy);
        distributionDrugTypeSpinner = v.findViewById(R.id.distributionDrugType);
        distributionQuantity = v.findViewById(R.id.distributionQuantity);
        distributionItemBatchLabSpinner = v.findViewById(R.id.distributionItemBatchLab);
        distributionItemBatchPharmacySpinner = v.findViewById(R.id.distributionItemBatchPharmacy);
        progressBar = v.findViewById(R.id.progress_bar);
        mDepartmentSpinner = v.findViewById(R.id.department);
        mSourceSpinner = v.findViewById(R.id.source);
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

    private void fillFields(final Distribution distribution) {
        if (distribution != null) {
            addMoreBtn.setVisibility(View.GONE);
            deleteCommodity.setVisibility(View.VISIBLE);
            isUpdateDistribution = true;
            updatedDistribution = distribution;
            List<DistributionItem> distributionItemList = distribution.getItems();
            //Change to Update Distribution Form

            edoperationDate.setText(distribution.getOperationDate().replace("-", "/"));

            distributionQuantity.setText(String.valueOf(distributionItemList.get(0).getQuantity()));

            //distributionExpirationDate.setText(distributionItemList.get(0).getExpiration().replace("-", "/"));


            //get the index from the test purpose string array
            int spinner_commodity_type_Str_Position = commodity_type_strs.indexOf(distribution.getCommodityType());
            mCommodityTypeSpinner.setSelection(spinner_commodity_type_Str_Position);


            if (distribution.getCommodityType().equals("Lab")) {
                //Item for Distribution
                Item item = new Select()
                        .from(Item.class)
                        .where("uuid = ?", distributionItemList.get(0).getItem())
                        .executeSingle();

                //get the index from the test purpose string array
                int spinner_item_strs_Position = itemsLab.indexOf(item.getName());
                distributionItemLabSpinner.setSelection(spinner_item_strs_Position);

                List<InventoryStockSummaryLab> itemBatchListLab = new Select().distinct().from(InventoryStockSummaryLab.class).where("name = ?", item.getName()).execute();
                for (InventoryStockSummaryLab inLabRow : itemBatchListLab) {
                    itemsBatchLab.add(inLabRow.getItemBatch());
                }
                //get the index from the test purpose string array
                int spinner_item_batch = itemsBatchLab.indexOf(itemBatchListLab.get(0).getItemBatch());
                distributionItemBatchLabSpinner.setSelection(spinner_item_batch);

                List<InventoryStockSummaryLab> itemExpiry = new Select().distinct().from(InventoryStockSummaryLab.class).where("itemBatch = ?", itemBatchListLab.get(0).getItemBatch()).execute();
                for (InventoryStockSummaryLab inLabRow : itemExpiry) {
                    expirationDate_strs.add(convertDateTime(inLabRow.getExpiration()));
                }
                int spinner_expiry_date = expirationDate_strs.indexOf(convertDateTime(itemBatchListLab.get(0).getExpiration()));
                distributionExpirationDateSpinner.setSelection(spinner_expiry_date);
            } else if (distribution.getCommodityType().equals("Pharmacy")) {
                Pharmacy pharmacy = new Select()
                        .from(Pharmacy.class)
                        .where("uuid = ?", distributionItemList.get(0).getItem())
                        .executeSingle();

                //get the index from the test purpose string array
                int spinner_item_pharmacy_strs_Position = itemsPharmacy.indexOf(pharmacy.getName());
                distributionItemPharmacySpinner.setSelection(spinner_item_pharmacy_strs_Position);

                int spinner_item_drug_type_strs_Position = drug_Type_strs.indexOf(distributionItemList.get(0).getItemDrugType());
                distributionDrugTypeSpinner.setSelection(spinner_item_drug_type_strs_Position);


                List<InventoryStockSummaryPharmacy> itemBatchListPharmacy = new Select().distinct().from(InventoryStockSummaryPharmacy.class).where("name = ?", pharmacy.getName()).execute();
                for (InventoryStockSummaryPharmacy inPharmRow : itemBatchListPharmacy) {
                    itemsBatchPharmacy.add(inPharmRow.getItemBatch());
                }
                //get the index from the test purpose string array
                int spinner_item_batch = itemsBatchPharmacy.indexOf(itemBatchListPharmacy.get(0).getItemBatch());
                distributionItemBatchPharmacySpinner.setSelection(spinner_item_batch);


                List<InventoryStockSummaryPharmacy> itemExpiry = new Select().distinct().from(InventoryStockSummaryPharmacy.class).where("itemBatch = ?", itemBatchListPharmacy.get(0).getItemBatch()).execute();
                for (InventoryStockSummaryPharmacy inPharmRow : itemExpiry) {
                    expirationDate_strs.add(convertDateTime(inPharmRow.getExpiration()));
                }
                int spinner_expiry_date = expirationDate_strs.indexOf(convertDateTime(itemBatchListPharmacy.get(0).getExpiration()));
                distributionExpirationDateSpinner.setSelection(spinner_expiry_date);

                OpenMRSCustomHandler.showJson(expirationDate_strs);

            } else {

            }
            //get the index from the test purpose string array
            Destination destination = new Select()
                    .from(Destination.class)
                    .where("uuid = ?", distribution.getSource())
                    .executeSingle();
            int spinner_source_str_Position = source_strs.indexOf(destination.getName());
            mSourceSpinner.setSelection(spinner_source_str_Position);


            Department department = new Select()
                    .from(Department.class)
                    .where("uuid = ?", distribution.getDepartment())
                    .executeSingle();
            int spinner_department_str_Position = department_strs.indexOf(department.getName());
            mDepartmentSpinner.setSelection(spinner_department_str_Position);

        }
    }

    private void addListeners() {
        List<Department> departments = new Select()
                .distinct()
                .from(Department.class)
                .groupBy("name")
                .execute();

        department_strs.add("--Select Department--");
        for (Department row : departments) {
            department_strs.add(row.getName());
        }

        List<String> instance_type_strs = new ArrayList<String>();
        instance_type_strs.add("Distribution");


        List<Destination> destinations = new Select()
                .distinct()
                .from(Destination.class)
                .groupBy("name")
                .execute();
        source_strs.add("--Select Source--");
        for (Destination row : destinations) {
            source_strs.add(row.getName());
        }


        List<InventoryStockSummaryLab> itemList = new Select().distinct().from(InventoryStockSummaryLab.class).execute();
        itemsLab.add("--Select Item Lab--");
        for (InventoryStockSummaryLab itemRow : itemList) {
            itemsLab.add(itemRow.getName());
        }

        List<InventoryStockSummaryPharmacy> itemListPharmacy = new Select().distinct().from(InventoryStockSummaryPharmacy.class).execute();
        itemsPharmacy.add("--Select Item Pharmacy--");
        for (InventoryStockSummaryPharmacy itemRow : itemListPharmacy) {
            itemsPharmacy.add(itemRow.getName());
        }

        itemsBatchLab.clear();
        itemsBatchLab.add("--Select Item Batch Lab--");
        distributionItemBatchLabSpinner.setSelection(0);

        expirationDate_strs.clear();
        expirationDate_strs.add("--Select Expiration Date--");
        distributionExpirationDateSpinner.setSelection(0);

        itemsBatchPharmacy.clear();
        itemsBatchPharmacy.add("--Select Item Batch Pharmacy--");
        distributionItemBatchPharmacySpinner.setSelection(0);


        List<String> commodity_source_strs = new ArrayList<String>();
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

        distributionAdapter(mInstanceTypeSpinner, instance_type_strs);
        distributionAdapter(mSourceSpinner, source_strs);
        distributionAdapter(mCommoditySourceSpinner, commodity_source_strs);
        distributionAdapter(mCommodityTypeSpinner, commodity_type_strs);
        distributionAdapter(mDataSystemSpinner, data_system_strs);
        distributionAdapter(mStatusSpinner, status_strs);
        distributionAdapter(mDepartmentSpinner, department_strs);
        distributionAdapter(distributionItemLabSpinner, itemsLab);
        distributionAdapter(distributionItemPharmacySpinner, itemsPharmacy);
        distributionAdapter(distributionDrugTypeSpinner, drug_Type_strs);
        distributionAdapter(distributionItemBatchLabSpinner, itemsBatchLab);
        distributionAdapter(distributionItemBatchPharmacySpinner, itemsBatchPharmacy);
        distributionAdapter(distributionExpirationDateSpinner, expirationDate_strs);


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

                        distributionItemBatchLabSpinner.setVisibility(View.VISIBLE);
                        distributionItemBatchPharmacySpinner.setVisibility(View.GONE);
                        break;
                    case 2: //Pharmacy
                        linearLayoutItemLab.setVisibility(View.GONE);
                        linearLayoutItemPharmacy.setVisibility(View.VISIBLE);
                        linearLayoutDrugType.setVisibility(View.VISIBLE);

                        distributionItemBatchLabSpinner.setVisibility(View.GONE);
                        distributionItemBatchPharmacySpinner.setVisibility(View.VISIBLE);
                        break;
                    default: //Hide all here
                        linearLayoutItemLab.setVisibility(View.GONE);
                        linearLayoutItemPharmacy.setVisibility(View.GONE);
                        linearLayoutDrugType.setVisibility(View.GONE);

                        distributionItemBatchLabSpinner.setVisibility(View.GONE);
                        distributionItemBatchPharmacySpinner.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        distributionItemLabSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View selectedItemView, int position, long id) {
                //Set the item batch spinners and expiration to 0
                //distributionItemBatchLabSpinner.setSelection(0);
                //distributionExpirationDateSpinner.setSelection(0);
                selectedItemLab = itemsLab.get(position);
                populateItemBatchLab(selectedItemLab);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        distributionItemBatchLabSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View selectedItemView, int position, long id) {
                distributionExpirationDateSpinner.setSelection(0);
                populateItemExpirationDateLab(itemsBatchLab.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Put an event listener on the item pharmacy so that it can be tracked based on selection
        distributionItemPharmacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                //Set the item batch spinners and expiration to 0
                //distributionItemBatchPharmacySpinner.setSelection(0);
                //distributionExpirationDateSpinner.setSelection(0);
                selectedItemPharmacy = itemsPharmacy.get(position);
                populateItemBatchPharmacy(selectedItemPharmacy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        distributionItemBatchPharmacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View selectedItemView, int position, long id) {
                //Set the item batch spinners and expiration to 0
                //distributionExpirationDateSpinner.setSelection(0);
                populateItemExpirationDatePharmacy(itemsBatchPharmacy.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void distributionAdapter(Spinner spinner, List<String> records) {
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
            if (isUpdateDistribution) {
                mPresenter.confirmUpdate(updateDistribution(updatedDistribution), distributionItemClass);
            } else {
                mPresenter.confirmRegister(createDistribution(), distributionItemClass);
            }
        }
        allCompleted = true;
    }

    private void addMore() {
        admDistribution = new Distribution();
        updateDistributionWithData(admDistribution);
    }

    private void deleteCommodity() {
        mPresenter.deleteCommodity();
        //new Delete().from(ReceiptItem.class).where("receiptId = ?", lastid).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public String convertDateTime(String date) {
        String unvalidatedDate = date.replace("+0100", "");

        DateTimeFormatter format = DateTimeFormat.forPattern("dd-MM-yyyy");
        LocalDateTime localDateTime = LocalDateTime.parse(unvalidatedDate);
        String formatDateTime = localDateTime.toString(format);

        return formatDateTime;
    }

    public void populateItemBatchLab(String selectedItemLab){
        itemsBatchLab.clear();
        itemsBatchLab.add("--Select Item Batch Lab--");
        //distributionItemBatchLabSpinner.setSelection(0);
        List<InventoryStockSummaryLab> itemBatchListLab = new Select().distinct().from(InventoryStockSummaryLab.class).where("name = ?", selectedItemLab).execute();
        for (InventoryStockSummaryLab inLabRow : itemBatchListLab) {
            itemsBatchLab.add(inLabRow.getItemBatch());
        }
    }

    /**
     *This function pulls out the expiry date for item batch from Lab
     * @param selectedItem String
     */
    public void populateItemExpirationDateLab(String selectedItem){
       expirationDate_strs.clear();
       expirationDate_strs.add("--Select Expiration Date--");
       //distributionExpirationDateSpinner.setSelection(0);
        List<InventoryStockSummaryLab> itemExpiry = new Select().distinct().from(InventoryStockSummaryLab.class).where("itemBatch = ?", selectedItem).execute();
        for (InventoryStockSummaryLab inLabRow : itemExpiry) {
            expirationDate_strs.add(convertDateTime(inLabRow.getExpiration()));
        }
    }

    public void populateItemBatchPharmacy(String selectedItemPharmacy){
        itemsBatchPharmacy.clear();
        itemsBatchPharmacy.add("--Select Item Batch Pharmacy--");
        //distributionItemBatchLabSpinner.setSelection(0);
        List<InventoryStockSummaryPharmacy> itemBatchListPharmacy = new Select().distinct().from(InventoryStockSummaryPharmacy.class).where("name = ?", selectedItemPharmacy).execute();
        for (InventoryStockSummaryPharmacy inPharmRow : itemBatchListPharmacy) {
            itemsBatchPharmacy.add(inPharmRow.getItemBatch());
        }
    }

    public void populateItemExpirationDatePharmacy(String selectedItem){
        expirationDate_strs.clear();
        expirationDate_strs.add("--Select Expiration Date--");
        //distributionExpirationDateSpinner.setSelection(0);
        List<InventoryStockSummaryPharmacy> itemExpiry = new Select().distinct().from(InventoryStockSummaryPharmacy.class).where("itemBatch = ?", selectedItem).execute();
        for (InventoryStockSummaryPharmacy inPharmRow : itemExpiry) {
            expirationDate_strs.add(convertDateTime(inPharmRow.getExpiration()));
        }
    }

}

