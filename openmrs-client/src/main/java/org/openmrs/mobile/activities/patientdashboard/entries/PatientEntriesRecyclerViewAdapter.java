package org.openmrs.mobile.activities.patientdashboard.entries;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.utilities.FontsUtil;

import java.util.List;

/**
 * Created by Arinze on 10/16/2018.
 */


public class PatientEntriesRecyclerViewAdapter extends RecyclerView.Adapter<PatientEntriesRecyclerViewAdapter.VisitViewHolder> {
    private PatientEntriesFragment mContext;
    private List<Encountercreate> mVisits;


    public PatientEntriesRecyclerViewAdapter(PatientEntriesFragment context, List<Encountercreate> items) {
        this.mContext = context;
        this.mVisits = items;
    }

    @Override
    public VisitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_patient_visit, parent, false);
        FontsUtil.setFont((ViewGroup) itemView);
        return new VisitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VisitViewHolder visitViewHolder, final int position) {
        final int adapterPos = visitViewHolder.getAdapterPosition();
        Encountercreate visit = mVisits.get(adapterPos);
//        visitViewHolder.mVisitStart.setText(DateUtils.convertTime1(visit.getStartDatetime(), DateUtils.DATE_WITH_TIME_FORMAT));
//        if (DateUtils.convertTime(visit.getStartDatetime()) != null) {
//            visitViewHolder.mVisitEnd.setVisibility(View.VISIBLE);
//            visitViewHolder.mVisitEnd.setText(DateUtils.convertTime1((visit.getStopDatetime()), DateUtils.DATE_WITH_TIME_FORMAT));
//            Drawable icon = mContext.getResources().getDrawable(R.drawable.past_visit_dot);
//            icon.setBounds(0, 0, icon.getIntrinsicHeight(), icon.getIntrinsicWidth());
//            visitViewHolder.mVisitStatus.setCompoundDrawables(icon, null, null, null);
//            visitViewHolder.mVisitStatus.setText(mContext.getString(R.string.past_visit_label));
//        } else {
//            visitViewHolder.mVisitEnd.setVisibility(View.INVISIBLE);
//            Drawable icon = mContext.getResources().getDrawable(R.drawable.active_visit_dot);
//            icon.setBounds(0, 0, icon.getIntrinsicHeight(), icon.getIntrinsicWidth());
//            visitViewHolder.mVisitStatus.setCompoundDrawables(icon, null, null, null);
//            visitViewHolder.mVisitStatus.setText(mContext.getString(R.string.active_visit_label));
//        }
//        if (visit.getLocation() != null) {
//            visitViewHolder.mVisitPlace.setText(mContext.getString(R.string.visit_in, visit.getLocation().getDisplay()));
//        }

        visitViewHolder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.goToVisitDashboard(mVisits.get(adapterPos).getId());
            }
        });
    }

    @Override
    public void onViewDetachedFromWindow(VisitViewHolder holder) {
        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return mVisits.size();
    }

    class VisitViewHolder extends RecyclerView.ViewHolder{
        private TextView mVisitPlace;
        private TextView mVisitStart;
        private TextView mVisitEnd;
        private TextView mVisitStatus;
        private RelativeLayout mRelativeLayout;

        public VisitViewHolder(View itemView) {
            super(itemView);
            mRelativeLayout = (RelativeLayout) itemView;
            mVisitStart = (TextView) itemView.findViewById(R.id.patientVisitStartDate);
            mVisitEnd = (TextView) itemView.findViewById(R.id.patientVisitEndDate);
            mVisitPlace = (TextView) itemView.findViewById(R.id.patientVisitPlace);
            mVisitStatus = (TextView) itemView.findViewById(R.id.visitStatusLabel);
        }
        public void clearAnimation() {
            mRelativeLayout.clearAnimation();
        }
    }
}
