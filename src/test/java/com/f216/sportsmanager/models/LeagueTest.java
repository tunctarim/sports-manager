package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class LeagueTest {

    private static final ISport FOOTBALL_SPORT = new ISport() {
        private enum MockPosition implements PlayerPosition {
            GK("GK"),
            FLD("FLD");

            private final String code;

            MockPosition(String code) {
                this.code = code;
            }
            @Override public String getCode() { return code; }
        }

        @Override
        public String getSportName() {
            return "Football";
        }

        @Override
        public int getSegmentCount() {
            return 2;
        }

        @Override
        public int getPointsPerWin() {
            return 3;
        }

        @Override
        public int getPointsPerDraw() {
            return 1;
        }

        @Override
        public int getRosterSize() {
            return 11;
        }

        @Override
        public int getSegmentLimit() {
            return 45;
        }

        @Override
        public int getTickInterval() {
            return 1000;
        }

        @Override
        public int getTotalMatchLength() {
            return 90;
        }

        @Override
        public List<String> getRequiredStats() {
            return List.of("goals", "assists");
        }

        @Override
        public EndCondition getEndCondition() {
            return EndCondition.TIME_LIMIT;
        }

        @Override
        public List<PlayerPosition> getRequiredPositions() {
            return List.of(MockPosition.GK, MockPosition.FLD);
        }
    };

    private static ISport sportStub(String name) {
        return new ISport() {
            private enum MockPosition implements PlayerPosition {
                GK("GK"),
                FLD("FLD");

                private final String code;

                MockPosition(String code) {
                    this.code = code;
                }
                @Override public String getCode() { return code; }
            }
            @Override
            public String getSportName() {
                return name;
            }

            @Override
            public int getSegmentCount() {
                return 1;
            }

            @Override
            public int getPointsPerWin() {
                return 2;
            }

            @Override
            public int getPointsPerDraw() {
                return 0;
            }

            @Override
            public int getRosterSize() {
                return 5;
            }

            @Override
            public int getSegmentLimit() {
                return 10;
            }

            @Override
            public int getTickInterval() {
                return 500;
            }

            @Override
            public int getTotalMatchLength() {
                return 10;
            }

            @Override
            public List<String> getRequiredStats() {
                return List.of();
            }

            @Override
            public EndCondition getEndCondition() {
                return EndCondition.SCORE_LIMIT;
            }
            @Override
            public List<PlayerPosition> getRequiredPositions() {
                return List.of(MockPosition.GK, MockPosition.FLD);
            }
        };
    }

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
            public void addPlayer(IPlayer player) {
            }

            @Override
            public void removePlayer(IPlayer player) {
            }

            @Override
            public String toString() {
                return "TeamStub[" + label + "]";
            }
        };
    }
    private League league;
    private ITeam teamA;
    private ITeam teamB;

    @BeforeEach
    void setUp() {
        league = new League("Premier League", FOOTBALL_SPORT);
        teamA = teamStub("A");
        teamB = teamStub("B");
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("valid args – league created successfully")
        void testValidConstruction() {
            assertNotNull(league);
            assertEquals("Premier League", league.getLeagueName());
            assertSame(FOOTBALL_SPORT, league.getSportType());
        }

        @Test
        @DisplayName("null name – throws IllegalArgumentException")
        void testNullNameThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new League(null, FOOTBALL_SPORT));
        }

        @Test
        @DisplayName("blank name – throws IllegalArgumentException")
        void testBlankNameThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new League("   ", FOOTBALL_SPORT));
        }

        @Test
        @DisplayName("empty name – throws IllegalArgumentException")
        void testEmptyNameThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new League("", FOOTBALL_SPORT));
        }

        @Test
        @DisplayName("null sport – throws IllegalArgumentException")
        void testNullSportThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new League("Some League", null));
        }

        @Test
        @DisplayName("new league has zero teams")
        void testTeamsInitiallyEmpty() {
            assertTrue(league.getTeams().isEmpty());
            assertEquals(0, league.getTeamCount());
        }
    }

    @Nested
    @DisplayName("getLeagueName")
    class LeagueNameTests {

        @Test
        @DisplayName("returns the name supplied at construction")
        void testGetLeagueName() {
            assertEquals("Premier League", league.getLeagueName());
        }

        @Test
        @DisplayName("names are case-sensitive")
        void testLeagueNameCaseSensitive() {
            League upper = new League("SERIE A", sportStub("Football"));
            League lower = new League("serie a", sportStub("Football"));
            assertNotEquals(upper.getLeagueName(), lower.getLeagueName());
        }
    }

    @Nested
    @DisplayName("getSportType")
    class SportTypeTests {

        @Test
        @DisplayName("returns the exact sport instance passed in")
        void testGetSportTypeIdentity() {
            assertSame(FOOTBALL_SPORT, league.getSportType());
        }

        @Test
        @DisplayName("two leagues can reference different sports")
        void testDifferentSportsAreDistinct() {
            ISport volleyball = sportStub("Volleyball");
            League vLeague = new League("Volleyball League", volleyball);
            assertNotSame(league.getSportType(), vLeague.getSportType());
            assertEquals("Volleyball", vLeague.getSportType().getSportName());
        }
    }

    @Nested
    @DisplayName("addTeam")
    class AddTeamTests {

        @Test
        @DisplayName("team count increases after addTeam")
        void testAddTeamIncreasesCount() {
            league.addTeam(teamA);
            assertEquals(1, league.getTeamCount());
        }

        @Test
        @DisplayName("added team is present in getTeams()")
        void testAddedTeamIsPresent() {
            league.addTeam(teamA);
            assertTrue(league.getTeams().contains(teamA));
        }

        @Test
        @DisplayName("multiple distinct teams can be added")
        void testMultipleTeamsAdded() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            assertEquals(2, league.getTeamCount());
            assertTrue(league.getTeams().contains(teamA));
            assertTrue(league.getTeams().contains(teamB));
        }

        @Test
        @DisplayName("null team is silently ignored")
        void testAddNullTeamIsIgnored() {
            league.addTeam(null);
            assertEquals(0, league.getTeamCount());
        }

        @Test
        @DisplayName("adding the same team twice results in only one entry")
        void testAddDuplicateTeamIsIgnored() {
            league.addTeam(teamA);
            league.addTeam(teamA); // duplicate
            assertEquals(1, league.getTeamCount());
        }

        @Test
        @DisplayName("adding null after a real team does not affect roster")
        void testAddNullAfterRealTeam() {
            league.addTeam(teamA);
            league.addTeam(null);
            assertEquals(1, league.getTeamCount());
        }
    }


    @Nested
    @DisplayName("removeTeam")
    class RemoveTeamTests {

        @Test
        @DisplayName("removing enrolled team decreases count")
        void testRemoveTeamDecreasesCount() {
            league.addTeam(teamA);
            league.removeTeam(teamA);
            assertEquals(0, league.getTeamCount());
        }

        @Test
        @DisplayName("removed team is absent from getTeams()")
        void testRemovedTeamIsAbsent() {
            league.addTeam(teamA);
            league.removeTeam(teamA);
            assertFalse(league.getTeams().contains(teamA));
        }

        @Test
        @DisplayName("removing a team that was never added is a no-op")
        void testRemoveNonExistentTeamIsNoOp() {
            league.addTeam(teamA);
            league.removeTeam(teamB); // teamB was never added
            assertEquals(1, league.getTeamCount());
        }

        @Test
        @DisplayName("removing null is silently ignored")
        void testRemoveNullIsIgnored() {
            league.addTeam(teamA);
            league.removeTeam(null);
            assertEquals(1, league.getTeamCount());
        }

        @Test
        @DisplayName("removing one team leaves other teams intact")
        void testRemoveOneTeamLeavesOthersIntact() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            league.removeTeam(teamA);
            assertFalse(league.getTeams().contains(teamA));
            assertTrue(league.getTeams().contains(teamB));
            assertEquals(1, league.getTeamCount());
        }
    }

    @Nested
    @DisplayName("getTeams – list view safety")
    class GetTeamsTests {

        @Test
        @DisplayName("getTeams() returns unmodifiable list – add throws")
        void testGetTeamsUnmodifiableAdd() {
            league.addTeam(teamA);
            List<ITeam> view = league.getTeams();
            assertThrows(UnsupportedOperationException.class, () -> view.add(teamB));
        }

        @Test
        @DisplayName("getTeams() returns unmodifiable list – remove throws")
        void testGetTeamsUnmodifiableRemove() {
            league.addTeam(teamA);
            List<ITeam> view = league.getTeams();
            assertThrows(UnsupportedOperationException.class, () -> view.remove(teamA));
        }

        @Test
        @DisplayName("returned list reflects subsequent addTeam calls")
        void testGetTeamsReflectsSubsequentAdds() {
            List<ITeam> before = league.getTeams();
            league.addTeam(teamA);
            // The view is a live unmodifiable wrapper, so size should update
            assertEquals(1, before.size());
        }
    }

    @Nested
    @DisplayName("getTeamCount")
    class TeamCountTests {

        @Test
        @DisplayName("empty league has count 0")
        void testEmptyLeagueCount() {
            assertEquals(0, league.getTeamCount());
        }

        @Test
        @DisplayName("count matches number of distinct teams added")
        void testCountMatchesAdds() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            assertEquals(2, league.getTeamCount());
        }

        @Test
        @DisplayName("count stays the same after a duplicate add")
        void testCountIdempotentOnDuplicate() {
            league.addTeam(teamA);
            league.addTeam(teamA);
            assertEquals(1, league.getTeamCount());
        }

        @Test
        @DisplayName("count decrements after remove")
        void testCountDecrementsAfterRemove() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            league.removeTeam(teamA);
            assertEquals(1, league.getTeamCount());
        }
    }
    @Nested
    @DisplayName("Edge-case integration")
    class EdgeCaseTests {

        @Test
        @DisplayName("empty league – getTeams returns empty list (never null)")
        void testGetTeamsNeverNull() {
            assertNotNull(league.getTeams());
            assertTrue(league.getTeams().isEmpty());
        }

        @Test
        @DisplayName("add then remove all teams leaves league empty")
        void testAddAndRemoveAllTeams() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            league.removeTeam(teamA);
            league.removeTeam(teamB);
            assertTrue(league.getTeams().isEmpty());
            assertEquals(0, league.getTeamCount());
        }

        @Test
        @DisplayName("re-adding a previously removed team succeeds")
        void testReAddRemovedTeam() {
            league.addTeam(teamA);
            league.removeTeam(teamA);
            league.addTeam(teamA); // re-enroll
            assertEquals(1, league.getTeamCount());
            assertTrue(league.getTeams().contains(teamA));
        }

        @Test
        @DisplayName("league name and sport are immutable across mutations")
        void testLeagueIdentityUnchangedAfterMutations() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            league.removeTeam(teamA);
            assertEquals("Premier League", league.getLeagueName());
            assertSame(FOOTBALL_SPORT, league.getSportType());
        }
    }

    @Nested
    @DisplayName("containsTeam")
    class ContainsTeamTests {

        @Test
        @DisplayName("returns true for an enrolled team")
        void testContainsEnrolledTeam() {
            league.addTeam(teamA);
            assertTrue(league.containsTeam(teamA));
        }

        @Test
        @DisplayName("returns false for a team that was never added")
        void testDoesNotContainUnenrolledTeam() {
            league.addTeam(teamA);
            assertFalse(league.containsTeam(teamB));
        }

        @Test
        @DisplayName("returns false for null")
        void testContainsNullReturnsFalse() {
            assertFalse(league.containsTeam(null));
        }

        @Test
        @DisplayName("returns false after the team has been removed")
        void testContainsReturnsFalseAfterRemove() {
            league.addTeam(teamA);
            league.removeTeam(teamA);
            assertFalse(league.containsTeam(teamA));
        }
    }

    @Nested
    @DisplayName("getStandings")
    class StandingsTests {

        private ITeam teamWithStats(int points, int wins, int losses, int pf, int pa) {
            return new ITeam() {
                @Override
                public List<IPlayer> getPlayers() {
                    return List.of();
                }

                @Override
                public Map<String, Integer> getTeamStats() {
                    Map<String, Integer> m = new java.util.HashMap<>();
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

        @Test
        @DisplayName("empty league – standings list is empty and non-null")
        void testEmptyLeagueStandingsEmpty() {
            List<StandingRecord> standings = league.getStandings();
            assertNotNull(standings);
            assertTrue(standings.isEmpty());
        }

        @Test
        @DisplayName("standings list size matches enrolled team count")
        void testStandingsSizeMatchesTeamCount() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            assertEquals(2, league.getStandings().size());
        }

        @Test
        @DisplayName("each enrolled team is represented in standings")
        void testEachTeamRepresentedInStandings() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            List<StandingRecord> standings = league.getStandings();
            List<ITeam> teams = standings.stream().map(StandingRecord::getTeam).toList();
            assertTrue(teams.contains(teamA));
            assertTrue(teams.contains(teamB));
        }

        @Test
        @DisplayName("standings are sorted by points descending")
        void testStandingsSortedByPointsDesc() {
            ITeam high = teamWithStats(9, 3, 0, 90, 30);
            ITeam low = teamWithStats(3, 1, 2, 50, 70);
            league.addTeam(low); // added in reverse order
            league.addTeam(high);
            List<StandingRecord> s = league.getStandings();
            assertEquals(9, s.get(0).getPoints()); // high points first
            assertEquals(3, s.get(1).getPoints());
        }

        @Test
        @DisplayName("equal points: sorted by goal difference descending")
        void testStandingsSortedByGoalDifferenceOnTie() {
            ITeam better = teamWithStats(6, 2, 1, 80, 40); // gd = +40
            ITeam worse = teamWithStats(6, 2, 1, 60, 50); // gd = +10
            league.addTeam(worse);
            league.addTeam(better);
            List<StandingRecord> s = league.getStandings();
            assertEquals(40, s.get(0).getGoalDifference());
            assertEquals(10, s.get(1).getGoalDifference());
        }

        @Test
        @DisplayName("getStandings() returns unmodifiable list")
        void testStandingsListIsUnmodifiable() {
            league.addTeam(teamA);
            List<StandingRecord> s = league.getStandings();
            assertThrows(UnsupportedOperationException.class,
                    () -> s.remove(0));
        }
    }


    @Nested
    @DisplayName("Fixture generation")
    class FixtureGenerationTests {

        @Test
        @DisplayName("0 teams enrolled → 0 fixtures generated")
        void testNoTeamsNoFixtures() {
            assertTrue(league.generateFixtures().isEmpty());
        }

        @Test
        @DisplayName("1 team enrolled → 0 fixtures (cannot play itself)")
        void testOneTeamNoFixtures() {
            league.addTeam(teamA);
            assertTrue(league.generateFixtures().isEmpty());
        }

        @Test
        @DisplayName("2 teams → 1 single-leg fixture")
        void testTwoTeamsSingleLeg() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            assertEquals(1, league.generateFixtures().size());
        }

        @Test
        @DisplayName("3 teams → 3 single-leg fixtures (n*(n-1)/2)")
        void testThreeTeamsSingleLeg() {
            ITeam teamC = teamStub("C");
            league.addTeam(teamA);
            league.addTeam(teamB);
            league.addTeam(teamC);
            assertEquals(3, league.generateFixtures().size());
        }

        @Test
        @DisplayName("2 teams → 2 double-leg fixtures")
        void testTwoTeamsDoubleLegged() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            assertEquals(2, league.generateDoubleLegged().size());
        }

        @Test
        @DisplayName("3 teams → 6 double-leg fixtures (n*(n-1))")
        void testThreeTeamsDoubleLegged() {
            ITeam teamC = teamStub("C");
            league.addTeam(teamA);
            league.addTeam(teamB);
            league.addTeam(teamC);
            assertEquals(6, league.generateDoubleLegged().size());
        }

        @Test
        @DisplayName("fixture list is unmodifiable")
        void testFixtureListIsUnmodifiable() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            List<Fixture> fixtures = league.generateFixtures();
            assertThrows(UnsupportedOperationException.class,
                    () -> fixtures.remove(0));
        }

        @Test
        @DisplayName("every fixture home and away team is from this league")
        void testFixtureTeamsAreEnrolled() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            for (Fixture f : league.generateFixtures()) {
                assertTrue(league.containsTeam(f.getHome()));
                assertTrue(league.containsTeam(f.getAway()));
            }
        }
    }


    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("contains league name")
        void testToStringContainsName() {
            assertTrue(league.toString().contains("Premier League"));
        }

        @Test
        @DisplayName("contains sport name")
        void testToStringContainsSport() {
            assertTrue(league.toString().contains("Football"));
        }

        @Test
        @DisplayName("contains team count")
        void testToStringContainsTeamCount() {
            league.addTeam(teamA);
            league.addTeam(teamB);
            assertTrue(league.toString().contains("2"));
        }
    }
}
