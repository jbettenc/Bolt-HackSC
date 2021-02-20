package com.cyclone.bolt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.util.List;

// https://guides.codepath.com/android/using-the-recyclerview

public class PreviousRaces extends AppCompatActivity {
    ImageView back_button;
    RecyclerView rv_previousMatches;
    ImageView profilePicture;
    static MatchHistoryListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.previous_races);

        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMainMenu();
            }
        });

        rv_previousMatches = findViewById(R.id.previousMatches);
        adapter = new MatchHistoryListAdapter();
        rv_previousMatches.setAdapter(adapter);
        rv_previousMatches.setLayoutManager(new LinearLayoutManager(this));

        profilePicture = findViewById(R.id.profilePicture);
        try {
            new DownloadImageTask(profilePicture).execute(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
        } catch(Exception e) {e.printStackTrace();}


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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error loading picture", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
