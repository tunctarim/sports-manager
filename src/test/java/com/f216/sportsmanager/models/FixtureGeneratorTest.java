package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ITeam;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FixtureGeneratorTest {

    private static ITeam teamStub(String label) {
        return new ITeam() {
            @Override
            public List<IPlayer> getPlayers() {
                return List.of();
            }

            @Override
            public Map<String, Integer> getTeamStats() {
                return Map.of();
            }

            @Override
            public void setTactic(Tactic t) {
            }

            @Override
            public Tactic getTactic() {
                return Tactic.DEFEND;
            }

            @Override
            public int getPoints() {
                return 0;
            }

            @Override
            public void updateRecord(int pf, int pa, int pts) {
            }

            @Override
            public void addPlayer(IPlayer p) {
            }

            @Override
            public void removePlayer(IPlayer p) {
            }

            @Override
            public String toString() {
                return "Team[" + label + "]";
            }
        };
    }
    @Nested
    @DisplayName("generate() — single-leg round-robin")
    class SingleLegTests {

        @Test
        @DisplayName("null team list – throws IllegalArgumentException")
        void testNullListThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> FixtureGenerator.generate(null));
        }

        @Test
        @DisplayName("list containing null element – throws IllegalArgumentException")
        void testListWithNullElementThrows() {
            List<ITeam> withNull = new ArrayList<>();
            withNull.add(teamStub("A"));
            withNull.add(null);
            assertThrows(IllegalArgumentException.class,
                    () -> FixtureGenerator.generate(withNull));
        }

        @Test
        @DisplayName("0 teams → 0 fixtures")
        void testZeroTeams() {
            assertEquals(0, FixtureGenerator.generate(List.of()).size());
        }

        @Test
        @DisplayName("1 team → 0 fixtures (cannot play itself)")
        void testOneTeam() {
            assertEquals(0, FixtureGenerator.generate(List.of(teamStub("A"))).size());
        }

        @Test
        @DisplayName("2 teams → 1 fixture")
        void testTwoTeams() {
            List<ITeam> teams = List.of(teamStub("A"), teamStub("B"));
            assertEquals(1, FixtureGenerator.generate(teams).size());
        }

        @Test
        @DisplayName("3 teams → 3 fixtures  (n*(n-1)/2)")
        void testThreeTeams() {
            List<ITeam> teams = List.of(teamStub("A"), teamStub("B"), teamStub("C"));
            assertEquals(3, FixtureGenerator.generate(teams).size());
        }

        @Test
        @DisplayName("4 teams → 6 fixtures  (n*(n-1)/2)")
        void testFourTeams() {
            List<ITeam> teams = List.of(
                    teamStub("A"), teamStub("B"), teamStub("C"), teamStub("D"));
            assertEquals(6, FixtureGenerator.generate(teams).size());
        }

        @Test
        @DisplayName("no fixture has the same team on both sides")
        void testNoSelfPlay() {
            List<ITeam> teams = List.of(teamStub("A"), teamStub("B"), teamStub("C"));
            for (Fixture f : FixtureGenerator.generate(teams)) {
                assertNotSame(f.getHome(), f.getAway());
            }
        }

        @Test
        @DisplayName("each pair appears exactly once")
        void testEachPairExactlyOnce() {
            ITeam a = teamStub("A");
            ITeam b = teamStub("B");
            ITeam c = teamStub("C");
            List<Fixture> fixtures = FixtureGenerator.generate(List.of(a, b, c));

            long abOrBa = fixtures.stream()
                    .filter(f -> (f.getHome() == a && f.getAway() == b)
                            || (f.getHome() == b && f.getAway() == a))
                    .count();
            assertEquals(1, abOrBa, "pair A-B should appear exactly once");
        }

        @Test
        @DisplayName("returned list is unmodifiable")
        void testReturnedListIsUnmodifiable() {
            List<Fixture> fixtures = FixtureGenerator.generate(
                    List.of(teamStub("A"), teamStub("B")));
            assertThrows(UnsupportedOperationException.class,
                    () -> fixtures.remove(0));
        }

        @Test
        @DisplayName("fixture count matches expectedFixtureCount(n) helper")
        void testFixtureCountMatchesHelper() {
            for (int n = 0; n <= 6; n++) {
                List<ITeam> teams = new ArrayList<>();
                for (int i = 0; i < n; i++)
                    teams.add(teamStub(String.valueOf(i)));
                assertEquals(FixtureGenerator.expectedFixtureCount(n),
                        FixtureGenerator.generate(teams).size(),
                        "For n=" + n);
            }
        }
    }
    @Nested
    @DisplayName("generateDoubleLegged() — double-leg round-robin")
    class DoubleLegTests {

        @Test
        @DisplayName("null team list – throws IllegalArgumentException")
        void testNullListThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> FixtureGenerator.generateDoubleLegged(null));
        }

        @Test
        @DisplayName("list containing null element – throws IllegalArgumentException")
        void testListWithNullElementThrows() {
            List<ITeam> withNull = new ArrayList<>();
            withNull.add(teamStub("A"));
            withNull.add(null);
            assertThrows(IllegalArgumentException.class,
                    () -> FixtureGenerator.generateDoubleLegged(withNull));
        }

        @Test
        @DisplayName("0 teams → 0 fixtures")
        void testZeroTeams() {
            assertEquals(0, FixtureGenerator.generateDoubleLegged(List.of()).size());
        }

        @Test
        @DisplayName("1 team → 0 fixtures")
        void testOneTeam() {
            assertEquals(0,
                    FixtureGenerator.generateDoubleLegged(List.of(teamStub("A"))).size());
        }

        @Test
        @DisplayName("2 teams → 2 fixtures (each plays home once)")
        void testTwoTeams() {
            List<ITeam> teams = List.of(teamStub("A"), teamStub("B"));
            assertEquals(2, FixtureGenerator.generateDoubleLegged(teams).size());
        }

        @Test
        @DisplayName("3 teams → 6 fixtures  (n*(n-1))")
        void testThreeTeams() {
            List<ITeam> teams = List.of(teamStub("A"), teamStub("B"), teamStub("C"));
            assertEquals(6, FixtureGenerator.generateDoubleLegged(teams).size());
        }

        @Test
        @DisplayName("4 teams → 12 fixtures  (n*(n-1))")
        void testFourTeams() {
            List<ITeam> teams = List.of(
                    teamStub("A"), teamStub("B"), teamStub("C"), teamStub("D"));
            assertEquals(12, FixtureGenerator.generateDoubleLegged(teams).size());
        }

        @Test
        @DisplayName("each pair appears exactly twice — once home, once away")
        void testEachPairTwice() {
            ITeam a = teamStub("A");
            ITeam b = teamStub("B");
            List<Fixture> fixtures = FixtureGenerator.generateDoubleLegged(List.of(a, b));

            long aAtHome = fixtures.stream()
                    .filter(f -> f.getHome() == a && f.getAway() == b).count();
            long bAtHome = fixtures.stream()
                    .filter(f -> f.getHome() == b && f.getAway() == a).count();

            assertEquals(1, aAtHome, "A should be home exactly once");
            assertEquals(1, bAtHome, "B should be home exactly once");
        }

        @Test
        @DisplayName("no fixture has same team on both sides")
        void testNoSelfPlay() {
            List<ITeam> teams = List.of(teamStub("A"), teamStub("B"), teamStub("C"));
            for (Fixture f : FixtureGenerator.generateDoubleLegged(teams)) {
                assertNotSame(f.getHome(), f.getAway());
            }
        }

        @Test
        @DisplayName("returned list is unmodifiable")
        void testReturnedListIsUnmodifiable() {
            List<Fixture> fixtures = FixtureGenerator.generateDoubleLegged(
                    List.of(teamStub("A"), teamStub("B")));
            assertThrows(UnsupportedOperationException.class,
                    () -> fixtures.remove(0));
        }

        @Test
        @DisplayName("fixture count matches expectedDoubleFixtureCount(n) helper")
        void testFixtureCountMatchesHelper() {
            for (int n = 0; n <= 6; n++) {
                List<ITeam> teams = new ArrayList<>();
                for (int i = 0; i < n; i++)
                    teams.add(teamStub(String.valueOf(i)));
                assertEquals(FixtureGenerator.expectedDoubleFixtureCount(n),
                        FixtureGenerator.generateDoubleLegged(teams).size(),
                        "For n=" + n);
            }
        }
    }

    @Nested
    @DisplayName("Utility helper methods")
    class HelperTests {

        @Test
        @DisplayName("expectedFixtureCount: 0 → 0, 2 → 1, 4 → 6")
        void testExpectedFixtureCount() {
            assertEquals(0, FixtureGenerator.expectedFixtureCount(0));
            assertEquals(1, FixtureGenerator.expectedFixtureCount(2));
            assertEquals(3, FixtureGenerator.expectedFixtureCount(3));
            assertEquals(6, FixtureGenerator.expectedFixtureCount(4));
            assertEquals(10, FixtureGenerator.expectedFixtureCount(5));
        }

        @Test
        @DisplayName("expectedDoubleFixtureCount: 0 → 0, 2 → 2, 4 → 12")
        void testExpectedDoubleFixtureCount() {
            assertEquals(0, FixtureGenerator.expectedDoubleFixtureCount(0));
            assertEquals(2, FixtureGenerator.expectedDoubleFixtureCount(2));
            assertEquals(6, FixtureGenerator.expectedDoubleFixtureCount(3));
            assertEquals(12, FixtureGenerator.expectedDoubleFixtureCount(4));
        }

        @Test
        @DisplayName("expectedFixtureCount with negative count – throws IllegalArgumentException")
        void testNegativeCountThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> FixtureGenerator.expectedFixtureCount(-1));
        }

        @Test
        @DisplayName("expectedDoubleFixtureCount with negative count – throws IllegalArgumentException")
        void testNegativeDoubleCountThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> FixtureGenerator.expectedDoubleFixtureCount(-1));
        }
    }

    @Nested
    @DisplayName("Fixture value object")
    class FixtureObjectTests {

        private ITeam home;
        private ITeam away;

        @BeforeEach
        void setUp() {
            home = teamStub("Home");
            away = teamStub("Away");
        }

        @Test
        @DisplayName("constructor with valid teams succeeds")
        void testValidFixture() {
            assertNotNull(new Fixture(home, away));
        }

        @Test
        @DisplayName("null home – throws IllegalArgumentException")
        void testNullHomeThrows() {
            assertThrows(IllegalArgumentException.class, () -> new Fixture(null, away));
        }

        @Test
        @DisplayName("null away – throws IllegalArgumentException")
        void testNullAwayThrows() {
            assertThrows(IllegalArgumentException.class, () -> new Fixture(home, null));
        }

        @Test
        @DisplayName("same team for both sides – throws IllegalArgumentException")
        void testSameTeamThrows() {
            assertThrows(IllegalArgumentException.class, () -> new Fixture(home, home));
        }

        @Test
        @DisplayName("getHome() returns correct team")
        void testGetHome() {
            assertSame(home, new Fixture(home, away).getHome());
        }

        @Test
        @DisplayName("getAway() returns correct team")
        void testGetAway() {
            assertSame(away, new Fixture(home, away).getAway());
        }

        @Test
        @DisplayName("equals: same home & away → equal")
        void testEqualsWhenSameTeams() {
            Fixture f1 = new Fixture(home, away);
            Fixture f2 = new Fixture(home, away);
            assertEquals(f1, f2);
        }

        @Test
        @DisplayName("equals: different direction → not equal")
        void testNotEqualsWhenReversed() {
            Fixture f1 = new Fixture(home, away);
            Fixture f2 = new Fixture(away, home);
            assertNotEquals(f1, f2);
        }

        @Test
        @DisplayName("toString is non-null and non-blank")
        void testToStringNonBlank() {
            String s = new Fixture(home, away).toString();
            assertNotNull(s);
            assertFalse(s.isBlank());
        }
    }
}
