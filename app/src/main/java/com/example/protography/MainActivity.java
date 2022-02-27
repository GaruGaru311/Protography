package com.example.protography;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.protography.databinding.ActivityMainBinding;
import com.example.protography.ui.Models.Image;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final String ADD_ACTIVITY = "isAddActivity";
    private SharedPreferences sharedPreferences;
    BottomNavigationView navView;
    private String mailUser;
    private String nameUser;
    private List<Image> allImages;
    private boolean justLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allImages = new ArrayList<>();

        if (savedInstanceState != null) {

            allImages = savedInstanceState.getParcelableArrayList("ALL_IMAGES");
            justLogged = false;
        }
        else {
            justLogged = true;

            // Ricerco tutte le immagini
            Query query = FirebaseDatabase.getInstance().getReference("Images");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    allImages.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Image image = dataSnapshot.getValue(Image.class);
                        allImages.add(image);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "Errore caricamento immagini: " + error.getMessage());
                }
            });
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);



        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mailUser = sharedPreferences.getString("EMAIL", null);
        nameUser = sharedPreferences.getString("FULLNAME", null);
        if (justLogged)
            Toast.makeText(MainActivity.this, getString(R.string.welcome) + " " + nameUser, Toast.LENGTH_LONG).show();



        navView = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(navView, navController);
        navView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);
    }

    //per evitare la rigenerazione del fragment sulla selezione del fragment già attivo
    private BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReselectedListener
            = item -> {
                //on item reselected do nothing
            };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("ALL_IMAGES", (ArrayList<Image>) allImages);
    }

    @Override
    protected void onResume() {
        super.onResume();


        //altrimenti tornando dall'attività di aggiunta sarebbe selezionato un item non corrispondente al fragment aperto.
        boolean isAddActivity = sharedPreferences.getBoolean(ADD_ACTIVITY, false);
        if (isAddActivity) {
            navView.setSelectedItemId(R.id.navigation_search);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(ADD_ACTIVITY);
            editor.commit();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();

        if (navView.getSelectedItemId() == R.id.navigation_add) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ADD_ACTIVITY, true);
            editor.commit();
        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(R.string.want_to_exit)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).create().show();
    }

    public List<Image> getAllImages() {
        return allImages;
    }


}