package com.cyclone.bolt;

import java.util.List;

public class User {
    String profilePicUrl;
    String name;
    Double mileSec;
    Double mileAvg;
    List<String> friends;
    Long numberOfWins;

    public User(String profilePicUrl, String name, Double mileSec, Double mileAvg, List<String> friends, Long numberOfWins) {
       this.profilePicUrl = profilePicUrl;
       this.name = name;
       this.mileSec = mileSec;
       this.mileAvg = mileAvg;
       this.friends = friends;
       this.numberOfWins = numberOfWins;
    }
}
