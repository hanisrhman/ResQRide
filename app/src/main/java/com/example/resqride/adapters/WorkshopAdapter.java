package com.example.resqride.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.R;
import com.example.resqride.models.WorkshopModel;

import java.util.ArrayList;

public class WorkshopAdapter
        extends RecyclerView.Adapter<WorkshopAdapter.ViewHolder> {

    // 🔔 Callback interface
    public interface OnRequestClick {
        void onRequest(WorkshopModel workshop);
    }

    ArrayList<WorkshopModel> list;
    OnRequestClick listener;

    public WorkshopAdapter(ArrayList<WorkshopModel> list,
                           OnRequestClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workshop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        WorkshopModel workshop = list.get(position);

        holder.txtName.setText(workshop.name);
        holder.txtRating.setText("⭐ " + workshop.rating);

        holder.btnRequest.setOnClickListener(v ->
                listener.onRequest(workshop)
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtRating;
        Button btnRequest;

        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtRating = itemView.findViewById(R.id.txtRating);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }
    }
}