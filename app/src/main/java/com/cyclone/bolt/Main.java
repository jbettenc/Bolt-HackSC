package com.cyclone.bolt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Main extends AppCompatActivity {

    Button b_startMatch;

    TextView tv_previousMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        b_startMatch = findViewById(R.id.b_startMatch);
        b_startMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        tv_previousMatches = findViewById(R.id.tv_previousMatches);
        tv_previousMatches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreviousRaces();
            }
        });
    }

    private void showPreviousRaces() {
        Intent intent = new Intent(Main.this, PreviousRaces.class);
        startActivity(intent);

        // TODO: Custom animations?
        finish();
    }
}