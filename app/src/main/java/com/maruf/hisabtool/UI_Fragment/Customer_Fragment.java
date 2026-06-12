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
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maruf.hisabtool.Adaptar.Customer_item_Adaptar;
import com.maruf.hisabtool.Database_Helper.DatabaseHelper;
import com.maruf.hisabtool.Fab_Add_Customer_Activity;
import com.maruf.hisabtool.Model.Customer_item_Model;
import com.maruf.hisabtool.R;

import java.util.ArrayList;
import java.util.List;


public class Customer_Fragment extends Fragment {

    ImageButton back_buttin;
    FloatingActionButton fab_add_customer;
    EditText searchEditText;
    RecyclerView recyclerView;
    Customer_item_Adaptar adaptar;
    List<Customer_item_Model> modelList = new ArrayList<>();
    DatabaseHelper dbhelper;
    ActionMode actionMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View customer_fragment = inflater.inflate(R.layout.fragment_customer_, container, false);
        back_buttin = customer_fragment.findViewById(R.id.back_buttin);
        fab_add_customer = customer_fragment.findViewById(R.id.fab_add_customer);
        recyclerView = customer_fragment.findViewById(R.id.recyclerView);
        searchEditText = customer_fragment.findViewById(R.id.searchEditText);

        dbhelper = new DatabaseHelper(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adaptar = new Customer_item_Adaptar(getContext(), modelList, selectedCount -> {
            if (selectedCount > 0) {
                if (actionMode == null) actionMode = getActivity().startActionMode(actionModeCallback);
                actionMode.setTitle(String.valueOf(selectedCount));
            } else {
                if (actionMode != null) actionMode.finish();
            }
        });
        recyclerView.setAdapter(adaptar);


        back_buttin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);

                    bottomNavigationView.setSelectedItemId(R.id.home);
                }
            }
        });


        fab_add_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Fab_Add_Customer_Activity.class));
            }
        });

        loadDataFromDatabase("");
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDataFromDatabase(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });


       return customer_fragment;
    }

        @Override
        public void onResume() {
            super.onResume();
            loadDataFromDatabase("");
        }

    private void loadDataFromDatabase(String text) {

        modelList.clear();

            SQLiteDatabase db = dbhelper.getReadableDatabase();

            Cursor customerCursor;

            if (text == null || text.isEmpty()) {
                customerCursor = dbhelper.get_All_date();
            } else {
                customerCursor = dbhelper.search_view(text);
            }

            if (customerCursor != null && customerCursor.moveToFirst()) {
                do {
                    String name = customerCursor.getString(1);
                    String phone = customerCursor.getString(2);

                    modelList.add(new Customer_item_Model(name, phone));

                } while (customerCursor.moveToNext());

                customerCursor.close();
            }

            adaptar.notifyDataSetChanged();

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
                    builder.setTitle("কাস্টমার ডিলিট নিশ্চিত করুন");
                    builder.setMessage("আপনি কি নিশ্চিতভাবে নির্বাচিত কাস্টমার(দের) ডিলিট করতে চান? ক্লাউড ব্যাকআপ থেকেও মুছে যাবে ভাই!");
                    builder.setIcon(android.R.drawable.ic_dialog_alert);

                    builder.setPositiveButton("হ্যাঁ", (dialog, which) -> {
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
                        loadDataFromDatabase("");
                        Toast.makeText(getContext(), "কাস্টমার ডিলিট হয়েছে ভাই", Toast.LENGTH_SHORT).show();
                    });

                    builder.setNegativeButton("না", (dialog, which) -> dialog.dismiss());
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



