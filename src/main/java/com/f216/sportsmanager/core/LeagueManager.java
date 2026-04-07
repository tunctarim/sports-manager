package com.f216.sportsmanager.core;

import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.League;
import com.f216.sportsmanager.models.MatchResult;
import com.f216.sportsmanager.models.StandingRecord;

import java.util.*;

public class LeagueManager {

    private final MatchEngine matchEngine;

    private League league;
    private ITeam  userTeam;

    private final List<List<Fixture>> schedule;
    private final List<MatchResult>   playedResults;

    private int     currentWeek;
    private boolean seasonEnded;
    private ITeam   champion;

    public LeagueManager() {
        this(new MatchEngine());
    }

    /** Seed constructor kept for unit tests — MatchEngine is created internally. */
    public LeagueManager(long seed) {
        this(new MatchEngine());
    }

    public LeagueManager(MatchEngine matchEngine) {
        if (matchEngine == null)
            throw new IllegalArgumentException("MatchEngine must not be null.");
        this.matchEngine   = matchEngine;
        this.schedule      = new ArrayList<>();
        this.playedResults = new ArrayList<>();
        this.currentWeek   = 0;
        this.seasonEnded   = false;
    }
    public void setLeagueData(League league) {
        if (league == null)
            throw new IllegalArgumentException("League must not be null.");
        if (league.getTeamCount() < 2)
            throw new IllegalArgumentException(
                    "A league needs at least 2 teams to generate a schedule.");

        this.league      = league;
        this.currentWeek = 0;
        this.seasonEnded = false;
        this.champion    = null;
        this.schedule.clear();
        this.playedResults.clear();
    }
    public void generateSchedule() {
        requireLeague("generateSchedule");
        schedule.clear();
        schedule.addAll(buildRoundRobinRounds(new ArrayList<>(league.getTeams())));
    }
    public void playMatchDay() {
        requireLeague("playMatchDay");
        requireSchedule("playMatchDay");

        if (seasonEnded) return;

        if (currentWeek >= schedule.size()) {
            checkSeasonEnd();
            return;
        }

        for (Fixture fixture : schedule.get(currentWeek)) {
            matchEngine.simulateMatch(
                    fixture,
                    league.getSportType(), getCurrentWeek(),
                    false
            );

            MatchResult result = matchEngine.generateMatchReports();

            playedResults.add(result);
            updateStandings(result);
        }

        currentWeek++;
        checkSeasonEnd();
    }
    public void updateStandings(MatchResult result) {
        requireLeague("updateStandings");
        if (result == null)
            throw new IllegalArgumentException("MatchResult must not be null.");

        var   sport = league.getSportType();
        ITeam home  = result.getHomeTeam();
        ITeam away  = result.getAwayTeam();
        int   hs    = result.getHomeScore();
        int   as_   = result.getAwayScore();

        if (result.isHomeWin()) {
            home.updateRecord(hs, as_, sport.getPointsPerWin());
            away.updateRecord(as_, hs, 0);
        } else if (result.isAwayWin()) {
            home.updateRecord(hs, as_, 0);
            away.updateRecord(as_, hs, sport.getPointsPerWin());
        } else {
            int draw = sport.getPointsPerDraw();
            home.updateRecord(hs, as_, draw);
            away.updateRecord(as_, hs, draw);
        }
    }
    public List<StandingRecord> getStandingsTable() {
        requireLeague("getStandingsTable");
        return league.getStandings();
    }
    public boolean checkSeasonEnd() {
        if (seasonEnded) return true;
        if (schedule.isEmpty()) return false;

        if (currentWeek >= schedule.size()) {
            seasonEnded = true;
            List<StandingRecord> standings = getStandingsTable();
            if (!standings.isEmpty()) {
                champion = standings.get(0).getTeam();
            }
            return true;
        }
        return false;
    }

    public League            getLeague()        {
        return league; }
    public int               getCurrentWeek()   {
        return currentWeek; }
    public int               getTotalWeeks()    {
        return schedule.size(); }
    public boolean           isSeasonEnded()    {
        return seasonEnded; }
    public ITeam             getChampion()      {
        return champion; }
    public ITeam             getUserTeam()      {
        return userTeam; }
    public void              setUserTeam(ITeam t) {
        this.userTeam = t; }
    public List<MatchResult> getPlayedResults() { return Collections.unmodifiableList(playedResults); }

    public List<Fixture> getCurrentWeekFixtures() {
        if (schedule.isEmpty() || currentWeek >= schedule.size())
            return Collections.emptyList();
        return Collections.unmodifiableList(schedule.get(currentWeek));
    }

    public List<Fixture> getFixturesForWeek(int weekIndex) {
        if (weekIndex < 0 || weekIndex >= schedule.size())
            return Collections.emptyList();
        return Collections.unmodifiableList(schedule.get(weekIndex));
    }

    public List<List<Fixture>> getFullSchedule() {
        List<List<Fixture>> copy = new ArrayList<>();
        for (List<Fixture> week : schedule)
            copy.add(Collections.unmodifiableList(week));
        return Collections.unmodifiableList(copy);
    }

    public Map<String, Object> getDashboardData() {
        requireLeague("getDashboardData");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentWeek",   currentWeek);
        data.put("totalWeeks",    schedule.size());
        data.put("seasonEnded",   seasonEnded);
        data.put("standings",     league.getStandings());
        data.put("recentResults", getRecentResults(5));
        data.put("nextFixtures",  getCurrentWeekFixtures());
        if (seasonEnded && champion != null) {
            data.put("champion", champion);
        }
        return Collections.unmodifiableMap(data);
    }

    private List<List<Fixture>> buildRoundRobinRounds(List<ITeam> teams) {
        List<List<Fixture>> rounds = new ArrayList<>();
        int n = teams.size();
        if (n < 2) return rounds;

        if (n % 2 != 0) { teams.add(null); n++; }

        int numRounds = n - 1;
        int half      = n / 2;

        ITeam       fixed    = teams.get(0); // g burda
        List<ITeam> rotating = new ArrayList<>(teams.subList(1, n)); // f b t burda

        // g f b t a b

        for (int r = 0; r < numRounds; r++) {
            List<Fixture> round = new ArrayList<>();

            if (fixed != null && rotating.get(0) != null)
                round.add(new Fixture(fixed, rotating.get(0)));

            for (int i = 1; i < half; i++) {
                ITeam home = rotating.get(i); // b aldım.
                ITeam away = rotating.get(n - 1 - i); // t aldım.
                if (home != null && away!= null)
                    round.add(new Fixture(home, away));
            }

            if (!round.isEmpty()) rounds.add(round);
            rotating.add(0, rotating.remove(rotating.size() - 1));
        }
        return rounds;
    }

    private List<MatchResult> getRecentResults(int count) {
        int size  = playedResults.size();
        int start = Math.max(0, size - count);
        return Collections.unmodifiableList(playedResults.subList(start, size));
    }

    private void requireLeague(String method) {
        if (league == null)
            throw new IllegalStateException(method + "() called before setLeagueData().");
    }

    private void requireSchedule(String method) {
        if (schedule.isEmpty())
            throw new IllegalStateException(method + "() called before generateSchedule().");
    }
}