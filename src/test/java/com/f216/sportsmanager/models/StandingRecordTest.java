package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ITeam;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class StandingRecordTest {


    private static ITeam teamStub(int points, int wins, int losses, int pf, int pa) {
        return new ITeam() {
            @Override
            public List<IPlayer> getPlayers() {
                return List.of();
            }

            @Override
            public Map<String, Integer> getTeamStats() {
                Map<String, Integer> m = new HashMap<>();
                m.put("wins", wins);
                m.put("losses", losses);
                m.put("pointsFor", pf);
                m.put("pointsAgainst", pa);
                m.put("pointDifferential", pf - pa);
                return m;
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
                return points;
            }

            @Override
            public void updateRecord(int pf2, int pa2, int pts) {
            }

            @Override
            public void addPlayer(IPlayer p) {
            }

            @Override
            public void removePlayer(IPlayer p) {
            }
        };
    }
    private static ITeam nullStatsTeam() {
        return new ITeam() {
            @Override
            public List<IPlayer> getPlayers() {
                return List.of();
            }

            @Override
            public Map<String, Integer> getTeamStats() {
                return null;
            }

            @Override
            public void setTactic(Tactic t) {
            }

            @Override
            public Tactic getTactic() {
                return Tactic.BALANCED;
            }

            @Override
            public int getPoints() {
                return 5;
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
        };
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("valid team – constructs without error")
        void testValidConstruction() {
            ITeam team = teamStub(6, 2, 1, 80, 50);
            assertNotNull(new StandingRecord(team));
        }

        @Test
        @DisplayName("null team – throws IllegalArgumentException")
        void testNullTeamThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new StandingRecord(null));
        }

        @Test
        @DisplayName("getTeam() returns the exact instance passed in")
        void testGetTeamReturnsSameInstance() {
            ITeam team = teamStub(3, 1, 2, 40, 70);
            StandingRecord sr = new StandingRecord(team);
            assertSame(team, sr.getTeam());
        }
    }

    @Nested
    @DisplayName("Stat accessors")
    class StatAccessorTests {

        private ITeam team;
        private StandingRecord sr;

        @BeforeEach
        void setUp() {
            // 9 pts, 3W 0L, 90 scored, 30 conceded → gd = +60
            team = teamStub(9, 3, 0, 90, 30);
            sr = new StandingRecord(team);
        }

        @Test
        @DisplayName("getPoints() delegates to ITeam.getPoints()")
        void testGetPoints() {
            assertEquals(9, sr.getPoints());
        }

        @Test
        @DisplayName("getWins() reads from stats map")
        void testGetWins() {
            assertEquals(3, sr.getWins());
        }

        @Test
        @DisplayName("getLosses() reads from stats map")
        void testGetLosses() {
            assertEquals(0, sr.getLosses());
        }

        @Test
        @DisplayName("getPointsFor() reads from stats map")
        void testGetPointsFor() {
            assertEquals(90, sr.getPointsFor());
        }

        @Test
        @DisplayName("getPointsAgainst() reads from stats map")
        void testGetPointsAgainst() {
            assertEquals(30, sr.getPointsAgainst());
        }

        @Test
        @DisplayName("getGoalDifference() = pointsFor - pointsAgainst")
        void testGetGoalDifference() {
            assertEquals(60, sr.getGoalDifference());
        }

        @Test
        @DisplayName("getMatchesPlayed() = wins + losses")
        void testGetMatchesPlayed() {
            assertEquals(3, sr.getMatchesPlayed()); // 3W + 0L
        }
    }

    @Nested
    @DisplayName("Missing / null stat map edge cases")
    class EdgeStatTests {

        @Test
        @DisplayName("null stats map – all getters return 0 (no NPE)")
        void testNullStatsMapDoesNotThrow() {
            StandingRecord sr = new StandingRecord(nullStatsTeam());
            assertDoesNotThrow(() -> {
                sr.getWins();
                sr.getLosses();
                sr.getPointsFor();
                sr.getPointsAgainst();
                sr.getGoalDifference();
                sr.getMatchesPlayed();
            });
        }

        @Test
        @DisplayName("null stats map – getPoints() still works from ITeam.getPoints()")
        void testNullStatsMapPointsFromTeam() {
            StandingRecord sr = new StandingRecord(nullStatsTeam());
            assertEquals(5, sr.getPoints());
        }

        @Test
        @DisplayName("blank stat map – all numeric getters return 0")
        void testEmptyStatsMapDefaultsToZero() {
            ITeam blank = new ITeam() {
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
                    return Tactic.ATTACK;
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
            };
            StandingRecord sr = new StandingRecord(blank);
            assertEquals(0, sr.getWins());
            assertEquals(0, sr.getLosses());
            assertEquals(0, sr.getGoalDifference());
            assertEquals(0, sr.getMatchesPlayed());
        }

        @Test
        @DisplayName("negative goal difference is reported correctly")
        void testNegativeGoalDifference() {
            StandingRecord sr = new StandingRecord(teamStub(0, 0, 3, 10, 60));
            assertEquals(-50, sr.getGoalDifference());
        }
    }

    @Nested
    @DisplayName("compareTo — sorting rules")
    class CompareToTests {

        @Test
        @DisplayName("higher points ranks first")
        void testHigherPointsRanksFirst() {
            StandingRecord high = new StandingRecord(teamStub(9, 3, 0, 90, 30));
            StandingRecord low = new StandingRecord(teamStub(3, 1, 2, 40, 70));
            assertTrue(high.compareTo(low) < 0, "higher points should sort before lower");
        }

        @Test
        @DisplayName("equal points: higher goal difference ranks first")
        void testEqualPointsGoalDiffDecides() {
            StandingRecord better = new StandingRecord(teamStub(6, 2, 0, 80, 40)); // gd +40
            StandingRecord worse = new StandingRecord(teamStub(6, 2, 0, 60, 50)); // gd +10
            assertTrue(better.compareTo(worse) < 0);
        }

        @Test
        @DisplayName("equal points, equal gd: higher pointsFor ranks first")
        void testEqualPointsAndGdPointsForDecides() {
            StandingRecord more = new StandingRecord(teamStub(6, 2, 0, 80, 50)); // pf 80
            StandingRecord less = new StandingRecord(teamStub(6, 2, 0, 60, 30)); // pf 60
            assertTrue(more.compareTo(less) < 0);
        }

        @Test
        @DisplayName("identical stats – compareTo returns 0")
        void testIdenticalStatsCompareToZero() {
            StandingRecord r1 = new StandingRecord(teamStub(6, 2, 0, 70, 40));
            StandingRecord r2 = new StandingRecord(teamStub(6, 2, 0, 70, 40));
            assertEquals(0, r1.compareTo(r2));
        }

        @Test
        @DisplayName("compareTo is antisymmetric: compare(a, b) = -compare(b, a)")
        void testCompareToAntisymmetric() {
            StandingRecord a = new StandingRecord(teamStub(9, 3, 0, 90, 30));
            StandingRecord b = new StandingRecord(teamStub(3, 1, 2, 40, 70));
            int ab = a.compareTo(b);
            int ba = b.compareTo(a);
            assertTrue(ab < 0 && ba > 0);
        }
    }
    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString is non-null and non-blank")
        void testToStringNonBlank() {
            StandingRecord sr = new StandingRecord(teamStub(6, 2, 1, 70, 50));
            String s = sr.toString();
            assertNotNull(s);
            assertFalse(s.isBlank());
        }

        @Test
        @DisplayName("toString contains key stat values")
        void testToStringContainsStats() {
            StandingRecord sr = new StandingRecord(teamStub(9, 3, 0, 90, 30));
            String s = sr.toString();
            assertTrue(s.contains("9")); // points
            assertTrue(s.contains("3")); // wins or matches played
            assertTrue(s.contains("90")); // pointsFor
        }
    }
}
