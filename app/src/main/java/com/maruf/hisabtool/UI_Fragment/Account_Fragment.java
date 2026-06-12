package com.maruf.hisabtool.UI_Fragment;

import static androidx.browser.customtabs.CustomTabsClient.getPackageName;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.maruf.hisabtool.Database_Helper.DatabaseHelper;
import com.maruf.hisabtool.MainActivity;
import com.maruf.hisabtool.Old_Login_Activity;
import com.maruf.hisabtool.R;


public class Account_Fragment extends Fragment {
    private TextView tvShowName, tvShowPhone, tvShowUid;
    LinearLayout layoutRateApp,btnShareApp,language;
    private LinearLayout btnLogout;
    private ImageButton back_buttin;
    private DatabaseHelper dbhelper;

    public Account_Fragment() {
        // খালি কনস্ট্রাক্টর
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View account_fragment = inflater.inflate(R.layout.fragment_account_, container, false);
        dbhelper = new DatabaseHelper(getActivity());

        tvShowName = account_fragment.findViewById(R.id.tvShowName);
        tvShowPhone = account_fragment.findViewById(R.id.tvShowPhone);
        tvShowUid = account_fragment.findViewById(R.id.tvShowUid);
        btnLogout = account_fragment.findViewById(R.id.btnLogout);
        back_buttin = account_fragment.findViewById(R.id.back_buttin);
        layoutRateApp = account_fragment.findViewById(R.id.layoutRateApp);
        btnShareApp = account_fragment.findViewById(R.id.btnShareApp);
        language = account_fragment.findViewById(R.id.language);

        loadUserProfileFromSession();

        if (back_buttin != null) {
            back_buttin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);
                        if (bottomNavigationView != null) {
                            bottomNavigationView.setSelectedItemId(R.id.home);
                        }
                    }
                }
            });
        }



        if (layoutRateApp != null) {
            layoutRateApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // getActivity() যোগ করা হয়েছে ভাই
                    if (getActivity() != null) {
                        String appPackageName = getActivity().getPackageName();

                        try {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                            marketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(marketIntent);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                            webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            // এখানে getActivity().getPackageManager() দেওয়া হয়েছে
                            if (webIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(webIntent);
                            } else {
                                Toast.makeText(getContext(), getString(R.string.toast_rate_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        }









        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());

                    // মাল্টি-ল্যাঙ্গুয়েজ স্ট্রিং রিসোর্স সেট করা হলো ভাই
                    builder.setTitle(getString(R.string.dialog_logout_title));
                    builder.setMessage(getString(R.string.dialog_logout_msg));

                    builder.setPositiveButton(getString(R.string.dialog_btn_logout_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();

                            try {
                                SQLiteDatabase db = dbhelper.getWritableDatabase();
                                db.execSQL("DELETE FROM customer_table");
                                db.execSQL("DELETE FROM history_due_table");
                                db.execSQL("DELETE FROM history_table");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (getActivity() != null) {
                                SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.clear();
                                editor.apply();

                                // টোস্ট মেসেজটিও ডাইনামিক করা হলো ভাই
                                Toast.makeText(getActivity(), getString(R.string.toast_logout_success), Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getActivity(), Old_Login_Activity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        }
                    });

                    builder.setNegativeButton(getString(R.string.dialog_btn_logout_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }

        if (language != null) {
            language.setOnClickListener(new View.OnClickListener() {
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

        app_share_code();

        return account_fragment;
    }

    private void loadUserProfileFromSession() {
        if (getActivity() != null && isAdded()) {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);

            String sessionName = sharedPref.getString("current_name", "ব্যবহারকারী");
            String sessionPhone = sharedPref.getString("current_phone", "নম্বর পাওয়া যায়নি");
            String sessionFirebaseUid = sharedPref.getString("firebase_uid", "আইডি পাওয়া যায়নি");

            if (tvShowName != null) {
                tvShowName.setText(sessionName);
            }

            if (tvShowPhone != null) {
                String cleanPhone = sessionPhone.replace("+88", "").trim();
                tvShowPhone.setText(cleanPhone);
            }

            if (tvShowUid != null) {
                tvShowUid.setText(sessionFirebaseUid);
            }
        }
    }

private void app_share_code(){

    btnShareApp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String shareMessage = "সহজ ও নিরাপদ উপায়ে আপনার ব্যবসা বা দোকানের হিসাব রাখতে ব্যবহার করুন ডিজিটাল অ্যাপ 'হিসাব টুল' (Hisab Tool)।\n\n" +
                    "🔗 ডাউনলোড লিংক:\n" +
                    "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName();

            // 🚀 অ্যান্ড্রয়েড শেয়ার সিস্টেম (Intent)
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            // শেয়ার উইন্ডো ওপেন হবে ভাই
            startActivity(Intent.createChooser(shareIntent, "অ্যাপটি শেয়ার করুন:"));

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
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }

    }





}