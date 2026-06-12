package com.maruf.hisabtool.Model;

public class Customer_item_Model {

    String tv_name;

    String tv_phone;

    boolean isSelected = false;



    public Customer_item_Model(String tv_name, String tv_phone) {
        this.tv_name = tv_name;
        this.tv_phone = tv_phone;
    }


    // =========================
    // GET
    // =========================

    public String getTv_name() {

        return tv_name;
    }

    public String getTv_phone() {

        return tv_phone;
    }



    // =========================
    // SELECT
    // =========================

    public boolean isSelected() {

        return isSelected;
    }

    public void setSelected(boolean selected) {

        isSelected = selected;
    }

}