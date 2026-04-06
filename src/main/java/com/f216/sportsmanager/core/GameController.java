package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.DashboardData;
import com.f216.sportsmanager.models.League;
import com.f216.sportsmanager.ui.DatabaseFactory;

public class GameController {
    private final LeagueManager leagueManager;
    private final DatabaseFactory databaseFactory;
    private ISport currentSport;

    public GameController(LeagueManager leagueManager, DatabaseFactory databaseFactory) {
        this.leagueManager = leagueManager;
        this.databaseFactory = databaseFactory;
    }


    public void initNewGame(ISport sport) {
        this.currentSport = sport;
        League myLeague = databaseFactory.generateLeague(sport);

        leagueManager.setLeagueData(myLeague);
        leagueManager.generateSchedule();
    }

    public void advanceWeek() {
        leagueManager.playMatchDay();
    }

    public void saveGame() {
        System.out.println("Saving...");
    }

    public void loadGame() {
        System.out.println("Loading...");
    }

    public DashboardData getDashboardData() {
        return leagueManager.getDashboardData();
    }

    public void changeTeamTactic(Tactic newTactic) {
        ITeam userTeam = leagueManager.getUserTeam();
        if (userTeam != null) {
            userTeam.setTactic(newTactic);
            System.out.println("Tactic is successfully changed: " + newTactic);
        }
    }


    public ISport getCurrentSport() {
        return currentSport;
    }
}