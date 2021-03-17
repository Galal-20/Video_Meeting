package com.example.videomeeting.Activities;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.videomeeting.R;
import com.example.videomeeting.utilities.Constants;
import com.example.videomeeting.utilities.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SingUpActivity extends AppCompatActivity {

    private EditText inputFirstName , inputLastName , inputEmail , inputPassword , inputConfirmPassword;
    private MaterialButton buttonSingUp;
    private ProgressBar singUpProgressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        preferenceManager = new PreferenceManager(getApplicationContext());

        findViewById(R.id.imageBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.signIn).setOnClickListener(v -> onBackPressed());

        inputFirstName = findViewById(R.id.inputFirstName);
        inputLastName = findViewById(R.id.inputLastName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonSingUp = findViewById(R.id.signUpButton);
        singUpProgressBar = findViewById(R.id.singUpProgressBar);

        buttonSingUp.setOnClickListener(v -> {
            if (inputFirstName.getText().toString().trim().isEmpty()){
                Toast.makeText(SingUpActivity.this, "Enter your First Name", Toast.LENGTH_SHORT).show();
            }else if (inputLastName.getText().toString().trim().isEmpty()){
                Toast.makeText(SingUpActivity.this, "Enter your Last Name", Toast.LENGTH_SHORT).show();
            }else if (inputEmail.getText().toString().trim().isEmpty()){
                Toast.makeText(SingUpActivity.this, "Enter your Email", Toast.LENGTH_SHORT).show();
            }else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                Toast.makeText(SingUpActivity.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
            }else if (inputPassword.getText().toString().trim().isEmpty()){
                Toast.makeText(SingUpActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
            }else if (inputConfirmPassword.getText().toString().trim().isEmpty()){
                Toast.makeText(SingUpActivity.this, "Enter Confirm Password", Toast.LENGTH_SHORT).show();
            }else if (!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())){
                Toast.makeText(SingUpActivity.this, "Password and ConfirmPassword must be same", Toast.LENGTH_SHORT).show();
            }else {
                singUp();
            }
        });
    }
    private void singUp(){
        buttonSingUp.setVisibility(View.VISIBLE);
        singUpProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String , Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME , inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME , inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD , inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN , true);
                    preferenceManager.putString(Constants.KEY_FIRST_NAME , inputFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME , inputLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL ,inputEmail.getText().toString());
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    singUpProgressBar.setVisibility(View.VISIBLE);
                    buttonSingUp.setVisibility(View.VISIBLE);
                    Toast.makeText(SingUpActivity.this, "Error, try again" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}