package com.f216.sportsmanager.models;

import com.f216.sportsmanager.interfaces.ITeam;

import java.util.Objects;

public final class Fixture {

    private final ITeam home;
    private final ITeam away;

    public Fixture(ITeam home, ITeam away) {
        if (home == null) {
            throw new IllegalArgumentException("Home team must not be null.");
        }
        if (away == null) {
            throw new IllegalArgumentException("Away team must not be null.");
        }
        if (home == away) {
            throw new IllegalArgumentException("A team cannot be scheduled to play against itself.");
        }
        this.home = home;
        this.away = away;
    }

    public ITeam getHome() {
        return home;
    }

    public ITeam getAway() {
        return away;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Fixture other))
            return false;
        return home == other.home && away == other.away;
    }

    @Override
    public int hashCode() {
        return Objects.hash(System.identityHashCode(home), System.identityHashCode(away));
    }

    @Override
    public String toString() {
        return home + " vs " + away;
    }
}
