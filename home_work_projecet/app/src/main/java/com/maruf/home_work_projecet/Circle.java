package com.maruf.home_work_projecet;

public class Circle extends Shape{
    float tv_radius;

    public Circle(String tv_shape_name, float tv_radius) {
        super(tv_shape_name);
        this.tv_radius = tv_radius;
    }

    public Circle(String tv_shape_name) {
        super(tv_shape_name);
    }


    @Override
    public double calculate_area() {
        return  Math.PI * tv_radius * tv_radius;
    }

    @Override
    public double calculate_perimeter() {
        return 2 * Math.PI * tv_radius;
    }

}
