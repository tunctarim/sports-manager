package com.f216.sportsmanager.interfaces;
import com.f216.sportsmanager.models.Gender;

import java.util.Map;

public interface IPlayer {
    String getID();
    String getName();
    int getAge();
    Gender getGender();
    PlayerPosition getPosition();
    Map <String, Integer> getStatsMap();
    int getStat(String statName);
    void setStat(String statName, int value);
    int getOverallRating();
    int getHealth();
    void updateHealth(int newHealth);
    boolean isInjured();
    void setInjury(int matches);
    void decrementInjuryCounter();
    int getInjuryMatchesRemaining();
}
