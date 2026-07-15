package com.example.resqride.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.resqride.NavigationActivity;
import com.example.resqride.R;
import com.example.resqride.models.WorkshopSOS;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WorkshopSOSAdapter
        extends RecyclerView.Adapter<WorkshopSOSAdapter.VH> {

    Context context;
    List<WorkshopSOS> list;
    String workshopId;

    FirebaseFirestore db;

    public WorkshopSOSAdapter(Context context,
                              List<WorkshopSOS> list,
                              String workshopId) {

        this.context = context;
        this.list = list;
        this.workshopId = workshopId;

        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_workshop_sos,
                        parent,
                        false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VH h,
            int pos) {

        WorkshopSOS sos = list.get(pos);

        if (sos == null) return;

        // Rider name
        h.txtRiderName.setText(
                sos.riderName != null ?
                        sos.riderName :
                        "Rider");

        // Load avatar safely
        loadAvatar(sos.riderId, h.imgAvatar);

        // Problem
        h.txtProblem.setText(
                sos.problem != null ?
                        sos.problem : "-");

        h.txtProblemType.setText(
                sos.problemType != null ?
                        sos.problemType : "-");

        // Vehicle
        h.txtVehicle.setText(
                sos.vehicleModel != null ?
                        "Vehicle: " + sos.vehicleModel :
                        "Vehicle: -");

        // Address
        h.txtAddress.setText(
                sos.address != null ?
                        "📍 " + sos.address :
                        "📍 Location unavailable");

        // Distance
        String distance = calculateDistance(
                sos.lat,
                sos.lng,
                sos.workshopLat,
                sos.workshopLng);

        h.txtDistance.setText(distance + " • Now");

        // ACCEPT
        h.btnAccept.setOnClickListener(v -> {

            db.collection("sos_requests")
                    .document(sos.id)
                    .update(
                            "status", "accepted",
                            "assignedWorkshopId", workshopId
                    )
                    .addOnSuccessListener(unused -> {

                        Intent i = new Intent(
                                context,
                                NavigationActivity.class);

                        i.putExtra("sosId", sos.id);

                        context.startActivity(i);
                    });
        });

        // DECLINE
        h.btnDecline.setOnClickListener(v -> {

            db.collection("sos_requests")
                    .document(sos.id)
                    .update(
                            "status", "waiting_workshop",
                            "assignedWorkshopId", null,
                            "assignedWorkshopName", null
                    );
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    ///////////////////////////////////////////////
    //// LOAD AVATAR FROM USERS COLLECTION
    ///////////////////////////////////////////////

    private void loadAvatar(String riderId, ImageView imgAvatar) {

        imgAvatar.setImageResource(R.drawable.ic_user);

        if (riderId == null || riderId.isEmpty()) {
            Log.d("AVATAR", "riderId is null");
            return;
        }

        db.collection("users")
                .document(riderId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Log.d("AVATAR", "user doc not exist");
                        return;
                    }

                    String avatarUrl = doc.getString("avatarUrl");

                    Log.d("AVATAR", "avatarUrl = " + avatarUrl);

                    if (avatarUrl == null || avatarUrl.isEmpty())
                        return;

                    Glide.with(context)
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .circleCrop()
                            .into(imgAvatar);

                })
                .addOnFailureListener(e ->
                        Log.e("AVATAR", "load failed", e));
    }

    ///////////////////////////////////////////////
    //// VIEW HOLDER
    ///////////////////////////////////////////////

    static class VH extends RecyclerView.ViewHolder {

        ImageView imgAvatar;

        TextView txtRiderName;
        TextView txtProblem;
        TextView txtProblemType;
        TextView txtDistance;
        TextView txtVehicle;
        TextView txtAddress;

        Button btnAccept;
        Button btnDecline;

        VH(@NonNull View v) {

            super(v);

            imgAvatar = v.findViewById(R.id.imgAvatar);

            txtRiderName = v.findViewById(R.id.txtRiderName);
            txtProblem = v.findViewById(R.id.txtProblem);
            txtProblemType = v.findViewById(R.id.txtProblemType);
            txtDistance = v.findViewById(R.id.txtDistance);
            txtVehicle = v.findViewById(R.id.txtVehicle);
            txtAddress = v.findViewById(R.id.txtAddress);

            btnAccept = v.findViewById(R.id.btnAccept);
            btnDecline = v.findViewById(R.id.btnDecline);
        }
    }

    ///////////////////////////////////////////////
    //// DISTANCE CALCULATION
    ///////////////////////////////////////////////

    private String calculateDistance(
            double rLat,
            double rLng,
            double wLat,
            double wLng) {

        float[] result = new float[1];

        Location.distanceBetween(
                rLat,
                rLng,
                wLat,
                wLng,
                result);

        return String.format("%.1f km", result[0] / 1000);
    }
}