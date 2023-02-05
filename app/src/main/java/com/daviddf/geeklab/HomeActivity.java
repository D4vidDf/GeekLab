package com.daviddf.geeklab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daviddf.geeklab.notification.Notifiaction;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    ConstraintLayout notification, battery,system, apps;
    MaterialButton tools, news;
    RecyclerView feed;
    ArrayList<Experiments> experimentsArrayList;
    Myadapter myadapter;
    FirebaseFirestore db;
    TextView errortext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        notification =  findViewById(R.id.notification);
        battery = findViewById(R.id.battery);
        system = findViewById(R.id.system);
        apps = findViewById(R.id.apps);
        errortext = findViewById(R.id.error_txt);

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent( HomeActivity.this, Notifiaction.class));
            }
        });

        feed = findViewById(R.id.news);
        feed.setHasFixedSize(true);
        feed.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        experimentsArrayList = new ArrayList<Experiments>();
        myadapter = new Myadapter(this,experimentsArrayList);

        feed.setAdapter(myadapter);


        EventChangeListner();
    }
    private void EventChangeListner() {
        db.collection("experiments").orderBy("Fecha", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(e != null) { Log.e("Error al conectar:",e.getMessage());
                            errortext.setVisibility(View.GONE);
                            return;
                        }

                        for (DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){
                            if (dc.getType() == DocumentChange.Type.ADDED){
                                if (experimentsArrayList.size() < 2)
                                experimentsArrayList.add(dc.getDocument().toObject(Experiments.class));
                            }
                            errortext.setVisibility(View.GONE);

                            myadapter.notifyDataSetChanged();
                        }

                    }
                });
    }
}