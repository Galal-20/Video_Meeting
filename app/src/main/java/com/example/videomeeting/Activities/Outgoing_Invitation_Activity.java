package com.example.videomeeting.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.videomeeting.R;
import com.example.videomeeting.models.User;
import com.example.videomeeting.network.ApiClient;
import com.example.videomeeting.network.ApiService;
import com.example.videomeeting.utilities.Constants;
import com.example.videomeeting.utilities.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Outgoing_Invitation_Activity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String meetingRoom = null;
    private String meetingType = null;

    private TextView textFirstChar;
    private TextView textUserName;
    private TextView textEmail;
    
    private int rejectionCount = 0;
    private int totalReceivers = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing__invitation_);

        preferenceManager = new PreferenceManager(getApplicationContext());

        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        meetingType = getIntent().getStringExtra("type");

        if (meetingType != null){
            if (meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }else {
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        }

        textFirstChar = findViewById(R.id.textFirstChar);
        textUserName = findViewById(R.id.textUsername);
        textEmail = findViewById(R.id.textEmail);

        User user =(User) getIntent().getSerializableExtra("user");
        if (user != null){
            textFirstChar.setText(user.firstName.substring(0,1));
            textUserName.setText(String.format("%s %s" , user.firstName , user.lastName));
            textEmail.setText(user.email);
        }

        ImageView imageStopInvitation = findViewById(R.id.imageStopInvitation);
        imageStopInvitation.setOnClickListener(v -> {
            if (getIntent().getBooleanExtra("isMultiple" , false)) {
                Type type = new TypeToken<ArrayList<User>>() {
                }.getType();
                ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                if (receivers != null){
                    totalReceivers = receivers.size();
                }
                cancelInvitation( null, receivers);
            }else {
                if (user != null){
                    totalReceivers = 1;
                    cancelInvitation(user.token , null);
                }
            }


        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() !=null){
                inviterToken = task.getResult();
                if (meetingType != null){
                    if (getIntent().getBooleanExtra("isMultiple" , false)){
                        Type type = new TypeToken<ArrayList<User>>(){}.getType();
                        ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                        if (receivers != null){
                            totalReceivers = receivers.size();
                        }
                        initiateMeeting(meetingType , null , receivers);
                    }else {
                        if (user !=null){
                            totalReceivers = 1;
                            initiateMeeting(meetingType , user.token, null);
                        }
                    }
                }
            }
        });
    }

    private void initiateMeeting(String meetingType , String receiverToken , ArrayList<User> receivers){
        try {
            JSONArray tokens = new JSONArray();

            if (receiverToken != null){
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0){
                StringBuilder userNames = new StringBuilder();
                for (int i = 0 ; i <receivers.size(); i++){
                    tokens.put(receivers.get(i).token);
                    userNames.append(receivers.get(i).firstName).append(" ").append(receivers.get(i).lastName).append("\n");
                }
                textFirstChar.setVisibility(View.GONE);
                textEmail.setVisibility(View.GONE);
                textUserName.setText(userNames.toString());
            }



            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE ,meetingType);
            data.put(Constants.KEY_FIRST_NAME , preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME ,  preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            meetingRoom = preferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                    UUID.randomUUID().toString().substring(0,5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM , meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody , String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(),
                remoteMessageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                        Toast.makeText(Outgoing_Invitation_Activity.this, "Calling", Toast.LENGTH_SHORT).show();
                    }else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(Outgoing_Invitation_Activity.this, "invitation canceled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else {
                    Toast.makeText(Outgoing_Invitation_Activity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(Outgoing_Invitation_Activity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
    private void cancelInvitation(String receiverToken , ArrayList<User> receivers){
        try {
            JSONArray tokens = new JSONArray();
            if (receiverToken != null){
                tokens.put(receiverToken);
            }
            if (receivers != null && receivers.size() > 0){
                for (User user : receivers){
                    tokens.put(user.token);
                }
            }


            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();
            data.put(Constants.REMOTE_MSG_TYPE , Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCEL);
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString() , Constants.REMOTE_MSG_INVITATION_RESPONSE);

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    try {
                        URL serverURL= new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new  JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals("audio")){
                            builder.setAudioOnly(true);
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(Outgoing_Invitation_Activity.this , builder.build());
                        finish();
                    }catch (Exception e){
                        Toast.makeText(Outgoing_Invitation_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    rejectionCount +=1;
                    if (rejectionCount == totalReceivers){
                        Toast.makeText(context, "invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(invitationResponseReceiver);
    }
}