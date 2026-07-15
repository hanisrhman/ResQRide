package com.example.resqride.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.*;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.resqride.R;
import com.example.resqride.SosStatusActivity;
import com.example.resqride.models.Workshop;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.*;

public class WorkshopSelectAdapter
        extends RecyclerView.Adapter<WorkshopSelectAdapter.VH> {

    Context context;
    List<Workshop> list;

    String sosId;
    double riderLat, riderLng;

    FirebaseFirestore db;

    boolean assigning = false;

    public WorkshopSelectAdapter(
            Context context,
            List<Workshop> list,
            String sosId,
            double riderLat,
            double riderLng) {

        this.context = context;
        this.list = list;
        this.sosId = sosId;
        this.riderLat = riderLat;
        this.riderLng = riderLng;

        db = FirebaseFirestore.getInstance();
    }

    // ================= CREATE VIEW =================

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_workshop_select,
                        parent,
                        false);

        return new VH(v);
    }

    // ================= BIND =================

    @Override
    public void onBindViewHolder(
            @NonNull VH holder,
            int position) {

        Workshop workshop = list.get(position);

        holder.txtName.setText(
                workshop.name != null ?
                        workshop.name :
                        "Workshop");

        holder.txtAddress.setText(
                workshop.address != null ?
                        workshop.address :
                        "");

        double km = distance(
                riderLat,
                riderLng,
                workshop.lat,
                workshop.lng);

        holder.txtRatingDistance.setText(
                String.format(
                        Locale.getDefault(),
                        "⭐ %.1f • %.1f km",
                        workshop.rating,
                        km));

        if(workshop.imageUrl != null &&
                !workshop.imageUrl.isEmpty()){

            Glide.with(context)
                    .load(workshop.imageUrl)
                    .placeholder(R.drawable.ic_workshop_placeholder)
                    .into(holder.imgWorkshop);

        }else{

            holder.imgWorkshop.setImageResource(
                    R.drawable.ic_workshop_placeholder);
        }

        holder.btnSelect.setEnabled(true);
        holder.btnSelect.setText("Select Workshop");

        holder.btnSelect.setOnClickListener(v -> {

            if(assigning) return;

            assigning = true;

            holder.btnSelect.setEnabled(false);
            holder.btnSelect.setText("Assigning...");

            assignWorkshop(workshop, holder);
        });
    }

    // ================= ASSIGN WORKSHOP =================

    private void assignWorkshop(
            Workshop workshop,
            VH holder){

        Map<String,Object> update = new HashMap<>();

        update.put("assignedWorkshopId", workshop.id);
        update.put("assignedWorkshopName", workshop.name);
        update.put("assignedWorkshopLat", workshop.lat);
        update.put("assignedWorkshopLng", workshop.lng);

        update.put("status","waiting_workshop");
        update.put("assignedAt",System.currentTimeMillis());

        db.collection("sos_requests")
                .document(sosId)
                .update(update)

                .addOnSuccessListener(unused -> {

                    assigning = false;

                    Toast.makeText(
                            context,
                            "Workshop assigned successfully",
                            Toast.LENGTH_SHORT).show();

                    // ✅ SEND NOTIFICATION HERE
                    sendNotificationToWorkshop(workshop.id);

                    Intent intent =
                            new Intent(context,
                                    SosStatusActivity.class);

                    intent.putExtra("sosId",sosId);

                    context.startActivity(intent);
                })

                .addOnFailureListener(e -> {

                    assigning = false;

                    holder.btnSelect.setEnabled(true);
                    holder.btnSelect.setText("Select Workshop");

                    Toast.makeText(
                            context,
                            "Failed: "+e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // ================= SEND NOTIFICATION =================

    private void sendNotificationToWorkshop(String workshopId){

        db.collection("workshops")
                .document(workshopId)
                .get()
                .addOnSuccessListener(doc -> {

                    if(!doc.exists()) return;

                    String token = doc.getString("fcmToken");

                    if(token == null) return;

                    // Save notification to Firestore (for badge)
                    Map<String,Object> notif = new HashMap<>();

                    notif.put("workshopId", workshopId);
                    notif.put("sosId", sosId);
                    notif.put("title", "New SOS Request");
                    notif.put("message", "You have a new SOS request");
                    notif.put("time", System.currentTimeMillis());
                    notif.put("read", false);

                    db.collection("notifications")
                            .add(notif);
                });
    }

    // ================= DISTANCE =================

    private double distance(
            double lat1,
            double lng1,
            double lat2,
            double lng2){

        float[] result = new float[1];

        Location.distanceBetween(
                lat1,lng1,
                lat2,lng2,
                result);

        return result[0]/1000;
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    // ================= VIEW HOLDER =================

    static class VH extends RecyclerView.ViewHolder{

        ImageView imgWorkshop;
        TextView txtName, txtAddress, txtRatingDistance;
        Button btnSelect;

        VH(View v){

            super(v);

            imgWorkshop =
                    v.findViewById(R.id.imgWorkshop);

            txtName =
                    v.findViewById(R.id.txtName);

            txtAddress =
                    v.findViewById(R.id.txtAddress);

            txtRatingDistance =
                    v.findViewById(R.id.txtRatingDistance);

            btnSelect =
                    v.findViewById(R.id.btnSelect);
        }
    }
}