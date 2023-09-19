package org.openmrs.mobile.activities.reportcommodity;

import org.openmrs.mobile.activities.BasePresenter;


public class ReportCommodityPresenter extends BasePresenter implements ReportCommodityContract.Presenter {

    private final ReportCommodityContract.View mReportCommodityView;

    public ReportCommodityPresenter(ReportCommodityContract.View ReportConsumptionContract) {
        mReportCommodityView = ReportConsumptionContract;
        mReportCommodityView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        mReportCommodityView.bindDrawableResources();
    }
}
