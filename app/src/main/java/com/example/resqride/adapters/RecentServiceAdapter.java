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
import com.example.resqride.SosDetailActivity;
import com.example.resqride.models.SosRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentServiceAdapter
        extends RecyclerView.Adapter<RecentServiceAdapter.VH> {

    Context context;
    List<SosRequest> list;

    public RecentServiceAdapter(Context context, List<SosRequest> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_service, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        SosRequest s = list.get(position);

        h.txtProblem.setText(
                s.problem != null ? s.problem : "Service"
        );

        h.txtWorkshop.setText(
                s.assignedWorkshopName != null
                        ? s.assignedWorkshopName
                        : "Workshop"
        );

        if (s.time > 0) {
            String time = new SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
            ).format(new Date(s.time));
            h.txtDate.setText(time);
        }

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, SosDetailActivity.class);
            i.putExtra("sosId", s.id);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtProblem, txtWorkshop, txtDate;

        VH(@NonNull View v) {
            super(v);
            txtProblem = v.findViewById(R.id.txtProblem);
            txtWorkshop = v.findViewById(R.id.txtWorkshop);
            txtDate = v.findViewById(R.id.txtDate);
        }
    }
}