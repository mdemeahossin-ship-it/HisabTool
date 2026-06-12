package com.maruf.hisabtool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maruf.hisabtool.Adaptar.History_Adapter;
import com.maruf.hisabtool.Database_Helper.DatabaseHelper;
import com.maruf.hisabtool.Model.History_Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Custimer_All_Details_Activity extends AppCompatActivity {
    RecyclerView rvTransactions;
    History_Adapter adapter;
    List<History_Model> modelList = new ArrayList<>();
    Button tk_payment, tk_rest;

    // 🎯 স্ট্যান্ডার্ড রুলস অনুযায়ী এগুলোকে static থেকে private করা হলো ভাই (Memory leak রোধ করতে)
    private TextView tvTotalPaid;
    private TextView tvTotalDue;
    private TextView tvTotalRest;
    TextView tv_name, tv_phone;
    ShapeableImageView customer_profile_image;
    ImageButton backBtn;

    DatabaseHelper dbhelper;
    String currentCustomerPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_custimer_all_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tk_payment = findViewById(R.id.tk_payment);
        tk_rest = findViewById(R.id.tk_rest);
        rvTransactions = findViewById(R.id.rvTransactions);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);
        tvTotalDue = findViewById(R.id.tvTotalDue);
        tvTotalRest = findViewById(R.id.tvTotalRest);
        backBtn = findViewById(R.id.backBtn);
        tv_name = findViewById(R.id.tv_name);
        tv_phone = findViewById(R.id.tv_phone);
        customer_profile_image = findViewById(R.id.customer_profile_image);

        dbhelper = new DatabaseHelper(Custimer_All_Details_Activity.this);

        rvTransactions.setLayoutManager(new LinearLayoutManager(Custimer_All_Details_Activity.this));
        adapter = new History_Adapter(modelList);
        rvTransactions.setAdapter(adapter);


        int image = getIntent().getIntExtra("image", 0);
        String name = getIntent().getStringExtra("name");
        currentCustomerPhone = getIntent().getStringExtra("phone");

        Glide.with(this)
                .load(image)
                .into(customer_profile_image);

        if (name != null && currentCustomerPhone != null) {
            tv_name.setText(name);
            tv_phone.setText(currentCustomerPhone);
        }

        backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        tk_payment.setOnClickListener(view -> {
            Intent intent = new Intent(Custimer_All_Details_Activity.this, Payment_Activity.class);
            intent.putExtra("phone", currentCustomerPhone);
            startActivity(intent);
        });

        tk_rest.setOnClickListener(view -> {
            Intent intent = new Intent(Custimer_All_Details_Activity.this, DueActivity.class);
            intent.putExtra("phone", currentCustomerPhone);
            startActivity(intent);
        });

        load_All_Data();
        getHistoryAndDueFromFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load_All_Data();
        getHistoryAndDueFromFirebase();
    }

    private void load_All_Data() {
        if (currentCustomerPhone == null || currentCustomerPhone.isEmpty()) {
            modelList.clear();
            adapter.notifyDataSetChanged();
            tvTotalPaid.setText("৳ 0");
            tvTotalDue.setText("৳ 0");
            tvTotalRest.setText("৳ 0");
            return;
        }

        new Thread(() -> {
            modelList.clear();
            double totalPaid = 0;
            double totalDue = 0;

            // 🟢 ১. PAYMENT DATA (লোকাল ডাটাবেজ থেকে জমা রিড)
            Cursor cursor = dbhelper.getHistory();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0); // ডাটাবেজের অরিজিনাল অটো-ইনক্রিমেন্ট ID ভাই
                    String date = cursor.getString(1);
                    String type = cursor.getString(2);
                    String amount = cursor.getString(3);

                    if (type != null && type.contains(currentCustomerPhone)) {
                        History_Model model = new History_Model(date, "pay", amount);
                        model.setId(id); // মডেল ক্লাসে আইডি সেট করা হচ্ছে (সর্টিং এর জন্য)
                        modelList.add(model);
                        try {
                            totalPaid += Double.parseDouble(amount);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            // 🔴 ২. DUE DATA (লোকাল ডাটাবেজ থেকে বাকি রিড)
            Cursor cursor2 = dbhelper.getHistory_due();
            if (cursor2 != null && cursor2.moveToFirst()) {
                do {
                    int id = cursor2.getInt(0); // ডাটাবেজের অরিজিনাল অটো-ইনক্রিমেন্ট ID ভাই
                    String date = cursor2.getString(1);
                    String type = cursor2.getString(2);
                    String amount = cursor2.getString(3);

                    if (type != null && type.contains(currentCustomerPhone)) {
                        History_Model model = new History_Model(date, "due", amount);
                        model.setId(id); // মডেল ক্লাসে আইডি সেট করা হচ্ছে
                        modelList.add(model);
                        try {
                            totalDue += Double.parseDouble(amount);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } while (cursor2.moveToNext());
                cursor2.close();
            }

            // 🎯 🎯 ম্যাজিক সর্টিং লজিক ভাই!
            // এটি জমা এবং বাকির সমস্ত ডাটা মিক্স করার পর ডাটাবেজের ID চেক করবে।
            // যার ID যত বড় (অর্থাৎ যেটা সবার শেষে বা একদম নতুন সেভ হয়েছে), তাকে টেনে সবার ওপরে (Top-এ) নিয়ে যাবে ভাই।
            Collections.sort(modelList, new Comparator<History_Model>() {
                @Override
                public int compare(History_Model o1, History_Model o2) {
                    return Integer.compare(o2.getId(), o1.getId()); // DESCENDING ORDER (বড় থেকে ছোট)
                }
            });

            double finalPaid = totalPaid;
            double finalDue = totalDue;
            double finalRest = finalDue - finalPaid;

            runOnUiThread(() -> {
                tvTotalPaid.setText("৳ " + (int) finalPaid);
                tvTotalDue.setText("৳ " + (int) finalDue);
                tvTotalRest.setText("৳ " + (int) finalRest);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    // ফায়ারবেস ক্লাউড থেকে ডাটা ব্যাকআপ নামানোর মেথড ভাই
    private void getHistoryAndDueFromFirebase() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUserId = sharedPref.getString("current_uid", null);

        if (currentUserId != null && !currentUserId.isEmpty() && currentCustomerPhone != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            // জমার ডাটা সিঙ্ক
            DatabaseReference paymentRef = database.getReference().child("Users").child(currentUserId).child("Payments");
            paymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        boolean hasNewData = false;
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String phone = data.child("phone").getValue(String.class);
                            if (phone != null && phone.equals(currentCustomerPhone)) {
                                String date = data.child("date").getValue(String.class);
                                String type = data.child("type").getValue(String.class);
                                String amount = data.child("amount").getValue(String.class);

                                dbhelper.insertHistory(date, type, amount);
                                hasNewData = true;
                            }
                        }
                        if (hasNewData) {
                            load_All_Data();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            // বাকির ডাটা সিঙ্ক
            DatabaseReference dueRef = database.getReference().child("Users").child(currentUserId).child("Dues");
            dueRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        boolean hasNewData = false;
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String phone = data.child("phone").getValue(String.class);
                            if (phone != null && phone.equals(currentCustomerPhone)) {
                                String date = data.child("date").getValue(String.class);
                                String type = data.child("type").getValue(String.class);
                                String amount = data.child("amount").getValue(String.class);

                                dbhelper.insertHistory_due(date, type, amount);
                                hasNewData = true;
                            }
                        }
                        if (hasNewData) {
                            load_All_Data();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}