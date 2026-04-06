package com.f216.sportsmanager.interfaces;

import com.f216.sportsmanager.enums.EndCondition;

import java.util.List;
import java.util.Map;

public interface ISport{

    String getSportName();

    int getSegmentCount();

    int getPointsPerWin();

    int getPointsPerDraw();

    int getRosterSize();

    int getSegmentLimit();
    // Returns the limit of the ticks. (1 segment(half) in a football game is 45 minutes, so it returns 45 or 1 segment(set) in a volleyball game is 25 points so it returns 25).

    int getTickInterval();
    // Returns how many milliseconds a “tick” (1 minute or 1 rally) lasts.

    int getTotalMatchLength();
    // Returns Segment Count * Segment Limit, which is the total match length.

    List<String> getRequiredStats();
    // Returns a list of strings so the Factory knows which random stats to generate for this specific sport.

    EndCondition getEndCondition();
    // Returns either TIME_LIMIT or SCORE_LIMIT to end a game(time limit for football, basketball, score limit for volleyball.)

    List<PlayerPosition> getRequiredPositions();
    // Returns required positions and the quantity of them for the sport.
}