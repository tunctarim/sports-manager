package com.f216.sportsmanager.core;

import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.League;
import com.f216.sportsmanager.models.MatchResult;
import com.f216.sportsmanager.models.StandingRecord;

import java.util.*;


public class LeagueManager {


    private League league;


    private final List<List<Fixture>> schedule;

    private final List<MatchResult> playedResults;


    private int currentWeek;

    private boolean seasonEnded;

    private ITeam champion;

    private final Random random;

    public LeagueManager() {
        this(new Random());
    }

    public LeagueManager(long seed) {
        this(new Random(seed));
    }

    private LeagueManager(Random random) {
        this.random        = random;
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
        List<ITeam> teams = new ArrayList<>(league.getTeams());
        schedule.addAll(buildRoundRobinRounds(teams));
    }
    private List<List<Fixture>> buildRoundRobinRounds(List<ITeam> teams) {
        List<List<Fixture>> rounds = new ArrayList<>();
        int n = teams.size();
        if (n < 2) return rounds;

        List<ITeam> list = new ArrayList<>(teams);

        // Pad to even number with a null "bye" team
        boolean padded = n % 2 != 0;
        if (padded) {
            list.add(null);
            n++;
        }

        int numRounds = n - 1;
        int half      = n / 2;

        // The first team is fixed; the rest rotate one step each round
        ITeam        fixed    = list.get(0); // g burda.
        List<ITeam>  rotating = new ArrayList<>(list.subList(1, n)); // f,b,t burda tutuluyor

        //g f b t a b

        for (int r = 0; r < numRounds; r++) {
            List<Fixture> round = new ArrayList<>();

            // Pair: fixed vs rotating[0]
            if (fixed != null && rotating.get(0) != null) {
                round.add(new Fixture(fixed, rotating.get(0)));
            }

            // Pairs: rotating[i] vs rotating[n-2-i]
            // rotating has (n-1) elements, indices 0 … n-2
            for (int i = 1; i < half; i++) {
                ITeam home = rotating.get(i); //b aldım
                ITeam away = rotating.get(n - 1 - i); //  t aldı.
                if (home != null && away != null) {
                    round.add(new Fixture(home, away));
                }
            }

            if (!round.isEmpty()) rounds.add(round);

            // Rotation: last element moves to the front
            rotating.add(0, rotating.remove(rotating.size() - 1));
        }

        return rounds;
    }
    public void playMatchDay() {
        requireLeague("playMatchDay");
        requireSchedule("playMatchDay");

        if (seasonEnded) return;

        if (currentWeek >= schedule.size()) {
            checkSeasonEnd();
            return;
        }

        List<Fixture> weekFixtures = schedule.get(currentWeek);
        for (Fixture fixture : weekFixtures) {
            MatchResult result = simulateMatch(fixture);
            playedResults.add(result);
            updateStandings(result);
        }

        currentWeek++;
        checkSeasonEnd();
    }
    private MatchResult simulateMatch(Fixture fixture) {
        ITeam  home  = fixture.getHome();
        ITeam  away  = fixture.getAway();
        ISport sport = league.getSportType();

        // Base ratings from average healthy-player overall
        double homeStrength = computeStrength(home);
        double awayStrength = computeStrength(away);

        // Small home-field advantage
        homeStrength *= 1.05;

        // Probability that a scoring opportunity goes to the home side
        double total           = homeStrength + awayStrength;
        double homeScoringProb = total > 0 ? homeStrength / total : 0.5;

        // Number of scoring chances = segments × random sample per segment
        int homeScore = 0;
        int awayScore = 0;
        int segmentCount = sport.getSegmentCount();

        for (int seg = 0; seg < segmentCount; seg++) {
            // 2–8 scoring chances per segment (sport-neutral abstraction)
            int chances = 2 + random.nextInt(7);
            for (int c = 0; c < chances; c++) {
                if (random.nextDouble() < homeScoringProb) {
                    homeScore++;
                } else {
                    awayScore++;
                }
            }
        }

        return new MatchResult(home, away, homeScore, awayScore, currentWeek + 1);
    }


