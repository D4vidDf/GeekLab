package com.daviddf.geeklab.ui.feed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daviddf.geeklab.Experiments
import com.daviddf.geeklab.Myadapter
import com.daviddf.geeklab.R
import com.daviddf.geeklab.databinding.FragmentFeedBinding
import com.google.firebase.firestore.*

class NewsFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var experimentsArrayList = ArrayList<Experiments>()
    private var myadapter: Myadapter? = null
    private var db: FirebaseFirestore? = null
    private var errimg: ImageView? = null
    private var errortext: TextView? = null

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val root = binding.root

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
            root.setPadding(0, insets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        recyclerView = root.findViewById(R.id.recycler)
        recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }

        errimg = root.findViewById(R.id.img_error)
        errortext = root.findViewById(R.id.error_txt)

        db = FirebaseFirestore.getInstance()
        myadapter = Myadapter(requireActivity(), experimentsArrayList)
        recyclerView?.adapter = myadapter

        eventChangeListener()
        return root
    }

    private fun eventChangeListener() {
        db?.collection("experiments")?.orderBy("Fecha", Query.Direction.DESCENDING)
            ?.addSnapshotListener { queryDocumentSnapshots, e ->
                if (e != null) {
                    errimg?.visibility = View.GONE
                    errortext?.visibility = View.GONE
                    Log.e("Error al conectar:", e.message ?: "")
                    return@addSnapshotListener
                }

                queryDocumentSnapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        experimentsArrayList.add(dc.document.toObject(Experiments::class.java))
                    }
                }
                myadapter?.notifyDataSetChanged()
                errimg?.visibility = View.GONE
                errortext?.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
