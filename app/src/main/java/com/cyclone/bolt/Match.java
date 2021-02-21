package com.cyclone.bolt;

import com.google.firebase.Timestamp;

import java.util.Map;

public class Match {

    Timestamp startTimestamp;
    float distance = 0, distanceRun = 0, opponentDistanceRun = 0;
    String opponentUuid;
    User opponent;
    Timestamp matchCompletedTimestamp;
    String matchId;
    Map<String, Object> athlete1;
    Map<String, Object> athlete2;


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

    public Match(String matchId, String opponentUuid, float distanceToRun, Timestamp startTimestamp, Timestamp endTimestamp, Map<String, Object> athlete1, Map<String, Object> athlete2) {
        this.matchId = matchId;
        this.startTimestamp = startTimestamp;
        this.matchCompletedTimestamp = endTimestamp;
        this.distance = distanceToRun;
        this.opponentUuid = opponentUuid;

        this.athlete1 = athlete1;
        this.athlete2 = athlete2;

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
        if(PreviousRaces.adapter != null) {
            PreviousRaces.adapter.notifyDataSetChanged();
        }
        if(CurrentMatch.tv_opponentName != null) {
            CurrentMatch.tv_opponentName.setText(user.name);
        }
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
