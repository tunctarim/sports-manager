package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ITeam;

import java.util.List;

public class DashboardData {
    private String leagueName;
    private int currentWeek;
    private int totalWeeks;
    private boolean seasonEnded;
    private List<StandingRecord> standings;
    private List<Fixture> weeklySchedule;
    private ITeam userTeam;
    private List<MatchResult> recentResults;
    private ITeam champion;

    public DashboardData(String leagueName, int currentWeek, int totalWeeks, boolean seasonEnded,
                         List<StandingRecord> standings, List<Fixture> weeklySchedule, ITeam userTeam,
                         List<MatchResult> recentResults, ITeam champion) {
        this.leagueName = leagueName;
        this.currentWeek = currentWeek;
        this.totalWeeks = totalWeeks;
        this.seasonEnded = seasonEnded;
        this.standings = standings;
        this.weeklySchedule = weeklySchedule;
        this.userTeam = userTeam;
        this.recentResults = recentResults;
        this.champion = champion;
    }

    public String getLeagueName() {
        return leagueName;
    }
    public int getCurrentWeek() {
        return currentWeek;
    }
    public int getTotalWeeks() {
        return totalWeeks;
    }
    public boolean isSeasonEnded() {
        return seasonEnded;
    }
    public List<StandingRecord> getStandings() {
        return standings;
    }
    public List<Fixture> getWeeklySchedule() {
        return weeklySchedule;
    }
    public ITeam getUserTeam() {
        return userTeam;
    }
    public List<MatchResult> getRecentResults() {
        return recentResults;
    }
    public ITeam getChampion() {
        return champion;
    }
}