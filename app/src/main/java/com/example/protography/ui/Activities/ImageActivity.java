package com.example.protography.ui.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.daimajia.androidanimations.library.specials.out.TakingOffAnimator;
import com.example.protography.R;
import com.example.protography.databinding.ActivityImageBinding;
import com.example.protography.ui.Models.Image;
import com.example.protography.ui.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";
    ActivityImageBinding binding;
    Image image;
    private SharedPreferences sharedPreferencesDefault;
    private List<String> imagesLiked;
    private String userKey;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        image = (Image) getIntent().getParcelableExtra("Immagine");

        // Carica dati immagine
        Picasso.get().load(image.getImageUrl()).into(binding.imageView);
        binding.user.setText(image.getImageNameUser());
        binding.title.setText(image.getImageTitle());

        if (image.getImageCategory().equals("Nature"))
            binding.chipNature.setChecked(true);
        else if (image.getImageCategory().equals("Urban"))
            binding.chipUrban.setChecked(true);
        else
            binding.chipPortrait.setChecked(true);

        // Apertura gmaps
        binding.coords.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + image.getCoords());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Controlla che ci sia un applicazione per la navigazione
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(ImageActivity.this, R.string.errorMaps, Toast.LENGTH_SHORT).show();
            }
        });

        // Imposto gli elementi grafici
        binding.description.setText(image.getImageDescription());
        binding.description.setContent(image.getImageDescription());
        binding.description.setTextMaxLength(200);
        binding.description.setSeeMoreTextColor(R.color.yellow);

        binding.saved.setText("Saves: " + String.format("%d",image.getSavesNumber()));

        binding.equipment.setText(image.getImageEquipment());

        if (image.getImageSettings() == null || image.getImageSettings().isEmpty())
            binding.settingsLayout.setVisibility(View.GONE);
        else
            binding.cameraSettings.setText(image.getImageSettings());

        if (image.getImageTime() == null || image.getImageTime().isEmpty())
            binding.timeLayout.setVisibility(View.GONE);
        else
            binding.bestTimeToGo.setText(image.getImageTime());

        if (image.getImageTips() == null || image.getImageTips().isEmpty())
            binding.tipsLayout.setVisibility(View.GONE);
        else
            binding.tips.setText(image.getImageTips());

        // Prendo user e le sue immagini piaciute
        sharedPreferencesDefault = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> imageL = sharedPreferencesDefault.getStringSet("IMAGES_LIKED", null);
        imagesLiked = new ArrayList<>();
        imagesLiked.addAll(imageL);
        userKey = sharedPreferencesDefault.getString("USER_KEY", null);
        String mail = sharedPreferencesDefault.getString("EMAIL", null);
        String fullName = sharedPreferencesDefault.getString("FULLNAME", null);
        String profileImg = sharedPreferencesDefault.getString("PROFILEIMG", null);
        currentUser = new User(fullName, mail, profileImg);
        currentUser.setFotoPiaciute(imagesLiked);

        //disabilito il salvataggio di una propria foto
        if(image.getImageNameUser().equals(fullName)) {
            binding.like.setActivated(false);
            binding.like.setAlpha(0.2f);
        }
        else
            binding.like.setActivated(true);


        // Controllo se l'immagine è dell'utente
        if (!image.getImageNameUser().equals(fullName))

            // Se non è dell'utente nascono il pulsante elimina
            binding.delete.setVisibility(View.GONE);
        else {

            // Se è dell'utente imposto il listener per l'elimina
            binding.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(ImageActivity.this)
                            .setTitle(R.string.sure_to_delete_image)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                    // Delete da Firebase
                                    Query q = FirebaseDatabase.getInstance().getReference("Images")
                                            .orderByChild("imageUrl").equalTo(image.getImageUrl());
                                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            for (DataSnapshot i: snapshot.getChildren())
                                                FirebaseDatabase.getInstance().getReference("Images").child(i.getKey()).removeValue();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                            Log.d(TAG, "ERRORE ELIMINAZIONE IMMAGINE");
                                        }
                                    });


                                    // Delete della vecchia immagine dallo storage
                                    StorageReference imageRef = FirebaseStorage.getInstance().getReference("Images").getStorage()
                                            .getReferenceFromUrl(image.getImageUrl());
                                    imageRef.delete().addOnSuccessListener(unused -> {

                                        Toast.makeText(ImageActivity.this, getString(R.string.image_correctly_deleted), Toast.LENGTH_SHORT).show();
                                    });

                                    finish();

                                }
                            }).create().show();



                }
            });
        }


        // Controllo se è un'immagine piaciuta a l'utente e imposto il like
        if (imagesLiked.contains(image.getImageUrl()))
            binding.like.setImageResource(R.drawable.ic_baseline_bookmark_24);
        else
            binding.like.setImageResource(R.drawable.ic_baseline_not_bookmark_24);

        binding.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isActivated()) {
                    if (imagesLiked.contains(image.getImageUrl())) {

                        // Se è piaciuta la rimuovo
                        imagesLiked.remove(image.getImageUrl());
                        currentUser.setFotoPiaciute(imagesLiked);
                        FirebaseDatabase.getInstance().getReference("Users").child(userKey).setValue(currentUser);

                        // Aggiorno shared
                        HashSet<String> fotoPiaciute = new HashSet<>();
                        fotoPiaciute.addAll(imagesLiked);
                        sharedPreferencesDefault.edit().remove("IMAGES_LIKED").commit();
                        sharedPreferencesDefault.edit().putStringSet("IMAGES_LIKED", fotoPiaciute).commit();

                        image.setSavesNumber(image.getSavesNumber() - 1);
                        binding.saved.setText("Saves: " + String.format("%d", image.getSavesNumber()));

                        Query query = FirebaseDatabase.getInstance().getReference("Images")
                                .orderByChild("imageUrl").equalTo(image.getImageUrl());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    snapshot.getRef().child("savesNumber").setValue(image.getSavesNumber());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                        binding.like.setImageResource(R.drawable.ic_baseline_not_bookmark_24);
                        Toast.makeText(ImageActivity.this, getString(R.string.disliked), Toast.LENGTH_SHORT).show();
                    } else {

                        // Se non è piaciuta la aggiungo
                        imagesLiked.add(image.getImageUrl());
                        currentUser.setFotoPiaciute(imagesLiked);
                        FirebaseDatabase.getInstance().getReference("Users").child(userKey).setValue(currentUser);

                        // Aggiorno shared
                        HashSet<String> fotoPiaciute = new HashSet<>();
                        fotoPiaciute.addAll(imagesLiked);
                        sharedPreferencesDefault.edit().remove("IMAGES_LIKED").commit();
                        sharedPreferencesDefault.edit().putStringSet("IMAGES_LIKED", fotoPiaciute).commit();

                        image.setSavesNumber(image.getSavesNumber() + 1);
                        binding.saved.setText("Saves: " + String.format("%d", image.getSavesNumber()));

                        Query query = FirebaseDatabase.getInstance().getReference("Images")
                                .orderByChild("imageUrl").equalTo(image.getImageUrl());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    snapshot.getRef().child("savesNumber").setValue(image.getSavesNumber());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                        binding.like.setImageResource(R.drawable.ic_baseline_bookmark_24);
                        Toast.makeText(ImageActivity.this, getString(R.string.liked), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                    Toast.makeText(ImageActivity.this, R.string.cant_save, Toast.LENGTH_SHORT).show();
            }
        });
    }


}