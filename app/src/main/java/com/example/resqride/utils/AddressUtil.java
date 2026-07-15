package com.example.resqride.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

public class AddressUtil {

    public static String getAddress(Context context, double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lng, 1);

            if (list != null && !list.isEmpty()) {
                Address a = list.get(0);
                return a.getAddressLine(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown location";
    }
}