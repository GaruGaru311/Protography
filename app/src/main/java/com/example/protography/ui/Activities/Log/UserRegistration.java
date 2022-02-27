package com.example.protography.ui.Activities.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.protography.R;
import com.example.protography.databinding.ActivityUserRegistrationBinding;
import com.example.protography.ui.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

public class UserRegistration extends AppCompatActivity {


    private TextView registerUser;
    private EditText editTextFullName, editTextEmail, editTextPassword, repeatPassword;
    private ShapeableImageView profileImage, chooseImage;
    private ActivityUserRegistrationBinding binding;
    private FirebaseAuth mAuth;
    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserRegistrationBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);
        mAuth = FirebaseAuth.getInstance();

        registerUser = binding.register;
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        editTextFullName = binding.NomeCompleto;
        editTextEmail = binding.registerAddress;
        editTextPassword = binding.registerPassword;
        profileImage = binding.profileImage;
        chooseImage = binding.selectProfilePhoto;
        repeatPassword = binding.confrimRegisterPassword;
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setFixAspectRatio(true).setCropShape(CropImageView.CropShape.OVAL).start(UserRegistration.this);
            }
        });
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

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String repeatedPassword = repeatPassword.getText().toString().trim();
        String nomeCompleto = editTextFullName.getText().toString().trim();

        // Messaggi d'errore
        String emptyName = getString(R.string.empty_name);
        String longName = getString(R.string.long_name);
        String emptyPassword = getString(R.string.password_empty);
        String emptyMail = getString(R.string.empty_email);
        String invalidEmail = getString(R.string.invalid_email);
        String passwordLenght = getString(R.string.password_at_least_six);
        String differentPsw = getString(R.string.differentPsw);


        if (nomeCompleto.isEmpty()) {
            editTextFullName.setError(emptyName);
            editTextFullName.requestFocus();
            return;
        }

        if (nomeCompleto.length() > 20) {
            editTextFullName.setError(longName);
            editTextFullName.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError(emptyPassword);
            editTextPassword.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            editTextEmail.setError(emptyMail);
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(invalidEmail);
            editTextEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError(passwordLenght);
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(repeatedPassword)) {
            repeatPassword.setError(differentPsw);
            repeatPassword.requestFocus();
            return;
        }

        if (imageUri == null){
            imageUri = Uri.parse("android.resource://"+ this.getPackageName()+"/drawable/blank_profile_picture");
        }

        AlertDialog dialog =  new AlertDialog.Builder(this)
                .setView(R.layout.loading_dialog)
                .setCancelable(false)
                .create();
        dialog.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            StorageReference mStorage = FirebaseStorage.getInstance().getReference("Images").child("UserPhotos");
                            StorageReference imageFilePath = mStorage.child(System.currentTimeMillis() + ".jpg");
                            imageFilePath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {

                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!urlTask.isSuccessful());
                                Uri downloadUrl = urlTask.getResult();
                                User user = new User(nomeCompleto, email, downloadUrl.toString());

                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user);

                                dialog.dismiss();
                                Toast.makeText(UserRegistration.this, getString(R.string.success_registration), Toast.LENGTH_LONG).show();
                                finish();
                            });
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(UserRegistration.this, getString(R.string.registration_failed) + ": " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}