package com.maruf.hisabtool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maruf.hisabtool.Database_Helper.DatabaseHelper;
import com.maruf.hisabtool.UI_Fragment.Account_Fragment;
import com.maruf.hisabtool.UI_Fragment.Customer_Fragment;
import com.maruf.hisabtool.UI_Fragment.Home_Fragment;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private DatabaseHelper dbhelper;
    private DatabaseReference mUserDb;
    private String currentUserId, currentUserName, currentUserPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#4F46E5"));
        }

        dbhelper = new DatabaseHelper(this);

        // 🎯 সেশন থেকে ইউজারের কারেন্ট আইডি, নাম এবং ফোন নম্বর রিড করা হচ্ছে ভাই
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getString("current_uid", null);
        currentUserName = sharedPref.getString("current_name", "ব্যবহারকারী");
        currentUserPhone = sharedPref.getString("current_phone", null);

        // আপনার তৈরি করা সেশন লকিং সিস্টেম ভাই (একদম পারফেক্ট)
        if (currentUserId == null || currentUserId.isEmpty()) {
            redirectToLogin();
            return;
        }

        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (savedInstanceState == null) {
            loadFragment(new Home_Fragment());
        }

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    fragment = new Home_Fragment();
                } else if (itemId == R.id.customer) {
                    fragment = new Customer_Fragment();
                } else if (itemId == R.id.account) {
                    fragment = new Account_Fragment();
                }

                if (fragment != null) {
                    loadFragment(fragment);
                    return true;
                }
                return false;
            }
        });

        syncCloudCustomers();
    }

    private void syncCloudCustomers() {
        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("Customers");
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean updated = false;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String name = data.hasChild("customer_name") ? data.child("customer_name").getValue(String.class) : data.child("name").getValue(String.class);
                        String phone = data.hasChild("customer_phone") ? data.child("customer_phone").getValue(String.class) : data.child("phone").getValue(String.class);

                        if (name != null && phone != null) {
                            if (!dbhelper.isCustomerExists(phone)) {
                                dbhelper.Insert_Data(name, phone);
                                updated = true;
                            }
                        }
                    }
                    if (updated) {
                        loadFragment(new Home_Fragment());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, Old_Login_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit();
        }
    }

    public String getLoggedInUserName() {
        return currentUserName;
    }

    public String getLoggedInUserPhone() {
        return currentUserPhone;
    }
}