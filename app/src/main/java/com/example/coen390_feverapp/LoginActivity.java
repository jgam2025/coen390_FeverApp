package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.InputType;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {
    Button btnlogin;
    Button btnSignup;
    DBHelper dbHelper;
    EditText etUsername, etPwd;
    CheckBox cbShowPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dbHelper= new DBHelper(this);
        etUsername=findViewById(R.id.etUsername);
        etPwd=findViewById(R.id.etPassword);
        btnlogin=findViewById(R.id.btnlogin);
        btnSignup = findViewById(R.id.btnSignup);
        cbShowPassword = findViewById(R.id.cbShowPassword);

        //allows user to login if they have created an account
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLoggedId = dbHelper.checkUser(etUsername.getText().toString(),etPwd.getText().toString());
                if(isLoggedId){
                    String username = etUsername.getText().toString();
                    SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("current_user",username);
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, BaseActivity.class);
                    startActivity(intent);
                }
                else{
                    //failed login if user has not created account
                    Toast.makeText(LoginActivity.this, "Login Failed ",Toast.LENGTH_LONG).show();
                }
            }
        });

        //gives users the option to show or hide their password when entering
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPwd.setSelection(etPwd.length());
        });
        Button btnPrivacy = findViewById(R.id.btnPrivacy);
        btnPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, PrivacySecurityActivity.class);
            startActivity(intent);
        });

        //redirects users to register page where they can create an account
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}