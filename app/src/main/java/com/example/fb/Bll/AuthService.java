package com.example.fb.Bll;


import android.content.Context;
import android.content.SharedPreferences;

import com.example.fb.Api.Facebook;
import com.example.fb.response.SignupResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class AuthService {

    boolean isSuccess = false;
    private Retrofit retrofit;
    private Facebook facebook;
    private SharedPreferences sharedPreferences;

    public AuthService(Context context) {
        this.sharedPreferences = context.getSharedPreferences("AUTH_STORAGE", MODE_PRIVATE);
        retrofit = new Retrofit.Builder().baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        facebook = retrofit.create(Facebook.class);
    }

    private static AuthService instance;
    public static AuthService getInstance(Context context){
        if(null == instance) instance = new AuthService(context);
        return instance;
    }

    private String token;
    private void setToken(String token){
        this.token = token;
       //.edit().putString("Auth_Token", token).apply();
    }

    public String getToken(){
       return token;// this.sharedPreferences.getString("Auth_Token", null);
    }
    public void logout(){
        this.setToken(null);
    }

    public boolean isUserLoggedIn(){
        return null != getToken();
    }

    public boolean checkUser(String email, String password) {
        Call<SignupResponse> usersCall = facebook.checkUser(email, password);
        try {
            Response<SignupResponse> loginResponse = usersCall.execute();
            if (loginResponse.isSuccessful() &&
                    loginResponse.body().getStatus().equals("Login success!")) {
                setToken("Bearer "+ loginResponse.body().getToken());
                isSuccess = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isSuccess;
    }
}