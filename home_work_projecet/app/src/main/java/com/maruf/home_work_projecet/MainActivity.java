package com.maruf.home_work_projecet;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText et_rect_l, et_rect_w, et_circle_r, et_tri_s1, et_tri_s2, et_tri_s3;
    Button btn_calculate;
    TextView tv_rect_result, tv_circle_result, tv_tri_result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        et_rect_l = findViewById(R.id.et_rect_l);
        et_rect_w = findViewById(R.id.et_rect_w);
        et_circle_r = findViewById(R.id.et_circle_r);
        et_tri_s1 = findViewById(R.id.et_tri_s1);
        et_tri_s2 = findViewById(R.id.et_tri_s2);
        et_tri_s3 = findViewById(R.id.et_tri_s3);
        btn_calculate = findViewById(R.id.btn_calculate);
        tv_rect_result = findViewById(R.id.tv_rect_result);
        tv_circle_result = findViewById(R.id.tv_circle_result);
        tv_tri_result = findViewById(R.id.tv_tri_result);


        btn_calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    // Rectangle
                    float rL = Float.parseFloat(et_rect_l.getText().toString());
                    float rW = Float.parseFloat(et_rect_w.getText().toString());
                    Shape rectangle = new Rectangle("Rectangle", rL, rW);

                    tv_rect_result.setText(rectangle.getTv_shape_name() +
                            "\nArea: " + rectangle.calculate_area() +
                            "\nPerimeter: " + rectangle.calculate_perimeter());

                    // Circle
                    float cR = Float.parseFloat(et_circle_r.getText().toString());
                    Shape circle = new Circle("Circle", cR);

                    tv_circle_result.setText(circle.getTv_shape_name() +
                            "\nArea: " + circle.calculate_area() +
                            "\nPerimeter: " + circle.calculate_perimeter());

                    // Triangle
                    float s1 = Float.parseFloat(et_tri_s1.getText().toString());
                    float s2 = Float.parseFloat(et_tri_s2.getText().toString());
                    float s3 = Float.parseFloat(et_tri_s3.getText().toString());
                    Shape triangle = new Triangle("Triangle", s1, s2, s3);

                    tv_tri_result.setText(triangle.getTv_shape_name() +
                            "\nArea: " + triangle.calculate_area() +
                            "\nPerimeter: " + triangle.calculate_perimeter());

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "সবগুলো ঘরে নম্বর দিন!", Toast.LENGTH_SHORT).show();
                }


            }

        });


    }
}