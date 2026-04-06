package com.f216.sportsmanager.sports;

import com.f216.sportsmanager.enums.*;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import com.f216.sportsmanager.models.BaseSport;
import java.util.List;

public class Football extends BaseSport {

    public Football(String name, int ppW, int ppD, int roster, int segments, int limit, EndCondition condition, int tickInterval, List<PlayerPosition> requiredPositions) {
        super(name, ppW, ppD, roster, segments, limit, condition, tickInterval, requiredPositions);
    }

    public enum FootballPosition implements PlayerPosition {
        GK("GK"),
        DEF("DEF"),
        MID("MID"),
        FWD("FWD");

        private final String code;

        FootballPosition(String code) {
            this.code = code;
        }

        @Override public String getCode() { return code; }
    }

    public List<PlayerPosition> getRequiredPositions() {
        return List.of(FootballPosition.GK, FootballPosition.DEF, FootballPosition.MID, FootballPosition.FWD);
    }

    @Override
    public List<String> getRequiredStats() {
        return List.of("Pace", "Shooting", "Passing", "Dribbling", "Defending", "Physical");
    }

    @Override
    public int getTotalMatchLength() {
        return getSegmentCount() * getSegmentLimit();
    }
}