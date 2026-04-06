package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ITeam;
import javafx.css.Match;

import java.util.List;

public class DashboardData {
    private String leagueName;
    private int currentWeek;
    private List<ITeam> standings;
    private List<Match> weeklySchedule;
    private ITeam userTeam;

    public DashboardData(String leagueName, int currentWeek, List<ITeam> standings, List<Match> weeklySchedule, ITeam userTeam) {
        this.leagueName = leagueName;
        this.currentWeek = currentWeek;
        this.standings = standings;
        this.weeklySchedule = weeklySchedule;
        this.userTeam = userTeam;
    }

    public String getLeagueName() {
        return leagueName;
    }
    public int getCurrentWeek() {
        return currentWeek;
    }
    public List<ITeam> getStandings() {
        return standings;
    }
    public List<Match> getWeeklySchedule() {
        return weeklySchedule;
    }
    public ITeam getUserTeam() {
        return userTeam;
    }
}