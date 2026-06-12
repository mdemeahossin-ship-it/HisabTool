package com.maruf.hisabtool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class New_User_Login_Activity extends AppCompatActivity {
    private TextInputEditText etRegisterName, etRegisterPhone;
    private Button btnRegisterSubmit;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_user_login);
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUid = sharedPref.getString("current_uid", null);
        if (currentUid != null && !currentUid.isEmpty()) {
            Intent intent = new Intent(New_User_Login_Activity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🎯 এখানে আপনার কাঙ্খিত মেইন ফিক্স! FirebaseAuth পারফেক্টলি ইনিশিয়েলাইজ করা হলো ভাই
        mAuth = FirebaseAuth.getInstance();

        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterPhone = findViewById(R.id.etRegisterPhone);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);

        mDatabase = FirebaseDatabase.getInstance("https://hisabtool-a89e5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        btnRegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = etRegisterName.getText().toString().trim();
                final String userPhone = etRegisterPhone.getText().toString().trim();

                if (userName.isEmpty()) {
                    etRegisterName.setError("আপনার নাম লিখুন ভাই");
                    return;
                }
                if (userPhone.isEmpty() || userPhone.length() != 11) {
                    etRegisterPhone.setError("সঠিক ১১ ডিজিটের নম্বর দিন ভাই");
                    return;
                }

                btnRegisterSubmit.setEnabled(false);
                final String formattedPhone = "+88" + userPhone;

                // ডাটাবেজে মোবাইল নম্বর চেক
                Query phoneQuery = mDatabase.orderByChild("phone").equalTo(formattedPhone);
                phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            btnRegisterSubmit.setEnabled(true);
                            // অলরেডি অ্যাকাউন্ট থাকলে পুরাতন লগইনে পাঠাতে বলবে
                            Toast.makeText(New_User_Login_Activity.this, "এই নম্বরে অলরেডি অ্যাকাউন্ট আছে ভাই! পুরাতন লগইন করুন।", Toast.LENGTH_LONG).show();
                        } else {
                            // নম্বর ইউনিক হলে একটি নতুন পুশ আইডি জেনারেট করে ওটিপি ভেরিফিকেশনে পাঠানো হবে ভাই
                            String generatedUid = mDatabase.push().getKey();
                            if (generatedUid == null) {
                                generatedUid = String.valueOf(System.currentTimeMillis());
                            }

                            btnRegisterSubmit.setEnabled(true);
                            Intent intent = new Intent(New_User_Login_Activity.this, OPT_Verify_Activity.class);
                            intent.putExtra("phone_num", userPhone);
                            intent.putExtra("user_name", userName);
                            intent.putExtra("oldUserUid", generatedUid); // জেনারেট করা আইডি ওটিপি পেজে পাস হলো
                            intent.putExtra("isNewUser", true); // নতুন ইউজার ফ্ল্যাগ ট্রু করা হলো ভাই
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnRegisterSubmit.setEnabled(true);
                        Toast.makeText(New_User_Login_Activity.this, "সার্ভার এরর! আবার ট্রাই করুন ভাই।", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}