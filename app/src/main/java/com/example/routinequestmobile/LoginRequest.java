package com.example.routinequestmobile;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // O Retrofit usa estes nomes para montar o JSON automaticamente
}