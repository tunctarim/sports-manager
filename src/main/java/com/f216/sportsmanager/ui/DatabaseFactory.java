package com.f216.sportsmanager.ui;

import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.BasePlayer;
import com.f216.sportsmanager.enums.Gender;
import com.f216.sportsmanager.models.BaseTeam;
import com.f216.sportsmanager.models.League;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseFactory {
    private final int MIN_STAT = 40;
    private final int MAX_STAT = 90;
    private final String LOGO_FOLDER = "assets/logos/";
    private final String PORTRAIT_FOLDER = "assets/portraits/";
    private final Random random;
    private final List<String> availableLogos;
    private final List<String> maleNames;
    private final List<String> femaleNames;
    private final List<String> surnames;

    public DatabaseFactory() {
        this.random = new Random();
        this.availableLogos = new ArrayList<>();
        this.maleNames = List.of("Ahmet", "Tunç", "Ali", "Arda", "Yiğit", "Efe", "Burak");
        this.femaleNames = List.of("Nehir", "Elif", "Özdenur", "Ayşe", "Fatma", "Hayriye");
        this.surnames = List.of("Öztürk", "Kaya", "Oğuz", "Yılmaz", "Özdemir", "Şen");
    }

    public League generateLeague(ISport sport) {
        if (sport == null)
            throw new IllegalArgumentException("Sport cannot be null.");
        String leagueName = sport.getSportName() + " League";
        League league = createLeagueInstance(leagueName, sport);
        int teamCount = getDefaultLeagueSize(sport);

        for (int i = 0; i < teamCount; i++) {
            ITeam team = createTeam(sport);
            List<IPlayer> roster = generateRoster(sport);
            for (IPlayer player : roster) {
                team.getPlayers().add(player);
            }
            league.addTeam(team);
        }

        return league;
    }

    public League createLeagueInstance(String leagueName, ISport sport) {
        if (sport == null) {
            throw new IllegalArgumentException("Sport cannot be null.");
        }
        return new League(leagueName, sport);
    }

    public ITeam createTeam(ISport sport) {
        if (sport == null)
            throw new IllegalArgumentException("Sport cannot be null.");

        String teamName = generateTeamName();
        String logoPath = pickRandomLogo();

        ITeam baseTeam = new BaseTeam(teamName, new ArrayList<>(), null) {
        };

        return baseTeam;
    }

    public List<IPlayer> generateRoster(ISport sport) {
        if (sport == null)
            throw new IllegalArgumentException("Sport cannot be null.");
        List<IPlayer> roster = new ArrayList<>();
        int rosterSize = sport.getRosterSize();
        for (int i = 0; i < rosterSize; i++) {
            Gender gender   = randomGender();
            String name     = generatePlayerName(gender);
            int age = 17 + random.nextInt(20);
            IPlayer player = new BasePlayer(name, age, gender, null) {
            };

            randomizeStats(player, sport);
            roster.add(player);
        }
        return roster;
    }

    public void randomizeStats(IPlayer player, ISport sport) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null.");
        if (sport == null)
            throw new IllegalArgumentException("Sport cannot be null.");

        List<String> requiredStats = sport.getRequiredStats();

        for (String statName : requiredStats) {
            int value = MIN_STAT + random.nextInt(MAX_STAT - MIN_STAT + 1);
            player.setStat(statName, value);
        }
    }

    public void loadAssetPointers() {
        availableLogos.clear();
        java.io.File logoDir = new java.io.File(LOGO_FOLDER);
        if (logoDir.exists() && logoDir.isDirectory()) {
            java.io.File[] files = logoDir.listFiles(
                    (dir, name) -> name.endsWith(".png") || name.endsWith(".jpg")
            );

            if (files != null) {
                for (java.io.File file : files) {
                    availableLogos.add(file.getPath());
                }
            }
        }

        if (availableLogos.isEmpty()) {
            for (int i = 1; i <= 20; i++) {
                availableLogos.add(LOGO_FOLDER + "team_logo_" + i + ".png");
            }
        }
    }

    public int getDefaultLeagueSize(ISport sport) {
        return 20;
    }

    public Gender randomGender() {
        return random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
    }

    public String generatePlayerName(Gender gender) {
        List<String> firstNames = (gender == Gender.MALE) ? maleNames : femaleNames;
        String firstName = firstNames.get(random.nextInt(firstNames.size()));
        String surname   = surnames.get(random.nextInt(surnames.size()));
        return firstName + " " + surname;
    }

    public String generateTeamName() {
        List<String> teamNames = List.of("Galatasaray", "Fenerbahçe", "Beşiktaş", "Trabzonspor", "Adana Demirspor", "Göztepe", "Karşıyakaspor");
        return teamNames.get(random.nextInt(teamNames.size()));
    }

    public String pickRandomLogo() {
        if (availableLogos.isEmpty()) {
            loadAssetPointers();
        }
        return availableLogos.get(random.nextInt(availableLogos.size()));
    }

    public List<String> getAvailableLogos() {
        return java.util.Collections.unmodifiableList(availableLogos);
    }
}
