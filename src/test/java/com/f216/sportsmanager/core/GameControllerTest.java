package com.f216.sportsmanager.core;

import static org.mockito.Mockito.*;

import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.models.League;
import com.f216.sportsmanager.ui.DatabaseFactory;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.ITeam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    private LeagueManager mockLeagueManager;
    private DatabaseFactory mockDatabaseFactory;
    private ISport mockSport;
    private League mockLeague;
    private GameController gameController;

    @BeforeEach
    public void setUp() {
        mockLeagueManager = mock(LeagueManager.class);
        mockDatabaseFactory = mock(DatabaseFactory.class);
        mockSport = mock(ISport.class);

        when(mockSport.getSportName()).thenReturn("Football");

        mockLeague = new League("Test League", mockSport);

        gameController = new GameController(mockLeagueManager, mockDatabaseFactory);
    }

    @Test
    public void testInitNewGameFlow() {
        when(mockDatabaseFactory.generateLeague(mockSport)).thenReturn(mockLeague);
        gameController.initNewGame(mockSport);

        verify(mockDatabaseFactory, times(1)).generateLeague(mockSport);
        verify(mockLeagueManager, times(1)).setLeagueData(mockLeague);
        verify(mockLeagueManager, times(1)).generateSchedule();

        assertEquals(mockSport, gameController.getCurrentSport());
    }

    @Test
    public void testAdvanceWeekTrigger() {
        gameController.advanceWeek();
        verify(mockLeagueManager, times(1)).playMatchDay();
    }

    @Test
    public void testChangeTeamTactic() {
        ITeam mockTeam = mock(ITeam.class);
        Tactic newTactic = Tactic.OFFENSIVE;

        when(mockLeagueManager.getUserTeam()).thenReturn(mockTeam);

        gameController.changeTeamTactic(newTactic);

        verify(mockTeam, times(1)).setTactic(newTactic);
    }

    @Test
    public void testGetDashboardData() {
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("week", 1);
        expectedData.put("teamName", "Test FC");

        when(mockLeagueManager.getDashboardData()).thenReturn(expectedData);

        Map<String, Object> actualData = gameController.getDashboardData();

        assertNotNull(actualData);
        assertEquals(expectedData, actualData);
        verify(mockLeagueManager, times(1)).getDashboardData();
    }
}