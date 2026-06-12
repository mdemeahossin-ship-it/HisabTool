package com.maruf.hisabtool.Model;

public class History_Model {
    private int id;
    String date;
    String type;
    String amount;


    public History_Model(String date, String type, String amount) {
        this.date = date;
        this.type = type;
        this.amount = amount;
    }


    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getAmount() {
        return amount;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
