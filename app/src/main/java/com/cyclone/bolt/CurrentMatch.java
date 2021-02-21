package com.cyclone.bolt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

public class CurrentMatch extends AppCompatActivity {

    static String currentMatchId;
    static Match currentMatch;

    Date startDate;

    static Button b_status;
    static TextView tv_opponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        currentMatchId = bundle.getString("currentMatchId");

        FirebaseCalls.fetchMatch(currentMatchId, new FirebaseCalls.SingleFirestoreCallback() {
            @Override
            public void onCallback(Match match) {
                loadMatchData(match);
            }
        });

        // Load the UI and start listening to location changes
        // Load the opponent details and the rest of the match details

        setContentView(R.layout.current_match);
//        tv_timer = findViewById(R.id.timer);

        b_status = findViewById(R.id.b_status);
        tv_opponentName = findViewById(R.id.tv_opponentName);
        // Load when we should start the match & start a LocationService immediately at that time

    }

    public static void matchComplete() {
        // Do some UI cleanup and changes now that the user has completed the run
        b_status.setText("Match complete");

        FirebaseCalls.pushPreviousMatch(FirebaseAuth.getInstance().getCurrentUser().getUid(), currentMatchId, currentMatch.startTimestamp);

//        Intent intent = new Intent(CurrentMatch.this, MatchDetail.class);
//        startActivity(intent);
    }

    private void loadMatchData(Match match) {
        this.currentMatch = match;
        // Start timer for startTimestamp
        startDate = currentMatch.startTimestamp.toDate();
        if(currentMatch.getOpponent() != null) {
            tv_opponentName.setText(currentMatch.getOpponent().name);
        } else {
            tv_opponentName.setText("");
        }

        startTimerThread();
    }

    private void startMatch() {
        startService(new Intent(this, LocationService.class));
    }

    public void startTimerThread() {
        Thread thread = new Thread(new Timer());
        thread.start();
    }

    private class Timer implements Runnable {

        @Override
        public void run() {
            long difference = 1;
            difference = startDate.getTime() - new Date().getTime();
            difference %= 1000;
            try {
                if(difference > 0)
                    Thread.sleep(difference);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            difference = 100;
            while(difference > 50) {
                difference = startDate.getTime() - new Date().getTime();
                int seconds = (int) (difference / 1000);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        b_status.setText(String.format("%d seconds...", seconds));
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            startMatch();
        }
    }
}