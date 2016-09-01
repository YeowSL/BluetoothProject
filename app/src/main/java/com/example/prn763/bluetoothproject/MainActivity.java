package com.example.prn763.bluetoothproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button receiverBtn, intermediateBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_intro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialization();

        receiverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReceiverActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        intermediateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothAgentActivity.class);
                startActivityForResult(intent, 2);
            }
        });


    }

    private void initialization() {
        receiverBtn = (Button)findViewById(R.id.receiver);
        intermediateBtn = (Button)findViewById(R.id.intermediate);
    }


}
