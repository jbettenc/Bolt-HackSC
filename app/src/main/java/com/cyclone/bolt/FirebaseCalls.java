package com.cyclone.bolt;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseCalls {

    public static void fetchUser(String uuid, SingleUserCallback callback) {
        System.out.println("FetchUser: " + uuid);
        FirebaseFirestore.getInstance().collection("users").document(uuid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = new User((String)documentSnapshot.getData().get("profilePicUrl"), (String)documentSnapshot.getData().get("name"), (Double)documentSnapshot.getData().get("mileTime"),
                        (Double)documentSnapshot.getData().get("mileAvg"), null, (Long)documentSnapshot.getData().get("numberOfWins"));
                callback.onCallback(user);
            }
        });
    }

    public static void fetchMatch(String matchUid, SingleFirestoreCallback firestoreCallback) {
        FirebaseFirestore.getInstance().collection("matchActivities").document(matchUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String uuid = String.valueOf(documentSnapshot.getData().get("athlete1"));
                System.out.println("FetchMatch: " + uuid);
                if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(uuid)) {
                    uuid = String.valueOf(documentSnapshot.getData().get("athlete2"));
                }

                Match match = new Match(documentSnapshot.getId(), uuid, Float.parseFloat(String.valueOf(documentSnapshot.getData().get("distance"))), ((Timestamp)documentSnapshot.getData().get("startTimestamp")));

                firestoreCallback.onCallback(match);
            }
        });
    }

    static List<Match> matches;
    public static void fetchLatestMatches(MultipleFirestoreCallback multipleFirestoreCallback) {
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("previousMatches").orderBy("timestamp").limit(20).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                matches = new ArrayList<>();
                for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    matches.add(new Match(doc.getId(), "", 0, null, null));
                }

                multipleFirestoreCallback.onCallback(matches);
            }
        });
    }

    // Places the user into the queue to be processed by the backend
    public static void requestMatch(SingleErrorCallback errorCallback) {
        FirebaseFirestore.getInstance().collection("matchwaiting")
                .document("matchwaiting").update("users", FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser().getUid())).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                errorCallback.onCallback("Failed to request a match. Please try again.");
            }
        });
    }

    public static void pushDistance(String uuid, String matchId, float distance) {
        FirebaseFirestore.getInstance().collection("matchActivities").document(uuid).update("distanceRun", distance);
    }

    static ListenerRegistration reg;
    public static void listenForNewMatch(String uuid, EventListener<DocumentSnapshot> callback) {
        reg = FirebaseFirestore.getInstance().collection("users").document(uuid).addSnapshotListener(callback);
    }

    public static void clearCurrentMatch(String uuid) {
        reg.remove();
        FirebaseFirestore.getInstance().collection("users").document(uuid).update("currentMatch", "");
    }

    public interface SingleFirestoreCallback {
        void onCallback(Match match);
    }

    public interface SingleUserCallback {
        void onCallback(User user);
    }

    public interface SingleFirestoreStringCallback {
        void onCallback(String string);
    }

    public interface MultipleFirestoreCallback {
        void onCallback(List<Match> matches);
    }

    public interface SingleErrorCallback {
        void onCallback(String msg);
    }
}
