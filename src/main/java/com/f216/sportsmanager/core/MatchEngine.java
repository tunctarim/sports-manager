package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.MatchResult;

import java.util.List;
import java.util.Random;

public class MatchEngine {

    private ITeam homeTeam;
    private ITeam awayTeam;
    private ISport sport;
    private int homeScore;
    private int awayScore;
    private EndCondition endCondition;
    private int tick;
    private int tickInterval;
    private int matchLength;
    private int segmentCount;
    private int segmentLimit;
    private int week;


    private float HomeAttackScore;
    private float HomeDefenseScore;
    private float AwayAttackScore;
    private float AwayDefenseScore;

    private float HomeCapability;
    private float AwayCapability;

    private float HomeScoreProbability;
    private float AwayScoreProbability;

    private final float homeAdvantageMultiplier = 1.1F;

    private final float DefensiveTacticGoalMultiplier = 0.8F;
    private final float AttackTacticGoalMultiplier = 1.2F;
    private float FixedMultiplier; //This would not be implemented until M3

    private boolean isLive;
    private MatchResult matchResult;

    public void simulateMatch(Fixture fixture, ISport s, int week, boolean isLive) {
        homeTeam = fixture.getHome();
        awayTeam = fixture.getAway();
        homeScore = 0;
        awayScore = 0;
        tick = 0;
        tickInterval = s.getTickInterval();
        segmentCount = s.getSegmentCount();
        segmentLimit = s.getSegmentLimit();
        sport = s;
        endCondition = sport.getEndCondition();
        matchLength = sport.getTotalMatchLength();
        HomeAttackScore = 0;
        HomeDefenseScore = 0;
        AwayAttackScore = 0;
        AwayDefenseScore = 0;
        HomeScoreProbability = 0;
        AwayScoreProbability = 0;
        FixedMultiplier = 10.F; //This should be a sport specific value and would not be implemented until M3
        this.week = week;
        this.isLive = isLive;

        runGameLoop();
    }

    private void runGameLoop() {  //Currently does not support any UI inputs
        if (isLive){
            for (int i = 0; i < segmentLimit; i++) {
                if (matchResult != null){
                    return;
                }
                processTick();
            }
        }
        else {
            for (int i = 0; i < segmentCount; i++) {
                for (int j = 0; j < segmentLimit; j++) {
                    if (matchResult != null){
                        return;
                    }
                    processTick();
                }
            }
        }
    }

    private void processTick() {
        tick++;
        calculateProbabilities();
        Random rand = new Random(System.currentTimeMillis());
        double total = HomeScoreProbability + AwayScoreProbability + 5.0;
        double roll  = rand.nextDouble() * total;

        if (roll < HomeScoreProbability) {
            homeScore++;
        }
        else if (roll < HomeScoreProbability + AwayScoreProbability) {
            awayScore++;
        }
        if (checkVictoryStatus()){
            matchResult = generateMatchReports();
        }
    }

    private void calculateProbabilities() {

        //This whole section is a placeholder until M3 where the Sport specific classes will implement a player and position based score calculation classes.
        if (homeTeam.getTactic() == Tactic.DEFEND){
            HomeAttackScore = DefensiveTacticGoalMultiplier;
            HomeDefenseScore = 1 + (1 - DefensiveTacticGoalMultiplier);
        }
        else if (homeTeam.getTactic() == Tactic.ATTACK){
            HomeAttackScore = AttackTacticGoalMultiplier;
            HomeDefenseScore = 1 - (1 - AttackTacticGoalMultiplier);
        }
        else if (homeTeam.getTactic() == Tactic.BALANCED) {
            HomeAttackScore = 1;
            HomeDefenseScore = 1;
        }
        else {
            throw new IllegalStateException("Invalid tactic for home team: " + homeTeam.getTactic());
        }

        if (awayTeam.getTactic() == Tactic.DEFEND){
            AwayAttackScore = DefensiveTacticGoalMultiplier;
            AwayDefenseScore = 1 + (1 - DefensiveTacticGoalMultiplier);
        }
        else if (awayTeam.getTactic() == Tactic.ATTACK){
            AwayAttackScore = AttackTacticGoalMultiplier;
            AwayDefenseScore = 1 - (1 - AttackTacticGoalMultiplier);
        }
        else if (awayTeam.getTactic() == Tactic.BALANCED) {
            AwayAttackScore = 1;
            AwayDefenseScore = 1;
        }
        else {
            throw new IllegalStateException("Invalid tactic for away team: " + awayTeam.getTactic());
        }



        List<IPlayer> homePlayers = homeTeam.getPlayers();
        List<IPlayer> awayPlayers = awayTeam.getPlayers();
        HomeCapability = 0;
        AwayCapability = 0;
        for (IPlayer p : homePlayers) {
            HomeCapability += p.getOverallRating();
        }
        HomeCapability = HomeCapability / homePlayers.size();
        for (IPlayer p : awayPlayers) {
            AwayCapability += p.getOverallRating();
        }
        AwayCapability = AwayCapability / awayPlayers.size();


        HomeScoreProbability = FixedMultiplier * HomeAttackScore * AwayDefenseScore * HomeCapability * homeAdvantageMultiplier;
        AwayScoreProbability = FixedMultiplier *  AwayAttackScore * HomeDefenseScore * AwayCapability;
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
        if (endCondition == EndCondition.SCORE_LIMIT && (homeScore >= matchLength || awayScore >= matchLength)){
            return true;
        }
        if (endCondition == EndCondition.KNOCKOUT){
            //Implement KNOCKOUT End Condition for M3
            return false;
        }
        return false;
    }

    public MatchResult generateMatchReports() {
        MatchResult matchResult = new MatchResult(homeTeam,awayTeam,homeScore,awayScore,week);
        return matchResult;
    }
}
