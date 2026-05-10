package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.MatchEvent;
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
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private ISport buildSportMock() {
        synchronized (MatchEngineTest.class) {
            ISport sport = mock(ISport.class, withSettings().stubOnly());
            when(sport.getTickInterval()).thenReturn(1);
            when(sport.getSegmentCount()).thenReturn(2);
            when(sport.getSegmentLimit()).thenReturn(45);
            when(sport.getTotalMatchLength()).thenReturn(90);
            when(sport.getEndCondition()).thenReturn(EndCondition.TIME_LIMIT);
            when(sport.getFixedMultiplier()).thenReturn(1.0F);
            when(sport.getLineupSize()).thenReturn(11);
            when(sport.getMaxSubstitutions()).thenReturn(5);
            return sport;
        }
    }

    private ITeam buildTeamMock(String name, List<IPlayer> players, Tactic tactic) {
        synchronized (MatchEngineTest.class) {
            ITeam team = mock(ITeam.class, withSettings().stubOnly());
            when(team.getTeamName()).thenReturn(name);
            when(team.getPlayers()).thenReturn(players);
            when(team.getTactic()).thenReturn(tactic);
            when(team.substitutePlayer(any(), any())).thenReturn(true);
            return team;
        }
    }

    private List<IPlayer> generateRandomTeam(int size, int minRating, int maxRating) {
        synchronized (MatchEngineTest.class) {
            List<IPlayer> team = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                IPlayer player = mock(IPlayer.class, withSettings().stubOnly());
                int rating = ThreadLocalRandom.current().nextInt(minRating, maxRating + 1);
                when(player.getOverallRating()).thenReturn(rating);
                when(player.getName()).thenReturn("Player " + i);
                team.add(player);
            }
            return team;
        }
    }

    private List<MatchResult> runMatches(int iterations, Supplier<Fixture> fixtureFactory, ISport sport, int week) {
        ForkJoinPool pool = new ForkJoinPool(THREAD_COUNT);
        try {
            return pool.submit(() ->
                    IntStream.range(0, iterations)
                            .parallel()
                            .mapToObj(i -> {
                                MatchEngine engine = new MatchEngine();
                                engine.simulateMatch(fixtureFactory.get(), sport, week, false);
                                return engine.generateMatchReports();
                            })
                            .collect(Collectors.toList())
            ).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            pool.shutdown();
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    class SimulateMatchNonLiveMode {
        @Test
        void matchCompletesWithTimeLimit() {
            ISport sport = buildSportMock();
            ITeam home = buildTeamMock("Home", generateRandomTeam(11, 60, 85), Tactic.BALANCED);
            ITeam away = buildTeamMock("Away", generateRandomTeam(11, 60, 85), Tactic.BALANCED);
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
    class TacticInfluence {
        @Test
        void attackMatchesAreHigherScoringThanDefendMatches() {
            ISport sport = buildSportMock();

            List<MatchResult> attackResults = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> new Fixture(
                            buildTeamMock("H", generateRandomTeam(11, 70, 80), Tactic.ATTACK),
                            buildTeamMock("A", generateRandomTeam(11, 70, 80), Tactic.ATTACK)
                    ),
                    sport, 1);
            int totalAttackGoals = attackResults.stream().mapToInt(r -> r.getHomeScore() + r.getAwayScore()).sum();

            List<MatchResult> defendResults = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> new Fixture(
                            buildTeamMock("H", generateRandomTeam(11, 70, 80), Tactic.DEFEND),
                            buildTeamMock("A", generateRandomTeam(11, 70, 80), Tactic.DEFEND)
                    ),
                    sport, 1);
            int totalDefendGoals = defendResults.stream().mapToInt(r -> r.getHomeScore() + r.getAwayScore()).sum();

            assertTrue(totalAttackGoals > totalDefendGoals);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    class ScoreDistribution {
        @Test
        void realisticGoalDistribution() {
            ISport sport = buildSportMock();

            List<MatchResult> results = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> new Fixture(
                            buildTeamMock("H", generateRandomTeam(11, 70, 80), Tactic.BALANCED),
                            buildTeamMock("A", generateRandomTeam(11, 70, 80), Tactic.BALANCED)
                    ),
                    sport, 1);

            double avgHomeGoals = results.stream().mapToInt(MatchResult::getHomeScore).average().orElse(0.0);
            double avgAwayGoals = results.stream().mapToInt(MatchResult::getAwayScore).average().orElse(0.0);

            assertTrue(avgHomeGoals >= 1.0 && avgHomeGoals <= 5.0);
            assertTrue(avgAwayGoals >= 1.0 && avgAwayGoals <= 5.0);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    class HomeAdvantage {
        @Test
        void homeTeamHasAdvantageStatistically() {
            ISport sport = buildSportMock();

            List<MatchResult> results = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> new Fixture(
                            buildTeamMock("H", generateRandomTeam(11, 70, 80), Tactic.BALANCED),
                            buildTeamMock("A", generateRandomTeam(11, 70, 80), Tactic.BALANCED)
                    ),
                    sport, 1);

            long homeWins = results.stream().filter(r -> r.getHomeScore() > r.getAwayScore()).count();
            long awayWins = results.stream().filter(r -> r.getAwayScore() > r.getHomeScore()).count();

            assertTrue(homeWins > awayWins);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    class PlayerCapability {
        @Test
        void higherRatedPlayersScoreMoreStatistically() {
            ISport sport = buildSportMock();

            List<MatchResult> results = runMatches(
                    SIMULATION_ITERATIONS,
                    () -> new Fixture(
                            buildTeamMock("H", generateRandomTeam(11, 85, 99), Tactic.BALANCED),
                            buildTeamMock("A", generateRandomTeam(11, 30, 50), Tactic.BALANCED)
                    ),
                    sport, 1);

            long powerhouseWins = results.stream().filter(r -> r.getHomeScore() > r.getAwayScore()).count();
            assertTrue(powerhouseWins >= 700);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    class EndConditionHandling {
        @Test
        void nullEndConditionThrows() {
            ISport nullSport = buildSportMock();
            when(nullSport.getEndCondition()).thenReturn(null);

            ITeam home = buildTeamMock("H", generateRandomTeam(5, 50, 90), Tactic.BALANCED);
            ITeam away = buildTeamMock("A", generateRandomTeam(5, 50, 90), Tactic.BALANCED);
            Fixture fixture = new Fixture(home, away);

            MatchEngine engine = new MatchEngine();
            assertThrows(IllegalStateException.class, () -> engine.simulateMatch(fixture, nullSport, 1, false));
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    class EdgeCases {
        @Test
        void largeNumberOfPlayers() {
            ISport sport = buildSportMock();
            ITeam home = buildTeamMock("H", generateRandomTeam(50, 40, 90), Tactic.BALANCED);
            ITeam away = buildTeamMock("A", generateRandomTeam(50, 40, 90), Tactic.BALANCED);
            Fixture fixture = new Fixture(home, away);

            MatchEngine engine = new MatchEngine();
            assertDoesNotThrow(() -> engine.simulateMatch(fixture, sport, 1, false));
        }

        @Test
        void substitutionEvent() {
            ISport sport = buildSportMock();
            ITeam home = buildTeamMock("HomeTeam", generateRandomTeam(11, 75, 75), Tactic.BALANCED);
            ITeam away = buildTeamMock("AwayTeam", generateRandomTeam(11, 75, 75), Tactic.BALANCED);
            Fixture fixture = new Fixture(home, away);

            IPlayer pOut;
            IPlayer pIn;
            synchronized (MatchEngineTest.class) {
                pOut = mock(IPlayer.class, withSettings().stubOnly());
                pIn = mock(IPlayer.class, withSettings().stubOnly());
                when(pOut.getName()).thenReturn("Out");
                when(pIn.getName()).thenReturn("In");
            }

            MatchEngine engine = new MatchEngine();
            engine.simulateMatch(fixture, sport, 1, false);
            engine.performSubstitution(true, pOut, pIn);

            List<MatchEvent> events = engine.getMatchEvents();
            long subEvents = events.stream().filter(e -> e.getType() == MatchEvent.EventType.SUBSTITUTION).count();
            assertEquals(1, subEvents);
        }
    }

    @Nested
    @Execution(ExecutionMode.CONCURRENT)
    class DebuggingUtility {
        @Test
        void debugSingleMatch() {
            ISport sport = buildSportMock();
            ITeam home = buildTeamMock("H", generateRandomTeam(11, 75, 75), Tactic.BALANCED);
            ITeam away = buildTeamMock("A", generateRandomTeam(11, 75, 75), Tactic.BALANCED);
            Fixture fixture = new Fixture(home, away);

            MatchEngine engine = new MatchEngine();
            engine.simulateMatch(fixture, sport, 1, false);

            MatchResult result = engine.generateMatchReports();
            assertNotNull(result);
        }
    }
}