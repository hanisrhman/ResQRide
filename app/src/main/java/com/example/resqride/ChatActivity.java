package com.example.resqride;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.ChatAdapter;
import com.example.resqride.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recycler;
    EditText edt;
    ImageButton btnSend,btnImage,btnFile,btnBack;

    FirebaseFirestore db;
    FirebaseStorage storage;

    String sosId,myId;

    List<ChatMessage> list=new ArrayList<>();
    ChatAdapter adapter;

    static final int PICK_IMAGE=1;
    static final int PICK_FILE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sosId=getIntent().getStringExtra("sosId");
        myId= FirebaseAuth.getInstance().getUid();

        db=FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();

        recycler=findViewById(R.id.recyclerChat);
        edt=findViewById(R.id.edtMessage);
        btnSend=findViewById(R.id.btnSend);
        btnImage=findViewById(R.id.btnImage);
        btnFile=findViewById(R.id.btnFile);
        btnBack=findViewById(R.id.btnBack);

        recycler.setLayoutManager(
                new LinearLayoutManager(this));

        adapter=new ChatAdapter(this,list,myId);
        recycler.setAdapter(adapter);

        btnBack.setOnClickListener(v->finish());

        btnSend.setOnClickListener(v->sendText());

        btnImage.setOnClickListener(v->pickImage());

        btnFile.setOnClickListener(v->pickFile());

        listen();
    }

    private void sendText(){

        String msg=edt.getText().toString().trim();

        if(msg.isEmpty())return;

        Map<String,Object> map=new HashMap<>();

        map.put("senderId",myId);
        map.put("message",msg);
        map.put("type","text");
        map.put("time",System.currentTimeMillis());
        map.put("seen",false);

        db.collection("sos_chats")
                .document(sosId)
                .collection("messages")
                .add(map);

        edt.setText("");
    }

    private void pickImage(){

        Intent i=new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i,PICK_IMAGE);
    }

    private void pickFile(){

        Intent i=new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i,PICK_FILE);
    }

    @Override
    protected void onActivityResult(
            int req,int res,Intent data){

        super.onActivityResult(req,res,data);

        if(res!= Activity.RESULT_OK||data==null)return;

        Uri uri=data.getData();

        uploadFile(uri,req==PICK_IMAGE?"image":"file");
    }

    private void uploadFile(Uri uri,String type){

        String name=UUID.randomUUID().toString();

        storage.getReference("chat_files/"+name)
                .putFile(uri)
                .continueWithTask(task->
                        task.getResult()
                                .getStorage()
                                .getDownloadUrl())
                .addOnSuccessListener(url->{

                    Map<String,Object> map=new HashMap<>();

                    map.put("senderId",myId);
                    map.put("type",type);
                    map.put("fileUrl",url.toString());
                    map.put("fileName",name);
                    map.put("time",System.currentTimeMillis());
                    map.put("seen",false);

                    db.collection("sos_chats")
                            .document(sosId)
                            .collection("messages")
                            .add(map);
                });
    }

    private void listen(){

        db.collection("sos_chats")
                .document(sosId)
                .collection("messages")
                .orderBy("time")
                .addSnapshotListener((snap,e)->{

                    if(snap==null)return;

                    list.clear();

                    for(DocumentSnapshot d:snap){

                        ChatMessage m=
                                d.toObject(ChatMessage.class);

                        list.add(m);
                    }

                    adapter.notifyDataSetChanged();

                    recycler.scrollToPosition(
                            list.size()-1);
                });
    }
}