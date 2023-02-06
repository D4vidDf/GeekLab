package com.daviddf.geeklab;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Feed extends AppCompatActivity {
    MaterialToolbar appbar;

    RecyclerView feed;
    ArrayList<Experiments> experimentsArrayList;
    Myadapter myadapter;
    FirebaseFirestore db;
    TextView errortext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        errortext = findViewById(R.id.error_txt);

        appbar = (MaterialToolbar) findViewById(R.id.topAppBar);
        appbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        feed = findViewById(R.id.feed);
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
                                experimentsArrayList.add(dc.getDocument().toObject(Experiments.class));
                            }
                            errortext.setVisibility(View.GONE);

                            myadapter.notifyDataSetChanged();
                        }

                    }
                });
    }
}