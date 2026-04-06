package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ITeam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class FixtureGenerator {

    /** Utility class — private constructor prevents instantiation. */
    private FixtureGenerator() {
        throw new AssertionError("FixtureGenerator is a utility class and must not be instantiated.");
    }

    public static List<Fixture> generate(List<ITeam> teams) {
        validateTeamList(teams);

        List<Fixture> fixtures = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                fixtures.add(new Fixture(teams.get(i), teams.get(j)));
            }
        }
        return Collections.unmodifiableList(fixtures);
    }

    public static List<Fixture> generateDoubleLegged(List<ITeam> teams) {
        validateTeamList(teams);

        List<Fixture> fixtures = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = 0; j < teams.size(); j++) {
                if (i != j) {
                    fixtures.add(new Fixture(teams.get(i), teams.get(j)));
                }
            }
        }
        return Collections.unmodifiableList(fixtures);
    }

    public static int expectedFixtureCount(int teamCount) {
        if (teamCount < 0)
            throw new IllegalArgumentException("Team count must not be negative.");
        return teamCount * (teamCount - 1) / 2;
    }

    public static int expectedDoubleFixtureCount(int teamCount) {
        if (teamCount < 0)
            throw new IllegalArgumentException("Team count must not be negative.");
        return teamCount * (teamCount - 1);
    }

    private static void validateTeamList(List<ITeam> teams) {
        if (teams == null) {
            throw new IllegalArgumentException("Team list must not be null.");
        }
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i) == null) {
                throw new IllegalArgumentException(
                        "Team list must not contain null elements (index " + i + ").");
            }
        }
    }
}
