package com.example.resqride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.resqride.R;
import com.example.resqride.models.WorkshopReview;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WorkshopReviewAdapter
        extends RecyclerView.Adapter<WorkshopReviewAdapter.VH> {

    Context context;
    List<WorkshopReview> list;

    FirebaseFirestore db;

    public WorkshopReviewAdapter(Context context,
                                 List<WorkshopReview> list){

        this.context = context;
        this.list = list;

        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType){

        View v =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.item_review,
                                parent,
                                false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VH h,
            int pos){

        WorkshopReview r = list.get(pos);

        if(r == null) return;

        h.txtReview.setText(r.review);

        h.txtRating.setText("⭐ " + r.rating);

        // LOAD RIDER INFO FROM USERS COLLECTION
        loadRiderInfo(r.riderId, h);
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    // ============================================
    // LOAD RIDER NAME AND AVATAR
    // ============================================

    private void loadRiderInfo(String riderId, VH h){

        if(riderId == null){

            h.txtName.setText("Rider");
            h.imgAvatar.setImageResource(R.drawable.ic_user);
            return;
        }

        db.collection("users")
                .document(riderId)
                .get()
                .addOnSuccessListener(doc->{

                    if(!doc.exists()){
                        h.txtName.setText("Rider");
                        h.imgAvatar.setImageResource(R.drawable.ic_user);
                        return;
                    }

                    String name =
                            doc.getString("name");

                    String avatar =
                            doc.getString("avatarUrl");

                    h.txtName.setText(
                            name != null ? name : "Rider");

                    Glide.with(context)
                            .load(avatar)
                            .placeholder(R.drawable.ic_user)
                            .into(h.imgAvatar);

                })
                .addOnFailureListener(e->{

                    h.txtName.setText("Rider");
                    h.imgAvatar.setImageResource(R.drawable.ic_user);

                });
    }

    // ============================================

    static class VH extends RecyclerView.ViewHolder{

        CircleImageView imgAvatar;
        TextView txtName, txtReview, txtRating;

        VH(View v){

            super(v);

            imgAvatar =
                    v.findViewById(R.id.imgAvatar);

            txtName =
                    v.findViewById(R.id.txtName);

            txtReview =
                    v.findViewById(R.id.txtReview);

            txtRating =
                    v.findViewById(R.id.txtRating);
        }
    }
}