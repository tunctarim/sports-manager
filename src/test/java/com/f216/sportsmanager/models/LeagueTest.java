package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.EndCondition;
import com.f216.sportsmanager.interfaces.ISport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class LeagueTest {

    ISport mockSport = new ISport() {
        public String getSportName() { return "Football"; }
        public int getSegmentCount() { return 2; }
        public int getPointsPerWin() { return 3; }
        public int getPointsPerDraw() { return 1;}
        public int getRosterSize() { return 11; }
        public int getSegmentLimit() { return 45; }
        public int getTickInterval() { return 1000; }
        public int getTotalMatchLength() { return 90; }
        public List<String> getRequiredStats() { return List.of(); }
        public EndCondition getEndCondition() { return EndCondition.TIME_LIMIT; }
    };

    @Test
    void testLeagueName() {
        League league = new League("Premier League", mockSport);
        assertEquals("Premier League", league.getLeagueName());
    }

    @Test
    void testSportType() {
        League league = new League("Premier League", mockSport);
        assertEquals(mockSport, league.getSportType());
    }

    @Test
    void testTeamsInitiallyEmpty() {
        League league = new League("Premier League", mockSport);
        assertTrue(league.getTeams().isEmpty());
    }
}
