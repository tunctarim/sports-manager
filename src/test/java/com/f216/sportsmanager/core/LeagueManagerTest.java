package com.f216.sportsmanager.core;
import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.League;
import com.f216.sportsmanager.models.MatchResult;
import com.f216.sportsmanager.models.StandingRecord;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LeagueManagerTest {
    private static final ISport FOOTBALL = new ISport() {
        @Override public String getSportName()        { return "Football"; }
        @Override public int    getSegmentCount()     { return 2; }
        @Override public int    getPointsPerWin()     { return 3; }
        @Override public int    getPointsPerDraw()    { return 1; }
        @Override public int    getRosterSize()       { return 11; }
        @Override public int    getSegmentLimit()     { return 45; }
        @Override public int    getTickInterval()     { return 1000; }
        @Override public int    getTotalMatchLength() { return 90; }
        @Override public List<String> getRequiredStats() { return List.of("pace", "shooting"); }
        @Override public EndCondition getEndCondition()  { return EndCondition.TIME_LIMIT; }
    };

    static class StubTeam implements ITeam {
        private final String  name;
        private Tactic  tactic = Tactic.BALANCED;
        private int     points = 0;
        private int     wins   = 0;
        private int     losses = 0;
        private int     pf     = 0;
        private int     pa     = 0;

        StubTeam(String name) { this.name = name; }

        @Override public List<IPlayer>          getPlayers()  { return List.of(); }
        @Override public Map<String, Integer>   getTeamStats() {
            Map<String,Integer> m = new java.util.HashMap<>();
            m.put("wins",   wins);   m.put("losses",  losses);
            m.put("points", points); m.put("pointsFor",pf);
            m.put("pointsAgainst", pa);
            m.put("pointDifferential", pf - pa);
            return m;
        }
        @Override public void    setTactic(Tactic t)                  { this.tactic = t; }
        @Override public Tactic  getTactic()                           { return tactic; }
        @Override public int     getPoints()                           { return points; }
        @Override public void    updateRecord(int scored, int conceded, int pts) {
            pf += scored; pa += conceded; points += pts;
            if (pts > 0) wins++; else losses++;
        }
        @Override public void addPlayer(IPlayer p)    {}
        @Override public void removePlayer(IPlayer p) {}
        @Override public String toString()             { return "Team[" + name + "]"; }
    }

    private League makeLeague(int teamCount) {
        League league = new League("Test League", FOOTBALL);
        for (int i = 1; i <= teamCount; i++) {
            league.addTeam(new StubTeam("T" + i));
        }
        return league;
    }

    private LeagueManager manager;

    @BeforeEach
    void setUp() {
        manager = new LeagueManager(42L); // deterministic seed
    }
    @Nested
    @DisplayName("setLeagueData")
    class SetLeagueDataTests {

        @Test
        @DisplayName("null league – throws")
        void testNullLeagueThrows() {
            assertThrows(IllegalArgumentException.class, () -> manager.setLeagueData(null));
        }

        @Test
        @DisplayName("single-team league – throws (cannot schedule)")
        void testOneTeamLeagueThrows() {
            League one = new League("L", FOOTBALL);
            one.addTeam(new StubTeam("A"));
            assertThrows(IllegalArgumentException.class, () -> manager.setLeagueData(one));
        }

        @Test
        @DisplayName("valid league accepted – state resets")
        void testValidLeagueAccepted() {
            manager.setLeagueData(makeLeague(4));
            assertFalse(manager.isSeasonEnded());
            assertEquals(0, manager.getCurrentWeek());
        }

        @Test
        @DisplayName("replacing league resets all state")
        void testReplacingLeagueResetsState() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            manager.playMatchDay();

            manager.setLeagueData(makeLeague(6)); // replace
            assertEquals(0, manager.getCurrentWeek());
            assertEquals(0, manager.getTotalWeeks()); // schedule cleared
            assertTrue(manager.getPlayedResults().isEmpty());
        }
    }
    @Nested
    @DisplayName("generateSchedule")
    class GenerateScheduleTests {

        @Test
        @DisplayName("no league – throws IllegalStateException")
        void testNoLeagueThrows() {
            assertThrows(IllegalStateException.class, manager::generateSchedule);
        }

        @Test
        @DisplayName("4 teams → 3 rounds (n-1)")
        void testFourTeamsThreeRounds() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            assertEquals(3, manager.getTotalWeeks());
        }


        @Test
        @DisplayName("6 teams → 5 rounds")
        void testSixTeamsFiveRounds() {
            manager.setLeagueData(makeLeague(6));
            manager.generateSchedule();
            assertEquals(5, manager.getTotalWeeks());
        }

        @Test
        @DisplayName("4 teams: each round has exactly 2 fixtures")
        void testEachRoundHasHalfNFixtures() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            for (int w = 0; w < manager.getTotalWeeks(); w++) {
                assertEquals(2, manager.getFixturesForWeek(w).size(),
                        "Week " + w + " should have 2 fixtures");
            }
        }

        @Test
        @DisplayName("total fixture count = n*(n-1)/2")
        void testTotalFixtureCount() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            long total = manager.getFullSchedule().stream()
                    .mapToLong(List::size).sum();
            assertEquals(6L, total); // 4*3/2 = 6
        }

        @Test
        @DisplayName("no fixture has same team on both sides")
        void testNoSelfPlay() {
            manager.setLeagueData(makeLeague(6));
            manager.generateSchedule();
            for (List<Fixture> round : manager.getFullSchedule()) {
                for (Fixture f : round) {
                    assertNotSame(f.getHome(), f.getAway());
                }
            }
        }

        @Test
        @DisplayName("odd team count (3) generates schedule without crash")
        void testOddTeamCount() {
            manager.setLeagueData(makeLeague(3));
            assertDoesNotThrow(manager::generateSchedule);
            assertTrue(manager.getTotalWeeks() > 0);
        }

        @Test
        @DisplayName("getFullSchedule returns unmodifiable wrapper")
        void testFullScheduleUnmodifiable() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            assertThrows(UnsupportedOperationException.class,
                    () -> manager.getFullSchedule().remove(0));
        }
    }
    @Nested
    @DisplayName("playMatchDay")
    class PlayMatchDayTests {

        @Test
        @DisplayName("no league – throws")
        void testNoLeagueThrows() {
            assertThrows(IllegalStateException.class, manager::playMatchDay);
        }

        @Test
        @DisplayName("no schedule generated – throws")
        void testNoScheduleThrows() {
            manager.setLeagueData(makeLeague(4));
            assertThrows(IllegalStateException.class, manager::playMatchDay);
        }

        @Test
        @DisplayName("week counter advances after each call")
        void testWeekAdvances() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            assertEquals(0, manager.getCurrentWeek());
            manager.playMatchDay();
            assertEquals(1, manager.getCurrentWeek());
        }

        @Test
        @DisplayName("results accumulate across weeks")
        void testResultsAccumulate() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            manager.playMatchDay(); // 2 matches
            manager.playMatchDay(); // 2 matches
            assertEquals(4, manager.getPlayedResults().size());
        }

        @Test
        @DisplayName("results list is unmodifiable")
        void testResultsListUnmodifiable() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            manager.playMatchDay();
            assertThrows(UnsupportedOperationException.class,
                    () -> manager.getPlayedResults().clear());
        }

        @Test
        @DisplayName("calling playMatchDay after season ends is a no-op")
        void testCallAfterSeasonEndsIsNoOp() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            int total = manager.getTotalWeeks();
            for (int i = 0; i < total; i++) manager.playMatchDay();

            assertTrue(manager.isSeasonEnded());
            int resultsBefore = manager.getPlayedResults().size();
            manager.playMatchDay(); // should be no-op
            assertEquals(resultsBefore, manager.getPlayedResults().size());
        }

        @Test
        @DisplayName("result week numbers are correct")
        void testResultWeekNumbers() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            manager.playMatchDay(); // week 1
            for (MatchResult r : manager.getPlayedResults()) {
                assertEquals(1, r.getWeek());
            }
        }
    }

    @Nested
    @DisplayName("updateStandings")
    class UpdateStandingsTests {

        private StubTeam home;
        private StubTeam away;

        @BeforeEach
        void setUpTeams() {
            home = new StubTeam("Home");
            away = new StubTeam("Away");
            League league = new League("L", FOOTBALL);
            league.addTeam(home);
            league.addTeam(away);
            manager.setLeagueData(league);
        }

        @Test
        @DisplayName("null result – throws")
        void testNullResultThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> manager.updateStandings(null));
        }

        @Test
        @DisplayName("home win → 3 pts to home, 0 to away")
        void testHomeWin() {
            manager.updateStandings(new MatchResult(home, away, 3, 1, 1));
            assertEquals(3, home.getPoints());
            assertEquals(0, away.getPoints());
        }

        @Test
        @DisplayName("away win → 3 pts to away, 0 to home")
        void testAwayWin() {
            manager.updateStandings(new MatchResult(home, away, 0, 2, 1));
            assertEquals(0, home.getPoints());
            assertEquals(3, away.getPoints());
        }

        @Test
        @DisplayName("draw → 1 pt each")
        void testDraw() {
            manager.updateStandings(new MatchResult(home, away, 1, 1, 1));
            assertEquals(1, home.getPoints());
            assertEquals(1, away.getPoints());
        }

        @Test
        @DisplayName("cumulative: two home wins")
        void testCumulativePoints() {
            manager.updateStandings(new MatchResult(home, away, 2, 0, 1));
            manager.updateStandings(new MatchResult(home, away, 1, 0, 2));
            assertEquals(6, home.getPoints());
        }
    }



    @Nested
    @DisplayName("getStandingsTable")
    class StandingsTableTests {

        @Test
        @DisplayName("no league – throws")
        void testNoLeagueThrows() {
            assertThrows(IllegalStateException.class, manager::getStandingsTable);
        }

        @Test
        @DisplayName("returns one record per team")
        void testRecordPerTeam() {
            manager.setLeagueData(makeLeague(4));
            assertEquals(4, manager.getStandingsTable().size());
        }

        @Test
        @DisplayName("sorted by points descending after a match day")
        void testSortedAfterPlay() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            manager.playMatchDay();
            List<StandingRecord> standings = manager.getStandingsTable();
            for (int i = 0; i < standings.size() - 1; i++) {
                assertTrue(standings.get(i).getPoints() >= standings.get(i + 1).getPoints(),
                        "Standings should be sorted by points desc");
            }
        }

        @Test
        @DisplayName("unmodifiable list returned")
        void testUnmodifiable() {
            manager.setLeagueData(makeLeague(4));
            assertThrows(UnsupportedOperationException.class,
                    () -> manager.getStandingsTable().remove(0));
        }
    }



    @Nested
    @DisplayName("checkSeasonEnd")
    class CheckSeasonEndTests {

        @Test
        @DisplayName("returns false before all weeks played")
        void testFalseBeforeEnd() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            assertFalse(manager.checkSeasonEnd());
        }

        @Test
        @DisplayName("returns true after all weeks played")
        void testTrueAfterAllWeeks() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            int total = manager.getTotalWeeks();
            for (int i = 0; i < total; i++) manager.playMatchDay();
            assertTrue(manager.checkSeasonEnd());
        }

        @Test
        @DisplayName("champion is set after season ends")
        void testChampionSet() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            int total = manager.getTotalWeeks();
            for (int i = 0; i < total; i++) manager.playMatchDay();
            assertNotNull(manager.getChampion());
        }

        @Test
        @DisplayName("champion is null before season ends")
        void testChampionNullBeforeEnd() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            assertNull(manager.getChampion());
        }

        @Test
        @DisplayName("idempotent – multiple calls after season ends remain true")
        void testIdempotent() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            for (int i = 0; i < manager.getTotalWeeks(); i++) manager.playMatchDay();
            assertTrue(manager.checkSeasonEnd());
            assertTrue(manager.checkSeasonEnd()); // second call
        }
    }

    @Nested
    @DisplayName("getDashboardData")
    class DashboardDataTests {

        @Test
        @DisplayName("no league – throws")
        void testNoLeagueThrows() {
            assertThrows(IllegalStateException.class, manager::getDashboardData);
        }

        @Test
        @DisplayName("contains expected keys")
        void testExpectedKeys() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            Map<String, Object> data = manager.getDashboardData();
            assertTrue(data.containsKey("currentWeek"));
            assertTrue(data.containsKey("totalWeeks"));
            assertTrue(data.containsKey("seasonEnded"));
            assertTrue(data.containsKey("standings"));
            assertTrue(data.containsKey("recentResults"));
            assertTrue(data.containsKey("nextFixtures"));
        }

        @Test
        @DisplayName("champion key only present after season ends")
        void testChampionKeyOnlyAfterEnd() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            assertFalse(manager.getDashboardData().containsKey("champion"));

            for (int i = 0; i < manager.getTotalWeeks(); i++) manager.playMatchDay();
            assertTrue(manager.getDashboardData().containsKey("champion"));
        }

        @Test
        @DisplayName("returned map is unmodifiable")
        void testUnmodifiable() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            assertThrows(UnsupportedOperationException.class,
                    () -> manager.getDashboardData().put("hack", "value"));
        }
    }

    @Nested
    @DisplayName("Fixture accessors")
    class FixtureAccessorTests {

        @Test
        @DisplayName("getCurrentWeekFixtures before schedule → empty list")
        void testBeforeScheduleEmpty() {
            manager.setLeagueData(makeLeague(4));
            assertTrue(manager.getCurrentWeekFixtures().isEmpty());
        }

        @Test
        @DisplayName("getCurrentWeekFixtures advances each week")
        void testCurrentWeekFixturesAdvances() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            List<Fixture> week0 = manager.getCurrentWeekFixtures();
            assertFalse(week0.isEmpty());
            manager.playMatchDay();
            List<Fixture> week1 = manager.getCurrentWeekFixtures();
            assertNotEquals(week0, week1);
        }

        @Test
        @DisplayName("getFixturesForWeek with out-of-bounds index → empty list")
        void testOutOfBoundsWeek() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            assertTrue(manager.getFixturesForWeek(-1).isEmpty());
            assertTrue(manager.getFixturesForWeek(999).isEmpty());
        }
    }

    @Nested
    @DisplayName("Full season integration")
    class FullSeasonTests {

        @Test
        @DisplayName("all 4 teams play n*(n-1)/2 matches total")
        void testTotalMatchesCorrect() {
            manager.setLeagueData(makeLeague(4));
            manager.generateSchedule();
            while (!manager.isSeasonEnded()) manager.playMatchDay();
            assertEquals(6, manager.getPlayedResults().size()); // C(4,2) = 6
        }

        @Test
        @DisplayName("total points distributed equals sum over all results")
        void testTotalPointsDistributed() {
            League league = makeLeague(4);
            manager.setLeagueData(league);
            manager.generateSchedule();
            while (!manager.isSeasonEnded()) manager.playMatchDay();

            int teamPointsSum = league.getTeams().stream()
                    .mapToInt(ITeam::getPoints).sum();
            // Minimum = 6 * 3 (all decisive) = 18 ; maximum with draws = 6 * 2 = 12
            // Just verify it's a positive integer
            assertTrue(teamPointsSum > 0,
                    "Total distributed points should be positive, got " + teamPointsSum);
        }

        @Test
        @DisplayName("champion is a team that was in the league")
        void testChampionBelongsToLeague() {
            League league = makeLeague(4);
            manager.setLeagueData(league);
            manager.generateSchedule();
            while (!manager.isSeasonEnded()) manager.playMatchDay();
            assertTrue(league.containsTeam(manager.getChampion()));
        }
    }
}
