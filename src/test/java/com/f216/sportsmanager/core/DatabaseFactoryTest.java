package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.*;
import com.f216.sportsmanager.interfaces.*;
import com.f216.sportsmanager.models.*;
import com.f216.sportsmanager.sports.Football;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseFactoryTest {

    private ISport football;
    private List<String> maleNames;
    private List<String> femaleNames;

    @BeforeEach
    void setUp() {
        football = new Football();
        maleNames = DatabaseFactory.loadNames("M");
        femaleNames = DatabaseFactory.loadNames("F");
    }

    @Test
    @DisplayName("1. Test League Generation and Team Limit")
    void testGenerateLeague() {
        League league = DatabaseFactory.generateLeague("Süper Lig", football);
        assertNotNull(league);
        assertEquals(20, league.getTeamCount(), "League should stop at exactly 20 teams.");
    }

    @Test
    @DisplayName("2. Total Roster Size Invariant")
    void testTotalRosterSize() {
        League league = DatabaseFactory.generateLeague("Roster Test", football);
        int totalPlayers = 0;

        for (ITeam team : league.getTeams()) {
            totalPlayers += team.getPlayers().size();
        }

        int expectedSize = 20 * 22; // 20 teams * 22 players per team
        assertEquals(expectedSize, totalPlayers, "The whole league should contain 440 players.");
    }

    @Test
    @DisplayName("3. Team Name Randomization - Shuffled Selection")
    void testTeamNameRandomization() {
        League leagueA = DatabaseFactory.generateLeague("League A", football);
        League leagueB = DatabaseFactory.generateLeague("League B", football);

        Set<String> namesA = new HashSet<>();
        for (ITeam t : leagueA.getTeams()) {
            namesA.add(t.getTeamName());
        }

        Set<String> namesB = new HashSet<>();
        for (ITeam t : leagueB.getTeams()) {
            namesB.add(t.getTeamName());
        }

        assertNotEquals(namesA, namesB, "Two generated leagues should not have the exact same 20 teams.");
    }

    @Test
    @DisplayName("4. Player Name Randomization - Unique Rosters")
    void testPlayerNameRandomization() {
        League league = DatabaseFactory.generateLeague("Player Random Test", football);

        ITeam team1 = league.getTeams().get(0);
        ITeam team2 = league.getTeams().get(1);

        Set<String> roster1 = new HashSet<>();
        for (IPlayer p : team1.getPlayers()) {
            roster1.add(p.getName());
        }

        Set<String> roster2 = new HashSet<>();
        for (IPlayer p : team2.getPlayers()) {
            roster2.add(p.getName());
        }

        assertNotEquals(roster1, roster2, "Different teams should have different player rosters.");
    }

    @Test
    @DisplayName("5. Test Stat Randomization Range")
    void testRandomizeStats() {
        IPlayer player = new DatabaseFactory.Player("Test", 20, Gender.MALE, Football.FootballPosition.GK);
        DatabaseFactory.randomizeStats(player, football);

        for (String stat : football.getRequiredStats()) {
            int value = player.getStat(stat);
            assertTrue(value >= 40 && value <= 95, "Stat " + stat + " out of range (40-95).");
        }
    }

    @Test
    @DisplayName("6. Test Persistence - Save and Load")
    void testSaveAndLoad() {
        League original = new League("Süper Lig", football);
        DatabaseFactory.save(original);

        League loaded = DatabaseFactory.load();
        assertNotNull(loaded);
        assertEquals(original.getLeagueName(), loaded.getLeagueName());

        File file = new File("league_data.dat");
        if (file.exists()) file.delete();
    }

    @Test
    @DisplayName("7. Test Name Loading and Shuffle Logic")
    void testLoadNames() {
        List<String> names = DatabaseFactory.loadNames("M");
        assertNotNull(names);
        assertFalse(names.isEmpty());

        List<String> namesSecondLoad = DatabaseFactory.loadNames("M");
        // Checking that Collections.shuffle worked
        assertNotEquals(names, namesSecondLoad, "Names should be in a different order after reloading.");
    }
}