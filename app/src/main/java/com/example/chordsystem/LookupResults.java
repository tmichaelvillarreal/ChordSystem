package com.example.chordsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class LookupResults extends AppCompatActivity {

    private TextView processTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookup_results);

        processTextView = findViewById(R.id.tv_lookupResultsActivity_process);
        Intent intent = getIntent();
        String process = intent.getStringExtra("process");

        processTextView.setText(process);
    }
}