package com.example.resqride.models;

public class WorkshopModel {

    public String id;
    public String name;
    public double lat;
    public double lng;
    public double rating;
    public boolean approved;
    public boolean online;

    // REQUIRED empty constructor (Firestore)
    public WorkshopModel() {}

    public WorkshopModel(String id, String name,
                         double lat, double lng,
                         double rating) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
    }
}