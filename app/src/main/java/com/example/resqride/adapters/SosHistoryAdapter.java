package com.example.resqride.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.R;
import com.example.resqride.SosDetailActivity;
import com.example.resqride.SosTrackingActivity;
import com.example.resqride.WorkshopSelectionActivity;
import com.example.resqride.models.SosRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SosHistoryAdapter
        extends RecyclerView.Adapter<SosHistoryAdapter.VH> {

    private final Context context;
    private final List<SosRequest> list;

    public SosHistoryAdapter(Context context, List<SosRequest> list) {
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
                .inflate(R.layout.item_history_sos, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        SosRequest s = list.get(position);

        // 🛠 Problem
        h.txtProblem.setText(
                s.problem != null ? s.problem : "Unknown problem"
        );

        // 🏍 Workshop
        h.txtWorkshop.setText(
                s.assignedWorkshopName != null
                        ? s.assignedWorkshopName
                        : "Searching workshop..."
        );

        // 🚲 Vehicle
        if (s.vehicleModel != null && !s.vehicleModel.isEmpty()) {
            h.txtVehicle.setText("Vehicle: " + s.vehicleModel);
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

        // ================= STATUS =================
        String status = s.status != null ? s.status : "unknown";
        h.txtStatus.setText(status.replace("_", " ").toUpperCase());

        applyStatusColor(h.txtStatus, status);

        // ================= CLICK ACTION =================
        h.itemView.setOnClickListener(v -> {

            Intent intent;

            switch (status) {

                case "selecting":
                case "waiting_workshop":
                case "selecting_workshop":
                    intent = new Intent(context, WorkshopSelectionActivity.class);
                    intent.putExtra("sosId", s.id);
                    intent.putExtra("lat", s.lat);
                    intent.putExtra("lng", s.lng);
                    break;

                case "accepted":
                case "arrived":
                case "on_the_way":
                case "in_progress":
                    intent = new Intent(context, SosTrackingActivity.class);
                    intent.putExtra("sosId", s.id);
                    break;

                case "completed":
                    intent = new Intent(context, SosDetailActivity.class);
                    intent.putExtra("sosId", s.id);
                    break;

                default:
                    intent = new Intent(context, SosDetailActivity.class);
                    intent.putExtra("sosId", s.id);
                    break;
            }

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= STATUS COLOR =================
    private void applyStatusColor(TextView tv, String status) {

        int color;

        switch (status) {
            case "selecting":
            case "selecting_workshop":
                color = 0xFFF97316; // ORANGE
                break;
            case "waiting_workshop":
                color = 0xFFEAB308; // AMBER
                break;
            case "accepted":
            case "on_the_way":
                color = 0xFF2563EB; // BLUE
                break;
            case "arrived":
                color = 0xFF7C3AED; // PURPLE
                break;
            case "in_progress":
                color = 0xFF0D9488; // TEAL
                break;
            case "completed":
                color = 0xFF16A34A; // GREEN
                break;
            default:
                color = 0xFF9CA3AF; // GRAY
                break;
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(50f);

        tv.setBackground(bg);
        tv.setTextColor(0xFFFFFFFF);
        tv.setPadding(24, 12, 24, 12);
    }

    // ================= VIEW HOLDER =================
    static class VH extends RecyclerView.ViewHolder {

        TextView txtProblem, txtWorkshop,
                txtVehicle, txtTime, txtStatus;

        VH(@NonNull View v) {
            super(v);
            txtProblem = v.findViewById(R.id.txtProblem);
            txtWorkshop = v.findViewById(R.id.txtWorkshop);
            txtVehicle = v.findViewById(R.id.txtVehicle);
            txtTime = v.findViewById(R.id.txtTime);
            txtStatus = v.findViewById(R.id.txtStatus);
        }
    }
}