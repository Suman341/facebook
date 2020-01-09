package com.example.fb;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.example.fb.Api.Facebook;
import com.example.fb.Model.ApiUser;
import com.example.fb.response.ImageResponse;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private EditText editTextD, editTextF, editTextL, editTextE, editTextP;
    private RadioGroup radioGender;
    private Calendar calendarInstance;
    private DatePickerDialog.OnDateSetListener dateOfBirthPicker;
    private Button signUpButton;
    private TextView loginLink;
    private ImageView userProfileImageView;
    private Toolbar toolbar;

    // Data store
    private String firstName, lastName, email, password, dateOfBirth, gender, profileImagePath;

    private Retrofit retrofit;
    private Facebook facebook;
    private String imageName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editTextF = findViewById(R.id.firstname);
        editTextL = findViewById(R.id.lastname);
        editTextE = findViewById(R.id.email);
        editTextP = findViewById(R.id.password);
        editTextD = findViewById(R.id.dob);
        signUpButton = findViewById(R.id.btnsignup);
        loginLink = findViewById(R.id.btnloginlink);

        userProfileImageView = findViewById(R.id.profileImage);


        radioGender = findViewById(R.id.gender);


        editTextD.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        loginLink.setOnClickListener(this);
        radioGender.setOnCheckedChangeListener(this);


        userProfileImageView.setOnClickListener(this);


        calendarInstance = Calendar.getInstance();
        dateOfBirthPicker = (datePicker, i, i1, i2) -> {
            calendarInstance.set(Calendar.YEAR, i);
            calendarInstance.set(Calendar.MONTH, i1);
            calendarInstance.set(Calendar.DAY_OF_MONTH, i2);
            String mydateFormat = "dd-MM-y";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mydateFormat, Locale.getDefault());
            editTextD.setText(simpleDateFormat.format(calendarInstance.getTime()));
        };

        getInstance();

    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        if (i == R.id.male) {
            gender = "Male";
        }
        if (i == R.id.female) {
            gender = "Female";
        }
        if (i == R.id.other) {
            gender = "Other";
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.dob) {
            new DatePickerDialog(SignupActivity.this, dateOfBirthPicker, calendarInstance.get(Calendar.YEAR), calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)).show();
        }


        if (view.getId() == R.id.btnsignup) {

            firstName = editTextF.getText().toString();
            lastName = editTextL.getText().toString();
            dateOfBirth = editTextD.getText().toString();
            email = editTextE.getText().toString();
            password = editTextP.getText().toString();


            if (validate()) {
                saveImageOnly();

                ApiUser user = new ApiUser(firstName, lastName, email, password, dateOfBirth, gender, imageName);
                userRegister(user);
            }
        }

        if (view.getId() == R.id.btnloginlink) {
            Intent loginpage = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(loginpage);
        }


        if (view.getId() == R.id.profileImage) {
            if (askPermission()){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Please select an image ", Toast.LENGTH_SHORT).show();
            }else{
                Uri uri = data.getData();
                userProfileImageView.setImageURI(uri);
                profileImagePath = getRealPathFromUri(uri);
            }
        }else{
            askPermission();
        }
    }


    public boolean askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
        return true;
    }


    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(),
                uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(colIndex);
        cursor.close();
        return result;
    }


    private void saveImageOnly() {
        File file = new File(profileImagePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("profileimage",
                file.getName(), requestBody);

        Call<ImageResponse> responseBodyCall = facebook.uploadProfileImage(body);
        StrictModeClass.StrictMode();

        //Synchronous methid
        try {
            Response<ImageResponse> imageResponseResponse = responseBodyCall.execute();
            imageName = imageResponseResponse.body().getFilename();
//            Toast.makeText(this, "Profile image inserted " + imageName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private boolean validate() {


        if (TextUtils.isEmpty(firstName)) {
            editTextF.setError("Enter a first name");
            editTextF.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            editTextL.setError("Enter a last name");
            editTextL.requestFocus();
            return false;
        }


        if (TextUtils.isEmpty(email)) {
            editTextE.setError("Enter a email");
            editTextE.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextE.setError("Invalid email format");
            editTextE.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextP.setError("Enter a password");
            editTextP.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(dateOfBirth)) {
            editTextD.setError("Enter a date of birth");
            Toast.makeText(this, "Enter a date of birth", Toast.LENGTH_SHORT).show();
            editTextD.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void getInstance() {
        retrofit = new Retrofit.Builder().baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        facebook = retrofit.create(Facebook.class);
    }

    private void userRegister(ApiUser apiUser) {

        Call<Void> userinsert = facebook.registerUser(apiUser);

        userinsert.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {

                    Toast.makeText(SignupActivity.this, "User has been successfully created", Toast.LENGTH_SHORT).show();

                    editTextF.setText(null);
                    editTextL.setText(null);
                    editTextE.setText(null);
                    editTextP.setText(null);
                    editTextD.setText(null);
                    radioGender.clearCheck();

                    // redirect back to login screen aster successful registration
                    finish();
                } else {

                    switch (response.code()) {
                        case 500:
                            Toast.makeText(SignupActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                            break;


                    }
                }

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("Ex", t.getMessage());

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
