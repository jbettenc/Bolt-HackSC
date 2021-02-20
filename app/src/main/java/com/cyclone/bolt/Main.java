package com.cyclone.bolt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Main extends AppCompatActivity {

    Button b_startMatch;

    TextView tv_previousMatches;

    FirebaseFirestore db;

    String currentMatchId = "";

    EventListener<DocumentSnapshot> listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        db = FirebaseFirestore.getInstance();

        b_startMatch = findViewById(R.id.b_startMatch);
        b_startMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send the request to firebase
                FirebaseCalls.requestMatch(new FirebaseCalls.SingleErrorCallback() {
                    @Override
                    public void onCallback(String msg) {
                        // This will allow us to listen for new updates to the currentMatch item
                        listener = new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                if(error != null) {
                                    Log.w("Main", "listen:error", error);
                                    return;
                                }
                                try {
                                    if (value != null && !value.getData().get("currentMatch").equals("") && !value.getData().get("currentMatch").equals(currentMatchId)) {
                                        currentMatchId = (String)value.getData().get("currentMatch");

                                        // Clear currentMatch
                                        FirebaseCalls.clearCurrentMatch(FirebaseAuth.getInstance().getUid());

                                        // Load our current match activity
                                        showCurrentMatch();
                                    }
                                } catch(NullPointerException e) {e.printStackTrace();}
                            }
                        };

                        FirebaseCalls.listenForNewMatch(FirebaseAuth.getInstance().getUid(), listener);
                    }
                });

                // TODO: Start listening to changes to /users/uuid/currentMatch
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

    private void showCurrentMatch() {
        Intent intent = new Intent(Main.this, CurrentMatch.class);
        startActivity(intent);
        intent.putExtra("currentMatchId", currentMatchId);

        finish();
    }
}