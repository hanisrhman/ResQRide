package com.example.resqride.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class Workshop {

    // ================= BASIC INFO =================

    public String id = "";
    public String name = "";
    public String address = "";
    public String phone = "";
    public String imageUrl = "";

    // ================= LOCATION =================

    public double lat = 0;
    public double lng = 0;

    // ================= RATING =================

    public double rating = 0;

    // ================= TIME =================
    // format: "09:00"

    public String openTime = "";
    public String closeTime = "";

    // ================= DISTANCE =================

    public double distanceKm = 0;

    // REQUIRED empty constructor for Firestore
    public Workshop() {}

    // ================= SET DISTANCE =================

    public void setDistance(double km) {

        this.distanceKm = km;
    }

    // ================= GET DISTANCE TEXT =================

    public String getDistanceText() {

        return String.format(
                Locale.getDefault(),
                "%.1f km",
                distanceKm
        );
    }

    // ================= GET RATING TEXT =================

    public String getRatingText() {

        return String.format(
                Locale.getDefault(),
                "⭐ %.1f",
                rating
        );
    }

    // ================= FORMAT TIME =================

    private String formatTime(String time24) {

        try {

            if (time24 == null || time24.isEmpty())
                return "";

            SimpleDateFormat input =
                    new SimpleDateFormat("HH:mm", Locale.getDefault());

            SimpleDateFormat output =
                    new SimpleDateFormat("h:mm a", Locale.getDefault());

            Date date = input.parse(time24);

            if (date == null)
                return time24;

            return output.format(date);

        }
        catch (Exception e) {

            return time24;
        }
    }

    // ================= CHECK OPEN NOW =================

    public boolean isOpenNow() {

        try {

            if (openTime == null || closeTime == null ||
                    openTime.isEmpty() || closeTime.isEmpty())
                return false;

            SimpleDateFormat sdf =
                    new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date now = sdf.parse(
                    sdf.format(new Date())
            );

            Date open = sdf.parse(openTime);
            Date close = sdf.parse(closeTime);

            if (open == null || close == null || now == null)
                return false;

            // Normal hours (example: 09:00 - 18:00)
            if (open.before(close)) {

                return now.after(open) && now.before(close);
            }

            // Overnight hours (example: 22:00 - 06:00)
            else {

                return now.after(open) || now.before(close);
            }

        }
        catch (Exception e) {

            return false;
        }
    }

    // ================= STATUS TEXT =================

    public String getStatusText() {

        if (openTime == null || closeTime == null ||
                openTime.isEmpty() || closeTime.isEmpty())
            return "Hours not available";

        String openFormatted = formatTime(openTime);
        String closeFormatted = formatTime(closeTime);

        if (isOpenNow()) {

            return "Open • " +
                    openFormatted +
                    " - " +
                    closeFormatted;
        }
        else {

            return "Closed • " +
                    openFormatted +
                    " - " +
                    closeFormatted;
        }
    }
}