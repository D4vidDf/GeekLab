package com.d4viddf.geeklabwear;

import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.d4viddf.geeklabwear.activity.AllTasksListAdapter;
import com.d4viddf.geeklabwear.activity.AllTasksListAsyncProvider;
import com.d4viddf.geeklabwear.activity.AsyncProvider;
import com.d4viddf.geeklabwear.activity.MyActivityInfo;
import com.d4viddf.geeklabwear.databinding.ActivityMainBinding;
import com.d4viddf.geeklabwear.icon.LauncherIconCreator;

public class MainActivity extends FragmentActivity implements AllTasksListAsyncProvider.Listener<AllTasksListAdapter>, Filterable {

    private ExpandableListView list;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        list = findViewById(R.id.expandableListView1);
        list.setOnChildClickListener(
                (parent, v, groupPosition, childPosition, id) -> {
                    ExpandableListAdapter adapter = parent.getExpandableListAdapter();
                    MyActivityInfo info = (MyActivityInfo) adapter.getChild(groupPosition, childPosition);
                    LauncherIconCreator.launchActivity(getApplicationContext(), info.getComponentName());
                    return false;
                }
        );
        list.setTextFilterEnabled(true);

        AllTasksListAsyncProvider provider = new AllTasksListAsyncProvider(getApplicationContext(), this);
        provider.execute();
    }

    @Override
    public void onProviderFinished(AsyncProvider<AllTasksListAdapter> task, AllTasksListAdapter value) {
        try {
            this.list.setAdapter(value);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "R.string.error_tasks", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Filter getFilter() {
        AllTasksListAdapter adapter = (AllTasksListAdapter) this.list.getExpandableListAdapter();
        if (adapter != null) {
            return adapter.getFilter();
        } else {
            return null;
        }
    }
}