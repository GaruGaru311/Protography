package com.example.protography.ui.ViewModels;


import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.protography.ui.Models.Image;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MapsViewModel extends ViewModel {
    private static final String TAG = "MapsViewModel";
    private MutableLiveData<List<Image>> images;
    private DatabaseReference mDatabaseRef;

    public MutableLiveData<List<Image>> getImages() {
        if (images == null){
            images = new MutableLiveData<List<Image>>();
            loadImages();
        }
        return images;
    }

    private void loadImages(){
        List<Image> listImages = new ArrayList<Image>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Images");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Si svuota per evitare di avere le immagini vecchie duplicate
                listImages.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Image image = postSnapshot.getValue(Image.class);
                    listImages.add(image);
                }
                images.setValue((List<Image>)listImages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}