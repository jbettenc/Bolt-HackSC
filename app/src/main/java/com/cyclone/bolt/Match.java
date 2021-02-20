package com.cyclone.bolt;

import com.google.firebase.Timestamp;

public class Match {

    Timestamp startTimestamp;
    float distance = 0, distanceRun = 0, opponentDistanceRun = 0;
    String opponentUuid;
    User opponent;
    Timestamp matchCompletedTimestamp;
    String matchId;

    public Match(String matchId, String opponentUuid, float distanceToRun, Timestamp startTimestamp) {
        this.matchId = matchId;
        this.startTimestamp = startTimestamp;
        this.distance = distanceToRun;
        this.opponentUuid = opponentUuid;

        if(opponentUuid != null && opponentUuid.length() > 0) {
            FirebaseCalls.fetchUser(opponentUuid, new FirebaseCalls.SingleUserCallback() {
                @Override
                public void onCallback(User user) {
                    setOpponent(user);
                }
            });
        }
    }

    public Match(String matchId, String opponentUuid, float distanceToRun, Timestamp startTimestamp, Timestamp endTimestamp) {
        this.matchId = matchId;
        this.startTimestamp = startTimestamp;
        this.matchCompletedTimestamp = endTimestamp;
        this.distance = distanceToRun;
        this.opponentUuid = opponentUuid;

        if(opponentUuid != null && opponentUuid.length() > 0) {
            FirebaseCalls.fetchUser(opponentUuid, new FirebaseCalls.SingleUserCallback() {
                @Override
                public void onCallback(User user) {
                    setOpponent(user);
                }
            });
        }
    }

    public void setOpponent(User user) {
        this.opponent = user;
        PreviousRaces.adapter.notifyDataSetChanged();
    }

    public User getOpponent() {
        return opponent;
    }

    public void setOpponentUuid(String uuid) {
        this.opponentUuid = uuid;
        FirebaseCalls.fetchUser(opponentUuid, new FirebaseCalls.SingleUserCallback() {
            @Override
            public void onCallback(User user) {
                setOpponent(user);
            }
        });
    }

    public String getMatchId() {
        return matchId;
    }
}
