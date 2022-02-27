package com.example.protography.ui.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.protography.databinding.ImageItemBinding;
import com.example.protography.ui.Activities.ImageActivity;
import com.example.protography.ui.Models.Image;
import com.example.protography.ui.Models.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class RecyclerViewAdapterFind extends RecyclerView.Adapter<RecyclerViewAdapterFind.ImageViewHolder> {

    private static final String TAG = "RecyclerViewAdapterFind";
    private List<Image> imageList;
    private ImageItemBinding binding;
    private Context context;
    private boolean showProfileImage;


    public RecyclerViewAdapterFind(List<Image> imageList, Context context, boolean showProfileImage) {
        this.imageList = imageList;
        this.context = context;
        this.showProfileImage = showProfileImage;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        binding = ImageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        View view = binding.getRoot();

        return new ImageViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(imageList.get(position));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Image image;
        private ImageView imageView;
        private ShapeableImageView user;
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView saves;
        private Context context;


        public ImageViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            titleTextView = binding.title;
            user = binding.imageProfile;
            descriptionTextView = binding.description;
            imageView = binding.imageView;
            saves = binding.savesNr;
            this.context = context;

            itemView.setOnClickListener(this);
        }

        public void bind(Image image) {
            this.image = image;
            titleTextView.setText(image.getImageTitle());

            if(showProfileImage) {
                Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("fullName").equalTo(image.getImageNameUser());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User u = dataSnapshot.getValue(User.class);
                            Picasso.get().load(u.getProfileImg()).into(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
            else
                user.setVisibility(View.GONE);

            if (image.getImageDescription().length() >= 40)
                descriptionTextView.setText(image.getImageDescription().substring(0, 40) + "...");
            else
                descriptionTextView.setText(image.getImageDescription());

            saves.setText(String.valueOf(image.getSavesNumber()));
            Picasso.get().load(image.getImageUrl()).into(imageView);
        }


        @Override
        public void onClick(View v) {

            Intent intent = new Intent(itemView.getContext(), ImageActivity.class);
            intent.putExtra("Immagine", image);

            // L'animazione funziona solo con sdk >= 21 e se lo screen è verticale
            if (android.os.Build.VERSION.SDK_INT >= 21 && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) itemView.getContext(), imageView, ViewCompat.getTransitionName(imageView));
                ((Activity) itemView.getContext()).startActivity(intent, options.toBundle());
            } else
                itemView.getContext().startActivity(intent);

        }
    }
}
