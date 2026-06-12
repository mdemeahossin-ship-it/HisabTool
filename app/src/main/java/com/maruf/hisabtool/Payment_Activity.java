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

public class Payment_Activity extends AppCompatActivity {
    TextInputEditText ed_Date, ed_details, ed_amount;
    AppCompatButton seve_bnt;
    DatabaseHelper dbhelper;
    String currentDate;
    String currentCustomerPhone;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ed_Date = findViewById(R.id.ed_Date);
        ed_details = findViewById(R.id.ed_details);
        ed_amount = findViewById(R.id.ed_amount);
        seve_bnt = findViewById(R.id.seve_bnt);

        dbhelper = new DatabaseHelper(Payment_Activity.this);
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

                String date = ed_Date.getText().toString().trim();
                String userDetails = ed_details.getText().toString().trim();
                String amount = ed_amount.getText().toString().trim();

                if (date.isEmpty()) {
                    ed_Date.setError("তারিখ সিলেক্ট বা লিখুন!");
                    ed_Date.requestFocus();
                    return;
                }

                if (amount.isEmpty()) {
                    ed_amount.setError("টাকার পরিমাণ লিখুন!");
                    ed_amount.requestFocus();
                    return;
                }

                try {
                    double parsedAmount = Double.parseDouble(amount);
                    if (parsedAmount <= 0) {
                        ed_amount.setError("টাকার পরিমাণ ০ এর বেশি হতে হবে!");
                        ed_amount.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    ed_amount.setError("সঠিক সংখ্যা বা অংক লিখুন!");
                    ed_amount.requestFocus();
                    return;
                }

                if (userDetails.isEmpty()) {
                    userDetails = "নগদ পরিশোধ";
                }

                long currentClickTime = System.currentTimeMillis();
                String type = userDetails + " (" + currentCustomerPhone + ") ##" + currentClickTime;

                // লোকাল SQLite সেভ
                boolean result = dbhelper.insertHistory(date, type, amount);

                if (result) {
                    Toast.makeText(Payment_Activity.this, "পেমেন্ট সফলভাবে সেভ হয়েছে", Toast.LENGTH_SHORT).show();

                    SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    String currentUserId = sharedPref.getString("current_uid", null);

                    if (currentUserId != null && !currentUserId.isEmpty()) {
                        DatabaseReference mPaymentDb = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child(currentUserId).child("Payments");

                        String pushId = mPaymentDb.push().getKey();

                        HashMap<String, Object> paymentMap = new HashMap<>();
                        paymentMap.put("date", date);
                        paymentMap.put("type", type); // 🎯 আপনার তৈরি করা কাস্টম টাইপ ও বিবরণ ক্লাউডে যাবে ভাই
                        paymentMap.put("amount", amount);
                        paymentMap.put("phone", currentCustomerPhone);
                        paymentMap.put("full_details_type", type);

                        if (pushId != null) {
                            mPaymentDb.child(pushId).setValue(paymentMap);
                        }
                    }

                    ed_details.setText("");
                    ed_amount.setText("");
                    finish();
                    overridePendingTransition(0, 0); // 🎯 সেভ হওয়ার পর কোনো অ্যানিমেশন বা ঝাঁকুনি হবে না ভাই
                } else {
                    Toast.makeText(Payment_Activity.this, "সেভ করতে সমস্যা হয়েছে! আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}