package com.daviddf.geeklab.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
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
    ImageView errimg;
    TextView errortext;

    private FragmentFeedBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentFeedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            // Apply the insets as padding to the view. Here we're setting all of the
            // dimensions, but apply as appropriate to your layout. You could also
            // update the views margin if more appropriate.
            root.setPadding(0,insets.top,0,0);

            // Return CONSUMED if we don't want the window insets to keep being passed
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });


        recyclerView = root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        errimg = (ImageView)root.findViewById(R.id.img_error);
        errortext = (TextView) root.findViewById(R.id.error_txt);

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

                            errimg.setVisibility(View.GONE);
                            errortext.setVisibility(View.GONE);

                            Log.e("Error al conectar:",e.getMessage());
                            return;
                        }

                        for (DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){

                            if (dc.getType() == DocumentChange.Type.ADDED){

                                experimentsArrayList.add(dc.getDocument().toObject(Experiments.class));

                            }

                            myadapter.notifyDataSetChanged();

                            errimg.setVisibility(View.GONE);
                            errortext.setVisibility(View.GONE);
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