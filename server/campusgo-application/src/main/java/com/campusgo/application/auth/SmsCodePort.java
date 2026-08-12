package com.campusgo.application.auth;

public interface SmsCodePort {

    void send(String phone, String scene);

    boolean verify(String phone, String scene, String code);
}