    private double computeStrength(ITeam team) {
        OptionalDouble avg = team.getPlayers().stream()
                .filter(p -> !p.isInjured())
                .mapToInt(IPlayer::getOverallRating)
                .average();

        double base = avg.isPresent() ? avg.getAsDouble() : 50.0;
        return applyTacticModifier(base, team.getTactic());
    }


    private double applyTacticModifier(double strength, Tactic tactic) {
        return switch (tactic) {
            case ATTACK   -> strength * 1.10;
            case DEFEND   -> strength * 0.90;
            case BALANCED -> strength;
        };
    }

    // -------------------------------------------------------------------------
    // Standings update (SDD §3.3.3 updateStandings)
    // -------------------------------------------------------------------------


    public void updateStandings(MatchResult result) {
        requireLeague("updateStandings");
        if (result == null)
            throw new IllegalArgumentException("MatchResult must not be null.");

        ISport sport = league.getSportType();
        ITeam  home  = result.getHomeTeam();
        ITeam  away  = result.getAwayTeam();
        int    hs    = result.getHomeScore();
        int    as_   = result.getAwayScore();

        if (result.isHomeWin()) {
            home.updateRecord(hs, as_, sport.getPointsPerWin());
            away.updateRecord(as_, hs, 0);
        } else if (result.isAwayWin()) {
            home.updateRecord(hs, as_, 0);
            away.updateRecord(as_, hs, sport.getPointsPerWin());
        } else {
            // Draw
            int drawPts = sport.getPointsPerDraw();
            home.updateRecord(hs, as_, drawPts);
            away.updateRecord(as_, hs, drawPts);
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


    public Map<String, Object> getDashboardData() {
        requireLeague("getDashboardData");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentWeek",   currentWeek);          // 0-indexed; UI adds 1 for display
        data.put("totalWeeks",    schedule.size());
        data.put("seasonEnded",   seasonEnded);
        data.put("standings",     league.getStandings()); // live, sorted
        data.put("recentResults", getRecentResults(5));
        data.put("nextFixtures",  getCurrentWeekFixtures());
        if (seasonEnded && champion != null) {
            data.put("champion", champion);
        }
        return Collections.unmodifiableMap(data);
    }

    // -------------------------------------------------------------------------
    // Read-only accessors
    // -------------------------------------------------------------------------

    public League getLeague() { return league; }


    public int getCurrentWeek() { return currentWeek; }

    public int getTotalWeeks()  { return schedule.size(); }

    public boolean isSeasonEnded() { return seasonEnded; }


    public ITeam getChampion() { return champion; }


    public List<MatchResult> getPlayedResults() {
        return Collections.unmodifiableList(playedResults);
    }


    public List<Fixture> getCurrentWeekFixtures() {
        if (schedule.isEmpty() || currentWeek >= schedule.size()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(schedule.get(currentWeek));
    }


    public List<Fixture> getFixturesForWeek(int weekIndex) {
        if (weekIndex < 0 || weekIndex >= schedule.size()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(schedule.get(weekIndex));
    }


    public List<List<Fixture>> getFullSchedule() {
        List<List<Fixture>> copy = new ArrayList<>();
        for (List<Fixture> week : schedule) {
            copy.add(Collections.unmodifiableList(week));
        }
        return Collections.unmodifiableList(copy);
    }

    private List<MatchResult> getRecentResults(int count) {
        int size  = playedResults.size();
        int start = Math.max(0, size - count);
        return Collections.unmodifiableList(playedResults.subList(start, size));
    }

    private void requireLeague(String method) {
        if (league == null)
            throw new IllegalStateException(
                    method + "() called before setLeagueData().");
    }

    private void requireSchedule(String method) {
        if (schedule.isEmpty())
            throw new IllegalStateException(
                    method + "() called before generateSchedule().");
    }
}