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
    @DisplayName("Test League Generation")
    void testGenerateLeague() {
        League league = DatabaseFactory.generateLeague("Test League", football);
        assertNotNull(league);
        assertEquals("Test League", league.getLeagueName());
        assertThrows(IllegalArgumentException.class, () -> DatabaseFactory.generateLeague("Fail", null));
    }

    @Test
    @DisplayName("Test Name Loading and Shuffle Logic")
    void testLoadNames() {
        List<String> names = DatabaseFactory.loadNames("M");
        assertNotNull(names);
        assertFalse(names.isEmpty());

        List<String> namesSecondLoad = DatabaseFactory.loadNames("M");
        assertNotEquals(names, namesSecondLoad, "Lists should be shuffled and thus likely different order");
    }

    @Test
    @DisplayName("Test Team Creation with Roster")
    void testCreateTeam() {
        ITeam team = DatabaseFactory.createTeam("Göztepe", football, Tactic.BALANCED);
        assertNotNull(team);
        assertEquals(22, team.getPlayers().size(), "Football roster must be 11 positions * 2 = 22");
    }

    @Test
    @DisplayName("Test Roster Generation and Gender-Name Matching")
    void testGenerateRoster() {
        List<IPlayer> roster = DatabaseFactory.generateRoster(football);
        assertEquals(22, roster.size());

        for (IPlayer player : roster) {
            String name = player.getName();
            Gender gender = player.getGender();

            if (!name.startsWith("Player_") && !name.equals("Generic Player")) {
                if (gender == Gender.FEMALE) {
                    assertFalse(maleNames.contains(name), "Female player " + name + " found in male pool");
                } else {
                    assertFalse(femaleNames.contains(name), "Male player " + name + " found in female pool");
                }
            }
        }
    }

    @Test
    @DisplayName("Test Stat Randomization Range")
    void testRandomizeStats() {
        IPlayer player = new DatabaseFactory.Player("Test", 20, Gender.MALE, Football.FootballPosition.GK);
        DatabaseFactory.randomizeStats(player, football);

        for (String stat : football.getRequiredStats()) {
            int value = player.getStat(stat);
            assertTrue(value >= 40 && value <= 95, "Stat " + stat + " is out of range: " + value);
        }
    }

    @Test
    @DisplayName("Test Persistence - Save and Load")
    void testSaveAndLoad() {
        League original = new League("Süper Lig", football);
        DatabaseFactory.save(original);

        League loaded = DatabaseFactory.load();
        assertNotNull(loaded);
        assertEquals(original.getLeagueName(), loaded.getLeagueName());

        File file = new File("league_data.dat");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("Test Asset Pointer Mapping")
    void testLoadAssetPointers() {
        Map<String, String> assets = DatabaseFactory.loadAssetPointers();
        assertNotNull(assets);
        // Ensure it doesn't crash even if the folder is missing or empty
    }
}