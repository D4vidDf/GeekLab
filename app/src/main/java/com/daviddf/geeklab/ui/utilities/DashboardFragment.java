package com.daviddf.geeklab.ui.utilities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.daviddf.geeklab.Consentimiento;
import com.daviddf.geeklab.Menu;
import com.daviddf.geeklab.Notifiaction;
import com.daviddf.geeklab.R;
import com.daviddf.geeklab.databinding.FragmentUtilitiesBinding;
import com.google.android.material.button.MaterialButton;

public class DashboardFragment extends Fragment {

    MaterialButton noti, dev, band, ia_hdr, speed, account, data, performance, qcolor;

    private FragmentUtilitiesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentUtilitiesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        noti = (MaterialButton) root.findViewById(R.id.not);

        noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent( getActivity(), Notifiaction.class));
            }
        });

        dev = (MaterialButton) root.findViewById(R.id.developer);

        dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Dev = new Intent(Intent.ACTION_VIEW);
                Dev.setClassName("com.android.settings","com.android.settings.Settings$DevelopmentSettingsDashboardActivity");
                startActivity(Dev);
            }
        });

        band = (MaterialButton) root.findViewById(R.id.band);

        band.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Band = new Intent(Intent.ACTION_VIEW);
                    Band.setClassName("com.android.settings", "com.android.settings.MiuiBandMode");
                    startActivity(Band);
                } catch (RuntimeException e) {
                    Toast errorToast = Toast.makeText(getActivity(), "Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        ia_hdr = (MaterialButton) root.findViewById(R.id.hdr);

        ia_hdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Hdr = new Intent(Intent.ACTION_VIEW);
                    Hdr.setClassName("com.android.settings", "com.android.settings.display.ScreenEnhanceEngineS2hActivity");
                    startActivity(Hdr);
                } catch (RuntimeException e) {
                    Toast errorToast = Toast.makeText(getActivity(), "Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        speed = (MaterialButton) root.findViewById(R.id.velo);

        speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Speed = new Intent(Intent.ACTION_VIEW);
                    Speed.setClassName("com.android.settings", "com.android.settings.wifi.linkturbo.WifiLinkTurboSettings");
                    startActivity(Speed);
                }catch (RuntimeException e){
                    Toast errorToast = Toast.makeText(getActivity(),"Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        account = (MaterialButton) root.findViewById(R.id.accounts);

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Account = new Intent(Intent.ACTION_VIEW);
                    Account.setClassName("com.android.settings", "com.android.settings.Settings$UserSettingsActivity");
                    startActivity(Account);
                }catch (RuntimeException e){
                    Toast errorToast = Toast.makeText(getActivity(),"Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        data = (MaterialButton) root.findViewById(R.id.usage);

        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Data = new Intent(Intent.ACTION_VIEW);
                    Data.setClassName("com.xiaomi.misettings","com.xiaomi.misettings.usagestats.UsageStatsMainActivity");
                    startActivity(Data);
                }catch (RuntimeException e){
                    Toast errorToast = Toast.makeText(getActivity(),"Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });

        performance = (MaterialButton) root.findViewById(R.id.rendimiento);

        performance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Performance = new Intent(Intent.ACTION_VIEW);
                    Performance.setClassName("com.qualcomm.qti.performancemode","com.qualcomm.qti.performancemode.PerformanceModeActivity");
                    startActivity(Performance);
                } catch (RuntimeException e){
                    Toast errorToast = Toast.makeText(getActivity(),"Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }

            }
        });

        qcolor = (MaterialButton) root.findViewById(R.id.qcolor);

        qcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent Performance = new Intent(Intent.ACTION_VIEW);
                    Performance.setClassName("com.qualcomm.qti.qcolor", "com.qualcomm.qti.qcolor.QColorActivity");
                    startActivity(Performance);
                } catch (RuntimeException e) {
                    Toast errorToast = Toast.makeText(getActivity(), "Esta función no está disponible en su terminal", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }
        });


        MaterialButton lp = (MaterialButton) root.findViewById(R.id.loop);

        lp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Consentimiento.class));
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