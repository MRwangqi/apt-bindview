package com.codelang.bindview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.codelang.apt_library.BindViewTools;
import com.example.BindView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.main)
    TextView tx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViewTools.bind(this);

        tx.setText("大萨达");
    }
}
