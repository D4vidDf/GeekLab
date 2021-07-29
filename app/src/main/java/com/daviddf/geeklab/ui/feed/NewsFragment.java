package com.daviddf.geeklab.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daviddf.geeklab.Experiments;
import com.daviddf.geeklab.Myadapter;
import com.daviddf.geeklab.R;
import com.daviddf.geeklab.databinding.FragmentFeedBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NewsFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<Experiments> experimentsArrayList;
    Myadapter myadapter;
    FirebaseFirestore db;

    private FragmentFeedBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentFeedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        db = FirebaseFirestore.getInstance();
        experimentsArrayList = new ArrayList<Experiments>();
        myadapter = new Myadapter(getActivity(),experimentsArrayList);

        recyclerView.setAdapter(myadapter);

        EventChangeListner();
        return root;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}