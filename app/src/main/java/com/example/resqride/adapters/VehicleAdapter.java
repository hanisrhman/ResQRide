package com.example.resqride.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.AddVehicleActivity;
import com.example.resqride.R;
import com.example.resqride.VehicleDetailActivity;
import com.example.resqride.models.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VH> {

    Context context;
    List<Vehicle> list;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public VehicleAdapter(Context context, List<Vehicle> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_vehicle, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        Vehicle v = list.get(position);

        h.txtModel.setText(v.model);
        h.txtPlate.setText(v.plateNumber);
        h.txtYear.setText("Year: " + v.year);
        h.txtService.setText(v.lastService);

        // OPEN DETAIL
        h.itemView.setOnClickListener(view -> {

            Intent intent =
                    new Intent(context, VehicleDetailActivity.class);

            intent.putExtra("vehicleId", v.id);

            context.startActivity(intent);
        });

        // MENU BUTTON
        h.btnMenu.setOnClickListener(view -> {

            PopupMenu popup =
                    new PopupMenu(context, h.btnMenu);

            popup.inflate(R.menu.menu_vehicle);

            popup.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.menu_edit) {

                    Intent intent =
                            new Intent(context, AddVehicleActivity.class);

                    intent.putExtra("vehicleId", v.id);

                    context.startActivity(intent);

                    return true;
                }

                if (item.getItemId() == R.id.menu_delete) {

                    showDeleteDialog(v);

                    return true;
                }

                return false;
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // DELETE DIALOG
    private void showDeleteDialog(Vehicle vehicle) {

        new AlertDialog.Builder(context)
                .setTitle("Delete Vehicle")
                .setMessage("Are you sure you want to delete this vehicle?")
                .setPositiveButton("Delete", (dialog, which) -> deleteVehicle(vehicle))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // DELETE VEHICLE
    private void deleteVehicle(Vehicle vehicle) {

        String userId = FirebaseAuth.getInstance().getUid();

        db.collection("users")
                .document(userId)
                .collection("vehicles")
                .document(vehicle.id)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(context,
                            "Vehicle deleted",
                            Toast.LENGTH_SHORT).show();

                    // DO NOT REMOVE MANUALLY
                    // Firestore listener updates automatically
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(context,
                            "Delete failed",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // VIEW HOLDER
    static class VH extends RecyclerView.ViewHolder {

        TextView txtModel, txtPlate, txtYear, txtService;
        ImageView btnMenu;

        VH(View v) {

            super(v);

            txtModel = v.findViewById(R.id.txtModel);
            txtPlate = v.findViewById(R.id.txtPlate);
            txtYear = v.findViewById(R.id.txtYear);
            txtService = v.findViewById(R.id.txtService);
            btnMenu = v.findViewById(R.id.btnMenu);
        }
    }
}