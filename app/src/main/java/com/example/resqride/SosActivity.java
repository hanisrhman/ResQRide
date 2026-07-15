package com.example.resqride;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.resqride.utils.AddressUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class SosActivity extends AppCompatActivity {

    private static final int REQ_PHOTO = 101;
    private static final int LOCATION_REQ = 102;

    private Uri mediaUri;
    private AlertDialog sosDialog;

    FirebaseFirestore db;
    FirebaseStorage storage;
    FusedLocationProviderClient locationClient;

    TextView txtMediaStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        showSosDialog();
    }

    // ================= SHOW SOS DIALOG =================
    private void showSosDialog() {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_sos_form, null);

        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtProblem = view.findViewById(R.id.edtProblem);
        EditText edtVehicleModel = view.findViewById(R.id.edtVehicleModel);
        EditText edtPlate = view.findViewById(R.id.edtPlate);
        Spinner spinnerVehicle = view.findViewById(R.id.spinnerVehicle);
        txtMediaStatus = view.findViewById(R.id.txtMediaStatus);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        sosDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        // Back arrow
        btnBack.setOnClickListener(v -> {
            sosDialog.dismiss();
            finish();
        });

        // Phone back button
        sosDialog.setOnCancelListener(dialog -> finish());

        loadVehicles(spinnerVehicle, edtVehicleModel, edtPlate);

        view.findViewById(R.id.btnTakePhoto).setOnClickListener(v ->
                startActivityForResult(
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                        REQ_PHOTO
                )
        );

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            sosDialog.dismiss();
            finish();
        });

        view.findViewById(R.id.btnSubmitSOS).setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String problem = edtProblem.getText().toString().trim();

            if (name.isEmpty() || problem.isEmpty()) {
                toast("Please fill all required fields");
                return;
            }

            String vehicleModel;
            String vehiclePlate;
            String vehicleId = null;

            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) spinnerVehicle.getTag();
            int pos = spinnerVehicle.getSelectedItemPosition();

            if (ids != null && ids.get(pos) != null) {
                vehicleId = ids.get(pos);

                String selected = spinnerVehicle.getSelectedItem().toString();
                vehicleModel = selected.substring(0, selected.indexOf("(")).trim();
                vehiclePlate = selected.substring(
                        selected.indexOf("(") + 1,
                        selected.indexOf(")")
                );
            } else {
                vehicleModel = edtVehicleModel.getText().toString().trim();
                vehiclePlate = edtPlate.getText().toString().trim();

                if (vehicleModel.isEmpty()) {
                    toast("Enter vehicle model");
                    return;
                }
            }

            sosDialog.dismiss();
            sendSOS(name, problem, vehicleId, vehicleModel, vehiclePlate);
        });

        sosDialog.show();

        if (sosDialog.getWindow() != null) {
            sosDialog.getWindow().setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.88)
            );
        }
    }

    // ================= LOAD VEHICLES (FIXED) =================
    private void loadVehicles(Spinner spinner,
                              EditText edtModel,
                              EditText edtPlate) {

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            spinner.setVisibility(View.GONE);
            edtModel.setVisibility(View.VISIBLE);
            edtPlate.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("vehicles")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<String> display = new ArrayList<>();
                    List<String> ids = new ArrayList<>();

                    for (QueryDocumentSnapshot d : snapshot) {
                        String model = d.getString("model");
                        String plate = d.getString("plateNumber");

                        display.add(model + " (" + plate + ")");
                        ids.add(d.getId());
                    }

                    display.add("Other vehicle");
                    ids.add(null);

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    display
                            );

                    spinner.setAdapter(adapter);
                    spinner.setTag(ids);

                    spinner.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {

                                @Override
                                public void onItemSelected(
                                        AdapterView<?> parent,
                                        View view,
                                        int position,
                                        long id) {

                                    if (ids.get(position) == null) {
                                        edtModel.setVisibility(View.VISIBLE);
                                        edtPlate.setVisibility(View.VISIBLE);
                                    } else {
                                        edtModel.setVisibility(View.GONE);
                                        edtPlate.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                });
    }

    // ================= CAMERA RESULT =================
    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == REQ_PHOTO && res == RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap == null) return;

            try {
                File file = new File(
                        getCacheDir(),
                        "sos_" + System.currentTimeMillis() + ".jpg"
                );

                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();

                mediaUri = Uri.fromFile(file);
                txtMediaStatus.setText("📸 Photo attached");

            } catch (Exception e) {
                toast("Failed to attach photo");
            }
        }
    }

    // ================= SEND SOS =================
    private void sendSOS(
            String name,
            String problem,
            String vehicleId,
            String model,
            String plate
    ) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ
            );
            return;
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        saveSOS(name, problem, vehicleId, model, plate, location);
                    } else {
                        toast("Unable to get location");
                    }
                });
    }

    // ================= SAVE SOS =================
    private void saveSOS(
            String name,
            String problem,
            String vehicleId,
            String model,
            String plate,
            Location location
    ) {

        String sosId = UUID.randomUUID().toString();
        String riderId = FirebaseAuth.getInstance().getUid();

        String address = AddressUtil.getAddress(
                this,
                location.getLatitude(),
                location.getLongitude()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("id", sosId);
        data.put("riderId", riderId);
        data.put("riderName", name);
        data.put("problem", problem);
        data.put("vehicleId", vehicleId);
        data.put("vehicleModel", model);
        data.put("vehiclePlate", plate);
        data.put("lat", location.getLatitude());
        data.put("lng", location.getLongitude());
        data.put("address", address);
        data.put("status", "selecting");
        data.put("time", System.currentTimeMillis());

        uploadPhotoAndSaveSOS(sosId, data);
    }

    // ================= UPLOAD PHOTO =================
    private void uploadPhotoAndSaveSOS(String sosId, Map<String, Object> data) {

        if (mediaUri == null) {
            saveSOSData(sosId, data);
            return;
        }

        StorageReference ref =
                storage.getReference("sos_media/" + sosId + ".jpg");

        ref.putFile(mediaUri)
                .continueWithTask(task -> ref.getDownloadUrl())
                .addOnSuccessListener(uri -> {
                    data.put("mediaUrl", uri.toString());
                    saveSOSData(sosId, data);
                })
                .addOnFailureListener(e ->
                        toast("Image upload failed"));
    }

    // ================= SAVE TO FIRESTORE =================
    private void saveSOSData(String sosId, Map<String, Object> data) {

        db.collection("sos_requests")
                .document(sosId)
                .set(data)
                .addOnSuccessListener(v -> {

                    Intent i = new Intent(
                            this,
                            WorkshopSelectionActivity.class
                    );
                    i.putExtra("sosId", sosId);
                    i.putExtra("lat", (double) data.get("lat"));
                    i.putExtra("lng", (double) data.get("lng"));
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e ->
                        toast(e.getMessage()));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}