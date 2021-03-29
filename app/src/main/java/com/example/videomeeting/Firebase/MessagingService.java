package com.example.videomeeting.Firebase;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.videomeeting.Activities.Incoming_Invitation_Activity;
import com.example.videomeeting.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);

        if (type !=null){
            if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                Intent i = new Intent(getApplicationContext() , Incoming_Invitation_Activity.class);
                i.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );
                i.putExtra(
                        Constants.KEY_FIRST_NAME,
                        remoteMessage.getData().get(Constants.KEY_FIRST_NAME)
                );
                i.putExtra(
                        Constants.KEY_LAST_NAME,
                        remoteMessage.getData().get(Constants.KEY_LAST_NAME)
                );
                i.putExtra(
                        Constants.KEY_EMAIL,
                        remoteMessage.getData().get(Constants.KEY_EMAIL)
                );
                i.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                i.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                i.putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM ,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM)
                );
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                Intent i = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                i.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
            }
        }
    }
}
