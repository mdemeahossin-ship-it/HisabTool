package com.maruf.home_work_projecet;

public class Rectangle extends Shape{
    float tv_length, tv_width;

    public Rectangle(String tv_shape_name, float tv_length, float tv_width) {
        super(tv_shape_name);
        this.tv_length = tv_length;
        this.tv_width = tv_width;
    }

    public Rectangle(String tv_shape_name) {
        super(tv_shape_name);
    }

    @Override
    public double calculate_area() {
        return tv_length * tv_width;
    }

    @Override
    public double calculate_perimeter() {
        return 2 * tv_length + tv_width;
    }
}
