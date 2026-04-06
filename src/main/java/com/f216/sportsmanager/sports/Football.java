package com.f216.sportsmanager.sports;

import com.f216.sportsmanager.enums.*;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import com.f216.sportsmanager.models.BaseSport;
import java.util.List;

public class Football extends BaseSport {

    public enum FootballPosition implements PlayerPosition {
        GK("GK"),
        DEF("DEF"),
        MID("MID"),
        FWD("FWD");

        private final String code;

        FootballPosition(String code) {
            this.code = code;
        }

        @Override
        public String getCode() {
            return code;
        }
    }

    public Football() {
        super("Football", 3, 1, 22, 2, 45, EndCondition.TIME_LIMIT, 1000, getRequiredPositionsStatic());
    }

    private static List<PlayerPosition> getRequiredPositionsStatic() {
        return List.of(FootballPosition.GK, FootballPosition.DEF, FootballPosition.MID, FootballPosition.FWD);
    }

    @Override
    public List<PlayerPosition> getRequiredPositions() {
        return getRequiredPositionsStatic();
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