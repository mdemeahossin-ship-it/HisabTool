package com.maruf.hisabtool.UI_Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maruf.hisabtool.Adaptar.Customer_item_Adaptar;
import com.maruf.hisabtool.Database_Helper.DatabaseHelper;
import com.maruf.hisabtool.Fab_Add_Customer_Activity;
import com.maruf.hisabtool.MainActivity;
import com.maruf.hisabtool.Model.Customer_item_Model;
import com.maruf.hisabtool.R;

import java.util.ArrayList;
import java.util.List;


public class Home_Fragment extends Fragment {
    private FloatingActionButton fab_add_customer;
    private RecyclerView item_recycler;
    private TextView item_numder, tv_rest;
    private TextView tvHomeUserName;
    private EditText searchEditText;
    private Customer_item_Adaptar adaptar;
    private DatabaseHelper dbhelper;
    private List<Customer_item_Model> list = new ArrayList<>();
    private ActionMode actionMode;
    private String adminPhone; // সেশন ফোন নম্বর ট্র্যাকিং চাবি

    public Home_Fragment() {
        // খালি কনস্ট্রাক্টর
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View home_fragment = inflater.inflate(R.layout.fragment_home_, container, false);
        item_recycler = home_fragment.findViewById(R.id.item_recycler);
        fab_add_customer = home_fragment.findViewById(R.id.fab_add_customer);
        item_numder = home_fragment.findViewById(R.id.item_numder);
        tv_rest = home_fragment.findViewById(R.id.tv_rest);
        tvHomeUserName = home_fragment.findViewById(R.id.tvHomeUserName);
        searchEditText = home_fragment.findViewById(R.id.searchEditText);

        dbhelper = new DatabaseHelper(getContext());

        if (getActivity() != null) {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            String currentUserName = sharedPref.getString("current_name", "ব্যবহারকারী");
            String currentUserId = sharedPref.getString("current_uid", null);
            String lastLoggedInUser = sharedPref.getString("last_user_id", "");

            if (currentUserId != null && !currentUserId.equals(lastLoggedInUser) && !lastLoggedInUser.isEmpty()) {
                SQLiteDatabase writeDb = dbhelper.getWritableDatabase();
                writeDb.execSQL("DELETE FROM customers");
                writeDb.execSQL("DELETE FROM history_table");
                writeDb.execSQL("DELETE FROM history_table_due");
                writeDb.close();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("last_user_id", currentUserId);
                editor.apply();
            } else if (lastLoggedInUser.isEmpty() && currentUserId != null) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("last_user_id", currentUserId);
                editor.apply();
            }

            // 🎯 MainActivity থেকে নাম এবং কারেন্ট ইউজার ফোন (currentUserPhone) কল ভাই
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                String mainActivityName = mainActivity.getLoggedInUserName();
                if (mainActivityName != null && !mainActivityName.isEmpty()) {
                    currentUserName = mainActivityName;
                }
                adminPhone = mainActivity.getLoggedInUserPhone(); // 🎯 currentUserPhone সাকসেসফুল কলড ভাই
            }

