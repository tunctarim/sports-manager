package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IMatchObserver;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.MatchEvent;
import com.f216.sportsmanager.models.MatchResult;

import java.util.ArrayList;
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
    private int currentSegment;


    private float HomeAttackScore;
    private float HomeDefenseScore;
    private float AwayAttackScore;
    private float AwayDefenseScore;

    private float HomeCapability;
    private float AwayCapability;

    private float HomeScoreProbability;
    private float AwayScoreProbability;

    private final float homeAdvantageMultiplier = 1.07F;

    private final float DefensiveTacticGoalMultiplier = 0.75F;
    private final float AttackTacticGoalMultiplier = 1.25F;
    private float FixedMultiplier; //This would not be implemented until M3

    private boolean isLive;
    private MatchResult matchResult;
    private int attackMultiply;

    private final Random rand = new Random();

    // --- LIVE MODE SUPPORT ---
    private volatile boolean isPaused = false;
    private final Object pauseLock = new Object();
    private final List<IMatchObserver> observers = new ArrayList<>();
    private final List<MatchEvent> matchEvents = new ArrayList<>();

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
        FixedMultiplier = s.getFixedMultiplier(); //This should be a sport specific value and would not be implemented until M3
        matchResult = null;
        attackMultiply = 0;
        this.week = week;
        this.isLive = isLive;
        isPaused = false;

        try {
            runGameLoop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (matchResult == null) {
            matchResult = generateMatchReports(); // safety fallback
        }
    }

    private void runGameLoop() throws InterruptedException {
        if (isLive) {
            // Live mode: process segment-by-segment with pauses at boundaries
            for (currentSegment = 0; currentSegment < segmentCount; currentSegment++) {
                // Process this segment tick-by-tick with delays
                for (int i = 0; i < segmentLimit; i++) {
                    synchronized (pauseLock) {
                        while (isPaused) {
                            pauseLock.wait();
                        }
                    }
                    if (matchResult != null) {
                        return;
                    }
                    processTick();
                    Thread.sleep(tickInterval);
                }

                // PAUSE AT SEGMENT END (e.g., halftime)
                if (currentSegment < segmentCount - 1) {  // Don't pause after final segment
                    isPaused = true;
                    // Notify observers of segment pause
                    notifySegmentEnd(currentSegment);
                }
            }
        } else {
            // Non-live mode: process entire match instantly
            while (tick < matchLength) {
                if (matchResult != null) {
                    return;
                }
                processTick();
            }
        }
    }

    private void processTick() {
        tick++;
        
        if (isLive) {
            for (IMatchObserver observer : observers) {
                observer.onTick(tick);
            }
        }

        calculateProbabilities();

        // 1. Define how likely ANY goal is to happen this tick (e.g., 5% chance)
        // Adjust this to control the "pace" of the game.
        double baseScoringChance = (FixedMultiplier / 20) + ((double) attackMultiply / 200);

        // 2. Normalize the scores so they are relative to each other
        // This prevents the code from "breaking" if scores are huge.
        double rawTotal = HomeScoreProbability + AwayScoreProbability + FixedMultiplier;

        // Safety check to avoid division by zero
        if (rawTotal > 0) {
            double homeShare = HomeScoreProbability / rawTotal;
            double awayShare = AwayScoreProbability / rawTotal;

            // 3. The "Roll"
            double roll = rand.nextDouble();

            // 4. Logic: First check if a goal happens at all
            if (roll < baseScoringChance) {
                // A goal happened! Now decide who got it based on their "share"
                // We use a second roll or sub-divide the baseScoringChance
                double goalRoll = rand.nextDouble();

                if (goalRoll < homeShare) {
                    homeScore++;
                    MatchEvent event = new MatchEvent(
                            MatchEvent.EventType.GOAL, tick, homeScore, awayScore,
                            "🎯 " + homeTeam.getTeamName() + " scores! (Minute " + getCurrentMinute() + ")"
                    );
                    matchEvents.add(event);
                    notifyObservers(event);
                } else {
                    awayScore++;
                    MatchEvent event = new MatchEvent(
                            MatchEvent.EventType.GOAL, tick, homeScore, awayScore,
                            "🎯 " + awayTeam.getTeamName() + " scores! (Minute " + getCurrentMinute() + ")"
                    );
                    matchEvents.add(event);
                    notifyObservers(event);
                }
            }
        }

        if (checkVictoryStatus()) {
            matchResult = generateMatchReports();
            if (isLive) notifyMatchEnded();
        }
    }

    private void calculateProbabilities() {
        HomeAttackScore = 0;
        HomeDefenseScore = 0;
        AwayAttackScore = 0;
        AwayDefenseScore = 0;

        //This whole section is a placeholder until M3 where the Sport specific classes will implement a player and position based score calculation classes.
        attackMultiply = 0;
        if (homeTeam.getTactic() == Tactic.DEFEND) {
            // Attack goes down (0.8), Defense goes up (1.2)
            HomeAttackScore = DefensiveTacticGoalMultiplier;
            HomeDefenseScore = 1 + (1 - DefensiveTacticGoalMultiplier);
        }
        else if (homeTeam.getTactic() == Tactic.ATTACK) {
            // Attack goes up (1.2), Defense goes down (0.8)
            HomeAttackScore = AttackTacticGoalMultiplier;
            HomeDefenseScore = 1 - (AttackTacticGoalMultiplier - 1); // Fixed subtraction
            attackMultiply += 2;
        }
        else if (homeTeam.getTactic() == Tactic.BALANCED) {
            HomeAttackScore = 1.0F;
            HomeDefenseScore = 1.0F;
            attackMultiply += 1;
        }
        else {
            throw new IllegalStateException("Invalid tactic for home team: " + homeTeam.getTactic());
        }


        if (awayTeam.getTactic() == Tactic.DEFEND) {
            AwayAttackScore = DefensiveTacticGoalMultiplier;
            AwayDefenseScore = 1 + (1 - DefensiveTacticGoalMultiplier);
        }
        else if (awayTeam.getTactic() == Tactic.ATTACK) {
            AwayAttackScore = AttackTacticGoalMultiplier;
            AwayDefenseScore = 1 - (AttackTacticGoalMultiplier - 1); // Fixed subtraction
            attackMultiply += 2;
        }
        else if (awayTeam.getTactic() == Tactic.BALANCED) {
            AwayAttackScore = 1.0F;
            AwayDefenseScore = 1.0F;
            attackMultiply += 1;
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
        MatchResult matchResult = new MatchResult(homeTeam, awayTeam, homeScore, awayScore, week);
        return matchResult;
    }

    // --- LIVE MODE SUPPORT METHODS ---

    /**
     * Adds an observer to receive live match events
     */
    public void addMatchObserver(IMatchObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer
     */
    public void removeMatchObserver(IMatchObserver observer) {
        observers.remove(observer);
    }

    /**
     * Pauses the live match
     */
    public void pauseMatch() {
        isPaused = true;
    }

    /**
     * Resumes the live match from pause
     */
    public void resumeMatch() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    /**
     * Returns true if the match is currently paused
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Gets the current match state during live play
     */
    public MatchSnapshot getCurrentMatchState() {
        return new MatchSnapshot(homeScore, awayScore, tick, matchLength, getCurrentMinute());
    }

    /**
     * Gets all match events recorded so far
     */
    public List<MatchEvent> getMatchEvents() {
        return new ArrayList<>(matchEvents);
    }

    /**
     * Helper method to convert ticks to match minutes
     */
    private int getCurrentMinute() {
        return Math.round((float) tick / segmentLimit * (matchLength / segmentCount));
    }

    /**
     * Notifies all observers of a match event
     */
    public int getCurrentSegment() {return currentSegment;}
    private void notifyObservers(MatchEvent event) {
        if (!isLive) return;
        for (IMatchObserver observer : observers) {
            observer.onMatchEvent(event);
        }
    }

    /**
     * Notifies observers when a segment ends
     */
    private void notifySegmentEnd(int segmentNumber) {
        if (!isLive) return;
        MatchEvent event = new MatchEvent(
                MatchEvent.EventType.SEGMENT_END, tick, homeScore, awayScore,
                "End of Segment " + (segmentNumber + 1), segmentNumber
        );
        matchEvents.add(event);
        for (IMatchObserver observer : observers) {
            observer.onSegmentEnd(segmentNumber, homeScore, awayScore);
        }
    }

    /**
     * Notifies observers when the match ends
     */
    private void notifyMatchEnded() {
        if (!isLive) return;
        if (matchResult != null) {
            for (IMatchObserver observer : observers) {
                observer.onMatchEnded(matchResult);
            }
        }
    }

    /**
     * Inner class to represent a snapshot of the current match state
     */
    public static class MatchSnapshot {
        public final int homeScore;
        public final int awayScore;
        public final int currentTick;
        public final int totalMatchLength;
        public final int currentMinute;

        public MatchSnapshot(int homeScore, int awayScore, int currentTick, int totalMatchLength, int currentMinute) {
            this.homeScore = homeScore;
            this.awayScore = awayScore;
            this.currentTick = currentTick;
            this.totalMatchLength = totalMatchLength;
            this.currentMinute = currentMinute;
        }

        @Override
        public String toString() {
            return String.format("Minute %d/%d | Score: %d - %d", currentMinute, totalMatchLength, homeScore, awayScore);
        }
    }
}
