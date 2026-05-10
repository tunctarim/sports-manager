package com.f216.sportsmanager.sports;

import com.f216.sportsmanager.enums.*;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import com.f216.sportsmanager.models.BaseSport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Volleyball extends BaseSport {

    public enum VolleyballPosition implements PlayerPosition {
        SET("SET"), // Setter
        OH("OH"),   // Outside Hitter
        OPP("OPP"), // Opposite Hitter
        MB("MB"),   // Middle Blocker
        LIB("LIB"); // Libero

        private final String code;

        VolleyballPosition(String code) {
            this.code = code;
        }

        @Override
        public String getCode() {
            return code;
        }
    }

    public Volleyball() {
        // PointsPerWin: 3, PointsPerDraw: 0 (no draws in volleyball)
        // RosterSize: 14, Segments: 5 (Sets), SegmentLimit: 25 (Points per set)
        // Note: Make sure EndCondition.SCORE_LIMIT exists in your EndCondition enum!
        super("Volleyball", 3, 0, 14, 5, 25, EndCondition.SCORE_LIMIT, 1000, getRequiredPositionsStatic(), 5.0F);
    }

    private static List<PlayerPosition> getRequiredPositionsStatic() {
        List<PlayerPosition> positions = new ArrayList<>();
        positions.addAll(Collections.nCopies(2, VolleyballPosition.SET));
        positions.addAll(Collections.nCopies(4, VolleyballPosition.OH));
        positions.addAll(Collections.nCopies(2, VolleyballPosition.OPP));
        positions.addAll(Collections.nCopies(4, VolleyballPosition.MB));
        positions.addAll(Collections.nCopies(2, VolleyballPosition.LIB));
        return positions;
    }

    @Override
    public List<PlayerPosition> getRequiredPositions() {
        return getRequiredPositionsStatic();
    }

    @Override
    public List<String> getRequiredStats() {
        return List.of("Spiking", "Blocking", "Serving", "Setting", "Digging", "Receiving");
    }

    @Override
    public int getTotalMatchLength() {
        // Represents the theoretical maximum points in a sweep, or just serves as a limit metric
        return getSegmentCount() * getSegmentLimit();
    }
}