package com.cyclone.bolt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PreviousRaces extends AppCompatActivity {
    TextView tv_mainMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.previous_races);

        tv_mainMenu = findViewById(R.id.tv_mainMenu);
        tv_mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMainMenu();
            }
        });
    }

    private void showMainMenu() {
        Intent intent = new Intent(PreviousRaces.this, Main.class);
        startActivity(intent);

        // TODO: Custom animations?
        finish();
    }
}
