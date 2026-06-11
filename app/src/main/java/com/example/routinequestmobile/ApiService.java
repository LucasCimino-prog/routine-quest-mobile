package com.example.routinequestmobile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Aqui estamos avisando o Retrofit qual é a rota de Login no Spring Boot
    @POST("/users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginData);

    @POST("/users/register")
    Call<Void> registerUser(@Body RegisterRequest registerData);

    // Cria uma nova tarefa para um usuário específico
    @POST("/tasks/user/{userId}")
    Call<Task> createTask(@Path("userId") Long userId, @Body Task newTask);

    // Rota que busca TODAS as tarefas de um usuário específico
    @GET("/tasks/user/{userId}")
    Call<List<Task>> getUserTasks(@Path("userId") Long userId);

    @DELETE("/tasks/{taskId}")
    Call<Void> deleteTask(@Path("taskId") Long taskId);

    // Busca tarefas pelo nome
    @GET("/tasks/user/{userId}/search")
    Call<List<Task>> searchTasks(@Path("userId") Long userId, @Query("name") String name);

    // Atualiza os dados de uma tarefa existente
    @PUT("/tasks/{taskId}")
    Call<Task> updateTask(@Path("taskId") Long taskId, @Body Task taskDetails);

    // SISTEMA DE CONCLUSÃO DE MISSÃO
    @POST("/tasks/{taskId}/complete")
    Call<Task> completeTask(@Path("taskId") Long taskId);

    @GET("users/{id}/stats")
    Call<UserStats> getUserStats(@Path("id") Long userId);

    @POST("/tasks/{taskId}/fail")
    Call<Task> failTask(@Path("taskId") Long taskId);
}