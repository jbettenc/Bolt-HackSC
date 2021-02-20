package com.cyclone.bolt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MatchDetail extends AppCompatActivity {

    TextView tv_name, tv_winLose, tv_raceDuration, tv_timeDifference, tv_raceType, tv_startTime, tv_endTime;
    Button b_avgSpeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_detail);

        tv_name = findViewById(R.id.tv_name);
        tv_winLose = findViewById(R.id.tv_winLose);
        tv_raceDuration = findViewById(R.id.tv_raceDuration);
        tv_timeDifference = findViewById(R.id.tv_timeDifference);
        tv_raceType = findViewById(R.id.tv_raceType);
        tv_startTime = findViewById(R.id.tv_startTime);
        tv_endTime = findViewById(R.id.tv_endTime);
        b_avgSpeed = findViewById(R.id.b_avgSpeed);
    }
}