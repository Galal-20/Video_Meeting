package com.example.videomeeting.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomeeting.R;
import com.example.videomeeting.utilities.Constants;
import com.example.videomeeting.utilities.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SinginActivity extends AppCompatActivity {

    private EditText inputEmail , inputPassword;
    private MaterialButton buttonSignIn;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singin);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent i = new Intent(getApplicationContext() , MainActivity.class);
            startActivity(i);
            finish();
        }
        findViewById(R.id.signUp).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SingUpActivity.class));
        });

        inputEmail = findViewById(R.id.inputSignInEmail);
        inputPassword = findViewById(R.id.inputSignInPassword);
        buttonSignIn = findViewById(R.id.signInButton);



        buttonSignIn.setOnClickListener(v -> {
            if (inputEmail.getText().toString().trim().isEmpty()){
                Toast.makeText(SinginActivity.this, "Enter your Email", Toast.LENGTH_SHORT).show();
            }else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                Toast.makeText(SinginActivity.this, "Enter valid Email", Toast.LENGTH_SHORT).show();
            } else if (inputPassword.getText().toString().trim().isEmpty()){
                Toast.makeText(SinginActivity.this, "Enter your Password", Toast.LENGTH_SHORT).show();
            }else {
                signIn();
            }
        });
    }

    private void signIn(){

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD , inputPassword.getText().toString())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN , true);
                        preferenceManager.putString(Constants.KEY_USER_ID , documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME , documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_LAST_NAME , documentSnapshot.getString(Constants.KEY_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_EMAIL , documentSnapshot.getString(Constants.KEY_EMAIL));
                        Intent i = new Intent(getApplicationContext() , MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }else {
                        Toast.makeText(SinginActivity.this, "Check Email or Password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}