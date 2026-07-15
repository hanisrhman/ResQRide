package com.example.resqride.models;

import com.google.firebase.Timestamp;

public class Maintenance {

    // 🔑 Firestore document ID
    public String id;

    // 📋 Data fields
    public String serviceType;
    public String workshop;
    public String notes;
    public Timestamp date;
    public Timestamp createdAt;

    // 🔥 REQUIRED empty constructor for Firestore
    public Maintenance() {}

    // Optional constructor
    public Maintenance(String serviceType,
                       String workshop,
                       String notes,
                       Timestamp date) {

        this.serviceType = serviceType;
        this.workshop = workshop;
        this.notes = notes;
        this.date = date;
        this.createdAt = Timestamp.now();
    }
}