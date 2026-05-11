package com.example.routinequestmobile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    // Aqui estamos avisando o Retrofit qual é a rota de Login no Spring Boot
    @POST("/users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginData);
    @POST("/users/register")
    Call<Void> registerUser(@Body RegisterRequest registerData);

}