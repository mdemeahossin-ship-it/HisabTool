package com.maruf.hisabtool.Adaptar;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.widget.TextView;

import com.maruf.hisabtool.Model.History_Model;
import com.maruf.hisabtool.R;

import java.util.List;

public class History_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<History_Model> list;

    public History_Adapter(List<History_Model> list) {
        this.list = list;
    }
    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getType().toLowerCase().contains("pay")) {
            return 1;
        } else {
            return 2;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_green, parent, false);
            return new GreenHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_red, parent, false);
            return new RedHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        History_Model model = list.get(position);

        if (holder instanceof GreenHolder) {
            GreenHolder greenHolder = (GreenHolder) holder;
            greenHolder.tv_date.setText(model.getDate());
            greenHolder.tv_type.setText(model.getType());
            greenHolder.tv_amount.setText("৳ " + model.getAmount());
            greenHolder.tv_amount.setTextColor(Color.parseColor("#4CAF50"));
        }

        // লাল আইটেম (বাকি যোগ)
        if (holder instanceof RedHolder) {
            RedHolder redHolder = (RedHolder) holder;
            redHolder.tv_date.setText(model.getDate());
            redHolder.tv_type.setText(model.getType());
            redHolder.tv_amount.setText("৳ " + model.getAmount());
            redHolder.tv_amount.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class GreenHolder extends RecyclerView.ViewHolder {
        TextView tv_date, tv_type, tv_amount;

        public GreenHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_type = itemView.findViewById(R.id.tv_type);
            tv_amount = itemView.findViewById(R.id.tv_amount);
        }
    }



    public static class RedHolder extends RecyclerView.ViewHolder {
        TextView tv_date, tv_type, tv_amount;

        public RedHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_type = itemView.findViewById(R.id.tv_type);
            tv_amount = itemView.findViewById(R.id.tv_amount);
        }
    }

}
