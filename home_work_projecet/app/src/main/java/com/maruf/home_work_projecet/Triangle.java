package com.maruf.home_work_projecet;

public class Triangle extends Shape{
    float tv_s1, tv_s2, tv_s3;

    public Triangle(String tv_shape_name, float tv_s1, float tv_s2, float tv_s3) {
        super(tv_shape_name);
        this.tv_s1 = tv_s1;
        this.tv_s2 = tv_s2;
        this.tv_s3 = tv_s3;
    }

    public Triangle(String tv_shape_name) {
        super(tv_shape_name);
    }



    @Override
    public double calculate_area() {
        double s = (tv_s1 + tv_s2 + tv_s3) / 2.0;
        return Math.sqrt(s * (s - tv_s1) * (s - tv_s2) * (s - tv_s3));
    }

    @Override
    public double calculate_perimeter() {
        return tv_s1 + tv_s2 + tv_s3;
    }
}
