package com.example.resqride.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.DeleteMaintenanceDialog;
import com.example.resqride.EditMaintenanceDialog;
import com.example.resqride.R;
import com.example.resqride.models.Maintenance;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MaintenanceAdapter
        extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {

    Context context;
    String vehicleId;
    List<Maintenance> list;

    public MaintenanceAdapter(Context context,
                              String vehicleId,
                              List<Maintenance> list) {
        this.context = context;
        this.vehicleId = vehicleId;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_maintenance, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h, int position) {

        Maintenance m = list.get(position);

        h.txtTitle.setText(
                m.serviceType == null ? "Service" : m.serviceType
        );

        h.txtWorkshop.setText(
                m.workshop == null ? "Unknown Workshop" : m.workshop
        );

        h.txtNotes.setText(m.notes == null ? "" : m.notes);

        if (m.date != null) {
            h.txtDate.setText(
                    new SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                    ).format(m.date.toDate())
            );
        } else {
            h.txtDate.setText("-");
        }

        // 🔥 LONG PRESS MENU
        h.itemView.setOnLongClickListener(v -> {

            if (m.id == null || vehicleId == null) {
                Toast.makeText(context,
                        "Maintenance ID not found",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            new AlertDialog.Builder(context)
                    .setTitle("Maintenance Options")
                    .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {

                        if (which == 0) {
                            EditMaintenanceDialog.show(
                                    context,
                                    vehicleId,
                                    m.id,
                                    m
                            );
                        } else {
                            DeleteMaintenanceDialog.show(
                                    context,
                                    vehicleId,
                                    m.id
                            );
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtWorkshop, txtNotes, txtDate;

        ViewHolder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtWorkshop = v.findViewById(R.id.txtWorkshop);
            txtNotes = v.findViewById(R.id.txtNotes);
            txtDate = v.findViewById(R.id.txtDate);
        }
    }
}