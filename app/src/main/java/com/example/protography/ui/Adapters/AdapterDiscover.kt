package com.example.protography.ui.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Geocoder
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.protography.databinding.ImageDiscoverBinding
import com.example.protography.ui.Activities.ImageActivity
import com.example.protography.ui.Models.Image
import com.example.protography.ui.Models.User
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.*

class AdapterDiscover(private val imageList: List<Image>, private val context: Context, private val showProfileImage: Boolean) : RecyclerView.Adapter<AdapterDiscover.ImageViewHolder>() {
    private var binding: ImageDiscoverBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // Create a new view, which defines the UI of the list item
        binding = ImageDiscoverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val view: View = binding!!.root
        return ImageViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class ImageViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var image: Image? = null
        private val imageView: ImageView
        private val user: ShapeableImageView
        private val titleTextView: TextView
        private val descriptionTextView: TextView
        private val context: Context
        private val userName: TextView
        private val place: TextView
        var gcd: Geocoder? = null
        fun bind(image: Image) {
            this.image = image
            titleTextView.text = image.imageTitle
            if (showProfileImage) {
                val query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("fullName").equalTo(image.imageNameUser)
                query.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot in snapshot.children) {
                            val u = dataSnapshot.getValue(User::class.java)!!
                            Picasso.get().load(u.profileImg).into(user)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            } else user.visibility = View.GONE
            if (image.imageDescription.length >= 120) descriptionTextView.text = image.imageDescription.substring(0, 120) + "..." else descriptionTextView.text = image.imageDescription
            userName.text = image.imageNameUser
            gcd = Geocoder(context, Locale.getDefault())
            try {
                val addresses = gcd!!.getFromLocation(image.latitude, image.longitude, 1)
                if (addresses.size > 0) {
                    if (addresses[0].locality != null) place.text = addresses[0].locality else if (addresses[0].countryName != null) place.text = addresses[0].countryName else place.text = "Unknown"
                } else place.text = "Unknown"
            } catch (e: IOException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            Picasso.get().load(image.imageUrl).into(imageView)
        }

        override fun onClick(v: View) {
            val intent = Intent(itemView.context, ImageActivity::class.java)
            val query = FirebaseDatabase.getInstance().getReference("Images").orderByChild("imageUid").equalTo(image!!.imageUid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        for (dataSnapshot in snapshot.children) {
                            image = dataSnapshot.getValue(Image::class.java)
                            intent.putExtra("Immagine", image)

                            // L'animazione funziona solo con sdk >= 21 e se lo screen è verticale
                            if (Build.VERSION.SDK_INT >= 21 && context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation((itemView.context as Activity), imageView, ViewCompat.getTransitionName(imageView)!!)
                                (itemView.context as Activity).startActivity(intent, options.toBundle())
                            } else itemView.context.startActivity(intent)
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        init {
            titleTextView = binding!!.title
            user = binding!!.imageProfile
            descriptionTextView = binding!!.description
            imageView = binding!!.imageView
            userName = binding!!.userName
            place = binding!!.place
            this.context = context
            itemView.setOnClickListener(this)
        }
    }

    companion object {
        private const val TAG = "RecyclerViewAdapterFind"
    }

}