package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.MatchResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.CONCURRENT)
class MatchEngineTest {

    private static final int SIMULATION_ITERATIONS = 1000;

    /**
     * Thread count for the parallel simulation pool inside runMatches().
     *
     * Runtime.availableProcessors() returns LOGICAL processors — on your 8-core/16-thread
     * CPU it returns 16, so this saturates every hardware thread. On a 4-core/8-thread CI
     * machine it returns 8, on a 2-core machine it returns 2, etc. No hard cap is set so
     * the pool always matches the host machine without manual tuning.
     */
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    // --- HELPER METHODS ---

    /**
     * Builds a fully configured sport mock with standard football timing values.
     * Each test creates its own — no shared mutable state between concurrent tests.
     */
    private ISport buildSportMock() {
        ISport sport = mock(ISport.class, withSettings().stubOnly());
        when(sport.getTickInterval()).thenReturn(1);
        when(sport.getSegmentCount()).thenReturn(2);
        when(sport.getSegmentLimit()).thenReturn(45);
        when(sport.getTotalMatchLength()).thenReturn(90);
        when(sport.getEndCondition()).thenReturn(EndCondition.TIME_LIMIT);
        return sport;
    }

    /**
     * Builds a team mock with the given players and tactic.
     * Each test creates its own — no shared mutable state between concurrent tests.
     */
    private ITeam buildTeamMock(List<IPlayer> players, Tactic tactic) {
        ITeam team = mock(ITeam.class, withSettings().stubOnly());
        when(team.getPlayers()).thenReturn(players);
        when(team.getTactic()).thenReturn(tactic);
        return team;
    }

