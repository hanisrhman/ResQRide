package com.example.resqride.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.resqride.R;
import com.example.resqride.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Context context;
    List<ChatMessage> list;
    String myId;

    private static final int VIEW_ME = 1;
    private static final int VIEW_OTHER = 2;

    public ChatAdapter(Context context,List<ChatMessage> list,String myId){
        this.context=context;
        this.list=list;
        this.myId=myId;
    }

    // ================= VIEW TYPE =================

    @Override
    public int getItemViewType(int position){

        ChatMessage msg=list.get(position);

        if(msg != null && msg.isMine(myId))
            return VIEW_ME;
        else
            return VIEW_OTHER;
    }

    // ================= CREATE VIEW =================

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,int viewType){

        if(viewType==VIEW_ME){

            View v= LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_me,parent,false);

            return new MeVH(v);

        }else{

            View v= LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_other,parent,false);

            return new OtherVH(v);
        }
    }

    // ================= BIND VIEW =================

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,int pos){

        ChatMessage msg=list.get(pos);

        if(msg == null) return;

        if(holder instanceof MeVH){

            MeVH h=(MeVH)holder;

            bindMessage(
                    h.txtMessage,
                    h.img,
                    h.btnFile,
                    h.txtSeen,
                    msg,
                    true
            );

        }else{

            OtherVH h=(OtherVH)holder;

            bindMessage(
                    h.txtMessage,
                    h.img,
                    h.btnFile,
                    null,
                    msg,
                    false
            );
        }
    }

    // ================= BIND MESSAGE =================

    private void bindMessage(
            TextView txt,
            ImageView img,
            Button btnFile,
            TextView txtSeen,
            ChatMessage msg,
            boolean isMe){

        // reset visibility
        txt.setVisibility(View.GONE);
        img.setVisibility(View.GONE);
        btnFile.setVisibility(View.GONE);

        if(txtSeen != null)
            txtSeen.setVisibility(View.GONE);

        // TEXT MESSAGE
        if("text".equals(msg.type)){

            txt.setVisibility(View.VISIBLE);
            txt.setText(msg.message);

        }

        // IMAGE MESSAGE
        else if("image".equals(msg.type)){

            img.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(msg.fileUrl)
                    .placeholder(R.drawable.ic_image)
                    .into(img);

            img.setOnClickListener(v->{

                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(msg.fileUrl));
                context.startActivity(i);
            });
        }

        // FILE MESSAGE
        else if("file".equals(msg.type)){

            btnFile.setVisibility(View.VISIBLE);
            btnFile.setText(
                    msg.fileName != null ?
                            msg.fileName :
                            "Open File"
            );

            btnFile.setOnClickListener(v->{

                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(msg.fileUrl));
                context.startActivity(intent);
            });
        }

        // SEEN STATUS (ONLY FOR MY MESSAGE)
        if(isMe && txtSeen != null){

            txtSeen.setVisibility(View.VISIBLE);

            if(msg.seen)
                txtSeen.setText("Seen");
            else
                txtSeen.setText("Delivered");
        }
    }

    // ================= COUNT =================

    @Override
    public int getItemCount(){
        return list.size();
    }

    // ================= VIEW HOLDERS =================

    static class MeVH extends RecyclerView.ViewHolder{

        TextView txtMessage, txtSeen;
        ImageView img;
        Button btnFile;

        MeVH(View v){
            super(v);

            txtMessage=v.findViewById(R.id.txtMessage);
            img=v.findViewById(R.id.imgMessage);
            btnFile=v.findViewById(R.id.btnFile);
            txtSeen=v.findViewById(R.id.txtSeen);
        }
    }

    static class OtherVH extends RecyclerView.ViewHolder{

        TextView txtMessage;
        ImageView img;
        Button btnFile;

        OtherVH(View v){
            super(v);

            txtMessage=v.findViewById(R.id.txtMessage);
            img=v.findViewById(R.id.imgMessage);
            btnFile=v.findViewById(R.id.btnFile);
        }
    }
}