package com.example.resqride.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.resqride.R;
import com.example.resqride.models.Workshop;

import java.util.List;

public class WorkshopCardAdapter
        extends RecyclerView.Adapter<WorkshopCardAdapter.VH> {

    Context context;
    List<Workshop> list;
    OnWorkshopClickListener listener;

    // ================= CLICK INTERFACE =================

    public interface OnWorkshopClickListener {
        void onWorkshopClick(Workshop workshop);
    }

    // ================= CONSTRUCTOR =================

    public WorkshopCardAdapter(
            Context context,
            List<Workshop> list,
            OnWorkshopClickListener listener) {

        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    // ================= CREATE VIEW =================

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_workshop_card,
                        parent,
                        false);

        return new VH(view);
    }

    // ================= BIND VIEW =================

    @Override
    public void onBindViewHolder(
            @NonNull VH holder,
            int position) {

        Workshop w = list.get(position);

        // ===== NAME =====

        holder.txtName.setText(
                w.name != null ? w.name : "Workshop");

        // ===== STATUS =====

        String status = w.getStatusText();

        holder.txtStatus.setText(status);

        if (w.isOpenNow()) {

            holder.txtStatus.setTextColor(
                    Color.parseColor("#16A34A")); // green

        } else {

            holder.txtStatus.setTextColor(
                    Color.parseColor("#DC2626")); // red
        }

        // ===== RATING + DISTANCE =====

        holder.txtRatingDistance.setText(
                w.getRatingText()
                        + " • "
                        + w.getDistanceText());

        // ===== ADDRESS =====

        holder.txtAddress.setText(
                w.address != null ? w.address : "");

        // ===== IMAGE =====

        if (w.imageUrl != null && !w.imageUrl.isEmpty()) {

            Glide.with(context)
                    .load(w.imageUrl)
                    .placeholder(
                            R.drawable.ic_workshop_placeholder)
                    .error(
                            R.drawable.ic_workshop_placeholder)
                    .into(holder.imgWorkshop);

        } else {

            holder.imgWorkshop.setImageResource(
                    R.drawable.ic_workshop_placeholder);
        }

        // ================= CARD CLICK =================

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onWorkshopClick(w);
            }
        });

        // ================= CALL =================

        holder.btnCall.setOnClickListener(v -> {

            if (w.phone == null || w.phone.isEmpty())
                return;

            Intent intent = new Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:" + w.phone));

            context.startActivity(intent);
        });

        // ================= NAVIGATE =================

        holder.btnNavigate.setOnClickListener(v -> {

            String uri =
                    "google.navigation:q="
                            + w.lat
                            + ","
                            + w.lng;

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri));

            intent.setPackage(
                    "com.google.android.apps.maps");

            context.startActivity(intent);
        });
    }

    // ================= COUNT =================

    @Override
    public int getItemCount() {

        return list != null ? list.size() : 0;
    }

    // ================= VIEW HOLDER =================

    public static class VH
            extends RecyclerView.ViewHolder {

        ImageView imgWorkshop;

        TextView txtName;
        TextView txtStatus;
        TextView txtRatingDistance;
        TextView txtAddress;

        Button btnCall;
        Button btnNavigate;

        public VH(@NonNull View itemView) {

            super(itemView);

            imgWorkshop =
                    itemView.findViewById(
                            R.id.imgWorkshop);

            txtName =
                    itemView.findViewById(
                            R.id.txtName);

            txtStatus =
                    itemView.findViewById(
                            R.id.txtStatus);

            txtRatingDistance =
                    itemView.findViewById(
                            R.id.txtRatingDistance);

            txtAddress =
                    itemView.findViewById(
                            R.id.txtAddress);

            btnCall =
                    itemView.findViewById(
                            R.id.btnCall);

            btnNavigate =
                    itemView.findViewById(
                            R.id.btnNavigate);
        }
    }
}