package com.cyclone.bolt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.text.SimpleDateFormat;

public class MatchDetail extends AppCompatActivity {

    ImageView profilePic;
    TextView tv_name, tv_winLose, tv_raceDuration, tv_timeDifference, tv_raceType, tv_startTime, tv_endTime;
    Button b_avgSpeed;
    ImageView back_button;

    Match match;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        match = MatchHistoryListAdapter.matches.get(bundle.getInt("matchNumber"));
        System.out.println(match.getMatchId());
        setContentView(R.layout.match_detail);

        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreviousRaces();
            }
        });

        profilePic = findViewById(R.id.profilePic);
        tv_name = findViewById(R.id.tv_name);
        tv_winLose = findViewById(R.id.tv_winLose);
        tv_raceDuration = findViewById(R.id.tv_raceDuration);
        tv_timeDifference = findViewById(R.id.tv_timeDifference);
        tv_raceType = findViewById(R.id.tv_raceType);
        tv_startTime = findViewById(R.id.tv_startTime);
        tv_endTime = findViewById(R.id.tv_endTime);
        b_avgSpeed = findViewById(R.id.b_avgSpeed);

        setupUI();
    }

    public void setupUI() {

        // Set the current user's profile picture
        try {
            new DownloadImageTask(profilePic).execute(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
        } catch(Exception e) {e.printStackTrace();}

        // Saves first name into tv_name
        tv_name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ")[0]);

        int currAthlete;
        if(match.athlete1.get("uuid").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            currAthlete = 1;
        } else
            currAthlete = 2;

        // Determine if the user wins or loses
        try {
            if (((Timestamp) match.athlete1.get("completionTime")).toDate().getTime() < ((Timestamp) match.athlete2.get("completionTime")).toDate().getTime()
                    && currAthlete == 1) {
                // Current user is winner!
                tv_winLose.setText("You Won!");
            } else if (((Timestamp) match.athlete1.get("completionTime")).toDate().getTime() > ((Timestamp) match.athlete2.get("completionTime")).toDate().getTime()
                    && currAthlete == 2) {
                tv_winLose.setText("You Won!");
            } else {
                tv_winLose.setText("You Lost.");
            }
        } catch(ClassCastException e) {
            tv_winLose.setText("Not Enough Data.");
        }

        if(match.startTimestamp != null && match.matchCompletedTimestamp != null)
            tv_raceDuration.setText(String.format("Race Duration: %0.2f", ((match.matchCompletedTimestamp.toDate().getTime() - match.startTimestamp.toDate().getTime()) % (1000)) / 60.0 ));
        else
            tv_raceDuration.setText("Race Duration: N/A");

        // Get the two run times of each of the users
        try {
            float diff = ((Timestamp) match.athlete1.get("completionTime")).toDate().getTime() - ((Timestamp) match.athlete2.get("completionTime")).toDate().getTime();
            if (currAthlete == 1)
                diff *= -1;
            tv_timeDifference.setText("Time Difference (s): " + (diff / 1000));
        } catch(ClassCastException e) {
            tv_timeDifference.setText("Time Difference (s): N/A");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:MM aa");
        if(match.startTimestamp != null)
            tv_startTime.setText("Start: " + sdf.format(match.startTimestamp.toDate()));
        else
            tv_startTime.setText("Start: N/A");


        if(match.matchCompletedTimestamp != null)
            tv_endTime.setText("End: " + sdf.format(match.matchCompletedTimestamp.toDate()));
        else
            tv_endTime.setText("End: N/A");

        // needs the times
        try {
            b_avgSpeed.setText("Your Average Speed: " + String.valueOf((1 / 1609.34) * (1.0 / (60 * 60 * 1000)) * match.distance / (currAthlete == 1 ? ((Timestamp) match.athlete1.get("endTimestamp")).toDate().getTime() - ((Timestamp) match.athlete1.get("startTimestamp")).toDate().getTime() :
                    ((Timestamp) match.athlete2.get("endTimestamp")).toDate().getTime() - ((Timestamp) match.athlete2.get("startTimestamp")).toDate().getTime())));
        } catch(Exception e) {
            b_avgSpeed.setText("Your Average Speed: N/A");
        }
    }

    private void showPreviousRaces() {
        Intent intent = new Intent(MatchDetail.this, PreviousRaces.class);
        startActivity(intent);
        finish();
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