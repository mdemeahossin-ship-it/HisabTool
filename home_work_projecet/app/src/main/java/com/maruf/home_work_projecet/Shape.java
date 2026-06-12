package com.maruf.home_work_projecet;

public abstract class Shape {
    String tv_shape_name;

    public abstract double calculate_area();
    public abstract double calculate_perimeter();

    public Shape(String tv_shape_name) {
        this.tv_shape_name = tv_shape_name;
    }

    public String getTv_shape_name() {
        return tv_shape_name;
    }

}
