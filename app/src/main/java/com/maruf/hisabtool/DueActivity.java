package com.maruf.hisabtool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.maruf.hisabtool.Database_Helper.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DueActivity extends AppCompatActivity {
    TextInputEditText ed_Date, ed_details, ed_rest;
    AppCompatButton seve_bnt;
    DatabaseHelper dbhelper;
    String currentDate;
    String currentCustomerPhone;
    private long lastClickTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_due);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ed_Date = findViewById(R.id.ed_Date);
        ed_details = findViewById(R.id.ed_details);
        ed_rest = findViewById(R.id.ed_rest);
        seve_bnt = findViewById(R.id.seve_bnt);

        dbhelper = new DatabaseHelper(DueActivity.this);
        currentCustomerPhone = getIntent().getStringExtra("phone");
        if (currentCustomerPhone == null) currentCustomerPhone = "";

        currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        ed_Date.setText(currentDate);

        seve_bnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (System.currentTimeMillis() - lastClickTime < 2000) {
                    return;
                }
                lastClickTime = System.currentTimeMillis();

                String date_1 = ed_Date.getText().toString().trim();
                String userDetails_1 = ed_details.getText().toString().trim();
                String due = ed_rest.getText().toString().trim();

                if (date_1.isEmpty()) {
                    ed_Date.setError("তারিখ সিলেক্ট করুন!");
                    ed_Date.requestFocus();
                    return;
                }

                if (due.isEmpty()) {
                    ed_rest.setError("বাকির পরিমাণ লিখুন!");
                    ed_rest.requestFocus();
                    return;
                }

                try {
                    double parsedDue = Double.parseDouble(due);
                    if (parsedDue <= 0) {
                        ed_rest.setError("বাকির পরিমাণ ০ এর বেশি হতে হবে!");
                        ed_rest.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    ed_rest.setError("সঠিক সংখ্যা বা অংক লিখুন!");
                    ed_rest.requestFocus();
                    return;
                }

                if (userDetails_1.isEmpty()) {
                    userDetails_1 = "বাকি যুক্ত হলো";
                }

                long currentClickTime = System.currentTimeMillis();
                String type_1 = userDetails_1 + " (" + currentCustomerPhone + ") ##" + currentClickTime;

                // লোকাল SQLite-এ সেভ করা
                boolean result = dbhelper.insertHistory_due(date_1, type_1, due);

                if (result) {
                    Toast.makeText(DueActivity.this, "বাকির হিসাব সফলভাবে সেভ হয়েছে", Toast.LENGTH_SHORT).show();

                    SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    String currentUserId = sharedPref.getString("current_uid", null);

                    if (currentUserId != null && !currentUserId.isEmpty()) {
                        DatabaseReference mDueDb = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child(currentUserId).child("Dues");

                        String pushId = mDueDb.push().getKey();

                        HashMap<String, Object> dueMap = new HashMap<>();
                        dueMap.put("date", date_1);
                        dueMap.put("type", type_1); // 🎯 আপনার তৈরি করা কাস্টম টাইপ ও বিবরণ ক্লাউডে যাবে ভাই
                        dueMap.put("amount", due);
                        dueMap.put("phone", currentCustomerPhone);
                        dueMap.put("full_details_type", type_1);

                        if (pushId != null) {
                            mDueDb.child(pushId).setValue(dueMap);
                        }
                    }

                    ed_details.setText("");
                    ed_rest.setText("");
                    finish();
                    overridePendingTransition(0, 0); // 🎯 সেভ হওয়ার পর কোনো অ্যানিমেশন বা ঝাঁকুনি হবে না ভাই
                } else {
                    Toast.makeText(DueActivity.this, "সেভ করতে সমস্যা হয়েছে! আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}