package com.maruf.hisabtool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.maruf.hisabtool.Database_Helper.DatabaseHelper;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class OPT_Verify_Activity extends AppCompatActivity {
    private EditText otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6;
    private Button btnVerifyLogin;
    private String mVerificationId, phoneNumber, userName, oldUserUid;
    private boolean isNewUser;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseHelper dbhelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opt_verify);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://hisabtool-a89e5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        dbhelper = new DatabaseHelper(this);

        phoneNumber = getIntent().getStringExtra("phone_num");
        userName = getIntent().getStringExtra("user_name");
        isNewUser = getIntent().getBooleanExtra("isNewUser", false);
        oldUserUid = getIntent().getStringExtra("oldUserUid");

        otpBox1 = findViewById(R.id.otpBox1);
        otpBox2 = findViewById(R.id.otpBox2);
        otpBox3 = findViewById(R.id.otpBox3);
        otpBox4 = findViewById(R.id.otpBox4);
        otpBox5 = findViewById(R.id.otpBox5);
        otpBox6 = findViewById(R.id.otpBox6);
        btnVerifyLogin = findViewById(R.id.btnVerifyLogin);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            startPhoneNumberVerification("+88" + phoneNumber);
        }

        btnVerifyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = otpBox1.getText().toString().trim() +
                        otpBox2.getText().toString().trim() +
                        otpBox3.getText().toString().trim() +
                        otpBox4.getText().toString().trim() +
                        otpBox5.getText().toString().trim() +
                        otpBox6.getText().toString().trim();

                if (code.length() < 6) {
                    Toast.makeText(OPT_Verify_Activity.this, "সঠিক ৬ ডিজিটের ওটিপি কোড দিন ভাই", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mVerificationId != null) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(OPT_Verify_Activity.this, "ওটিপি পাঠাতে ব্যর্থ হয়েছে ভাই!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    mVerificationId = verificationId;
                    Toast.makeText(OPT_Verify_Activity.this, "কোড পাঠানো হয়েছে ভাই", Toast.LENGTH_SHORT).show();
                }
            };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String finalUid = (oldUserUid != null && !oldUserUid.isEmpty()) ? oldUserUid : user.getUid();
                                String finalName = userName != null ? userName : "ব্যবহারকারী";
                                String formattedPhone = "+88" + phoneNumber;

                                // 🎯 ফিক্সড লজিক: ইউজার যদি একদম নতুন হয়, তবে ফায়ারবেস ক্লাউডে তার সম্পূর্ণ ডাটা রাইট করা হচ্ছে ভাই
                                if (isNewUser) {
                                    HashMap<String, Object> userMap = new HashMap<>();
                                    userMap.put("uid", finalUid);
                                    userMap.put("name", finalName);
                                    userMap.put("user_name", finalName);
                                    userMap.put("phone", formattedPhone);

                                    // ফায়ারবেস ডাটাবেজে সেভ করা হলো
                                    mDatabase.child(finalUid).setValue(userMap);
                                }

                                // পুরাতন SQLite লোকাল ডাটা টেবিল ক্লিয়ার মেকানিজম ভাই
                                try {
                                    if (dbhelper != null) {
                                        dbhelper.getWritableDatabase().execSQL("DELETE FROM customers");
                                        dbhelper.getWritableDatabase().execSQL("DELETE FROM history_due_table");
                                        dbhelper.getWritableDatabase().execSQL("DELETE FROM history_table");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                // লোকাল সেশন মেমোরি সেভ (বারবার লগইন ঠেকানোর ট্রিক)
                                SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.clear();
                                editor.putString("current_uid", finalUid);
                                editor.putString("firebase_uid", user.getUid());
                                editor.putString("current_phone", phoneNumber);
                                editor.putString("current_name", finalName);
                                editor.putString("last_user_id", finalUid);
                                editor.apply();

                                if (isNewUser) {
                                    Toast.makeText(OPT_Verify_Activity.this, "নতুন অ্যাকাউন্ট তৈরি সফল হয়েছে ভাই!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(OPT_Verify_Activity.this, "পুরাতন অ্যাকাউন্ট সফলভাবে ফেরত দেওয়া হয়েছে ভাই!", Toast.LENGTH_LONG).show();
                                }

                                Intent intent = new Intent(OPT_Verify_Activity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(OPT_Verify_Activity.this, "ভেরিফিকেশন কোড ভুল হয়েছে ভাই!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



    }
}