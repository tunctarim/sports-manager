package com.f216.sportsmanager.core;

import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.models.League;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.DashboardData;
import java.util.List;

public class GameControllerTest {

    private LeagueManager mockLeagueManager;
    private DatabaseFactory mockDatabaseFactory;
    private ISport mockSport;
    private League mockLeague;
    private GameController gameController;

    @BeforeEach
    public void setUp() {
        mockLeagueManager = Mockito.mock(LeagueManager.class);
        mockDatabaseFactory = Mockito.mock(DatabaseFactory.class);
        mockSport = Mockito.mock(ISport.class);
        mockLeague = Mockito.mock(League.class);
        Mockito.when(mockSport.getSportName()).thenReturn("Football");

        gameController = new GameController(mockLeagueManager);
    }

    @Test
    public void testAdvanceWeekTrigger() {
        gameController.advanceWeek();
        Mockito.verify(mockLeagueManager, Mockito.times(1)).playMatchDay();
    }

    @Test
    public void testChangeTeamTactic() {
        ITeam mockTeam = Mockito.mock(ITeam.class);
        Tactic newTactic = Tactic.ATTACK;

        Mockito.when(mockLeagueManager.getUserTeam()).thenReturn(mockTeam);

        gameController.changeTeamTactic(newTactic);

        Mockito.verify(mockTeam).setTactic(newTactic);
    }

    @Test
    public void testGetDashboardData() {
        DashboardData expectedData = new DashboardData(
            "Test League", 1, 10, false, List.of(), List.of(), null, List.of(), null
        );

        Mockito.when(mockLeagueManager.getDashboardData()).thenReturn(expectedData);

        DashboardData actualData = gameController.getDashboardData();

        assertNotNull(actualData);
        assertEquals(expectedData, actualData);
    }
}