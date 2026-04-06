package com.f216.sportsmanager.interfaces;
import com.f216.sportsmanager.enums.Tactic;

import java.util.List;
import java.util.Map;

public interface ITeam {

    List<IPlayer> getPlayers();
    Map<String, Integer> getTeamStats();

    void setTactic(Tactic newTactic);
    Tactic getTactic();

    int getPoints();
    void updateRecord(int pf, int pa, int pts);

    void addPlayer(IPlayer player);
    void removePlayer(IPlayer player);

}
