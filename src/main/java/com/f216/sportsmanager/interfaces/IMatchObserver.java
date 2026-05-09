package com.f216.sportsmanager.interfaces;

import com.f216.sportsmanager.models.MatchEvent;
import com.f216.sportsmanager.models.MatchResult;

public interface IMatchObserver {

    /**
     * Called whenever a match event occurs (goal, foul, etc.)
     */
    void onMatchEvent(MatchEvent event);

    /**
     * Called when a segment ends (halftime, quarter break, etc.)
     */
    void onSegmentEnd(int segmentNumber, int homeScore, int awayScore);

    /**
     * Called when the match ends
     */
    void onMatchEnded(MatchResult result);

    /**
     * Called on every tick of the match
     */
    void onTick(int tick);
}
