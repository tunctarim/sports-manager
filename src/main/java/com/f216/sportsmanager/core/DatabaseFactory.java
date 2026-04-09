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
    public static League generateLeague(String leagueName, ISport sport) {
        if (sport == null) throw new IllegalArgumentException("Sport cannot be null");

        League league = new League(leagueName, sport);
        String teamFilePath = "src/main/resources/teamNames.txt";
        int TEAM_LIMIT = 20;

        try {
            List<String> allNames = Files.readAllLines(Paths.get(teamFilePath));

            Collections.shuffle(allNames);

            for (int i = 0; i < allNames.size() && league.getTeamCount() < TEAM_LIMIT; i++) {
                String name = allNames.get(i).trim();
                if (name.isBlank()) continue;

                ITeam team = createTeam(name, sport, Tactic.BALANCED);
                league.addTeam(team);
            }

        } catch (IOException e) {
            System.err.println("Error reading teamNames.txt: " + e.getMessage());
        }

        return league;
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
            throw new IllegalArgumentException("Team name cannot be empty.");
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
        int positionIndex = 0;

        while (roster.size() < sport.getRosterSize()) {
            PlayerPosition pos = positions.get(positionIndex % positions.size());

            Gender randomGender = Gender.values()[RANDOM.nextInt(Gender.values().length)];
            List<String> targetPool = (randomGender == Gender.FEMALE) ? namesF : namesM;

            String name = targetPool.isEmpty()
                    ? "Player_" + (roster.size() + 1)
                    : targetPool.removeFirst();

            IPlayer p = new Player(name, 20, randomGender, pos);
            randomizeStats(p, sport);
            roster.add(p);

            positionIndex++;
        }
        return roster;
    }
    /**
     * Assigns random generated stats to players.
     */
    public static void randomizeStats(IPlayer player, ISport sport) {
        List<String> neededStats = sport.getRequiredStats();

        for (String statName : neededStats) {
            int randomValue = RANDOM.nextInt(40, 96);
            player.setStat(statName, randomValue);
        }
    }

    /**
     * Scans local folders for available image files.
     */
    public static Map<String, String> loadAssetPointers() {
        Map<String, String> assetMap = new HashMap<>();
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

    public static void save(League league) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_PATH))) {
            oos.writeObject(league);
        } catch (IOException e) {
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
