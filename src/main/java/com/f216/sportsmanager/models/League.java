package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class League implements Serializable {

    private final String leagueName;
    private final ISport sportType;
    private final List<ITeam> teams;



    public League(String leagueName, ISport sportType) {
        if (leagueName == null || leagueName.isBlank()) {
            throw new IllegalArgumentException("League name must not be null or blank.");
        }
        if (sportType == null) {
            throw new IllegalArgumentException("Sport type must not be null.");
        }
        this.leagueName = leagueName;
        this.sportType = sportType;
        this.teams = new ArrayList<>();
    }
    public void addTeam(ITeam team) {
        if (team == null) {
            return; // null team is a no-op
        }
        if (teams.contains(team)) {
            return; // duplicate team is a no-op
        }
        teams.add(team);
    }
    public void removeTeam(ITeam team) {
        if (team == null) {
            return;
        }
        teams.remove(team);
    }

        public List<ITeam> getTeams() {
        return Collections.unmodifiableList(teams);
    }


    public int getTeamCount() {
        return teams.size();
    }


    public String getLeagueName() {
        return leagueName;
    }


    public ISport getSportType() {
        return sportType;
    }


    public boolean containsTeam(ITeam team) {
        if (team == null)
            return false;
        return teams.contains(team);
    }

    public List<StandingRecord> getStandings() {
        return teams.stream()
                .map(StandingRecord::new)
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Fixture> generateFixtures() {
        return FixtureGenerator.generate(teams);
    }


    public List<Fixture> generateDoubleLegged() {
        return FixtureGenerator.generateDoubleLegged(teams);
    }


    @Override
    public String toString() {
        return String.format("League{name='%s', sport='%s', teams=%d}",
                leagueName, sportType.getSportName(), teams.size());
    }
}