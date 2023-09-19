package org.openmrs.mobile.activities.patientdashboard.fingerprint;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.databases.Util;

import java.util.List;

public class FingerPrintAdapter extends   RecyclerView.Adapter<FingerPrintAdapter.ViewHolder> {

    private  List<ItemFingerPrint> data;
    public FingerPrintAdapter(List<ItemFingerPrint> data ) {
             this.data=data;
        }
    public void setData(List<ItemFingerPrint> data ) {
        this.data= data;
       notifyDataSetChanged();
    }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fingerprint, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ItemFingerPrint  item = data.get(position);
            holder.sn.setText(item.getSn());
            holder.position.setText(item.getPosition());
            holder.quality.setText(item.getQuality());
        }

        @Override
        public int getItemCount() {
            return data!=null?data.size():0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView sn;
            TextView quality;
            TextView position;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                sn= itemView.findViewById(R.id.sn); // counter
                position= itemView.findViewById(R.id.position);// finger print position // left thumb etc
                quality = itemView.findViewById(R.id.quanlity);// quality capture
            }
        }
    }
