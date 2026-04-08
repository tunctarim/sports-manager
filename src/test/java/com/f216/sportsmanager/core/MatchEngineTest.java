package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.MatchResult;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchEngineTest {

    private MatchEngine matchEngine;
    private AutoCloseable closeable;
    private static final int SIMULATION_ITERATIONS = 1000;
    private final Random random = new Random();

    @Mock private ITeam mockHomeTeam;
    @Mock private ITeam mockAwayTeam;
    @Mock private ISport mockSport;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        matchEngine = new MatchEngine();

        when(mockSport.getTickInterval()).thenReturn(1);
        when(mockSport.getSegmentCount()).thenReturn(2);
        when(mockSport.getSegmentLimit()).thenReturn(45);
        when(mockSport.getTotalMatchLength()).thenReturn(90);
        when(mockSport.getEndCondition()).thenReturn(EndCondition.TIME_LIMIT);

        when(mockHomeTeam.getTactic()).thenReturn(Tactic.BALANCED);
        when(mockAwayTeam.getTactic()).thenReturn(Tactic.BALANCED);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // --- HELPER METHODS ---

    private List<MatchResult> runMatches(int iterations, Fixture fixture, ISport sport, int week) {
        List<MatchResult> results = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            matchEngine.simulateMatch(fixture, sport, week, false);
            results.add(matchEngine.generateMatchReports());
        }
        return results;
    }

    /**
     * Generates a team of mocked players with random overall ratings between minRating and maxRating.
     */
    private List<IPlayer> generateRandomTeam(int size, int minRating, int maxRating) {
        List<IPlayer> team = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            IPlayer player = mock(IPlayer.class);
            int rating = random.nextInt((maxRating - minRating) + 1) + minRating;
            when(player.getOverallRating()).thenReturn(rating);
            team.add(player);
        }
        return team;
    }

    // --- TESTS ---

    @Nested
    @DisplayName("simulateMatch - Non-Live Mode")
    class SimulateMatchNonLiveMode {

        @Test
        @DisplayName("match completes successfully with randomized teams")
        void matchCompletesWithTimeLimit() {
            // Fix: Generate lists first before stubbing
            List<IPlayer> randomHomeTeam = generateRandomTeam(11, 60, 85);
            List<IPlayer> randomAwayTeam = generateRandomTeam(11, 60, 85);

            when(mockHomeTeam.getPlayers()).thenReturn(randomHomeTeam);
            when(mockAwayTeam.getPlayers()).thenReturn(randomAwayTeam);

            Fixture fixture = new Fixture(mockHomeTeam, mockAwayTeam);
            matchEngine.simulateMatch(fixture, mockSport, 1, false);

            MatchResult result = matchEngine.generateMatchReports();
            assertNotNull(result);
            assertEquals(mockHomeTeam, result.getHomeTeam());
            assertEquals(mockAwayTeam, result.getAwayTeam());
            assertEquals(1, result.getWeek());
        }
    }

    @Nested
    @DisplayName("Tactic Influence on Scoring (Statistical Analysis)")
    class TacticInfluence {

        @Test
        @DisplayName("attacking matches produce higher total goals than defensive matches")
        void attackMatchesAreHigherScoringThanDefendMatches() {
            // Already fixed in previous version, keeping format consistent
            List<IPlayer> teamA = generateRandomTeam(11, 70, 80);
            List<IPlayer> teamB = generateRandomTeam(11, 70, 80);

            when(mockHomeTeam.getPlayers()).thenReturn(teamA);
            when(mockAwayTeam.getPlayers()).thenReturn(teamB);
            Fixture fixture = new Fixture(mockHomeTeam, mockAwayTeam);

            // Scenario 1: All Out Attack
            when(mockHomeTeam.getTactic()).thenReturn(Tactic.ATTACK);
            when(mockAwayTeam.getTactic()).thenReturn(Tactic.ATTACK);
            List<MatchResult> attackResults = runMatches(SIMULATION_ITERATIONS, fixture, mockSport, 1);
            int totalAttackGoals = attackResults.stream().mapToInt(r -> r.getHomeScore() + r.getAwayScore()).sum();

            // Scenario 2: All Out Defense
            when(mockHomeTeam.getTactic()).thenReturn(Tactic.DEFEND);
            when(mockAwayTeam.getTactic()).thenReturn(Tactic.DEFEND);
            List<MatchResult> defendResults = runMatches(SIMULATION_ITERATIONS, fixture, mockSport, 1);
            int totalDefendGoals = defendResults.stream().mapToInt(r -> r.getHomeScore() + r.getAwayScore()).sum();

            // Assert that tactics change the flow of the game without forcing a specific winner
            assertTrue(totalAttackGoals > totalDefendGoals,
                    "Two attacking teams should produce more total goals than two defending teams. Attack Goals: " + totalAttackGoals + ", Defend Goals: " + totalDefendGoals);
        }
    }

    @Nested
    @DisplayName("Home Advantage")
    class HomeAdvantage {

        @Test
        @DisplayName("home team wins majority of matches with roughly equal teams")
        void homeTeamHasAdvantageStatistically() {
            // Fix: Generate lists first before stubbing
            List<IPlayer> randomHomeTeam = generateRandomTeam(11, 70, 80);
            List<IPlayer> randomAwayTeam = generateRandomTeam(11, 70, 80);

            when(mockHomeTeam.getPlayers()).thenReturn(randomHomeTeam);
            when(mockAwayTeam.getPlayers()).thenReturn(randomAwayTeam);

            Fixture fixture = new Fixture(mockHomeTeam, mockAwayTeam);
            List<MatchResult> results = runMatches(SIMULATION_ITERATIONS, fixture, mockSport, 1);

            long homeWins = results.stream().filter(r -> r.getHomeScore() > r.getAwayScore()).count();
            long awayWins = results.stream().filter(r -> r.getAwayScore() > r.getHomeScore()).count();

            assertTrue(homeWins > awayWins, "Home team should have a statistical edge when team ratings are equal. Home: " + homeWins + " Away: " + awayWins);
        }
    }

    @Nested
    @DisplayName("Player Capability")
    class PlayerCapability {

        @Test
        @DisplayName("powerhouse team wins heavily against amateur team")
        void higherRatedPlayersScoreMoreStatistically() {
            // Fix: Generate lists first before stubbing
            List<IPlayer> powerhouseTeam = generateRandomTeam(11, 85, 99);
            List<IPlayer> amateurTeam = generateRandomTeam(11, 30, 50);

            when(mockHomeTeam.getPlayers()).thenReturn(powerhouseTeam);
            when(mockAwayTeam.getPlayers()).thenReturn(amateurTeam);

            Fixture fixture = new Fixture(mockHomeTeam, mockAwayTeam);
            List<MatchResult> results = runMatches(SIMULATION_ITERATIONS, fixture, mockSport, 1);

            long powerhouseWins = results.stream().filter(r -> r.getHomeScore() > r.getAwayScore()).count();

            assertTrue(powerhouseWins >= 85, "The 85+ rated team should dominate the 50- rated team almost every time. Won: " + powerhouseWins);
        }
    }

    @Nested
    @DisplayName("End Condition Handling")
    class EndConditionHandling {
        @Test
        @DisplayName("null end condition throws exception")
        void nullEndConditionThrows() {
            // Fix: Generate lists first before stubbing
            List<IPlayer> randomHomeTeam = generateRandomTeam(5, 50, 90);
            List<IPlayer> randomAwayTeam = generateRandomTeam(5, 50, 90);

            when(mockHomeTeam.getPlayers()).thenReturn(randomHomeTeam);
            when(mockAwayTeam.getPlayers()).thenReturn(randomAwayTeam);
            when(mockSport.getEndCondition()).thenReturn(null);

            Fixture fixture = new Fixture(mockHomeTeam, mockAwayTeam);
            assertThrows(IllegalStateException.class, () -> matchEngine.simulateMatch(fixture, mockSport, 1, false));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("large number of players on team handled gracefully")
        void largeNumberOfPlayers() {
            // Fix: Generate lists first before stubbing
            List<IPlayer> hugeHomeTeam = generateRandomTeam(50, 40, 90);
            List<IPlayer> hugeAwayTeam = generateRandomTeam(50, 40, 90);

            when(mockHomeTeam.getPlayers()).thenReturn(hugeHomeTeam);
            when(mockAwayTeam.getPlayers()).thenReturn(hugeAwayTeam);

            Fixture fixture = new Fixture(mockHomeTeam, mockAwayTeam);
            assertDoesNotThrow(() -> matchEngine.simulateMatch(fixture, mockSport, 1, false));
        }
    }

    @Nested
    @DisplayName("Debugging Utility")
    class DebuggingUtility {

        @Test
        @DisplayName("Sandbox test for debugging single match logic - Put Breakpoints Here")
        void debugSingleMatch() {
            // This test is specifically designed to be run once for easy debugging
            List<IPlayer> homeTeam = generateRandomTeam(11, 75, 75);
            List<IPlayer> awayTeam = generateRandomTeam(11, 75, 75);

            when(mockHomeTeam.getPlayers()).thenReturn(homeTeam);
            when(mockAwayTeam.getPlayers()).thenReturn(awayTeam);
            when(mockHomeTeam.getTactic()).thenReturn(Tactic.BALANCED);
            when(mockAwayTeam.getTactic()).thenReturn(Tactic.BALANCED);

            Fixture fixture = new Fixture(mockHomeTeam, mockAwayTeam);

            // Set your breakpoint on the line below
            matchEngine.simulateMatch(fixture, mockSport, 1, false);

            MatchResult result = matchEngine.generateMatchReports();

            assertNotNull(result);
            System.out.println("DEBUG RESULT -> Home: " + result.getHomeScore() + " | Away: " + result.getAwayScore());
        }
    }
}