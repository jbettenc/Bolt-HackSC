package com.cyclone.bolt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PreviousRaces extends AppCompatActivity {
    TextView tv_mainMenu;
    RecyclerView rv_previousMatches;
    static MatchHistoryListAdapter adapter;
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

        rv_previousMatches = findViewById(R.id.previousMatches);
        adapter = new MatchHistoryListAdapter();
        rv_previousMatches.setAdapter(adapter);
        rv_previousMatches.setLayoutManager(new LinearLayoutManager(this));

        FirebaseCalls.fetchLatestMatches(new FirebaseCalls.MultipleFirestoreCallback() {
            @Override
            public void onCallback(List<Match> matches) {
                matchCallback(matches);
            }
        });
    }

    private void showMainMenu() {
        Intent intent = new Intent(PreviousRaces.this, Main.class);
        startActivity(intent);

        // TODO: Custom animations?
        finish();
    }

    // This will get an individual match's data
    private void matchCallback(Match match) {
        if(match.getMatchId() != null) {
            adapter.setMatch(match);
        }
    }

    // This will get our list of UUIDs
    private void matchCallback(List<Match> matches) {
        for(Match m : matches) {
            if(m.getMatchId() != null) {
                System.out.println("Match callback: " + "Match ID: " + m.getMatchId());
                adapter.setMatch(m);
                FirebaseCalls.fetchMatch(m.getMatchId(), new FirebaseCalls.SingleFirestoreCallback() {
                    @Override
                    public void onCallback(Match match) {
                        matchCallback(match);
                    }
                });
            } else {
                System.out.println("MatchCallback: getMatchId() is null");
            }
        }
    }
}
