package com.nearcompany.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.nearcompany.R;
import com.nearcompany.utility.Utility;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utility utility = Utility.getInstance(this);

        utility.generateHashkey();
    }
}
