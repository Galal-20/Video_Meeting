package com.example.videomeeting.listeners;

import com.example.videomeeting.models.User;

public interface UsersListener {

    void initialVideoMeeting(User user);
    void initialAudioMeeting(User user);
    void onMultipleUsersAction(Boolean isMultipleUsersSelected);
}
