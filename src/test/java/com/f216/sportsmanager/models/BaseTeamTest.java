package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.Gender;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import org.junit.jupiter.api.*;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.enums.Tactic;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaseTeamTest {

    enum TestPosition implements PlayerPosition {
        FORWARD("FW"),
        MIDFIELDER("MF");

        private final String code;
        TestPosition(String code) {
            this.code = code;
        }

        @Override public String getCode() { return code; }
    }

    static class TestTeam extends BaseTeam {
        public TestTeam(String name, List<IPlayer> players, Tactic tactic) {
            super(name, players, tactic);
        }
    }

    static class TestPlayer extends BasePlayer {
        TestPlayer(String name, int age, Gender gender, PlayerPosition position) {
            super(name, age, gender, position);
        }
    }

    private TestTeam team;
    private TestPlayer playerA;
    private TestPlayer playerB;

    @BeforeEach
    void setUp() {
        playerA = new TestPlayer("Alice", 25, Gender.FEMALE, TestPosition.FORWARD);
        playerB = new TestPlayer("Bob", 22, Gender.MALE, TestPosition.MIDFIELDER);
        team = new TestTeam("Test Team", new ArrayList<>(List.of(playerA)), Tactic.ATTACK);
    }

    // -------------------------------------------------------------------------

    @Nested
    class ConstructorTests {

        @Test
        void testValidConstructor() {
            assertNotNull(team);
            assertEquals(Tactic.ATTACK, team.getTactic());
            assertEquals(1, team.getPlayers().size());
        }

        @Test
        void testNullNameThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TestTeam(null, new ArrayList<>(), Tactic.ATTACK));
        }

        @Test
        void testBlankNameThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TestTeam("   ", new ArrayList<>(), Tactic.ATTACK));
        }

        @Test
        void testEmptyNameThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TestTeam("", new ArrayList<>(), Tactic.ATTACK));
        }

        @Test
        void testInitialPointsAreZero() {
            assertEquals(0, team.getPoints());
        }

        @Test
        void testInitialStatsAllZero() {
            Map<String, Integer> stats = team.getTeamStats();
            assertEquals(0, stats.get("wins"));
            assertEquals(0, stats.get("losses"));
            assertEquals(0, stats.get("points"));
            assertEquals(0, stats.get("pointsFor"));
            assertEquals(0, stats.get("pointsAgainst"));
            assertEquals(0, stats.get("pointDifferential"));
        }
    }

    // -------------------------------------------------------------------------

    @Nested
    class TacticTests {

        @Test
        void testGetTactic() {
            assertEquals(Tactic.ATTACK, team.getTactic());
        }

        @Test
        void testSetTacticUpdates() {
            team.setTactic(Tactic.DEFEND);
            assertEquals(Tactic.DEFEND, team.getTactic());
        }

        @Test
        void testSetTacticOverridesPrevious() {
            team.setTactic(Tactic.DEFEND);
            team.setTactic(Tactic.ATTACK);
            assertEquals(Tactic.ATTACK, team.getTactic());
        }
    }

    // -------------------------------------------------------------------------

    @Nested
    class UpdateRecordTests {

        @Test
        void testWinIncrementedOnPositivePoints() {
            team.updateRecord(100, 80, 2);
            assertEquals(1, team.getTeamStats().get("wins"));
            assertEquals(0, team.getTeamStats().get("losses"));
        }

        @Test
        void testLossIncrementedOnZeroPoints() {
            team.updateRecord(80, 100, 0);
            assertEquals(0, team.getTeamStats().get("wins"));
            assertEquals(1, team.getTeamStats().get("losses"));
        }

        @Test
        void testPointsAccumulate() {
            team.updateRecord(100, 80, 2);
            team.updateRecord(90, 85, 2);
            assertEquals(4, team.getPoints());
        }

        @Test
        void testPointsForAndAgainstAccumulate() {
            team.updateRecord(100, 80, 2);
            team.updateRecord(90, 95, 0);
            Map<String, Integer> stats = team.getTeamStats();
            assertEquals(190, stats.get("pointsFor"));
            assertEquals(175, stats.get("pointsAgainst"));
        }

        @Test
        void testMultipleResultsTrackWinsAndLosses() {
            team.updateRecord(100, 80, 2);
            team.updateRecord(80, 100, 0);
            team.updateRecord(95, 90, 2);
            Map<String, Integer> stats = team.getTeamStats();
            assertEquals(2, stats.get("wins"));
            assertEquals(1, stats.get("losses"));
        }

        @ParameterizedTest(name = "pf={0}, pa={1} → differential={2}")
        @CsvSource({
                "110, 90,  20",
                "90, 110, -20",
                "100, 100,  0"
        })
        void testPointDifferential(int pf, int pa, int expectedDiff) {
            team.updateRecord(pf, pa, pf > pa ? 2 : 0);
            assertEquals(expectedDiff, team.getTeamStats().get("pointDifferential"));
        }
    }

    // -------------------------------------------------------------------------

    @Nested
    class PlayerManagementTests {

        @Test
        void testAddPlayerIncreasesCount() {
            team.addPlayer(playerB);
            assertEquals(2, team.getPlayers().size());
        }

        @Test
        void testAddedPlayerIsPresent() {
            team.addPlayer(playerB);
            assertTrue(team.getPlayers().contains(playerB));
        }

        @Test
        void testRemovePlayerDecreasesCount() {
            team.removePlayer(playerA);
            assertTrue(team.getPlayers().isEmpty());
        }

        @Test
        void testRemovedPlayerIsAbsent() {
            team.removePlayer(playerA);
            assertFalse(team.getPlayers().contains(playerA));
        }

        @Test
        void testRemoveAbsentPlayerDoesNothing() {
            team.removePlayer(playerB);
            assertEquals(1, team.getPlayers().size());
        }

        @Test
        void testGetPlayersReturnsDefensiveCopy() {
            team.getPlayers().clear();
            assertEquals(1, team.getPlayers().size());
        }

        @Test
        void testExternalAddToReturnedListDoesNotAffectRoster() {
            team.getPlayers().add(playerB);
            assertEquals(1, team.getPlayers().size());
        }
    }

    // -------------------------------------------------------------------------

    @Nested
    class IntegrationTests {

        @Test
        void testFullMatchCycle() {
            team.updateRecord(95, 80, 2);
            team.updateRecord(70, 90, 0);
            team.updateRecord(100, 85, 2);

            Map<String, Integer> stats = team.getTeamStats();
            assertEquals(2, stats.get("wins"));
            assertEquals(1, stats.get("losses"));
            assertEquals(4, team.getPoints());
            assertEquals(265, stats.get("pointsFor"));
            assertEquals(255, stats.get("pointsAgainst"));
            assertEquals(10, stats.get("pointDifferential"));
        }

        @Test
        void testRosterChangesDuringSession() {
            team.addPlayer(playerB);
            assertEquals(2, team.getPlayers().size());
            team.removePlayer(playerA);
            assertEquals(1, team.getPlayers().size());
            assertTrue(team.getPlayers().contains(playerB));
            assertFalse(team.getPlayers().contains(playerA));
        }

        @Test
        void testTacticChangeDoesNotAffectStats() {
            team.updateRecord(100, 80, 2);
            team.setTactic(Tactic.DEFEND);
            assertEquals(1, team.getTeamStats().get("wins"));
            assertEquals(Tactic.DEFEND, team.getTactic());
        }
    }
}