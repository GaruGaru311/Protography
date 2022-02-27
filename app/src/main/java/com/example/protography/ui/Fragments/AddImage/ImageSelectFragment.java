package com.example.protography.ui.Fragments.AddImage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageView;

import android.widget.Toast;

import com.example.protography.R;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;
import com.example.protography.databinding.FragmentImageSelectBinding;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;


public class ImageSelectFragment extends Fragment implements View.OnClickListener, Step {

    ImageView selectedImage;
    Button reselectBtn;
    private Uri imageUri;
    SharedPreferences sharedPref;
    private FragmentImageSelectBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentImageSelectBinding.inflate(inflater, container, false);
        selectedImage = binding.image;
        reselectBtn = binding.reselectBtn;
        reselectBtn.setOnClickListener(this);

        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            selectedImage.setImageURI(imageUri);
            reselectBtn.setText(R.string.select_another);
        }

        if(imageUri != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("IMAGEURI", imageUri.toString());
            editor.apply();
        }
    }

    @Override
    public void onClick(View v) {
        CropImage.activity().start(getContext(), this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(imageUri != null)
            reselectBtn.setText(R.string.select_another);

        selectedImage.setImageURI(Uri.parse(sharedPref.getString("IMAGEURI", "DEFAULT")));
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public VerificationError verifyStep() {
        if(imageUri == null)
           return new VerificationError(getString(R.string.you_must_select_image));
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        selectedImage.setImageResource(0);
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull @NotNull VerificationError error) {
        Toast.makeText(this.getContext(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
}