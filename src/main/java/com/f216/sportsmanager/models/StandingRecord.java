package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ITeam;

import java.util.Map;


public class StandingRecord implements Comparable<StandingRecord> {

    private final ITeam team;


    public StandingRecord(ITeam team) {
        if (team == null) {
            throw new IllegalArgumentException("Team must not be null.");
        }
        this.team = team;
    }


    public ITeam getTeam() {
        return team;
    }


    public int getWins() {
        return statOrZero("wins");
    }


    public int getLosses() {
        return statOrZero("losses");
    }

    public int getPoints() {
        return team.getPoints();
    }

    public int getPointsFor() {
        return statOrZero("pointsFor");
    }


    public int getPointsAgainst() {
        return statOrZero("pointsAgainst");
    }


    public int getGoalDifference() {
        return statOrZero("pointDifferential");
    }

    public int getMatchesPlayed() {
        return getWins() + getLosses();
    }


    @Override
    public int compareTo(StandingRecord other) {
        // 1. Points descending
        int cmp = Integer.compare(other.getPoints(), this.getPoints());
        if (cmp != 0)
            return cmp;
        // 2. Goal difference descending
        cmp = Integer.compare(other.getGoalDifference(), this.getGoalDifference());
        if (cmp != 0)
            return cmp;
        // 3. Points for descending (tiebreaker)
        return Integer.compare(other.getPointsFor(), this.getPointsFor());
    }


    @Override
    public String toString() {
        return String.format(
                "StandingRecord{team=%s, pts=%d, mp=%d, w=%d, l=%d, gf=%d, ga=%d, gd=%+d}",
                team,
                getPoints(),
                getMatchesPlayed(),
                getWins(),
                getLosses(),
                getPointsFor(),
                getPointsAgainst(),
                getGoalDifference());
    }

    private int statOrZero(String key) {
        Map<String, Integer> stats = team.getTeamStats();
        if (stats == null)
            return 0;
        return stats.getOrDefault(key, 0);
    }
}
