package com.daviddf.geeklab

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Picasso

class Myadapter : RecyclerView.Adapter<Myadapter.MyViewHolder> {
    private var activity: FragmentActivity? = null
    private var context: Context? = null
    private var experimentsArrayList: ArrayList<Experiments>

    constructor(context: Context, experimentsArrayList: ArrayList<Experiments>) {
        this.context = context;
        this.experimentsArrayList = experimentsArrayList
    }

    constructor(activity: FragmentActivity, experimentsArrayList: ArrayList<Experiments>) {
        this.activity = activity
        this.experimentsArrayList = experimentsArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(activity ?: context).inflate(R.layout.item, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val experiments = experimentsArrayList[position]
        holder.titulo.text = experiments.titulo
        holder.tag.text = experiments.tag
        Picasso.get().load(experiments.imagen).placeholder(R.drawable.preset).into(holder.portada)

        holder.portada.setOnClickListener {
            val uri = Uri.parse(experiments.url)
            val customtab = CustomTabsIntent.Builder()
            openCustomTabs(activity ?: (context as? Activity) ?: return@setOnClickListener, customtab.build(), uri)
        }
    }

    override fun getItemCount(): Int = experimentsArrayList.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: MaterialTextView = itemView.findViewById(R.id.titulo)
        val tag: MaterialButton = itemView.findViewById(R.id.tag)
        val portada: ImageView = itemView.findViewById(R.id.portada)
    }

    companion object {
        fun openCustomTabs(activity: Activity, customTabsIntent: CustomTabsIntent, uri: Uri) {
            val packageName = "com.android.chrome"
            customTabsIntent.intent.setPackage(packageName)
            try {
                customTabsIntent.launchUrl(activity, uri)
            } catch (e: Exception) {
                Toast.makeText(activity, R.string.error_navigation_chorme, Toast.LENGTH_LONG).show()
            }
        }
    }
}
