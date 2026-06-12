package com.maruf.hisabtool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

import java.util.HashMap;

public class Fab_Add_Customer_Activity extends AppCompatActivity {
    private TextInputEditText ed_name, ed_phone;
    private AppCompatButton button_insert;
    private DatabaseHelper dbhelper;
    private ImageButton back_buttin;
    private DatabaseReference mCustomerDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fab_add_customer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ed_name = findViewById(R.id.ed_name);
        ed_phone = findViewById(R.id.ed_phone);
        button_insert = findViewById(R.id.button_insert);
        back_buttin = findViewById(R.id.back_buttin);
        dbhelper = new DatabaseHelper(Fab_Add_Customer_Activity.this);

        // 🎯 ব্যাক বাটন ক্লিক লজিক (ঝাঁকুনি ছাড়া ক্লোজ হবে)
        back_buttin.setOnClickListener(view -> {
            finish();
            overridePendingTransition(0, 0);
        });

        button_insert.setOnClickListener(view -> {
            String name = ed_name.getText().toString().trim();
            String phone = ed_phone.getText().toString().trim();

            if (name.isEmpty()) {
                ed_name.setError("গ্রাহকের নাম লিখুন!");
                ed_name.requestFocus();
                return;
            }
            if (phone.isEmpty()) {
                ed_phone.setError("মোবাইল নম্বর লিখুন!");
                ed_phone.requestFocus();
                return;
            }
            if (phone.length() != 11 || !phone.startsWith("01")) {
                ed_phone.setError("সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন!");
                ed_phone.requestFocus();
                return;
            }

            button_insert.setEnabled(false);
            boolean result = dbhelper.Insert_Data(name, phone);

            if (result) {
                Toast.makeText(Fab_Add_Customer_Activity.this, "কাস্টমার লোকাল ফোনে যোগ হয়েছে", Toast.LENGTH_SHORT).show();

                SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                String currentUserId = sharedPref.getString("current_uid", null);

                if (currentUserId != null && !currentUserId.isEmpty()) {
                    mCustomerDb = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(currentUserId).child("Customers");

                    String customerPushId = mCustomerDb.push().getKey();

                    HashMap<String, Object> customerData = new HashMap<>();
                    customerData.put("customer_name", name);
                    customerData.put("customer_phone", phone);
                    customerData.put("balance", "৳ ০.০");
                    customerData.put("customer_id", customerPushId);

                    if (customerPushId != null) {
                        mCustomerDb.child(customerPushId).setValue(customerData)
                                .addOnCompleteListener(task -> {
                                    button_insert.setEnabled(true);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Fab_Add_Customer_Activity.this, "ক্লাউড ব্যাকআপ সফল ভাই!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Fab_Add_Customer_Activity.this, "অফলাইন মোড: সার্ভারে ব্যাকআপ হয়নি, তবে ফোনে সেভ আছে।", Toast.LENGTH_LONG).show();
                                    }
                                    ed_name.setText("");
                                    ed_phone.setText("");
                                    finish();
                                    overridePendingTransition(0, 0); // 🎯 ক্লাউড ব্যাকআপ শেষে ঝাঁকুনি ছাড়া স্মুথলি ক্লোজ হবে ভাই
                                });
                    }
                } else {
                    button_insert.setEnabled(true);
                    Toast.makeText(Fab_Add_Customer_Activity.this, "সেশন নেই! শুধু লোকাল ফোনে সেভ হলো ভাই।", Toast.LENGTH_SHORT).show();
                    ed_name.setText("");
                    ed_phone.setText("");
                    finish();
                    overridePendingTransition(0, 0); // 🎯 অফলাইন সেশনেও ঝাঁকুনি ছাড়া স্মুথলি ক্লোজ হবে ভাই
                }
            } else {
                button_insert.setEnabled(true);
                Toast.makeText(Fab_Add_Customer_Activity.this, "এই নম্বরে অলরেডি কাস্টমার আছে", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🎯 ব্যবহারকারী যদি ফোনের নিজস্ব ব্যাক বাটন চেপেও ব্যাক করে, তাও যেন স্ক্রিন ঝাঁকুনি ছাড়া বন্ধ হয়
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}