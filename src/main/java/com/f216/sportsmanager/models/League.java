package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a sports league that groups teams under a single sport type.
 *
 * <p>Design assumptions:
 * <ul>
 *   <li>A league name must be non-null and non-blank.</li>
 *   <li>The sport reference must be non-null; it defines the rules for this league.</li>
 *   <li>The same {@code ITeam} instance cannot be registered twice (identity equality).</li>
 *   <li>{@code getTeams()} returns an unmodifiable view so callers cannot mutate the
 *       internal roster directly.</li>
 *   <li>{@code removeTeam} silently ignores a team that is not currently enrolled.</li>
 * </ul>
 */
public class League {

    private final String leagueName;
    private final ISport sportType;
    private final List<ITeam> teams;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new League.
     *
     * @param leagueName a non-null, non-blank display name for this league
     * @param sportType  the sport that governs this league's rules; must not be null
     * @throws IllegalArgumentException if {@code leagueName} is null / blank
     * @throws IllegalArgumentException if {@code sportType} is null
     */
    public League(String leagueName, ISport sportType) {
        if (leagueName == null || leagueName.isBlank()) {
            throw new IllegalArgumentException("League name must not be null or blank.");
        }
        if (sportType == null) {
            throw new IllegalArgumentException("Sport type must not be null.");
        }
        this.leagueName = leagueName;
        this.sportType  = sportType;
        this.teams      = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Team management
    // -------------------------------------------------------------------------

    /**
     * Adds a team to this league.
     *
     * <p>A {@code null} argument or a team that is already enrolled are both silently ignored
     * so that callers do not need to guard against these conditions before calling.
     *
     * @param team the team to add; {@code null} is ignored
     */
    public void addTeam(ITeam team) {
        if (team == null) {
            return; // null team is a no-op
        }
        if (teams.contains(team)) {
            return; // duplicate team is a no-op
        }
        teams.add(team);
    }

    /**
     * Removes a team from this league.
     *
     * <p>If the specified team is not enrolled, this method does nothing.
     *
     * @param team the team to remove; {@code null} is ignored
     */
    public void removeTeam(ITeam team) {
        if (team == null) {
            return;
        }
        teams.remove(team);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable view of all teams currently enrolled in this league.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<ITeam> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    /**
     * Returns the number of teams currently enrolled in this league.
     *
     * @return team count ≥ 0
     */
    public int getTeamCount() {
        return teams.size();
    }

    /**
     * Returns the display name of this league.
     *
     * @return never {@code null} or blank
     */
    public String getLeagueName() {
        return leagueName;
    }

    /**
     * Returns the sport that governs this league's rules.
     *
     * @return never {@code null}
     */
    public ISport getSportType() {
        return sportType;
    }

    // -------------------------------------------------------------------------
    // Convenience queries
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the given team is currently enrolled in this league.
     *
     * @param team the team to check; {@code null} always returns {@code false}
     * @return {@code true} if enrolled, {@code false} otherwise
     */
    public boolean containsTeam(ITeam team) {
        if (team == null) return false;
        return teams.contains(team);
    }

    // -------------------------------------------------------------------------
    // Standings
    // -------------------------------------------------------------------------

    /**
     * Returns a sorted standings table for this league.
     *
     * <p>The list is ordered by league points (descending), then by goal/point
     * difference (descending), then by points-for (descending).
     * Each entry is a live-reading {@link StandingRecord} backed by the team itself.
     *
     * @return an unmodifiable, sorted list of standing records; never {@code null}
     */
    public List<StandingRecord> getStandings() {
        return teams.stream()
                .map(StandingRecord::new)
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    // -------------------------------------------------------------------------
    // Fixture generation
    // -------------------------------------------------------------------------

    /**
     * Generates a <b>single-leg</b> round-robin schedule for this league's current roster.
     * Each pair of teams plays exactly once.
     *
     * @return an unmodifiable list of {@link Fixture} objects; empty if fewer than 2 teams enrolled
     * @see FixtureGenerator#generate(List)
     */
    public List<Fixture> generateFixtures() {
        return FixtureGenerator.generate(teams);
    }

    /**
     * Generates a <b>double-leg</b> round-robin schedule for this league's current roster.
     * Each pair of teams plays twice — once at each home.
     *
     * @return an unmodifiable list of {@link Fixture} objects; empty if fewer than 2 teams enrolled
     * @see FixtureGenerator#generateDoubleLegged(List)
     */
    public List<Fixture> generateDoubleLegged() {
        return FixtureGenerator.generateDoubleLegged(teams);
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("League{name='%s', sport='%s', teams=%d}",
                leagueName, sportType.getSportName(), teams.size());
    }
}