package com.sportsmanager.core.model;
import java.util.Map;

public interface IPlayer {
    String getID();
    String getName();
    int getAge();
    String getGender();
    String getPosition();
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
