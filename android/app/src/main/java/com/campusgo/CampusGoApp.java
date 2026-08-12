package com.campusgo;

import android.app.Application;

import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.RetrofitClient;

public class CampusGoApp extends Application {

    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(this);
        RetrofitClient.init(sessionManager);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
