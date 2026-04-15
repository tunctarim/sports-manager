package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ITeam;

import java.io.Serializable;
import java.util.*;

public abstract class BaseTeam implements ITeam, Serializable {


    private final String teamName;
    private final String teamId;
    private List<IPlayer> players;
    private Tactic tactic;
    private int points;
    private int wins;
    private int losses;
    private int pointsFor;
    private int pointsAgainst;

    public BaseTeam(String name, List<IPlayer> players, Tactic tactic) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Team name cannot be empty.");
        this.teamName = name;
        this.tactic = tactic;
        this.players = players;
        this.teamId = UUID.randomUUID().toString();
        this.points = 0;
        this.wins = 0;
        this.losses = 0;
        this.pointsFor = 0;
        this.pointsAgainst = 0;
    }

    @Override
    public String getTeamName() {
        return teamName;
    }


    @Override
    public List<IPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    @Override
    public Map<String, Integer> getTeamStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("wins", wins);
        stats.put("losses", losses);
        stats.put("points", points);
        stats.put("pointsFor", pointsFor);
        stats.put("pointsAgainst", pointsAgainst);
        stats.put("pointDifferential", pointsFor - pointsAgainst);
        return stats;
    }

    @Override
    public void setTactic(Tactic newTactic) {
        this.tactic = newTactic;
    }

    @Override
    public Tactic getTactic() {
        return tactic;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public void updateRecord(int pf, int pa, int pts) {
        this.pointsFor += pf;
        this.pointsAgainst += pa;
        this.points += pts;

        if (pts > 0) {
            this.wins++;
        } else {
            this.losses++;
        }
    }

    @Override
    public void addPlayer(IPlayer player) {
        players.add(player);
    }

    @Override
    public void removePlayer(IPlayer player) {
        players.remove(player);
    }
}