    /**
     * Runs {@code iterations} matches in parallel using a ForkJoinPool sized to
     * THREAD_COUNT (= logical processors on the host machine).
     *
     * Thread safety:
     * - Each iteration creates its own MatchEngine — no engine state is shared.
     * - The fixture factory ensures a fresh Fixture and fresh Players are generated for each match.
     * - The sport mocks passed in must already be fully stubbed and
     * must NOT be mutated after this call begins (frozen read-only contract).
     * - ThreadLocalRandom is used elsewhere for player generation; no shared RNG.
     */
    private List<MatchResult> runMatches(int iterations, Supplier<Fixture> fixtureFactory, ISport sport, int week) {
        ForkJoinPool pool = new ForkJoinPool(THREAD_COUNT);
        try {
            return pool.submit(() ->
                    IntStream.range(0, iterations)
                            .parallel()
                            .mapToObj(i -> {
                                MatchEngine engine = new MatchEngine();
                                // Instantiate a completely isolated Fixture (and new players) for this specific thread
                                engine.simulateMatch(fixtureFactory.get(), sport, week, false);
                                return engine.generateMatchReports();
                            })
                            .collect(Collectors.toList())
            ).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel simulation interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Parallel simulation failed", e.getCause());
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Generates a list of mocked players with random overall ratings in [minRating, maxRating].
     * Uses ThreadLocalRandom — zero contention across parallel threads.
     */
    private List<IPlayer> generateRandomTeam(int size, int minRating, int maxRating) {
        List<IPlayer> team = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            IPlayer player = mock(IPlayer.class, withSettings().stubOnly());
            int rating = ThreadLocalRandom.current().nextInt(minRating, maxRating + 1);
            when(player.getOverallRating()).thenReturn(rating);
            team.add(player);
        }
        return team;
    }

    // --- TESTS ---

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("simulateMatch - Non-Live Mode")
    class SimulateMatchNonLiveMode {

        @Test
        @DisplayName("match completes successfully on time limit with randomized teams")
        void matchCompletesWithTimeLimit() {
            ISport sport   = buildSportMock();
            ITeam  home    = buildTeamMock(generateRandomTeam(11, 60, 85), Tactic.BALANCED);
            ITeam  away    = buildTeamMock(generateRandomTeam(11, 60, 85), Tactic.BALANCED);
            Fixture fixture = new Fixture(home, away);

            MatchEngine engine = new MatchEngine();
            engine.simulateMatch(fixture, sport, 1, false);

            MatchResult result = engine.generateMatchReports();
            assertNotNull(result);
            assertEquals(home, result.getHomeTeam());
            assertEquals(away, result.getAwayTeam());
            assertEquals(1, result.getWeek());
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("Tactic Influence on Scoring (Statistical Analysis)")
    class TacticInfluence {

        @Test
        @DisplayName("attacking matches produce higher total goals than defensive matches")
        void attackMatchesAreHigherScoringThanDefendMatches() {
            ISport sport = buildSportMock();

            // Scenario 1: All Out Attack — Generate teams INSIDE the lambda
            List<MatchResult> attackResults = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> {
                        ITeam attackHome = buildTeamMock(generateRandomTeam(11, 70, 80), Tactic.ATTACK);
                        ITeam attackAway = buildTeamMock(generateRandomTeam(11, 70, 80), Tactic.ATTACK);
                        return new Fixture(attackHome, attackAway);
                    },
                    sport, 1);
            int totalAttackGoals = attackResults.stream()
                    .mapToInt(r -> r.getHomeScore() + r.getAwayScore()).sum();

            // Scenario 2: All Out Defense — Generate teams INSIDE the lambda
            List<MatchResult> defendResults = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> {
                        ITeam defendHome = buildTeamMock(generateRandomTeam(11, 70, 80), Tactic.DEFEND);
                        ITeam defendAway = buildTeamMock(generateRandomTeam(11, 70, 80), Tactic.DEFEND);
                        return new Fixture(defendHome, defendAway);
                    },
                    sport, 1);
            int totalDefendGoals = defendResults.stream()
                    .mapToInt(r -> r.getHomeScore() + r.getAwayScore()).sum();

            assertTrue(totalAttackGoals > totalDefendGoals,
                    "Two attacking teams should produce more total goals than two defending teams. "
                            + "Attack Goals: " + totalAttackGoals + ", Defend Goals: " + totalDefendGoals);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("Realism and Score Distribution")
    class ScoreDistribution {

        @Test
        @DisplayName("Most football matches should have around 1-5 goals for each team")
        void realisticGoalDistribution() {
            ISport sport = buildSportMock();

            // Generate teams INSIDE the lambda for true statistical variance
            List<MatchResult> results = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> {
                        ITeam home = buildTeamMock(generateRandomTeam(11, 70, 80), Tactic.BALANCED);
                        ITeam away = buildTeamMock(generateRandomTeam(11, 70, 80), Tactic.BALANCED);
                        return new Fixture(home, away);
                    },
                    sport, 1);

            double avgHomeGoals = results.stream()
                    .mapToInt(MatchResult::getHomeScore).average().orElse(0.0);
            double avgAwayGoals = results.stream()
                    .mapToInt(MatchResult::getAwayScore).average().orElse(0.0);

            assertTrue(avgHomeGoals >= 1.0 && avgHomeGoals <= 5.0,
                    "Average home goals should be realistic (between 1 and 5). Actual: " + avgHomeGoals);
            assertTrue(avgAwayGoals >= 1.0 && avgAwayGoals <= 5.0,
                    "Average away goals should be realistic (between 1 and 5). Actual: " + avgAwayGoals);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("Home Advantage")
    class HomeAdvantage {

        @Test
        @DisplayName("home team wins majority of matches with roughly equal teams")
        void homeTeamHasAdvantageStatistically() {
            ISport sport = buildSportMock();

            // Generate teams INSIDE the lambda
            List<MatchResult> results = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> {
                        ITeam home = buildTeamMock(generateRandomTeam(11, 70, 80), Tactic.BALANCED);
                        ITeam away = buildTeamMock(generateRandomTeam(11, 70, 80), Tactic.BALANCED);
                        return new Fixture(home, away);
                    },
                    sport, 1);

            long homeWins = results.stream()
                    .filter(r -> r.getHomeScore() > r.getAwayScore()).count();
            long awayWins = results.stream()
                    .filter(r -> r.getAwayScore() > r.getHomeScore()).count();

            assertTrue(homeWins > awayWins,
                    "Home team should have a statistical edge when team ratings are equal. "
                            + "Home: " + homeWins + " Away: " + awayWins);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("Player Capability")
    class PlayerCapability {

        @Test
        @DisplayName("powerhouse team wins heavily against amateur team")
        void higherRatedPlayersScoreMoreStatistically() {
            ISport sport = buildSportMock();

            // Generate teams INSIDE the lambda to prevent statistical lock-in
            List<MatchResult> results = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> {
                        ITeam home = buildTeamMock(generateRandomTeam(11, 85, 99), Tactic.BALANCED);
                        ITeam away = buildTeamMock(generateRandomTeam(11, 30, 50), Tactic.BALANCED);
                        return new Fixture(home, away);
                    },
                    sport, 1);

            long powerhouseWins = results.stream()
                    .filter(r -> r.getHomeScore() > r.getAwayScore()).count();

            assertTrue(powerhouseWins >= 700,
                    "The 85+ rated team should dominate the 50- rated team almost every time. Won: " + powerhouseWins);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("End Condition Handling")
    class EndConditionHandling {

        @Test
        @DisplayName("null end condition throws exception")
        void nullEndConditionThrows() {
            ISport nullSport = buildSportMock();
            when(nullSport.getEndCondition()).thenReturn(null);

            ITeam   home    = buildTeamMock(generateRandomTeam(5, 50, 90), Tactic.BALANCED);
            ITeam   away    = buildTeamMock(generateRandomTeam(5, 50, 90), Tactic.BALANCED);
            Fixture fixture = new Fixture(home, away);

            MatchEngine engine = new MatchEngine();
            assertThrows(IllegalStateException.class,
                    () -> engine.simulateMatch(fixture, nullSport, 1, false));
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("large number of players on team handled gracefully")
        void largeNumberOfPlayers() {
            ISport  sport   = buildSportMock();
            ITeam   home    = buildTeamMock(generateRandomTeam(50, 40, 90), Tactic.BALANCED);
            ITeam   away    = buildTeamMock(generateRandomTeam(50, 40, 90), Tactic.BALANCED);
            Fixture fixture = new Fixture(home, away);

            MatchEngine engine = new MatchEngine();
            assertDoesNotThrow(() -> engine.simulateMatch(fixture, sport, 1, false));
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("Debugging Utility")
    class DebuggingUtility {

        @Test
        @DisplayName("Sandbox test for debugging single match logic - Put Breakpoints Here")
        void debugSingleMatch() {
            ISport  sport   = buildSportMock();
            ITeam   home    = buildTeamMock(generateRandomTeam(11, 75, 75), Tactic.BALANCED);
            ITeam   away    = buildTeamMock(generateRandomTeam(11, 75, 75), Tactic.BALANCED);
            Fixture fixture = new Fixture(home, away);

            MatchEngine engine = new MatchEngine();

            // Set your breakpoint on the line below
            engine.simulateMatch(fixture, sport, 1, false);

            MatchResult result = engine.generateMatchReports();
            assertNotNull(result);
            System.out.println("DEBUG RESULT -> Home: " + result.getHomeScore()
                    + " | Away: " + result.getAwayScore());
        }
    }
}