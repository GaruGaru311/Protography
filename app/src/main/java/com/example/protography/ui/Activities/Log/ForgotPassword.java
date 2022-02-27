package com.example.protography.ui.Activities.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.example.protography.R;
import com.example.protography.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailEditText;
    private ActivityForgotPasswordBinding binding;
    private Button resetPasswordButton;
    

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);


        emailEditText = binding.email;
        resetPasswordButton = binding.resetButton;


        auth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }

            private void resetPassword() {

                String email = emailEditText.getText().toString().trim();

                // Messaggi d'errore
                String emailRequired = getString(R.string.email_required);
                String invalidEmail = getString(R.string.invalid_email_try_again);

                if(email.isEmpty()){
                    emailEditText.setError(emailRequired);
                    emailEditText.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailEditText.setError(invalidEmail);
                    emailEditText.requestFocus();
                    return;
                }


                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ForgotPassword.this, R.string.check_email, Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else
                            Toast.makeText(ForgotPassword.this, R.string.errore_retrieving_data, Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

    }

}