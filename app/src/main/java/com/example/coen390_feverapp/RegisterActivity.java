package com.example.coen390_feverapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    EditText etUser,etPwd,etRepwd;
    Button btnRegister;
    ImageView imageViewArrow;
    DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etUser=findViewById(R.id.editTextText);
        etPwd=findViewById(R.id.editTextText2);
        etRepwd=findViewById(R.id.editTextText3);
        btnRegister=findViewById(R.id.button1);
        dbHelper= new DBHelper(this);
        imageViewArrow=findViewById(R.id.imageViewArrow);


        imageViewArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String user, pwd, rePwd;
                user = etUser.getText().toString();
                pwd = etPwd.getText().toString();
                rePwd = etRepwd.getText().toString();
                if (user.equals("")||pwd.equals("")||rePwd.equals("")){
                    Toast.makeText(RegisterActivity.this, "please fill all the fields",Toast.LENGTH_LONG).show();
                }
                else{
                    if(pwd.equals(rePwd)){
                        if(dbHelper.checkUsername(user)){
                            Toast.makeText(RegisterActivity.this, "User Already Exist",Toast.LENGTH_LONG).show();
                            return;
                        }
                       boolean registeredSuccess=dbHelper.insertData(user,pwd);
                       if(registeredSuccess){
                           Toast.makeText(RegisterActivity.this, "User Registered Successfully ",Toast.LENGTH_LONG).show();
                           Intent intent = new Intent(RegisterActivity.this, TemperatureMeasurementPage.class);
                           startActivity(intent);
                           finish();
                       }
                       else {
                           Toast.makeText(RegisterActivity.this, "User Registered Failed ",Toast.LENGTH_LONG).show();
                       }
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "try again passwords doesn't match ",Toast.LENGTH_LONG).show();
                    }

                }

            }
        });

    }

}