package com.cyclone.bolt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://firebase.google.com/docs/android/setup
// https://firebase.google.com/docs/auth/android/firebaseui

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    FirebaseFirestore db;
    FirebaseUser user;

    Button b_signInWithGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        overridePendingTransition(0, 0);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            db = FirebaseFirestore.getInstance();
            checkPermissionsAndData();
        } else {
            setContentView(R.layout.login);
            db = FirebaseFirestore.getInstance();

            b_signInWithGoogle = findViewById(R.id.b_signInWithGoogle);
            b_signInWithGoogle.setOnClickListener(view -> createSignInIntent());
            createSignInIntent();
        }
    }

    public void createSignInIntent() {
        b_signInWithGoogle.setVisibility(View.GONE);

        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();
                checkPermissionsAndData();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                b_signInWithGoogle.setVisibility(View.VISIBLE);
                System.err.println("Sign in failed.");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if(grantResults.length < permissions.length) {
                    // error
                    setContentView(R.layout.permissions_required);
                } else {
                    for(int i = 0; i < permissions.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            // error
                            setContentView(R.layout.permissions_required);
                            return;
                        }
                    }

                    // success
                    checkUserData();
                }
                break;
        }
    }

    private void checkPermissionsAndData() {

        // We are going to need location & internet permissions in order to ensure basic functionality of our app
        List<String> permsToRequest = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permsToRequest.add(Manifest.permission.INTERNET);
        }

        // Access background location on newer devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        if(permsToRequest.size() > 0) {
            // Request Permissions
            ActivityCompat.requestPermissions(this, permsToRequest.toArray(new String[permsToRequest.size()]), 0);
        } else {
            checkUserData();
        }
    }

    private void checkUserData() {
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if(!documentSnapshot.exists() || documentSnapshot.getData().get("mileTime") == null) {
                setContentView(R.layout.enter_mile_time);
                EditText minutesEditText = findViewById(R.id.minutesEditText);
                EditText secondEditText = findViewById(R.id.secondsEditText);
                Button continueButton = findViewById(R.id.continueButton);
                continueButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Integer.valueOf(minutesEditText.getText().toString()) > 0 || Integer.valueOf(secondEditText.getText().toString()) > 0) {
                            int totalSec = Integer.valueOf(minutesEditText.getText().toString()) * 60 + Integer.valueOf(secondEditText.getText().toString());
                            saveNewUserProfile(user, totalSec);
                        }
                    }
                });
            } else {
                // Check and see if the user has changed their google profile picture or display name and update it in our servers accordingly
                if(!documentSnapshot.getData().get("profilePicUrl").equals(user.getPhotoUrl().toString())) {
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("profilePicUrl", user.getPhotoUrl().toString());
                    db.collection("users").document(user.getUid()).update(newData);
                }
                if(documentSnapshot.getData().get("name").equals(user.getDisplayName())) {
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("name", user.getDisplayName());
                    db.collection("users").document(user.getUid()).update(newData);
                }

                goToMainMenu();
            }
        }).addOnFailureListener(e -> System.out.println("checkUserData: " + "Error checking if user exists: " + e.getMessage()));
    }

    private void saveNewUserProfile(FirebaseUser user, int mileTime) {
        Map<String, Object> userData = new HashMap<>();
        if(user != null) {
            userData.put("name", user.getDisplayName());
            userData.put("email", user.getEmail());
            userData.put("uuid", user.getUid());
            userData.put("profilePicUrl", user.getPhotoUrl().toString());
            userData.put("mileTime", mileTime);
            db.collection("users").document(user.getUid()).set(userData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.err.println("Sign in success");
                            goToMainMenu();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("BOLT", "Error adding document", e);
                        }
                    });
        }
    }

    private void goToMainMenu() {
        Intent intent = new Intent(Login.this, Main.class);
        startActivity(intent);
    }
}