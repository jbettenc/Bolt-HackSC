package com.cyclone.bolt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Date;

public class CurrentMatch extends AppCompatActivity {

    String currentMatchId;
    static Match currentMatch;

    Date startDate;

    TextView tv_timer;

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

        // Load when we should start the match & start a LocationService immediately at that time

    }

    public static void matchComplete() {
        // TODO: Do some UI cleanup and changes now that the user has completed the run
    }

    private void loadMatchData(Match match) {
        this.currentMatch = match;
        // Start timer for startTimestamp
        startDate = currentMatch.startTimestamp.toDate();

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
                Thread.sleep(difference);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            difference = 100;
            while(difference > 50) {
                difference = startDate.getTime() - new Date().getTime();
                int seconds = (int) (difference / 1000) % 60;
                tv_timer.setText(String.format("%d seconds...", seconds));
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