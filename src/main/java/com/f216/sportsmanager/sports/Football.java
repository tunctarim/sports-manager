package com.f216.sportsmanager.sports;

import com.f216.sportsmanager.enums.*;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import com.f216.sportsmanager.models.BaseSport;

import java.util.ArrayList;
import java.util.Collections;
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
        super("Football", 3, 1, 22, 11, 5, 2, 45, EndCondition.TIME_LIMIT, 1000, getRequiredPositionsStatic(), 1.0F);
    }

    private static List<PlayerPosition> getRequiredPositionsStatic() {
        List<PlayerPosition> positions = new ArrayList<>();
        positions.addAll(Collections.nCopies(2, FootballPosition.GK));
        positions.addAll(Collections.nCopies(8, FootballPosition.DEF));
        positions.addAll(Collections.nCopies(8, FootballPosition.MID));
        positions.addAll(Collections.nCopies(4, FootballPosition.FWD));
        return positions;
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
