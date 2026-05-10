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
    private int homeSegmentsWon;
    private int awaySegmentsWon;
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
    private float FixedMultiplier;

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
        homeSegmentsWon = 0;
        awaySegmentsWon = 0;
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
        FixedMultiplier = s.getFixedMultiplier();
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
                if (endCondition == EndCondition.SCORE_LIMIT) {
                    while (true) {
                        synchronized (pauseLock) {
                            while (isPaused) pauseLock.wait();
                        }
                        if (matchResult != null) return;
                        
                        int currentH = homeScore;
                        int currentA = awayScore;
                        processTick();
                        Thread.sleep(tickInterval);
                        
                        if (homeScore == 0 && awayScore == 0 && (currentH > 0 || currentA > 0)) {
                            break; // segment was won and reset by processTick
                        }
                    }
                } else {
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
                }

                // PAUSE AT SEGMENT END (e.g., halftime)
                if (currentSegment < segmentCount - 1 && matchResult == null) {  // Don't pause after final segment
                    isPaused = true;
                    notifySegmentEnd(currentSegment);
                }
            }
        } else {
            // Non-live mode: process entire match instantly
            if (endCondition == EndCondition.SCORE_LIMIT) {
                while (matchResult == null) {
                    processTick();
                }
            } else {
                while (tick < matchLength) {
                    if (matchResult != null) {
                        return;
                    }
                    processTick();
                }
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

        double baseScoringChance = (FixedMultiplier / 20) + ((double) attackMultiply / 200);

        double rawTotal = HomeScoreProbability + AwayScoreProbability + FixedMultiplier;

        if (rawTotal > 0) {
            double homeShare = HomeScoreProbability / rawTotal;
            double awayShare = AwayScoreProbability / rawTotal;

            double roll = rand.nextDouble();

            if (roll < baseScoringChance) {
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

        if (endCondition == EndCondition.SCORE_LIMIT) {
            if (homeScore >= segmentLimit && homeScore - awayScore >= 2) {
                homeSegmentsWon++;
                homeScore = 0; awayScore = 0;
            } else if (awayScore >= segmentLimit && awayScore - homeScore >= 2) {
                awaySegmentsWon++;
                homeScore = 0; awayScore = 0;
            } else if ((homeScore >= segmentLimit || awayScore >= segmentLimit) && Math.abs(homeScore - awayScore) < 2) {
                // Must win by 2 points (standard volleyball rules)
            } else if (homeScore >= segmentLimit && awayScore < segmentLimit - 1) {
                homeSegmentsWon++;
                homeScore = 0; awayScore = 0;
            } else if (awayScore >= segmentLimit && homeScore < segmentLimit - 1) {
                awaySegmentsWon++;
                homeScore = 0; awayScore = 0;
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

        attackMultiply = 0;
        if (homeTeam.getTactic() == Tactic.DEFEND) {
            HomeAttackScore = DefensiveTacticGoalMultiplier;
            HomeDefenseScore = 1 + (1 - DefensiveTacticGoalMultiplier);
        }
        else if (homeTeam.getTactic() == Tactic.ATTACK) {
            HomeAttackScore = AttackTacticGoalMultiplier;
            HomeDefenseScore = 1 - (AttackTacticGoalMultiplier - 1);
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
            AwayDefenseScore = 1 - (AttackTacticGoalMultiplier - 1);
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

    private boolean checkVictoryStatus() {
        if (endCondition == null) {
            throw new IllegalStateException("endCondition is null");
        }
        if (endCondition == EndCondition.TIME_LIMIT && tick >= matchLength) {
            return true;
        }
        if (endCondition == EndCondition.SCORE_LIMIT) {
            int segmentsToWin = (segmentCount / 2) + 1;
            if (homeSegmentsWon >= segmentsToWin || awaySegmentsWon >= segmentsToWin) {
                return true;
            }
        }
        if (endCondition == EndCondition.KNOCKOUT){
            return false;
        }
        return false;
    }

    public MatchResult generateMatchReports() {
        if (endCondition == EndCondition.SCORE_LIMIT) {
            return new MatchResult(homeTeam, awayTeam, homeSegmentsWon, awaySegmentsWon, week);
        }
        return new MatchResult(homeTeam, awayTeam, homeScore, awayScore, week);
    }

    // --- LIVE MODE SUPPORT METHODS ---
    public void addMatchObserver(IMatchObserver observer) {
        observers.add(observer);
    }

    public void removeMatchObserver(IMatchObserver observer) {
        observers.remove(observer);
    }

    public void pauseMatch() {
        isPaused = true;
    }

    public void resumeMatch() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public MatchSnapshot getCurrentMatchState() {
        if (endCondition == EndCondition.SCORE_LIMIT) {
            return new MatchSnapshot(homeSegmentsWon, awaySegmentsWon, tick, matchLength, getCurrentMinute());
        }
        return new MatchSnapshot(homeScore, awayScore, tick, matchLength, getCurrentMinute());
    }

    public List<MatchEvent> getMatchEvents() {
        return new ArrayList<>(matchEvents);
    }

    private int getCurrentMinute() {
        if (segmentCount == 0) return 0;
        return Math.round((float) tick / segmentLimit * (matchLength / segmentCount));
    }

    public int getCurrentSegment() {return currentSegment;}

    private void notifyObservers(MatchEvent event) {
        if (!isLive) return;
        for (IMatchObserver observer : observers) {
            observer.onMatchEvent(event);
        }
    }

    private void notifySegmentEnd(int segmentNumber) {
        if (!isLive) return;
        
        int hScore = (endCondition == EndCondition.SCORE_LIMIT) ? homeSegmentsWon : homeScore;
        int aScore = (endCondition == EndCondition.SCORE_LIMIT) ? awaySegmentsWon : awayScore;

        MatchEvent event = new MatchEvent(
                MatchEvent.EventType.SEGMENT_END, tick, hScore, aScore,
                "End of Segment " + (segmentNumber + 1), segmentNumber
        );
        matchEvents.add(event);
        for (IMatchObserver observer : observers) {
            observer.onSegmentEnd(segmentNumber, hScore, aScore);
        }
    }

    private void notifyMatchEnded() {
        if (!isLive) return;
        if (matchResult != null) {
            for (IMatchObserver observer : observers) {
                observer.onMatchEnded(matchResult);
            }
        }
    }

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