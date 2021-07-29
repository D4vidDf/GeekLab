package com.daviddf.geeklab.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.daviddf.geeklab.R;
import com.daviddf.geeklab.databinding.FragmentSettingsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    SwitchMaterial dark;
    MaterialTextView text;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MaterialButton twitter = (MaterialButton) root.findViewById(R.id.twitter);
        MaterialButton github = (MaterialButton) root.findViewById(R.id.github);

        dark = (SwitchMaterial) root.findViewById(R.id.nightday);
        text = (MaterialTextView) root.findViewById(R.id.night_mode);

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/d4viddf")));
            }
        });

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/D4vidDf/GeekLab")));
            }
        });



        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            dark.setChecked(true);
            text.setText(R.string.activado);
        }
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO){
            dark.setChecked(false);
            text.setText(R.string.desactivado);

        }
        if (dark.isChecked()) {
            text.setText(R.string.activado);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            text.setText(R.string.desactivado);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        dark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                    text.setText(R.string.activado);
                    SettingsFragment.super.onCreate(savedInstanceState);

                } else {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    text.setText(R.string.desactivado);
                    SettingsFragment.super.onCreate(savedInstanceState);
                }
            }
        });

        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}