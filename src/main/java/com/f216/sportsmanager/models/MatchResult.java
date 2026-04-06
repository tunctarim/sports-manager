package com.f216.sportsmanager.models;


import com.f216.sportsmanager.interfaces.ITeam;

import java.util.Objects;

public final class MatchResult {

    private final ITeam homeTeam;
    private final ITeam awayTeam;
    private final int   homeScore;
    private final int   awayScore;
    private final int   week;


    public MatchResult(ITeam homeTeam, ITeam awayTeam,
                       int homeScore, int awayScore, int week) {

        if (homeTeam == null)
            throw new IllegalArgumentException("Home team must not be null.");
        if (awayTeam == null)
            throw new IllegalArgumentException("Away team must not be null.");
        if (homeTeam == awayTeam)
            throw new IllegalArgumentException("Home and away teams must be different.");
        if (homeScore < 0)
            throw new IllegalArgumentException("Home score must be >= 0, got: " + homeScore);
        if (awayScore < 0)
            throw new IllegalArgumentException("Away score must be >= 0, got: " + awayScore);
        if (week < 1)
            throw new IllegalArgumentException("Week must be >= 1, got: " + week);

        this.homeTeam  = homeTeam;
        this.awayTeam  = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.week      = week;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public ITeam getHomeTeam()  { return homeTeam;  }
    public ITeam getAwayTeam()  { return awayTeam;  }
    public int   getHomeScore() { return homeScore; }
    public int   getAwayScore() { return awayScore; }
    public int   getWeek()      { return week;      }


    public boolean isHomeWin() { return homeScore > awayScore; }

    public boolean isAwayWin() { return awayScore > homeScore; }

    public boolean isDraw()    { return homeScore == awayScore; }


    public ITeam getWinner() {
        if (isHomeWin()) return homeTeam;
        if (isAwayWin()) return awayTeam;
        return null;
    }


    public ITeam getLoser() {
        if (isHomeWin()) return awayTeam;
        if (isAwayWin()) return homeTeam;
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchResult other)) return false;
        return homeScore == other.homeScore
                && awayScore == other.awayScore
                && week == other.week
                && homeTeam == other.homeTeam
                && awayTeam == other.awayTeam;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                System.identityHashCode(homeTeam),
                System.identityHashCode(awayTeam),
                homeScore, awayScore, week);
    }

    @Override
    public String toString() {
        return String.format("Week %d | %s %d – %d %s",
                week, homeTeam, homeScore, awayScore, awayTeam);
    }
}