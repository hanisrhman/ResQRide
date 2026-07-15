package com.example.resqride.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.R;
import com.example.resqride.ReceiptActivity;
import com.example.resqride.models.SosRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkshopHistoryAdapter
        extends RecyclerView.Adapter<WorkshopHistoryAdapter.VH> {

    private final Context context;
    private final List<SosRequest> list;

    public WorkshopHistoryAdapter(Context context, List<SosRequest> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_workshop_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        SosRequest s = list.get(position);

        // 👤 Rider name
        h.txtRiderName.setText(
                s.riderName != null ? s.riderName : "Unknown Rider"
        );

        // 🛠 Problem
        h.txtProblem.setText(
                s.problem != null ? s.problem : "Unknown problem"
        );

        // 🏍 Vehicle
        if (s.vehicleModel != null && !s.vehicleModel.isEmpty()) {
            h.txtVehicle.setText(s.vehicleModel);
            h.txtVehicle.setVisibility(View.VISIBLE);
        } else {
            h.txtVehicle.setVisibility(View.GONE);
        }

        // ⏰ Time
        if (s.time > 0) {
            String time = new SimpleDateFormat(
                    "dd MMM yyyy • hh:mm a",
                    Locale.getDefault()
            ).format(new Date(s.time));
            h.txtTime.setText(time);
        } else {
            h.txtTime.setText("-");
        }

        // 👉 CLICK → OPEN RECEIPT
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ReceiptActivity.class);
            i.putExtra("sosId", s.id);
            i.putExtra("role", "workshop"); // optional
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================
    static class VH extends RecyclerView.ViewHolder {

        TextView txtRiderName, txtProblem, txtVehicle, txtTime;

        VH(@NonNull View v) {
            super(v);
            txtRiderName = v.findViewById(R.id.txtRiderName);
            txtProblem = v.findViewById(R.id.txtProblem);
            txtVehicle = v.findViewById(R.id.txtVehicle);
            txtTime = v.findViewById(R.id.txtTime);
        }
    }
}