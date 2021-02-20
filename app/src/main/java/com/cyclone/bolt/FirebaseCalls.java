package com.cyclone.bolt;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                String uuid = "";
                try {
                    uuid = String.valueOf(((Map<String, Object>) documentSnapshot.getData().get("athlete1")).get("uuid"));
                    System.out.println("FetchMatch: " + uuid);
                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(uuid)) {
                        uuid = String.valueOf(((Map<String, Object>) documentSnapshot.getData().get("athlete2")).get("uuid"));
                    }
                } catch(Exception e) {e.printStackTrace();}

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
                    matches.add(new Match(doc.getId().trim(), "", 0, null, null));
                }

                System.out.println("MATCHES LENGTH " + matches.size());
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
        Map<String, Object> temp = new HashMap<>();
        temp.put("distanceRun", distance);

        try {
            FirebaseFirestore.getInstance().collection("matchActivities").document(matchId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (((Map<String, Object>)documentSnapshot.getData().get("athlete1")).get("uuid").equals(uuid)) {
                        pushDistance("athlete1", uuid, matchId, distance, ((Map<String, Object>)documentSnapshot.getData().get("athlete1")));
                    } else {
                        pushDistance("athlete2", uuid, matchId, distance, ((Map<String, Object>)documentSnapshot.getData().get("athlete2")));
                    }
                }
            });
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void pushDistance(String athlete, String uuid, String matchId, float distance, Map<String, Object> temp) {
        temp.put("distanceRun", distance);
        if (athlete.equals("athlete1")) {
            FirebaseFirestore.getInstance().collection("matchActivities").document(matchId).update("athlete1", temp);
        } else {
            FirebaseFirestore.getInstance().collection("matchActivities").document(matchId).update("athlete2", temp);
        }
    }

    static ListenerRegistration reg;
    public static void listenForNewMatch(String uuid, EventListener<DocumentSnapshot> callback) {
        reg = FirebaseFirestore.getInstance().collection("users").document(uuid).addSnapshotListener(callback);
    }

    public static void clearCurrentMatch(String uuid) {
        reg.remove();
        FirebaseFirestore.getInstance().collection("users").document(uuid).update("currentMatch", "");
    }

    public static void pushPreviousMatch(String uuid, String matchId, Timestamp timestamp) {
        Map<String, Object> temp = new HashMap<>();
        temp.put("timestamp", timestamp);
        FirebaseFirestore.getInstance().collection("users").document(uuid).collection("previousMatches").document(matchId).set(temp);
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
