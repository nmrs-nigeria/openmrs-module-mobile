package org.openmrs.mobile.activities.patientdashboard.entries;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.application.OpenMRSInflater;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.ObscreateLocal;
import org.openmrs.mobile.models.ObsgroupLocal;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.ImageUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arinze on 10/17/2018.
 */

public class EntriesExpandableListAdapter extends BaseExpandableListAdapter {

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    PatientDashboardContract.PatientEntriesPresenter mPresenter;
    private Context mContext;
    private List<Encountercreate> mEncounters;
    private List<ViewGroup> mChildLayouts;
    private SparseArray<Bitmap> mBitmapCache;
    private List<ObscreateLocal> observationsLocal = new ArrayList<>();
    private Gson gson=new GsonBuilder().create();
    private Type obscreatetype = new TypeToken<List<ObscreateLocal>>(){}.getType();
    public EntriesExpandableListAdapter(Context context, List<Encountercreate> encounters, PatientDashboardContract.PatientEntriesPresenter presenter) {
        this.mContext = context;
        this.mEncounters = encounters;
        this.mBitmapCache = new SparseArray<Bitmap>();
        this.mChildLayouts = generateChildLayouts();
        this.mPresenter = presenter;

    }

    private List<ViewGroup> generateChildLayouts() {
        List<ViewGroup> layouts = new ArrayList<ViewGroup>();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        OpenMRSInflater openMRSInflater = new OpenMRSInflater(inflater);

        for (Encountercreate encounter : this.mEncounters) {
            ViewGroup convertView = (ViewGroup) inflater.inflate(R.layout.list_visit_item, null);
            LinearLayout contentLayout = (LinearLayout) convertView.findViewById(R.id.listVisitItemLayoutContent);

            LinearLayout relativeLayout = new LinearLayout(mContext);
            ImageButton imageButton = new ImageButton(mContext);
//            TextView textView = new TextView(mContext);
//                    textView.setText(encounter.getUuid());
//                    textView.setVisibility(View.INVISIBLE);
            imageButton.setImageResource(R.drawable.ico_edit);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPresenter.startFormDisplayActivityWithEncounter(encounter);
                }
            });
            relativeLayout.addView(imageButton);
//            relativeLayout.addView(textView);
            contentLayout.addView(relativeLayout);
//            layouts.add(contentLayout);
            for (ObscreateLocal obs : encounter.getObservationsLocal()) {
                if (obs.getGroupMembers().isEmpty()) {
                    convertView = openMRSInflater.addKeyValueStringView(contentLayout, obs.getQuestionLabel(), obs.getAnswerLabel());
                }
                else{
                    convertView = openMRSInflater.addKeyValueStringViewGroup(contentLayout, "------------", "------------");
                    for (ObsgroupLocal obsMember : obs.getGroupMembers()){
                        convertView = openMRSInflater.addKeyValueStringView(contentLayout, obsMember.getQuestionLabel(), obsMember.getAnswerLabel());
                    }
                }
            }
            layouts.add(convertView);

        }

        return layouts;
    }

    @Override
    public int getGroupCount() {
        return mEncounters.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return  1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mEncounters.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildLayouts.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getGroupView(int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (null == convertView) {
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_visit_group, null);
        }

        final TextView encounterName = (TextView) rowView.findViewById(R.id.listVisitGroupEncounterName);
        final TextView detailsSelector = (TextView) rowView.findViewById(R.id.listVisitGroupDetailsSelector);
        final Encountercreate encounter = mEncounters.get(groupPosition);
        String encdate = encounter.getEncounterDatetime();
        // Encounter data return encdate possible rejection on NMRS
       // Util.logEncounter(encounter);

        String disp = encounter.getFormname() +" (" + DateUtils.convertTime(DateUtils.convertTime(encdate), DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT) + ")";
        encounterName.setText(disp);

        if (isExpanded) {
            detailsSelector.setText(mContext.getString(R.string.list_visit_selector_hide));
            bindDrawableResources(R.drawable.exp_list_hide_details, detailsSelector, RIGHT);
        } else {
            detailsSelector.setText(mContext.getString(R.string.list_visit_selector_show));
            bindDrawableResources(R.drawable.exp_list_show_details, detailsSelector, RIGHT);
        }
        FontsUtil.setFont(encounterName, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
        FontsUtil.setFont(detailsSelector, FontsUtil.OpenFonts.OPEN_SANS_LIGHT);
        encounterName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        detailsSelector.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        return rowView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return (ViewGroup) getChild(groupPosition, childPosition);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        this.mChildLayouts = generateChildLayouts();
        super.notifyDataSetChanged();
    }

    private void bindDrawableResources(int drawableID, TextView textView, int direction) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        Drawable image = mContext.getResources().getDrawable(drawableID);
        if(direction == LEFT) {
            image.setBounds(0, 0, (int)(40 * scale + 0.5f), (int)(40 * scale + 0.5f));
            textView.setCompoundDrawablePadding((int)(13 * scale + 0.5f));
            textView.setCompoundDrawables(image, null, null, null);
        }else {
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            textView.setCompoundDrawablePadding((int)(10 * scale + 0.5f));
            textView.setCompoundDrawables(null, null, image, null);
        }
    }

    private void createImageBitmap(Integer key, ViewGroup.LayoutParams layoutParams) {
        if (mBitmapCache.get(key) == null) {
            mBitmapCache.put(key, ImageUtils.decodeBitmapFromResource(mContext.getResources(), key,
                    layoutParams.width, layoutParams.height));
        }
    }
}
