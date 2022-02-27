package com.example.protography.ui.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.protography.R;
import com.example.protography.databinding.ActivityEditProfileBinding;
import com.example.protography.ui.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editTextFullName;
    private ShapeableImageView profileImage, chooseImage;
    private Button save;
    private ActivityEditProfileBinding binding;
    private SharedPreferences sharedPreferencesDefault;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        profileImage = binding.EditProfileImage;
        chooseImage = binding.selectProfilePhoto;
        editTextFullName = binding.EditNomeCompleto;
        save = binding.Save;

        sharedPreferencesDefault = PreferenceManager.getDefaultSharedPreferences(this);
        Picasso.get().load(sharedPreferencesDefault.getString("PROFILEIMG", null)).into(profileImage);
        editTextFullName.setText(sharedPreferencesDefault.getString("FULLNAME", null));
        imageUri = Uri.parse(sharedPreferencesDefault.getString("PROFILEIMG", null));


        chooseImage.setOnClickListener(v -> CropImage.activity().setFixAspectRatio(true).setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this));

        save.setOnClickListener(v -> {

            String nomeCompleto = editTextFullName.getText().toString().trim();

            if (nomeCompleto.isEmpty()) {
                editTextFullName.setError(getString(R.string.empty_name));
                editTextFullName.requestFocus();
                return;
            }

            if (nomeCompleto.length() > 20) {
                editTextFullName.setError(getString(R.string.long_name));
                editTextFullName.requestFocus();
                return;
            }

            if (imageUri.toString().equals(sharedPreferencesDefault.getString("PROFILEIMG", null))
                    && nomeCompleto.equals(sharedPreferencesDefault.getString("FULLNAME", null))) {
                Toast.makeText(EditProfileActivity.this, "No changes made", Toast.LENGTH_LONG).show();
                return;
            }

            EditUser(nomeCompleto);
        });
    }


    public void EditUser(String nomeCompleto){

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(R.layout.loading_dialog)
                .setCancelable(false)
                .create();
        dialog.show();

        //Se viene cambiata l'immagine, elimino la vecchia e inserisco la nuova, altrimenti faccio solo update user
        if (!imageUri.toString().equals(sharedPreferencesDefault.getString("PROFILEIMG", null))) {

            //delete della vecchia immagine
            StorageReference imageRef = FirebaseStorage.getInstance().getReference("Images").child("UserPhotos").getStorage()
                    .getReferenceFromUrl(sharedPreferencesDefault.getString("PROFILEIMG", null));
            imageRef.delete().addOnSuccessListener(unused -> {
                //insert nuova immagine
                StorageReference mStorage = FirebaseStorage.getInstance().getReference("Images");
                StorageReference imageFilePath = mStorage.child("UserPhotos").child(System.currentTimeMillis() + ".jpg");
                imageFilePath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();
                    UpdateUser(nomeCompleto, downloadUrl);
                });
            });
        }
        else {
            UpdateUser(nomeCompleto, null);
        }
    }

    public void UpdateUser(String nomeCompleto, Uri uploadUrl){

        if(uploadUrl != null)
            imageUri = uploadUrl;

        //update user
        User user = new User(nomeCompleto, sharedPreferencesDefault.getString("EMAIL", null), imageUri.toString());
        List<String> imagesLiked;
        Set<String> imageL = sharedPreferencesDefault.getStringSet("IMAGES_LIKED", null);
        imagesLiked = new ArrayList<>();
        imagesLiked.addAll(imageL);
        user.setFotoPiaciute(imagesLiked);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(user);

        Query query = FirebaseDatabase.getInstance().getReference("Images")
                .orderByChild("imageNameUser").equalTo((sharedPreferencesDefault.getString("FULLNAME", null)));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().child("imageNameUser").setValue(nomeCompleto);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        //update shared pref
        SharedPreferences.Editor editor = sharedPreferencesDefault.edit();
        editor.putString("FULLNAME", user.getFullName()).apply();
        editor.putBoolean("NAMECHANGED", true).apply();
        editor.putString("PROFILEIMG", imageUri.toString()).apply();
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profileImage.setImageURI(imageUri);
        }
    }
}