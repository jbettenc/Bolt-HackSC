package com.cyclone.bolt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

public class Main extends AppCompatActivity {

    Button b_startMatch;
    ImageView back_button;

    ImageButton b_avatar;

    FirebaseFirestore db;

    String currentMatchId = "";

    EventListener<DocumentSnapshot> listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0,0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.main);

        db = FirebaseFirestore.getInstance();

        back_button = findViewById(R.id.back_button);
        b_avatar = findViewById(R.id.avatar);
        try {
            new DownloadImageTask(b_avatar).execute(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
        } catch(Exception e) {e.printStackTrace();}

        b_startMatch = findViewById(R.id.b_status);
        b_startMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                b_startMatch.setText("Looking for match...");
                b_startMatch.setEnabled(false);
                back_button.setVisibility(View.VISIBLE);
                // Send the request to firebase
                FirebaseCalls.requestMatch(new FirebaseCalls.SingleErrorCallback() {
                    @Override
                    public void onCallback(String msg) {
                        //error
                        back_button.setVisibility(View.GONE);
                        b_avatar.setEnabled(true);
                        b_startMatch.setEnabled(true);
                        b_startMatch.setText("Start Match");
                    }
                });

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
//                                System.out.println(value.getData().get("currentMatch"));
                                currentMatchId = (String) value.getData().get("currentMatch");

                                // Clear currentMatch
                                FirebaseCalls.clearCurrentMatch(FirebaseAuth.getInstance().getUid());

                                // Load our current match activity
                                showCurrentMatch();
                            }
                        } catch(NullPointerException e) {e.printStackTrace();}
                    }
                };

                FirebaseCalls.listenForNewMatch(FirebaseAuth.getInstance().getUid(), listener);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("https://bolt-21.herokuapp.com/match")
                                .build();
                        try {
                            Response response = client.newCall(request).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        });

        b_avatar = findViewById(R.id.avatar);
        b_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreviousRaces();
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back_button.setVisibility(View.GONE);
                b_avatar.setEnabled(true);
                b_startMatch.setEnabled(true);
                b_startMatch.setText("Start Match");
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
        intent.putExtra("currentMatchId", currentMatchId);
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