            if (tvHomeUserName != null) {
                tvHomeUserName.append(" " + currentUserName);
            }
        }

        item_recycler.setHasFixedSize(true);
        item_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🎯 আপনার ৩ প্যারামিটারের ইন্টারফেস ম্যাচ লজিক ঠিক রাখা হলো ভাই
        adaptar = new Customer_item_Adaptar(getContext(), list, selectedCount -> {
            if (selectedCount > 0) {
                if (actionMode == null && getActivity() != null) {
                    actionMode = getActivity().startActionMode(actionModeCallback);
                }
                if (actionMode != null) {
                    actionMode.setTitle(String.valueOf(selectedCount));
                }
            } else {
                if (actionMode != null) actionMode.finish();
            }
        });
        item_recycler.setAdapter(adaptar);

        fab_add_customer.setOnClickListener(view ->
                startActivity(new Intent(getActivity(), Fab_Add_Customer_Activity.class))
        );

        loadData("");
        getCustomersFromFirebase();
        getHistoryAndDueFromFirebaseHome();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadData(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        return home_fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(searchEditText != null ? searchEditText.getText().toString() : "");
        getCustomersFromFirebase();
    }

    private void loadData(String text) {
        list.clear();
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor customerCursor = null;

        try {
            if (text == null || text.isEmpty()) {
                customerCursor = dbhelper.get_All_date();
            } else {
                customerCursor = dbhelper.search_view(text);
            }

            if (customerCursor != null && customerCursor.moveToFirst()) {
                do {
                    String name = customerCursor.getString(1);
                    String phone = customerCursor.getString(2);
                    // 🎯 আপনার কাস্টম মডেল ক্লাস মেথড (২ প্যারামিটার)
                    list.add(new Customer_item_Model(name, phone));
                } while (customerCursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (customerCursor != null) customerCursor.close();
        }

        adaptar.notifyDataSetChanged();
        if (item_numder != null) {
            item_numder.setText(list.size() + " জন");
        }

        double totalAppDue = 0;
        java.util.HashMap<String, Double> paidMap = new java.util.HashMap<>();
        java.util.HashMap<String, Double> dueMap = new java.util.HashMap<>();

        Cursor cursorPay = null;
        Cursor cursorDue = null;

        try {
            cursorPay = db.rawQuery("SELECT type, amount FROM history_table", null);
            if (cursorPay != null && cursorPay.moveToFirst()) {
                do {
                    String type = cursorPay.getString(0);
                    String amountStr = cursorPay.getString(1);
                    if (type != null && amountStr != null) {
                        try {
                            double amt = Double.parseDouble(amountStr.trim());
                            for (Customer_item_Model customer : list) {
                                String phone = customer.getTv_phone();
                                if (type.contains(phone)) {
                                    paidMap.put(phone, paidMap.getOrDefault(phone, 0.0) + amt);
                                }
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                } while (cursorPay.moveToNext());
            }

            cursorDue = db.rawQuery("SELECT type_1, due FROM history_table_due", null);
            if (cursorDue != null && cursorDue.moveToFirst()) {
                do {
                    String type_1 = cursorDue.getString(0);
                    String amountStr = cursorDue.getString(1);
                    if (type_1 != null && amountStr != null) {
                        amountStr = amountStr.replace("৳", "").replace(",", "").trim();
                        try {
                            double amt = Double.parseDouble(amountStr);
                            for (Customer_item_Model customer : list) {
                                String phone = customer.getTv_phone();
                                if (type_1.contains(phone)) {
                                    dueMap.put(phone, dueMap.getOrDefault(phone, 0.0) + amt);
                                }
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                } while (cursorDue.moveToNext());
            }

            for (Customer_item_Model customer : list) {
                String phone = customer.getTv_phone();
                double totalPaid = paidMap.getOrDefault(phone, 0.0);
                double totalDue = dueMap.getOrDefault(phone, 0.0);
                double customerRest = totalDue - totalPaid;

                if (customerRest > 0) {
                    totalAppDue += customerRest;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursorPay != null) cursorPay.close();
            if (cursorDue != null) cursorDue.close();
        }

        if (tv_rest != null) {
            tv_rest.setText("৳ " + (int) totalAppDue);
        }
    }

    private void getCustomersFromFirebase() {
        if (getActivity() == null) return;
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUserId = sharedPref.getString("current_uid", null);

        if (currentUserId != null && !currentUserId.isEmpty()) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference customerRef = database.getReference().child("Users").child(currentUserId).child("Customers");

            customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        boolean hasNewCustomer = false;
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String name = data.hasChild("customer_name") ? data.child("customer_name").getValue(String.class) : data.child("name").getValue(String.class);
                            String phone = data.hasChild("customer_phone") ? data.child("customer_phone").getValue(String.class) : data.child("phone").getValue(String.class);

                            if (name != null && phone != null) {
                                boolean exists = false;
                                for (Customer_item_Model item : list) {
                                    if (item.getTv_phone() != null && item.getTv_phone().equals(phone)) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    dbhelper.Insert_Data(name, phone);
                                    hasNewCustomer = true;
                                }
                            }
                        }
                        if (hasNewCustomer) {
                            loadData(searchEditText != null ? searchEditText.getText().toString() : "");
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void getHistoryAndDueFromFirebaseHome() {
        if (getActivity() == null) return;
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUserId = sharedPref.getString("current_uid", null);

        if (currentUserId != null && !currentUserId.isEmpty()) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            database.getReference().child("Users").child(currentUserId).child("Payments")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    String date = data.child("date").getValue(String.class);
                                    String type = data.child("type").getValue(String.class);
                                    String amount = data.child("amount").getValue(String.class);
                                    dbhelper.insertHistory(date, type, amount);
                                }
                                loadData(searchEditText != null ? searchEditText.getText().toString() : "");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

            database.getReference().child("Users").child(currentUserId).child("Dues")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    String date = data.child("date").getValue(String.class);
                                    String type = data.child("type").getValue(String.class);
                                    String amount = data.child("amount").getValue(String.class);
                                    dbhelper.insertHistory_due(date, type, amount);
                                }
                                loadData(searchEditText != null ? searchEditText.getText().toString() : "");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                if (getContext() != null) {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());

                    // এখানে string resource আইডি ব্যবহার করা হলো ভাই
                    builder.setTitle(getString(R.string.dialog_delete_title));
                    builder.setMessage(getString(R.string.dialog_delete_msg));
                    builder.setIcon(android.R.drawable.ic_dialog_alert);

                    builder.setPositiveButton(getString(R.string.dialog_btn_yes), (dialog, which) -> {
                        adaptar.deleteSelectedItems(phone -> {
                            dbhelper.deleteCustomer(phone);
                            if (getActivity() != null) {
                                SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                                String currentUserId = sharedPref.getString("current_uid", null);

                                if (currentUserId != null && !currentUserId.isEmpty()) {
                                    DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference()
                                            .child("Users").child(currentUserId).child("Customers");

                                    deleteRef.orderByChild("customer_phone").equalTo(phone)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot child : snapshot.getChildren()) {
                                                        child.getRef().removeValue();
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            });
                                }
                            }
                        });
                        mode.finish();
                        loadData("");
                        // টোস্ট মেসেজটিও মাল্টি-ল্যাঙ্গুয়েজ করা হলো ভাই
                        Toast.makeText(getContext(), getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
                    });

                    builder.setNegativeButton(getString(R.string.dialog_btn_no), (dialog, which) -> dialog.dismiss());
                    builder.create().show();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };
}