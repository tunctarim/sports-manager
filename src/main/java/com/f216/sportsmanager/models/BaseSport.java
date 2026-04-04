package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.enums.EndCondition; // Assuming you created this Enum
import java.util.List;

public abstract class BaseSport implements ISport {

    private final String sportName;
    private final int pointsPerWin;
    private final int pointsPerDraw;
    private final int rosterSize;
    private final int segmentCount;
    private final int segmentLimit;
    private final EndCondition endCondition;
    private final int tickInterval;

    public BaseSport(String name, int ppW, int ppD, int roster, int segments, int limit, EndCondition condition, int tickInterval) {

        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Sport name cannot be empty.");
        if (roster <= 0) throw new IllegalArgumentException("Roster size must be positive.");
        if (segments <= 0) throw new IllegalArgumentException("Segment count must be at least 1.");
        if (limit < 0) throw new IllegalArgumentException("Segment limit cannot be negative.");
        if (tickInterval <= 0) throw new IllegalArgumentException("Tick interval must be at least 1 second.");

        this.sportName = name;
        this.pointsPerWin = ppW;
        this.pointsPerDraw = ppD;
        this.rosterSize = roster;
        this.segmentCount = segments;
        this.segmentLimit = limit;
        this.endCondition = condition;
        this.tickInterval = tickInterval;
    }

    @Override
    public String getSportName() {
        return sportName;
    }

    @Override
    public int getPointsPerWin() {
        return pointsPerWin;
    }

    @Override
    public int getPointsPerDraw() {
        return pointsPerDraw;
    }

    @Override
    public int getRosterSize() {
        return rosterSize;
    }

    @Override
    public int getSegmentCount() {
        return segmentCount;
    }

    @Override
    public int getSegmentLimit() {
        return segmentLimit;
    }

    @Override
    public EndCondition getEndCondition() {
        return endCondition;
    }

    @Override
    public int getTickInterval() {
        return this.tickInterval;
    }

    @Override
    public int getTotalMatchLength() {
        return segmentCount * segmentLimit;
    }

    @Override
    public abstract List<String> getRequiredStats();
}