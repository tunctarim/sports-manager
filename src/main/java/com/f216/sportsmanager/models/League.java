package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import java.util.ArrayList;
import java.util.List;

public class League {

    private String leagueName;
    private ISport sportType;
    private List<ITeam> teams;

    public League(String leagueName, ISport sportType) {
        this.leagueName = leagueName;
        this.sportType = sportType;
        this.teams = new ArrayList<>();
    }

    public void addTeam(ITeam team) {
        teams.add(team);
    }

    public List<ITeam> getTeams() {
        return teams;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public ISport getSportType() {
        return sportType;
    }
}