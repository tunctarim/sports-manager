package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.MatchResult;

public class MatchEngine {

    private ITeam homeTeam;
    private ITeam awayTeam;
    private ISport sport;
    private int homeScore;
    private int awayScore;
    private EndCondition endCondition;
    private int tick;
    private int matchLength;
    public void simulateMatch(ITeam home, ITeam away, ISport s, boolean isLive) {
        homeTeam = home;
        awayTeam = away;
        sport = s;
        endCondition = sport.getEndCondition();
        matchLength = sport.getTotalMatchLength();
    }

    private void runGameLoop() {

    }

    private void processTick() {

    }

    private double calculateProbabilities() {
        return 0;
    }

    private boolean determineScoringEvents() {
        return false;
    }

    private boolean checkVictoryStatus() {
        if (endCondition == null) {
            throw new IllegalStateException("endCondition is null");
        }
        if (endCondition == EndCondition.TIME_LIMIT && tick >= matchLength) {
            return true;
        }
        if (endCondition == EndCondition.SCORE_LIMIT && (homeScore >= 5 || awayScore >= 5)){
            return true;
        }
        if (endCondition == EndCondition.KNOCKOUT){
            return true;
        }
        return true;
    }

    public MatchResult generateMatchReports() {
        //TODO: Check hardcoded week value
        MatchResult matchResult = new MatchResult(homeTeam,awayTeam,homeScore,awayScore,1);
        return matchResult;
    }
}
