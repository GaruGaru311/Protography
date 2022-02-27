package com.example.protography.ui.Activities.Log;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.protography.MainActivity;
import com.example.protography.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;


public class LaunchScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String appTheme = sharedPref.getString("THEME", null);

        if (appTheme != null)
        {
            if (appTheme.equals("DARK"))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else if (appTheme.equals("LIGHT"))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);

        chooseActivityToLaunch();
    }

    private void chooseActivityToLaunch() {
        String mail = sharedPref.getString("EMAIL", null);
        String password = sharedPref.getString("PSW", null);

        if (password != null){
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LaunchScreenActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LaunchScreenActivity.this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LaunchScreenActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            });
        }
        else {
            startActivity(new Intent(LaunchScreenActivity.this, LoginActivity.class));
            finish();
        }
    }
}