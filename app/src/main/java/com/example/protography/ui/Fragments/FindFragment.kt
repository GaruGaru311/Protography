package com.example.protography.ui.Fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.protography.R
import com.example.protography.databinding.FragmentFindBinding
import com.example.protography.ui.Adapters.AdapterDiscover
import com.example.protography.ui.Models.Image
import com.google.android.material.chip.Chip
import com.google.firebase.database.*
import java.util.*

class FindFragment : Fragment() {
    private val database = FirebaseDatabase.getInstance()
    private val databaseRoot = database.reference
    private var binding: FragmentFindBinding? = null
    private var filters: RelativeLayout? = null
    private var chip1: Chip? = null
    private var chip2: Chip? = null
    private var chip3: Chip? = null
    private var query: Query? = null
    var recyclerView: RecyclerView? = null
    var toolbar: Toolbar? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFindBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val nameUser: String?
        nameUser = prefs.getString("FULLNAME", null)
        val imageList: MutableList<Image?> = ArrayList()
        val selectedChip: MutableList<String> = ArrayList()
        query = FirebaseDatabase.getInstance().getReference("Images")
        filters = binding!!.chipGroupLayout
        chip1 = binding!!.chip1
        chip2 = binding!!.chip2
        chip3 = binding!!.chip3
        toolbar = binding!!.toolbar
        recyclerView = binding!!.recyclerViewFind
        swipeRefreshLayout = binding!!.swipeFind
        toolbar!!.inflateMenu(R.menu.app_bar_menu)
        toolbar!!.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.filter -> {
                    if (filters!!.visibility == View.GONE) filters!!.visibility = View.VISIBLE else filters!!.visibility = View.GONE
                    true
                }
                else -> false
            }
        }
        val adapterDiscover = AdapterDiscover(imageList, context, true)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView!!.layoutManager = layoutManager
        val snapHelper: SnapHelper = PagerSnapHelper()
        recyclerView!!.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(recyclerView)
        recyclerView!!.adapter = adapterDiscover
        val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                selectedChip.add(buttonView.text.toString())
                filter(query, imageList, adapterDiscover, selectedChip, nameUser)
            } else {
                selectedChip.remove(buttonView.text.toString())
                filter(query, imageList, adapterDiscover, selectedChip, nameUser)
            }
        }
        chip1!!.setOnCheckedChangeListener(checkedChangeListener)
        chip2!!.setOnCheckedChangeListener(checkedChangeListener)
        chip3!!.setOnCheckedChangeListener(checkedChangeListener)
        load(query, imageList, adapterDiscover, selectedChip, nameUser)
        swipeRefreshLayout!!.setOnRefreshListener {
            swipeRefreshLayout!!.isRefreshing = false
            load(query, imageList, adapterDiscover, selectedChip, nameUser)
            recyclerView!!.adapter = adapterDiscover
        }
    }

    fun load(query: Query, imageList: MutableList<Image?>, adapterDiscover: AdapterDiscover, selectedChip: List<String>, nameUser: String?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imageList.clear()
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    for (dataSnapshot in snapshot.children) {
                        val image = dataSnapshot.getValue(Image::class.java)!!
                        when (selectedChip.size) {
                            0 -> if (image.imageNameUser != nameUser) imageList.add(image)
                            1 -> if (image.imageNameUser != nameUser) if (image.imageCategory == selectedChip[0]) imageList.add(image)
                            2 -> if (image.imageNameUser != nameUser) if (image.imageCategory == selectedChip[0] || image.imageCategory == selectedChip[1]) imageList.add(image)
                            3 -> if (image.imageNameUser != nameUser) if (image.imageCategory == selectedChip[0] || image.imageCategory == selectedChip[1] || image.imageCategory == selectedChip[2]) imageList.add(image)
                        }
                    }
                    Collections.shuffle(imageList)
                    adapterDiscover.notifyDataSetChanged()
                    recyclerView!!.adapter = adapterDiscover
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Errore caricamento immagini: " + error.message)
            }
        })
    }

    fun filter(query: Query, imageList: List<Image?>, adapterDiscover: AdapterDiscover?, selectedChip: List<String>, nameUser: String?) {
        val imageListTMP: MutableList<Image?> = ArrayList()
        val adapterDiscover2 = AdapterDiscover(imageListTMP, context, true)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imageListTMP.clear()
                for (i in imageList.indices) {
                    when (selectedChip.size) {
                        0 -> if (imageList[i]!!.imageNameUser != nameUser) imageListTMP.add(imageList[i])
                        1 -> if (imageList[i]!!.imageNameUser != nameUser) if (imageList[i]!!.imageCategory == selectedChip[0]) imageListTMP.add(imageList[i])
                        2 -> if (imageList[i]!!.imageNameUser != nameUser) if (imageList[i]!!.imageCategory == selectedChip[0] || imageList[i]!!.imageCategory == selectedChip[1]) imageListTMP.add(imageList[i])
                        3 -> if (imageList[i]!!.imageNameUser != nameUser) if (imageList[i]!!.imageCategory == selectedChip[0] || imageList[i]!!.imageCategory == selectedChip[1] || imageList[i]!!.imageCategory == selectedChip[2]) imageListTMP.add(imageList[i])
                    }
                }
                adapterDiscover2.notifyDataSetChanged()
                recyclerView!!.adapter = adapterDiscover2
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Errore caricamento immagini: " + error.message)
            }
        })
    }

    companion object {
        private const val TAG = "FindFragment"
    }
}