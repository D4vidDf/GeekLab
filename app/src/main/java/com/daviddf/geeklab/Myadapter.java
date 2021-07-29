package com.daviddf.geeklab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Myadapter extends RecyclerView.Adapter<Myadapter.MyViewHolder> {
    FragmentActivity activity;
    Context context;
    ArrayList<Experiments> experimentsArrayList;
    MaterialButton Tag;

    public Myadapter( Context context, ArrayList<Experiments> experimentsArrayList) {

        this.context = context;
        this.experimentsArrayList = experimentsArrayList;
    }

    public Myadapter(FragmentActivity activity, ArrayList<Experiments> experimentsArrayList) {

        this.activity = activity;
        this.experimentsArrayList = experimentsArrayList;

    }


    @NonNull
    @Override
    public Myadapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(activity).inflate(R.layout.item,parent,false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Myadapter.MyViewHolder holder, int position) {

        Experiments experiments = experimentsArrayList.get(position);
        holder.Titulo.setText(experiments.Titulo);
        holder.Tag.setText(experiments.Tag);
        Picasso.get().load(experiments.Imagen).placeholder(R.mipmap.ic_launcher).into(holder.Portada);

        holder.Portada.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                /*Intent url = new Intent(Intent.ACTION_VIEW);
                url.setData(Uri.parse(experiments.Url));
                context.startActivity(url);*/

                CustomTabsIntent.Builder customtab = new CustomTabsIntent.Builder();
                openCustomTabs(activity,customtab.build(), Uri.parse(experiments.Url));

            }
        });

        if (holder.Tag.getText().equals("Experimentos")){
            holder.Tag.setBackgroundColor(0xff0384fc);
        }

        if (holder.Tag.getText().equals("Noticias")){
            holder.Tag.setBackgroundColor(0xff7703fc);
        }

        if (holder.Tag.getText().equals("Artículo")){
            holder.Tag.setBackgroundColor(0xffde4f23);
        }

    }

    @Override
    public int getItemCount() {
        return experimentsArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView Titulo;
        MaterialButton Tag;
        ImageView Portada;

        public MyViewHolder(@NonNull  View itemView) {
            super(itemView);
            Tag = itemView.findViewById(R.id.tag);
            Titulo = itemView.findViewById(R.id.titulo);
            Portada = itemView.findViewById(R.id.portada);

        }
    }

    public static void openCustomTabs (Activity activity, CustomTabsIntent customTabsIntent, Uri uri){

        String packageName = "com.android.chrome";

        if(packageName != null){

            customTabsIntent.intent.setPackage(packageName);
            try {
                customTabsIntent.launchUrl(activity, uri);
            } catch (Exception e){
                Toast errorToast = Toast.makeText(activity, R.string.error_navigation_chorme, Toast.LENGTH_LONG);
                errorToast.show();
            }

        } else {

            activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));


        }

    }
}
