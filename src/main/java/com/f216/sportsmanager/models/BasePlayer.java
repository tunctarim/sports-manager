package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.Gender;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import com.f216.sportsmanager.interfaces.IPlayer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BasePlayer implements IPlayer, Serializable {

    private final int MAX_STAT   = 100;
    private final int MIN_STAT   = 1;
    private final int MAX_HEALTH = 100;
    private final int MIN_HEALTH = 0;
    private final String id;
    private final String name;
    private final int age;
    private final Gender gender;
    private final PlayerPosition position;
    private final Map<String, Integer> stats;
    private int health;
    private int injuryMatchesRemaining;

    public BasePlayer(String name, int age, Gender gender, PlayerPosition position){
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Player name cannot be empty.");
        if (age <= 0)
            throw new IllegalArgumentException("Age must be positive: " + age);
        if (gender == null)
            throw new IllegalArgumentException("Gender cannot be null.");
        if (position == null)
            throw new IllegalArgumentException("Position cannot be null.");

        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.position = position;
        this.stats = new HashMap<>();
        this.health = MAX_HEALTH;
        this.injuryMatchesRemaining = 0;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public Gender getGender() {
        return gender;
    }

    @Override
    public PlayerPosition getPosition() {
        return position;
    }

    @Override
    public Map<String, Integer> getStatsMap() {
        return Collections.unmodifiableMap(stats);
    }

    @Override
    public int getStat(String statName) {
        if (statName == null) return 0;
        return stats.getOrDefault(statName.toLowerCase(), 0);
    }

    @Override
    public void setStat(String statName, int value) {
        if (statName == null || statName.isBlank())
            throw new IllegalArgumentException("Statistic cannot be empty.");

        int clamped = Math.max(MIN_STAT, Math.min(MAX_STAT, value));
        stats.put(statName.toLowerCase(), clamped);
    }

    @Override
    public int getOverallRating() {
        if (stats.isEmpty()) return 0;
        return (int) stats.values().stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void updateHealth(int newHealth) {
        this.health = Math.max(MIN_HEALTH, Math.min(MAX_HEALTH, newHealth));
    }

    @Override
    public boolean isInjured() {
        return injuryMatchesRemaining > 0 || health == MIN_HEALTH;
    }

    @Override
    public void setInjury(int matches) {
        if (matches < 0)
            throw new IllegalArgumentException(
                    "Injury matches cannot be negative: " + matches);
        this.injuryMatchesRemaining = matches;
    }

    @Override
    public void decrementInjuryCounter() {
        if (injuryMatchesRemaining > 0) {
            injuryMatchesRemaining--;
        }
    }

    @Override
    public int getInjuryMatchesRemaining() {
        return injuryMatchesRemaining;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasePlayer other)) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s (%s | %s | OVR:%d | HP:%d)",
                name,
                position.getCode(),
                gender.getDisplayName(),
                getOverallRating(),
                health);
    }
}
