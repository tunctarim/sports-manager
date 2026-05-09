package com.f216.sportsmanager.models;

public class MatchEvent {

    public enum EventType {
        GOAL, POSSESSION, FOUL, INJURY, SUBSTITUTION, MATCH_START, MATCH_END, SEGMENT_END
    }

    private final EventType type;
    private final int tick;
    private final int homeScore;
    private final int awayScore;
    private final String description;
    private final int segmentNumber; // -1 if not applicable

    public MatchEvent(EventType type, int tick, int homeScore, int awayScore, String description) {
        this(type, tick, homeScore, awayScore, description, -1);
    }

    public MatchEvent(EventType type, int tick, int homeScore, int awayScore, String description, int segmentNumber) {
        this.type = type;
        this.tick = tick;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.description = description;
        this.segmentNumber = segmentNumber;
    }

    public EventType getType() { return type; }
    public int getTick() { return tick; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public String getDescription() { return description; }
    public int getSegmentNumber() { return segmentNumber; }

    @Override
    public String toString() {
        return String.format("[Tick %d] %s | Score: %d - %d | %s", tick, type, homeScore, awayScore, description);
    }
}
