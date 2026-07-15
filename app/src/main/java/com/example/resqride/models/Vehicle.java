package com.example.resqride.models;

public class Vehicle {

    public String id;
    public String ownerId;
    public String model;
    public String plateNumber;
    public String year;
    public String lastService;

    public Vehicle() {} // Firestore

    public Vehicle(String id,
                   String model,
                   String plateNumber,
                   String year,
                   String lastService) {

        this.id = id;
        this.model = model;
        this.plateNumber = plateNumber;
        this.year = year;
        this.lastService = lastService;
    }
}