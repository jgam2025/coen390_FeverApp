package com.example.coen390_feverapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    Button btnlogin;
    Button btnSignup;
    DBHelper dbHelper;
    EditText etUsername, etPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dbHelper= new DBHelper(this);
        etUsername=findViewById(R.id.etUsername);
        etPwd=findViewById(R.id.etPassword);
        btnlogin=findViewById(R.id.btnlogin);
        btnSignup = findViewById(R.id.btnSignup);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLoggedId = dbHelper.checkUser(etUsername.getText().toString(),etPwd.getText().toString());
                if(isLoggedId){
                    Intent intent = new Intent(LoginActivity.this, TemperatureMeasurementPage.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(LoginActivity.this, "login failed ",Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }
}