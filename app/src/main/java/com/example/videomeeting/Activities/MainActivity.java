package com.example.videomeeting.Activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.videomeeting.R;
import com.example.videomeeting.adapters.UserAdapter;
import com.example.videomeeting.listeners.UsersListener;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utilities.Constants;
import com.example.videomeeting.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements UsersListener {

    private TextView textView , signOut;
    private PreferenceManager preferenceManager;
    private AlertDialog dialogDelete;
    private List<User> users;
    private UserAdapter usersAdapter;
    private TextView textErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageConference;
    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(getApplicationContext());
        imageConference = findViewById(R.id.imageConference);
        textView = findViewById(R.id.nameTitle);
        textView.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        findViewById(R.id.textSignOut).setOnClickListener(v -> {
            signout();
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() !=null){
                sendFCMTokenToDatabase(task.getResult());
            }
        });

        RecyclerView usersRecyclerView = findViewById(R.id.usersRecyclerView);
        textErrorMessage = findViewById(R.id.textErrorMessage);
        users = new ArrayList<>();
        usersAdapter = new UserAdapter(users , this);
        usersRecyclerView.setAdapter(usersAdapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);
        getUsers();

        checkForBatteryOptimizations();

       /* FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() !=null){
                    sendFCMTokenToDatabase(task.getResult().getToken());
                }
            }
        });*/
    }

    private void getUsers(){
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database  = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task -> {
            swipeRefreshLayout.setRefreshing(false);
            String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
            if (task.isSuccessful() && task.getResult() !=null){
                users.clear();
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                    if (myUserId.equals(documentSnapshot.getId())){
                        continue; //display the user list expect for the currently signed in user, so  no one will have a meeting with himself , we are excluding a signed in user from the list
                    }
                    User user = new User();
                    user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                    user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                    user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                    user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                    users.add(user);
                }
                if (users.size() > 0){
                    usersAdapter.notifyDataSetChanged();
                }else {
                    textErrorMessage.setText(String.format("%s" , "No users available"));
                    textErrorMessage.setVisibility(View.VISIBLE);
                }
            }else {
                textErrorMessage.setText(String.format("%s" , "No users available"));
                textErrorMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sendFCMTokenToDatabase(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN , token)
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Token updated failure"+ e.getMessage(), LENGTH_SHORT).show());
    }

    private void signout(){
        if (dialogDelete == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View v = LayoutInflater.from(this).inflate(
                    R.layout.dialog_sign_out ,
                    (ViewGroup) findViewById(R.id.layoutDeleteContainer)
            );
            builder.setView(v);
            dialogDelete = builder.create();
            if (dialogDelete.getWindow() != null){
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            v.findViewById(R.id.textyes).setOnClickListener(v12 -> {
                Toast.makeText(MainActivity.this, "Signing out...", LENGTH_SHORT).show();
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
                HashMap<String  , Object> update = new HashMap<>();
                update.put(Constants.KEY_FCM_TOKEN , FieldValue.delete());
                documentReference.update(update).
                        addOnSuccessListener(aVoid -> {
                            preferenceManager.clearPreferences();
                            startActivity(new Intent(getApplicationContext() , SinginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable to Sign Out pleas Check your internet" + e.getMessage(), LENGTH_SHORT).show());
            });
            v.findViewById(R.id.textCancel).setOnClickListener(v1 -> dialogDelete.dismiss());
        }
        dialogDelete.show();

    }

    @Override
    public void initialVideoMeeting(User user) {
        if (user.token == null || user.token.trim().isEmpty()){
            Toast.makeText(this,
                    user.firstName + " " + user.lastName + "is not available for meeting ",
                    Toast.LENGTH_SHORT).show();
        }else {
            Intent i = new Intent(getApplicationContext() , Outgoing_Invitation_Activity.class);
            i.putExtra("user" , user);
            i.putExtra("type" , "video");
            startActivity(i);
        }

    }

    @Override
    public void initialAudioMeeting(User user) {
        if (user.token == null || user.token.trim().isEmpty()){
            Toast.makeText(this,
                    user.firstName + " " + user.lastName + " is not available for meeting",
                    Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(getApplicationContext(), Outgoing_Invitation_Activity.class);
            intent.putExtra("user" , user);
            intent.putExtra("type" , "audio");
            startActivity(intent);
        }
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected){
            imageConference.setVisibility(View.VISIBLE);
            imageConference.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext() , Outgoing_Invitation_Activity.class);
                intent.putExtra("selectedUsers" , new Gson().toJson(usersAdapter.getSelectedUsers()));
                intent.putExtra("type" , "video");
                intent.putExtra("isMultiple" , true);
                startActivity(intent);
            });
        }else {
            imageConference.setVisibility(View.GONE);
        }
    }

    private void checkForBatteryOptimizations(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled. It can interrupt running background services");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent,REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS){
            checkForBatteryOptimizations();
        }
    }
}