package com.daviddf.geeklab;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FeedExperiments extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Experiments> experimentsArrayList;
    Myadapter myadapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_experiments);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        experimentsArrayList = new ArrayList<Experiments>();
        myadapter = new Myadapter(FeedExperiments.this,experimentsArrayList);

        recyclerView.setAdapter(myadapter);

        EventChangeListner();
    }

    private void EventChangeListner() {
        db.collection("experiments").orderBy("Fecha", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(e != null) {
                            Log.e("Error al conectar:",e.getMessage());
                            return;
                        }

                        for (DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){

                            if (dc.getType() == DocumentChange.Type.ADDED){

                                experimentsArrayList.add(dc.getDocument().toObject(Experiments.class));

                            }

                            myadapter.notifyDataSetChanged();
                        }

                    }
                });
    }
}