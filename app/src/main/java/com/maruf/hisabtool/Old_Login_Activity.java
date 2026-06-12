package com.maruf.hisabtool;

import static android.app.PendingIntent.getActivity;
import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class Old_Login_Activity extends AppCompatActivity {
    private TextInputEditText etMobileNumber;
    private Button btnProceed;

    private TextView tvCreateNewAccount,Language;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_old);
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUid = sharedPref.getString("current_uid", null);

        if (currentUid != null && !currentUid.isEmpty()) {
            // ✅ ইউজার আগে একবার লগইন করে থাকলে সরাসরি MainActivity-তে চলে যাবে ভাই!
            Intent intent = new Intent(Old_Login_Activity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // নিচের বাকি কোডগুলো যেন রান হতে না পারে তাই রিটার্ন
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        etMobileNumber = findViewById(R.id.etMobileNumber);
        btnProceed = findViewById(R.id.btnProceed);
        tvCreateNewAccount = findViewById(R.id.tvCreateNewAccount);
        Language = findViewById(R.id.Language);

        mDatabase = FirebaseDatabase.getInstance("https://hisabtool-a89e5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        tvCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Old_Login_Activity.this, New_User_Login_Activity.class);
                startActivity(intent);
            }
        });




        if (Language != null) {
            Language.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 📝 ভাষার নামের সাথে দেশের ফ্ল্যাগ ইমোজি যোগ করা হয়েছে ভাই
                    String[] languages = {
                            "🇬🇧  English",
                            "🇧🇩  বাংলা (Bengali)",
                            "🇮🇳  हिंदी (Hindi)",
                            "🇵🇰  اردو (Urdu)",
                            "🇸🇦  العربية (Arabic)",
                            "🇳🇵  नेपाली (Nepali)"
                    };

                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
                    builder.setTitle("Select Language / ভাষা নির্বাচন করুন");

                    builder.setItems(languages, new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            // লিস্টের সিরিয়াল অনুযায়ী আগের মতোই কাজ করবে ভাই
                            if (which == 0) {
                                setAppLanguage("en"); // English
                            } else if (which == 1) {
                                setAppLanguage("bn"); // Bangla
                            } else if (which == 2) {
                                setAppLanguage("hi"); // Hindi
                            } else if (which == 3) {
                                setAppLanguage("ur"); // Urdu
                            } else if (which == 4) {
                                setAppLanguage("ar"); // Arabic
                            } else if (which == 5) {
                                setAppLanguage("ne"); // Nepali
                            }
                        }
                    });

                    builder.show();
                }
            });
        }





        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userPhone = etMobileNumber.getText().toString().trim();

                if (userPhone.isEmpty()) {
                    etMobileNumber.setError("মোবাইল নম্বর লিখুন ভাই");
                    return;
                }
                if (userPhone.length() != 11) {
                    etMobileNumber.setError("সঠিক ১১ ডিজিটের নম্বর দিন ভাই");
                    return;
                }

                btnProceed.setEnabled(false);
                final String formattedPhone = "+88" + userPhone;

                // ডাটাবেজে মোবাইল নম্বরটি ম্যাচ করানো হচ্ছে
                Query query = mDatabase.orderByChild("phone").equalTo(formattedPhone);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String dbUid = "";
                            String dbName = "";
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                dbUid = userSnapshot.getKey();
                                dbName = userSnapshot.child("name").getValue(String.class);
                                break;
                            }

                            btnProceed.setEnabled(true);
                            // ডাটাবেজে নম্বর থাকলে পুরাতন ডাটা উদ্ধারের জন্য ওটিপি ভেরিফিকেশনে পাঠানো হলো
                            Intent intent = new Intent(Old_Login_Activity.this, OPT_Verify_Activity.class);
                            intent.putExtra("phone_num", userPhone);
                            intent.putExtra("user_name", dbName);
                            intent.putExtra("oldUserUid", dbUid);
                            intent.putExtra("isNewUser", false);
                            startActivity(intent);
                            finish();
                        } else {
                            // ❌ ডাটাবেজে নম্বর না থাকলে ওটিপি পাঠানো ব্লক
                            btnProceed.setEnabled(true);
                            Toast.makeText(Old_Login_Activity.this, "এই নম্বরে কোনো অ্যাকাউন্ট নেই ভাই! দয়া করে নতুন অ্যাকাউন্ট খুলুন।", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnProceed.setEnabled(true);
                        // আপনার তৈরি করা ক্লাউড রুলসের ব্যাকআপ সেফটি লুপ ভাই
                        Intent intent = new Intent(Old_Login_Activity.this, OPT_Verify_Activity.class);
                        intent.putExtra("phone_num", userPhone);
                        intent.putExtra("isNewUser", false);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });




    }




    private void setAppLanguage(String langCode) {
        java.util.Locale locale = new java.util.Locale(langCode);
        java.util.Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // 🔄 অ্যাপের মেইন স্ক্রিন রিস্টার্ট করা হচ্ছে যাতে ভাষা সাথে সাথে চেঞ্জ হয়
        Intent intent = new Intent(this, Old_Login_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (this != null) {
            this.finish();
        }

    }

}