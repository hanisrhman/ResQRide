package com.example.resqride.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SosRequest {

    // ===== DOCUMENT ID =====
    public String id;

    // ===== RIDER =====
    public String riderId;
    public String riderName;

    // ===== SOS DETAILS =====
    public String problem;
    public String status;               // selecting | waiting_workshop | accepted | completed

    // ===== VEHICLE =====
    public String vehicleId;
    public String vehicleModel;
    public String vehiclePlate;

    // ===== WORKSHOP =====
    public String assignedWorkshopId;
    public String assignedWorkshopName;

    // ===== LOCATION =====
    public double lat;
    public double lng;
    public String address;
    public String riderAddress;

    // ===== TIME (IMPORTANT) =====
    public long time;                   // SOS created time
    public Long assignedAt;              // workshop accepted time
    public Long completedAt;             // COMPLETED TIME (🔥 REQUIRED)

    // ===== PAYMENT =====
    public String paymentMethod;
    public String paymentStatus;

    // ===== RATING =====
    public Float rating;
    public String review;

    // 🔥 REQUIRED EMPTY CONSTRUCTOR
    public SosRequest() {}
}