package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.text.InputType;
import android.widget.CheckBox;


import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    EditText etUser,etPwd,etRepwd;
    Button btnRegister;
    ImageView imageViewArrow;
    DBHelper dbHelper;
    CheckBox cbShowPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etUser=findViewById(R.id.editTextText);
        etPwd=findViewById(R.id.editTextText2);
        etRepwd=findViewById(R.id.editTextText3);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        btnRegister=findViewById(R.id.button1);
        dbHelper= new DBHelper(this);
        imageViewArrow=findViewById(R.id.imageViewArrowScanPage2);


        imageViewArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        //hide password
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                etRepwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                etRepwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

            etPwd.setSelection(etPwd.length());
            etRepwd.setSelection(etRepwd.length());
        });

        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String user, pwd, rePwd;
                user = etUser.getText().toString();
                pwd = etPwd.getText().toString();
                rePwd = etRepwd.getText().toString();
                if (user.equals("")||pwd.equals("")||rePwd.equals("")){
                    Toast.makeText(RegisterActivity.this, "Please Fill In All The Fields",Toast.LENGTH_LONG).show();
                }
                else{
                    if(pwd.equals(rePwd)){
                        if(dbHelper.checkUsername(user)){
                            Toast.makeText(RegisterActivity.this, "User Already Exists",Toast.LENGTH_LONG).show();
                            return;
                        }
                       boolean registeredSuccess=dbHelper.insertData(user,pwd);
                       if(registeredSuccess){
                           Toast.makeText(RegisterActivity.this, "User Registered Successfully",Toast.LENGTH_LONG).show();
                           SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);//save this registration
                           SharedPreferences.Editor editor = sharedPrefs.edit();
                           editor.putString("current_user",user);
                           editor.apply();
                           int userID = dbHelper.getUserID(user);
                           Profile profile = new Profile(user,userID);
                           dbHelper.insertProfile(profile);

                           Intent intent = new Intent(RegisterActivity.this, BaseActivity.class);
                           startActivity(intent);
                           finish();
                       }
                       else {
                           Toast.makeText(RegisterActivity.this, "User Registration Failed",Toast.LENGTH_LONG).show();
                       }
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Passwords Do Not Match",Toast.LENGTH_LONG).show();
                    }

                }

            }
        });

    }

}