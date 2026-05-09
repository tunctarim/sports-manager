package com.f216.sportsmanager.sports;

import com.f216.sportsmanager.enums.*;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import com.f216.sportsmanager.models.BaseSport;
import java.util.List;

public class Basketball extends BaseSport {

    public enum BasketballPosition implements PlayerPosition {
        PG("PG"),
        SG("SG"),
        SF("SF"),
        PF("PF"),
        C("C");

        private final String code;

        BasketballPosition(String code) {
            this.code = code;
        }

        @Override
        public String getCode() {
            return code;
        }
    }

    public Basketball() {
        super("Basketball", 1, 0, 15, 4, 10, EndCondition.TIME_LIMIT, 1000, getRequiredPositionsStatic());
    }

    private static List<PlayerPosition> getRequiredPositionsStatic() {
        return List.of(
                BasketballPosition.PG,
                BasketballPosition.SG,
                BasketballPosition.SF,
                BasketballPosition.PF,
                BasketballPosition.C
        );
    }

    @Override
    public List<PlayerPosition> getRequiredPositions() {
        return getRequiredPositionsStatic();
    }

    @Override
    public List<String> getRequiredStats() {
        return List.of("Shooting", "Playmaking", "Rebounding", "Defense", "Athleticism", "IQ");
    }

    @Override
    public int getTotalMatchLength() {
        return getSegmentCount() * getSegmentLimit();
    }
}