package com.maruf.hisabtool.Adaptar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.maruf.hisabtool.Custimer_All_Details_Activity;
import com.maruf.hisabtool.Model.Customer_item_Model;
import com.maruf.hisabtool.R;

import java.util.ArrayList;
import java.util.List;

public class Customer_item_Adaptar extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Customer_item_Model> modelList;
    int[] images = {
            R.drawable.imag,
            R.drawable.imag_1,
            R.drawable.imag_2,
            R.drawable.imag_4
    };

    List<Customer_item_Model> selectedList = new ArrayList<>();
    OnCustomerSelectListener listener;

    private final int TYPE_NORMAL = 0;
    private final int TYPE_SELECTED = 1;

    public Customer_item_Adaptar(Context context, List<Customer_item_Model> modelList, OnCustomerSelectListener listener) {
        this.context = context;
        this.modelList = modelList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return modelList.get(position).isSelected() ? TYPE_SELECTED : TYPE_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_SELECTED) {
            // সিলেক্ট হলে আপনার customer_item_selected.xml ফাইল লোড হবে
            return new SelectedViewHolder(inflater.inflate(R.layout.customer_item_selected, parent, false));
        } else {
            // নরমাল থাকলে customer_item.xml ফাইল লোড হবে
            return new NormalViewHolder(inflater.inflate(R.layout.customer_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Customer_item_Model item = modelList.get(position);
        int currentPos = holder.getAdapterPosition();

        // ১. সিলেক্টেড ভিউ হোল্ডারের কাজ
        if (holder instanceof SelectedViewHolder) {
            SelectedViewHolder selectedHolder = (SelectedViewHolder) holder;
            selectedHolder.customer_name.setText(item.getTv_name());
            selectedHolder.customer_phone.setText(item.getTv_phone());

            int imageRes = images[position % images.length];

            Glide.with(context)
                    .load(imageRes)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(selectedHolder.profile_selected_image);

            // সিলেক্টেড অবস্থায় ক্লিক করলে আন-সিলেক্ট হবে
            selectedHolder.itemView.setOnClickListener(v -> handleSelection(item, currentPos));
        }
        // ২. নরমাল ভিউ হোল্ডারের কাজ
        else if (holder instanceof NormalViewHolder) {
            NormalViewHolder normalHolder = (NormalViewHolder) holder;
            normalHolder.customer_name.setText(item.getTv_name());
            normalHolder.customer_phone.setText(item.getTv_phone());

            int imgRes = images[position % images.length];

            Glide.with(context)
                    .load(imgRes)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(normalHolder.profile_image);

            normalHolder.itemView.setOnClickListener(v -> {
                if (!selectedList.isEmpty()) {
                    handleSelection(item, currentPos);
                } else {
                    Intent intent = new Intent(context, Custimer_All_Details_Activity.class);
                    intent.putExtra("image", imgRes);
                    intent.putExtra("name", item.getTv_name());
                    intent.putExtra("phone", item.getTv_phone());
                    context.startActivity(intent);
                }
            });

            // লং ক্লিকে সিলেকশন চালু
            normalHolder.itemView.setOnLongClickListener(v -> {
                handleSelection(item, currentPos);
                return true;
            });
        }
    }

    private void handleSelection(Customer_item_Model item, int position) {
        item.setSelected(!item.isSelected());
        if (item.isSelected()) {
            selectedList.add(item);
        } else {
            selectedList.remove(item);
        }
        notifyItemChanged(position);
        if (listener != null) listener.onItemSelectUpdate(selectedList.size());
    }

    public void deleteSelectedItems(OnDatabaseDeleteListener dbListener) {
        for (Customer_item_Model item : selectedList) {
            dbListener.onDeleteFromDB(item.getTv_phone());
        }
        modelList.removeAll(selectedList);
        selectedList.clear();
        notifyDataSetChanged();
    }

    public void clearSelection() {
        for (Customer_item_Model item : selectedList) {
            item.setSelected(false);
        }
        selectedList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return modelList != null ? modelList.size() : 0;
    }

    public interface OnCustomerSelectListener { void onItemSelectUpdate(int selectedCount); }
    public interface OnDatabaseDeleteListener { void onDeleteFromDB(String phone); }

    // হোল্ডার ১: নরমাল লেআউটের জন্য
    public static class NormalViewHolder extends RecyclerView.ViewHolder {
        TextView customer_name, customer_phone;
        ShapeableImageView profile_image;
        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            customer_name = itemView.findViewById(R.id.customer_name);
            customer_phone = itemView.findViewById(R.id.customer_phone);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }

    public static class SelectedViewHolder extends RecyclerView.ViewHolder {
        TextView customer_name, customer_phone;
        ShapeableImageView profile_selected_image;
        ImageView img_check;
        public SelectedViewHolder(@NonNull View itemView) {
            super(itemView);
            customer_name = itemView.findViewById(R.id.customer_name);
            customer_phone = itemView.findViewById(R.id.customer_phone);
            profile_selected_image = itemView.findViewById(R.id.profile_selected_image);
            img_check = itemView.findViewById(R.id.img_check);
        }
    }
}