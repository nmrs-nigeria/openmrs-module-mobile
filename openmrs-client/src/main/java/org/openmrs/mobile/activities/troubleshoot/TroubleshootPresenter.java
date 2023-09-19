package org.openmrs.mobile.activities.troubleshoot;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.activities.addeditreceipt.AddEditReceiptContract;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.repository.ReceiptRepository;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;

import java.util.List;

public class TroubleshootPresenter extends BasePresenter implements TroubleshootContract.Presenter {

    private final TroubleshootContract.View troubleshootInfoView;
    private ReceiptRepository receiptRepository;
    private RestApi restApi;
    private Receipt mReceipt;
    private ReceiptItem mReceiptItem;
    private String patientToUpdateId;
    private List<String> mCountries;
    private boolean registeringReceipt = false;
    private List<Receipt> multipleReceipt;
    private long receiptToUpdateId;

    public TroubleshootPresenter(TroubleshootContract.View troubleshootInfoView,
                                   long receiptToUpdateId) {
        this.troubleshootInfoView = troubleshootInfoView;
        this.troubleshootInfoView.setPresenter(this);
    }

    @Override
    public void subscribe() {

    }
}
