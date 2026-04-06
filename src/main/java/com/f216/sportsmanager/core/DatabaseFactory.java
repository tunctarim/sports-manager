package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.Gender;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.*;
import com.f216.sportsmanager.models.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DatabaseFactory {
    private static final String NAMES_FILE = "src/main/resources/names";
    private static final String SAVE_PATH = "league_data.dat";
    private static final Random RANDOM = new Random();
    static class Player extends BasePlayer implements Serializable {
        Player(String name, int age, Gender gender, PlayerPosition position) {
            super(name, age, gender, position);
        }
    }
    static class Team extends BaseTeam implements Serializable {
        public Team(String name, List<IPlayer> players, Tactic tactic) {
            super(name, players, tactic);
        }
    }


    /**
     * Creates a league for the specified sport type.
     */
    public static League generateLeague(String name, ISport sport) {
        if (sport == null) {
            throw new IllegalArgumentException("Sport type must not be null[cite: 317].");
        }
        return new League(name, sport);
    }

    protected static List<String> loadNames(String gender) {
        try {
            List<String> allNames = Files.readAllLines(Paths.get(NAMES_FILE + gender + ".txt"));
            if (allNames.isEmpty()) return new ArrayList<>(List.of("Generic Player"));

            List<String> mutableList = new ArrayList<>(allNames);
            Collections.shuffle(mutableList);
            return mutableList;
        } catch (IOException e) {
            return new ArrayList<>(List.of("Generic Player"));
        }
    }

    /**
     * Creates a team of the specified sport type.
     */
    public static ITeam createTeam(String teamName, ISport sport, Tactic tactic) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be empty[cite: 317].");
        }
        return new Team(teamName, generateRoster(sport), tactic);
    }

    /**
     * Generates a roster in accordance with the rules of the sport.
     */
    public static List<IPlayer> generateRoster(ISport sport) {
        List<IPlayer> roster = new ArrayList<>();
        List<String> namesF = loadNames("F");
        List<String> namesM = loadNames("M");
        List<PlayerPosition> positions = sport.getRequiredPositions();

        for (PlayerPosition pos : positions) {
            for (int i = 0; i < 2; i++) {
                Gender randomGender = Gender.values()[RANDOM.nextInt(Gender.values().length)];

                List<String> targetPool = (randomGender == Gender.FEMALE) ? namesF : namesM;

                String name = targetPool.isEmpty()
                        ? "Player_" + (roster.size() + 1)
                        : targetPool.removeFirst();

                IPlayer p = new Player(name, 20, randomGender, pos);
                randomizeStats(p, sport);
                roster.add(p);
            }
        }
        return roster;
    }
    /**
     * Assigns random generated stats to players.
     */
    public static void randomizeStats(IPlayer player, ISport sport) {
        List<String> neededStats = sport.getRequiredStats();

        for (String statName : neededStats) {
            // Defensive coding: ensuring stats are in a realistic "border" range [cite: 227, 549]
            int randomValue = RANDOM.nextInt(40, 96);
            player.setStat(statName, randomValue);
        }
    }

    /**
     * Scans local folders for available image files.
     */
    public static Map<String, String> loadAssetPointers() {
        Map<String, String> assetMap = new HashMap<>();
        // Proactive check: "Assumptions are our enemy" [cite: 655]
        File assetFolder = new File("src/main/resources/assets");

        if (assetFolder.exists() && assetFolder.isDirectory()) {
            File[] files = assetFolder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        assetMap.put(f.getName(), f.getAbsolutePath());
                    }
                }
            }
        }
        return assetMap;
    }

    // --- Persistence Logic (The "Standard") ---

    public static void save(League league) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_PATH))) {
            oos.writeObject(league);
        } catch (IOException e) {
            // "These messages carry a lot of information. Use them." [cite: 34, 42]
            e.printStackTrace();
        }
    }

    public static League load() {
        File file = new File(SAVE_PATH);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_PATH))) {
            return (League) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
