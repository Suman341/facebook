package com.example.fb;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fb.Api.Facebook;
import com.example.fb.Bll.AuthService;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Retrofit retrofit;
    private Facebook facebook;

    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin, btnSignup;
    private String mytoken;
    private String loginEmail, loginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (AuthService.getInstance(this).isUserLoggedIn())
        {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.loginButton);
        btnSignup = findViewById(R.id.signUpButton);

        btnSignup.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
        getInstance();

    }

    private void getInstance() {

        retrofit = new Retrofit.Builder().baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        facebook = retrofit.create(Facebook.class);
    }

    private void loginApiUser(String email, String password) {
        AuthService loginBLL = AuthService.getInstance(this);

        StrictModeClass.StrictMode();
        if (loginBLL.checkUser(email, password)) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Either email or password is incorrect", Toast.LENGTH_SHORT).show();
            editTextEmail.requestFocus();
        }


    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.signUpButton) {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        }

        if (view.getId() == R.id.loginButton) {
            loginEmail = editTextEmail.getText().toString();
            loginPassword = editTextPassword.getText().toString();

            if (validate()) {
                loginApiUser(loginEmail, loginPassword);
            }
        }
    }


    private boolean validate() {

        if (TextUtils.isEmpty(loginEmail)) {
            editTextEmail.setError("Enter a email");
            editTextEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {
            editTextEmail.setError("Invalid email format");
            editTextEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(loginPassword)) {
            editTextPassword.setError("Enter a password");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }
}
