package com.f216.sportsmanager.ui;

import com.f216.sportsmanager.core.GameController;
import com.f216.sportsmanager.core.LeagueManager;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.IMatchObserver;
import com.f216.sportsmanager.interfaces.IPlayer;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.interfaces.ITeam;
import com.f216.sportsmanager.models.Fixture;
import com.f216.sportsmanager.models.MatchEvent;
import com.f216.sportsmanager.models.MatchResult;
import com.f216.sportsmanager.models.StandingRecord;
import com.f216.sportsmanager.models.DashboardData;
import com.f216.sportsmanager.sports.Basketball;
import com.f216.sportsmanager.sports.Football;
import com.f216.sportsmanager.sports.Volleyball;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.*;

public class SportsManagerApp extends Application {

    private Stage primaryStage;

    private final LeagueManager  leagueManager = new LeagueManager();
    private final GameController gc = new GameController(leagueManager);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ISport selectedSport = null;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Sports Manager");
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setOnCloseRequest(e -> executor.shutdownNow());
        showMainMenu();
        stage.show();
    }

    private void showMainMenu() {
        VBox root = gradientVBox(20, "#0f0c29", "#302b63", "#24243e");
        root.setPadding(new Insets(30));

        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(
                TeamLogoGenerator.generate("SM", 60),
                boldLabel("Sports Manager", "#e2e8f0", 36, "-fx-effect: dropshadow(gaussian,#00d4ff,10,0.7,0,0);")
        );

        Label sub = label("Feel free to enjoy!", "#94a3b8", 20);
        sub.setStyle(sub.getStyle() + "-fx-font-style:italic;");

        Button btnNew  = menuBtn("🆕   New Game", "#0ea5e9", "#0284c7");
        Button btnLoad = menuBtn("📂   Load Game", "#8b5cf6", "#7c3aed");
        Button btnExit = menuBtn("🚪   Exit", "#ef4444", "#dc2626");

        HBox bottom = hbox(100, ghostBtn("⚙  Settings"), ghostBtn("ℹ  About Us"));

        btnNew.setOnAction(e  -> showSportSelection());
        btnLoad.setOnAction(e -> {
            gc.loadGame();
            showLeagueStandings();
        });
        btnExit.setOnAction(e -> { executor.shutdownNow(); primaryStage.close(); });

        ((Button) bottom.getChildren().get(1)).setOnAction(e -> showAboutScreen());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        root.getChildren().addAll(header, sub, separator(), btnNew, btnLoad, btnExit, spacer, bottom);
        setScene(root);
    }

    private void showAboutScreen() {
        VBox root = gradientVBox(15, "#1e1b4b", "#312e81", "#1e1b4b");
        root.setPadding(new Insets(30));

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        Label logoCircle = new Label("SM");
        logoCircle.setStyle(
                "-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#c4b5fd;" + "-fx-background-color:rgba(139,92,246,0.3);" +
                        "-fx-background-radius:28;-fx-min-width:56;-fx-min-height:56;" + "-fx-alignment:center;-fx-border-color:#8b5cf6;-fx-border-radius:28;-fx-border-width:2;"
        );
        VBox titleBox = new VBox(4,
                boldLabel("Sports Manager", "#e2e8f0", 26, ""),
                label("v1.0  •  Team F216", "#c4b5fd", 11)
        );
        header.getChildren().addAll(logoCircle, titleBox);

        Label tagline = label("Feel free to enjoy!", "#94a3b8", 13);
        tagline.setStyle(tagline.getStyle() + "-fx-font-style:italic;");

        Separator sep = separator();

        Label teamLabel = label("DEVELOPMENT TEAM", "#7c3aed", 11);

        String[][] members = {
                {"NÖ",  "Nehir Özsarı"},
                {"YÖ",  "Yiğit Öztürk"},
                {"ATT", "Ahmet Tunç Tarım"}
        };

        GridPane teamGrid = new GridPane();
        teamGrid.setHgap(10); teamGrid.setVgap(10);
        teamGrid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < members.length; i++) {
            String initials = members[i][0];
            String name     = members[i][1];

            Label avatar = new Label(initials);
            avatar.setStyle(
                    "-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#c4b5fd;" +
                            "-fx-background-color:rgba(139,92,246,0.25);" +
                            "-fx-background-radius:18;-fx-min-width:36;-fx-min-height:36;" +
                            "-fx-alignment:center;-fx-border-color:rgba(139,92,246,0.5);" +
                            "-fx-border-radius:18;-fx-border-width:1;"
            );

            VBox nameBox = new VBox(2,
                    label(name, "#e2e8f0", 13),
                    label("Team F216", "#7c3aed", 11)
            );

            HBox card = new HBox(10, avatar, nameBox);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(8, 12, 8, 12));
            card.setStyle(
                    "-fx-background-color:rgba(255,255,255,0.06);" +
                            "-fx-background-radius:8;" +
                            "-fx-border-color:rgba(139,92,246,0.3);" +
                            "-fx-border-radius:8;-fx-border-width:0.5;"
            );

            teamGrid.add(card, i % 2, i / 2);
            GridPane.setHgrow(card, Priority.ALWAYS);
        }

        HBox infoRow = new HBox(10);
        infoRow.setAlignment(Pos.CENTER);
        infoRow.getChildren().addAll(
                infoCard("Java",   "Language"),
                infoCard("JavaFX", "UI Framework"),
                infoCard("2026",   "Year")
        );

        Button btnBack = menuBtn("← Back to Menu", "#6d28d9", "#5b21b6");
        btnBack.setOnAction(e -> showMainMenu());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        root.getChildren().addAll(header, tagline, sep, teamLabel, teamGrid, infoRow, spacer, btnBack);
        setScene(root);
    }

    private VBox infoCard(String value, String label) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setPrefWidth(140);
        card.setStyle(
                "-fx-background-color:rgba(255,255,255,0.05);" + "-fx-background-radius:8;" + "-fx-border-color:rgba(255,255,255,0.1);" + "-fx-border-radius:8;-fx-border-width:0.5;"
        );
        card.getChildren().addAll(
                boldLabel(value, "#a78bfa", 16, ""), this.label(label, "#64748b", 11)
        );
        return card;
    }

    private void showSportSelection() {
        VBox root = gradientVBox(10, "#1e1b4b", "#312e81", "#1e1b4b");
        root.setPadding(new Insets(20));
        root.getChildren().add(screenTitle("🏆  Select Your Sport"));

        Object[][] sports = {
                {"⚽", "Football",   "11v11  •  Time limit  •  2 halves", "#16a34a", new Football()},
                {"🏐", "Volleyball", "Score limit  •  Sets  •  6 players", "#0ea5e9", new Volleyball()},
                {"🏀", "Basketball", "4 quarters  •  Time limit  •  5v5",  "#ea580c", new Basketball()}
        };

        VBox cards = new VBox(8);
        cards.setAlignment(Pos.CENTER);

        for (Object[] s : sports) {
            String emoji = (String) s[0];
            String name  = (String) s[1];
            String desc  = (String) s[2];
            String color = (String) s[3];
            ISport sport = (ISport) s[4];

            HBox card = sportCard(emoji, name, desc, color);
            card.setOnMouseClicked(e -> {
                if (sport == null) {
                    alert("Coming Soon", name + " is not yet implemented.\n" + "Add the ISport implementation and update showSportSelection().");
                    return;
                }
                selectedSport = sport;
                gc.initNewGame(sport);
                showTeamManagement();
            });
            cards.getChildren().add(card);
        }
        VBox.setVgrow(cards, Priority.ALWAYS);

        Button back = ghostBtn("← Back");
        back.setOnAction(e -> showMainMenu());
        root.getChildren().addAll(cards, back);
        setScene(root);
    }

    private void showTeamManagement() {
        DashboardData data = gc.getDashboardData();
        ITeam myTeam = leagueManager.getUserTeam();

        VBox root = gradientVBox(15, "#052e16", "#14532d", "#052e16");
        root.setPadding(new Insets(20));

        String sportName = selectedSport != null ? selectedSport.getSportName() : "Sport Manager";
        root.getChildren().add(screenTitle("🛠  Team Management  –  " + sportName));

        if (myTeam == null) {
            List<StandingRecord> standings = data.getStandings();
            if (standings != null && !standings.isEmpty()) {
                myTeam = standings.get(0).getTeam();
                leagueManager.setUserTeam(myTeam);
            }
        }

        if (myTeam == null) {
            root.getChildren().add(label("No league loaded. Start a new game.", "#fca5a5", 14));
            Button back = ghostBtn("← Back");
            back.setOnAction(e -> showMainMenu());
            root.getChildren().add(back);
            setScene(root);
            return;
        }

        final ITeam team = myTeam;

        HBox teamHeader = new HBox(20);
        teamHeader.setAlignment(Pos.CENTER_LEFT);
        teamHeader.setPadding(new Insets(15, 20, 15, 20));
        teamHeader.setStyle("-fx-background-color:rgba(0,0,0,0.35);-fx-background-radius:14;");

        Canvas logo = TeamLogoGenerator.generate(team.getTeamName(), 56);
        Label  nameL = boldLabel(team.getTeamName(), "#86efac", 18, "");
        Label  tacL = label("Tactic: " + team.getTactic().name(), "#34d399", 13);
        Label  ptsL = label("Points: " + team.getPoints(), "#6ee7b7", 13);
        teamHeader.getChildren().addAll(logo, new VBox(4, nameL, tacL, ptsL));

        HBox cols = new HBox(15);
        cols.setAlignment(Pos.TOP_CENTER);
        VBox col1 = mgmtCol("📋 Squad Roster", buildRosterItems(team));
        VBox col2 = mgmtCol("📊 Player Stats", buildPlayerStats(team));
        VBox col3 = mgmtCol("🎯 Tactics", buildTacticsInfo(team));
        HBox.setHgrow(col1, Priority.ALWAYS);
        HBox.setHgrow(col2, Priority.ALWAYS);
        HBox.setHgrow(col3, Priority.ALWAYS);
        cols.getChildren().addAll(col1, col2, col3);

        Button btnTactic = actionBtn("🔄 Change Tactic", "#0e7490");
        Button btnMatch = actionBtn("▶  Play Match", "#b45309");
        Button btnSave = actionBtn("💾 Save Game", "#0369a1");
        Button btnStandings = actionBtn("📊 Standings", "#7f1d1d");
        Button btnBack = ghostBtn("← Back");

        HBox row1 = hbox(15, btnTactic, btnMatch);
        HBox row2 = hbox(15, btnSave, btnStandings, btnBack);

        btnTactic.setOnAction(e -> {
            ChoiceDialog<Tactic> dlg = new ChoiceDialog<>(team.getTactic(), Tactic.values());
            dlg.setTitle("Change Tactic");
            dlg.setHeaderText("Select a tactic for " + team.getTeamName());
            dlg.setContentText("Tactic:");
            dlg.showAndWait().ifPresent(t -> {
                gc.changeTeamTactic(t);
                showTeamManagement();
            });
        });

        btnMatch.setOnAction(e -> showMatchScreen());
        btnSave.setOnAction(e -> { gc.saveGame(); alert("Saved", "Game saved successfully."); });
        btnStandings.setOnAction(e -> showLeagueStandings());
        btnBack.setOnAction(e -> showSportSelection());

        root.getChildren().addAll(teamHeader, cols, row1, row2);
        setScene(scrollPane(root, "#052e16"));
    }

    private void showLeagueStandings() {
        DashboardData data = gc.getDashboardData();

        int currentWeek = data.getCurrentWeek();
        int totalWeeks  = data.getTotalWeeks();
        boolean seasonEnded = data.isSeasonEnded();
        List<StandingRecord> standings = data.getStandings();
        List<MatchResult> recentResults = data.getRecentResults();
        List<Fixture> nextFixtures = data.getWeeklySchedule();

        String championName = "";
        if (seasonEnded && data.getChampion() != null) {
            championName = data.getChampion().getTeamName();
        }

        VBox root = gradientVBox(10, "#450a0a", "#7f1d1d", "#450a0a");
        root.setPadding(new Insets(15));

        String sportName = selectedSport != null ? selectedSport.getSportName() : "";
        root.getChildren().addAll(
                screenTitle("📊  League Standings" + (sportName.isEmpty() ? "" : "  –  " + sportName)),
                label("Week " + currentWeek + " / " + totalWeeks + (seasonEnded ? "  🏆 CHAMPION: " + championName : ""), "#fca5a5", 13)
        );

        TableView<StandingsRow> table = new TableView<>();
        table.setStyle("-fx-background-color:rgba(0,0,0,0.4);" + "-fx-control-inner-background:transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<StandingsRow, String> colLogo = new TableColumn<>("");
        colLogo.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        colLogo.setCellFactory(tc -> new TableCell<>() {

            @Override
            protected void updateItem(String n, boolean empty) {
                super.updateItem(n, empty);
                setGraphic(empty || n == null ? null : TeamLogoGenerator.generate(n, 32));
            }
        });
        colLogo.setPrefWidth(40); colLogo.setResizable(false);

        table.getColumns().addAll(colLogo,
                strCol("#", "pos",40),
                strCol("Team","teamName",250),
                strCol("MP","mp",40),
                strCol("W", "w",40),
                strCol("L", "l", 40),
                strCol("GD", "gd", 50),
                strCol("Pts","pts", 50)
        );

        ObservableList<StandingsRow> rows = FXCollections.observableArrayList();
        if (standings != null) {
            for (int i = 0; i < standings.size(); i++) {
                StandingRecord sr = standings.get(i);
                rows.add(new StandingsRow(
                        String.valueOf(i + 1),
                        sr.getTeam().getTeamName(),
                        String.valueOf(sr.getMatchesPlayed()),
                        String.valueOf(sr.getWins()),
                        String.valueOf(sr.getLosses()),
                        (sr.getGoalDifference() >= 0 ? "+" : "") + sr.getGoalDifference(),
                        String.valueOf(sr.getPoints())
                ));
            }
        }
        table.setItems(rows);

        VBox resultsBox = new VBox(6);
        resultsBox.setStyle("-fx-background-color:rgba(0,0,0,0.25);-fx-background-radius:10;-fx-padding:10;");
        resultsBox.getChildren().add(boldLabel("📋 Recent Results", "#fca5a5", 14, ""));
        if (recentResults == null || recentResults.isEmpty()) {
            resultsBox.getChildren().add(label("No matches played yet.", "#94a3b8", 12));
        } else {
            for (MatchResult r : recentResults) {
                String txt = r.getHomeTeam().getTeamName() + "  " + r.getHomeScore() + " – " + r.getAwayScore() + "  " + r.getAwayTeam().getTeamName() + "   (Week " + r.getWeek() + ")";
                resultsBox.getChildren().add(label(txt, "#fecdd3", 12));
            }
        }

        VBox fixturesBox = new VBox(6);
        fixturesBox.setStyle("-fx-background-color:rgba(0,0,0,0.25);-fx-background-radius:10;-fx-padding:10;");
        fixturesBox.getChildren().add(boldLabel("📅 Next Fixtures", "#fca5a5", 14, ""));
        if (nextFixtures == null || nextFixtures.isEmpty()) {
            fixturesBox.getChildren().add(label("No upcoming fixtures.", "#94a3b8", 12));
        } else {
            for (Fixture f : nextFixtures) {
                String txt = f.getHome().getTeamName() + "  vs  " + f.getAway().getTeamName();
                fixturesBox.getChildren().add(label(txt, "#fecdd3", 12));
            }
        }

        HBox extraInfo = new HBox(15, resultsBox, fixturesBox);
        extraInfo.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(resultsBox,  Priority.ALWAYS);
        HBox.setHgrow(fixturesBox, Priority.ALWAYS);

        Button btnNext = actionBtn("Next Week ▶", "#991b1b");
        Button btnTeam = actionBtn("🛠 My Team",   "#166534");
        Button btnMenu = ghostBtn("← Main Menu");

        btnNext.setDisable(seasonEnded);
        btnNext.setOnAction(e -> {
            gc.advanceWeek();
            showLeagueStandings();
        });
        btnTeam.setOnAction(e -> showTeamManagement());
        btnMenu.setOnAction(e -> showMainMenu());

        root.getChildren().addAll(table, extraInfo, hbox(15, btnNext, btnTeam, btnMenu));
        setScene(scrollPane(root, "#450a0a"));
    }

    private void showMatchScreen() {
        DashboardData data = gc.getDashboardData();
        ITeam myTeam = leagueManager.getUserTeam();

        List<StandingRecord> standings = data.getStandings();

        ITeam opponent = null;
        if (standings != null) {
            opponent = standings.stream()
                    .map(StandingRecord::getTeam)
                    .filter(t -> myTeam == null || !t.getTeamName().equals(myTeam.getTeamName()))
                    .findFirst()
                    .orElse(null);
        }

        VBox root = gradientVBox(12, "#431407", "#7c2d12", "#431407");
        root.setPadding(new Insets(15));

        String sportName = selectedSport != null ? selectedSport.getSportName() : "";
        root.getChildren().add(screenTitle("🏟  Match Screen" + (sportName.isEmpty() ? "" : "  –  " + sportName)));

        HBox scoreboard = new HBox();
        scoreboard.setAlignment(Pos.CENTER);
        scoreboard.setPadding(new Insets(10, 15, 10, 15));
        scoreboard.setStyle("-fx-background-color:rgba(0,0,0,0.45);-fx-background-radius:16;");

        VBox leftPanel  = teamScorePanel(myTeam);
        Label scoreLbl  = boldLabel("– vs –", "#fde68a", 22, "-fx-padding:0 24;");
        VBox rightPanel = teamScorePanel(opponent);
        HBox.setHgrow(leftPanel,  Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        scoreboard.getChildren().addAll(leftPanel, scoreLbl, rightPanel);

        Label periodLbl = label("Waiting to kick off...", "#fed7aa", 13);
        
        Label clockLbl = boldLabel("00:00", "#fde68a", 22, "");
        boolean hasTimeLimit = selectedSport != null && selectedSport.getEndCondition() == com.f216.sportsmanager.enums.EndCondition.TIME_LIMIT;
        if (!hasTimeLimit) {
            clockLbl.setVisible(false);
            clockLbl.setManaged(false);
        }

        HBox timeBox = new HBox(20, clockLbl, periodLbl);
        timeBox.setAlignment(Pos.CENTER);

        VBox lineup1 = lineupPanel(myTeam, "🏠");
        VBox lineup2 = lineupPanel(opponent, "✈");
        HBox.setHgrow(lineup1, Priority.ALWAYS);
        HBox.setHgrow(lineup2, Priority.ALWAYS);
        HBox lineups = hbox(10, lineup1, lineup2);

        VBox eventBox = new VBox(6);
        eventBox.getChildren().add(boldLabel("📋  Match Events", "#fed7aa", 14, ""));
        ObservableList<String> events = FXCollections.observableArrayList("Waiting for kick off...");
        ListView<String> eventLog = new ListView<>(events);
        VBox.setVgrow(eventLog, Priority.ALWAYS);
        eventLog.setStyle("-fx-background-color:rgba(0,0,0,0.35);" + "-fx-control-inner-background:transparent;");
        eventBox.getChildren().add(eventLog);
        HBox.setHgrow(eventBox, Priority.ALWAYS);

        HBox centerContent = new HBox(15, lineups, eventBox);
        centerContent.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(centerContent, Priority.ALWAYS);

        Button btnSim  = actionBtn("▶  Simulate Match", "#9a3412");
        Button btnPause = actionBtn("⏸  Pause", "#d97706");
        Button btnTac  = actionBtn("🔄 Change Tactic",   "#7c2d12");
        Button btnSub  = actionBtn("🔁 Substitute",      "#0284c7");
        Button btnBack = ghostBtn("← Back");

        btnPause.setDisable(true);

        btnPause.setOnAction(e -> {
            if (leagueManager.getMatchEngine().isPaused()) {
                leagueManager.getMatchEngine().resumeMatch();
                btnPause.setText("⏸  Pause");
            } else {
                leagueManager.getMatchEngine().pauseMatch();
                btnPause.setText("▶  Resume");
            }
        });

        btnTac.setOnAction(e -> {
            if (myTeam == null) return;
            ChoiceDialog<Tactic> dlg = new ChoiceDialog<>(myTeam.getTactic(), Tactic.values());
            dlg.setTitle("Change Tactic");
            dlg.setHeaderText("Change tactic mid-match:");
            dlg.setContentText("Tactic:");
            dlg.showAndWait().ifPresent(t -> gc.changeTeamTactic(t));
        });

        btnSub.setOnAction(e -> {
            if (myTeam == null) return;
            List<IPlayer> currentPlayers = myTeam.getPlayers();
            if (currentPlayers.isEmpty()) return;

            int lineupSize = selectedSport != null ? selectedSport.getLineupSize() : 11;
            List<IPlayer> activePlayers = currentPlayers.subList(0, Math.min(lineupSize, currentPlayers.size()));

            List<IPlayer> candidates = new ArrayList<>();
            if (selectedSport != null) {
                if (currentPlayers.size() > lineupSize) {
                    candidates = new ArrayList<>(currentPlayers.subList(lineupSize, currentPlayers.size()));
                } else {
                    candidates = com.f216.sportsmanager.core.DatabaseFactory.generateRoster(selectedSport).subList(0, Math.min(5, selectedSport.getRosterSize()));
                }
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Substitute Player");
            dialog.setHeaderText("Select players to swap:");

            ComboBox<String> outCombo = new ComboBox<>();
            for (IPlayer p : activePlayers) {
                outCombo.getItems().add(p.getName() + " (OVR:" + p.getOverallRating() + ")");
            }
            if (!activePlayers.isEmpty()) outCombo.getSelectionModel().select(0);

            ComboBox<String> inCombo = new ComboBox<>();
            for (IPlayer p : candidates) {
                inCombo.getItems().add(p.getName() + " (OVR:" + p.getOverallRating() + ")");
            }
            if (!candidates.isEmpty()) inCombo.getSelectionModel().select(0);

            VBox dialogVbox = new VBox(10, new Label("Player to substitute OUT:"), outCombo, new Label("Player to substitute IN:"), inCombo);
            dialogVbox.setPadding(new Insets(10));
            dialog.getDialogPane().setContent(dialogVbox);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            final List<IPlayer> finalCandidates = candidates;
            
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    int outIdx = outCombo.getSelectionModel().getSelectedIndex();
                    int inIdx = inCombo.getSelectionModel().getSelectedIndex();
                    if (outIdx >= 0 && inIdx >= 0 && outIdx < activePlayers.size() && inIdx < finalCandidates.size()) {
                        IPlayer pOut = activePlayers.get(outIdx);
                        IPlayer pIn = finalCandidates.get(inIdx);
                        
                        boolean isLive = btnSim.isDisabled() && !btnPause.isDisabled();
                        if (isLive) {
                            boolean isHome = leagueManager.getMatchEngine().getHomeTeam() != null && leagueManager.getMatchEngine().getHomeTeam().getTeamName().equals(myTeam.getTeamName());
                            boolean success = leagueManager.getMatchEngine().performSubstitution(isHome, pOut, pIn);
                            if (!success) {
                                Platform.runLater(() -> alert("Substitution Failed", "Substitution limit reached."));
                                return;
                            }
                        } else {
                            myTeam.substitutePlayer(pOut, pIn);
                        }
                        
                        Platform.runLater(() -> {
                            lineups.getChildren().set(0, lineupPanel(myTeam, "🏠"));
                        });
                    }
                }
            });
        });

        btnBack.setOnAction(e -> showTeamManagement());

        btnSim.setOnAction(e -> {
            btnSim.setDisable(true);
            btnPause.setDisable(false);
            btnPause.setText("⏸  Pause");
            events.clear();
            events.add("⚽  Kick off!");
            periodLbl.setText("In Progress...");
            
            IMatchObserver observer = new IMatchObserver() {
                @Override
                public void onMatchEvent(MatchEvent event) {
                    Platform.runLater(() -> {
                        events.add(0, event.getDescription());
                        if (leagueManager.getMatchEngine().getCurrentMatchState() != null) {
                            int hs = leagueManager.getMatchEngine().getCurrentMatchState().homeScore;
                            int as = leagueManager.getMatchEngine().getCurrentMatchState().awayScore;
                            scoreLbl.setText(hs + "  –  " + as);
                        }
                    });
                }

                @Override
                public void onSegmentEnd(int segmentNumber, int homeScore, int awayScore) {
                    Platform.runLater(() -> {
                        events.add(0, "End of Segment " + (segmentNumber + 1) + " - Paused");
                        btnPause.setText("▶  Resume");
                    });
                }

                @Override
                public void onMatchEnded(MatchResult result) {
                    // Result handling is done by the main thread after advanceWeek
                }

                @Override
                public void onTick(int tick) {
                    if (hasTimeLimit) {
                        Platform.runLater(() -> {
                            clockLbl.setText(String.format("%02d:00", tick));
                        });
                    }
                    Platform.runLater(() -> {
                        if (leagueManager.getMatchEngine().getCurrentMatchState() != null && selectedSport != null) {
                            int segment = leagueManager.getMatchEngine().getCurrentSegment();
                            int segmentCount = selectedSport.getSegmentCount();
                            String sportPeriodName = "Segment";
                            if (selectedSport instanceof Football) {
                                sportPeriodName = "Half";
                            } else if (selectedSport instanceof Basketball) {
                                sportPeriodName = "Quarter";
                            } else if (selectedSport instanceof Volleyball) {
                                sportPeriodName = "Set";
                            }
                            periodLbl.setText(sportPeriodName + " " + (segment + 1) + "/" + segmentCount);
                        } else {
                            periodLbl.setText("In Progress...");
                        }
                    });
                }
            };
            
            leagueManager.getMatchEngine().addMatchObserver(observer);

            executor.submit(() -> {
                gc.advanceWeek();
                leagueManager.getMatchEngine().removeMatchObserver(observer);
                Platform.runLater(() -> {
                    List<MatchResult> results = leagueManager.getPlayedResults();
                    if (!results.isEmpty()) {
                        MatchResult last = null;
                        if (myTeam != null) {
                            for (int i = results.size() - 1; i >= 0; i--) {
                                MatchResult r = results.get(i);
                                if (r.getHomeTeam().getTeamName().equals(myTeam.getTeamName())
                                        || r.getAwayTeam().getTeamName().equals(myTeam.getTeamName())) {
                                    last = r;
                                    break;
                                }
                            }
                        }
                        if (last == null) last = results.get(results.size() - 1);

                        boolean isHome = myTeam != null
                                && last.getHomeTeam().getTeamName().equals(myTeam.getTeamName());
                        int myScore  = isHome ? last.getHomeScore() : last.getAwayScore();
                        int oppScore = isHome ? last.getAwayScore() : last.getHomeScore();
                        scoreLbl.setText(myScore + "  –  " + oppScore);
                        periodLbl.setText("🏁  Full Time");

                        events.add(0, "🏁  Full Time!");
                        events.add(0, "Final: " + last.getHomeTeam().getTeamName() + "  " + last.getHomeScore() + " – " + last.getAwayScore() + "  " + last.getAwayTeam().getTeamName());
                        if (last.isHomeWin())
                            events.add(0, "🏆  " + last.getHomeTeam().getTeamName() + " wins!");
                        else if (last.isAwayWin())
                            events.add(0, "🏆  " + last.getAwayTeam().getTeamName() + " wins!");
                        else
                            events.add(0, "🤝  Draw!");
                    }
                    btnSim.setDisable(false);
                    btnPause.setDisable(true);
                });
            });
        });

        root.getChildren().addAll(scoreboard, timeBox, centerContent, hbox(15, btnSim, btnPause, btnTac, btnSub, btnBack));

        setScene(scrollPane(root, "#431407"));
    }

    private VBox gradientVBox(int spacing, String... colors) {
        VBox v = new VBox(spacing);
        v.setAlignment(Pos.TOP_CENTER);
        v.setStyle("-fx-background-color: linear-gradient(to bottom," + String.join(",", colors) + ");");
        return v;
    }

    private Label screenTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:white;" + "-fx-padding:8 16;-fx-background-color:rgba(0,0,0,0.35);" + "-fx-background-radius:14;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label label(String text, String color, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "px;-fx-text-fill:" + color + ";");
        return l;
    }

    private Label boldLabel(String text, String color, int size, String extra) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "px;-fx-text-fill:" + color + ";-fx-font-weight:bold;" + extra);
        return l;
    }

    private Button menuBtn(String text, String base, String hover) {
        Button btn = new Button(text);
        btn.setMaxWidth(400);
        btn.setPrefHeight(60);
        String s = "-fx-background-color:" + base + ";-fx-text-fill:white;" + "-fx-font-size:16px;-fx-font-weight:bold;" + "-fx-background-radius:14;-fx-cursor:hand;";
        btn.setStyle(s);
        btn.setOnMouseEntered(e -> btn.setStyle(s.replace(base, hover)));
        btn.setOnMouseExited(e  -> btn.setStyle(s));
        return btn;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;" + "-fx-font-size:13px;-fx-font-weight:bold;" + "-fx-background-radius:10;-fx-cursor:hand;-fx-padding:6 14;");
        return btn;
    }

    private Button ghostBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:rgba(255,255,255,0.08);-fx-text-fill:#94a3b8;" + "-fx-font-size:12px;-fx-background-radius:8;" + "-fx-cursor:hand;-fx-padding:5 12;");
        return btn;
    }

    private HBox sportCard(String emoji, String name, String desc, String color) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 16, 10, 16));
        card.setPrefWidth(400);
        String base = "-fx-background-color:rgba(255,255,255,0.07);"
                + "-fx-background-radius:12;-fx-cursor:hand;"
                + "-fx-border-color:" + color + "66;"
                + "-fx-border-radius:12;-fx-border-width:1;";
        card.setStyle(base);
        card.setOnMouseEntered(e -> card.setStyle(base.replace("0.07", "0.15")));
        card.setOnMouseExited( e -> card.setStyle(base));

        Label em = new Label(emoji);
        em.setStyle("-fx-font-size:20px;");

        card.getChildren().addAll(em, new VBox(2,
                boldLabel(name, "white", 14, ""),
                label(desc, "#94a3b8", 11)
        ));
        return card;
    }

    private VBox mgmtCol(String header, List<String> rows) {
        VBox col = new VBox(6);
        col.setAlignment(Pos.TOP_LEFT);
        col.setPadding(new Insets(20));
        col.setStyle("-fx-background-color:rgba(0,0,0,0.30);-fx-background-radius:12;");
        col.getChildren().addAll(boldLabel(header, "#86efac", 13, ""), separator());
        rows.forEach(r -> {
            Label l = label(r, "#d1fae5", 11);
            l.setWrapText(true);
            col.getChildren().add(l);
        });
        return col;
    }

    private VBox teamScorePanel(ITeam team) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        if (team == null) {
            box.getChildren().add(label("TBD", "#94a3b8", 14));
            return box;
        }
        Label n = boldLabel(team.getTeamName(), "white", 16, "");
        n.setTextAlignment(TextAlignment.CENTER);
        n.setWrapText(true);
        box.getChildren().addAll(
                TeamLogoGenerator.generate(team.getTeamName(), 48),
                n,
                label("(" + team.getTactic().name() + ")", "#fed7aa", 12)
        );
        return box;
    }

    private VBox lineupPanel(ITeam team, String icon) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color:rgba(0,0,0,0.30);-fx-background-radius:10;");

        String headerText = team != null ? icon + " " + team.getTeamName() + " Lineup" : icon + " TBD";
        box.getChildren().addAll(boldLabel(headerText, "#fed7aa", 13, ""), separator());

        if (team != null) {
            List<IPlayer> players = team.getPlayers();
            int lineupSize = selectedSport != null ? selectedSport.getLineupSize() : 11;
            for (int i = 0; i < Math.min(players.size(), lineupSize); i++) {
                IPlayer p = players.get(i);
                String pos = p.getPosition() != null ? p.getPosition().getCode() : "?";
                String injured = p.isInjured() ? " 🚑" : "";
                Label l = label((i+1) + ". [" + pos + "] " + p.getName() + "  OVR:" + p.getOverallRating() + injured, p.isInjured() ? "#f87171" : "#ffedd5", 11);
                box.getChildren().add(l);
            }
        }
        return box;
    }

    private TableColumn<StandingsRow, String> strCol(String h, String prop, double w) {
        TableColumn<StandingsRow, String> c = new TableColumn<>(h);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w); c.setStyle("-fx-alignment:CENTER;");
        return c;
    }

    private Separator separator() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color:#334155;");
        return s;
    }

    private HBox hbox(int spacing, javafx.scene.Node... nodes) {
        HBox box = new HBox(spacing);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(nodes);
        return box;
    }

    private ScrollPane scrollPane(VBox root, String bg) {
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background: " + bg + "; -fx-control-inner-background: " + bg + "; -fx-background-color: transparent;");
        return sp;
    }

    private void setScene(Region content) {
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(content));
        } else {
            primaryStage.getScene().setRoot(content);
        }
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private List<String> buildRosterItems(ITeam team) {
        List<String> items = new ArrayList<>();
        List<IPlayer> players = team.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            IPlayer p = players.get(i);
            String pos = p.getPosition() != null ? p.getPosition().getCode() : "?";
            String injured = p.isInjured() ? " 🚑" : "";
            items.add((i+1) + ". [" + pos + "] " + p.getName() + injured);
        }
        return items;
    }

    private List<String> buildPlayerStats(ITeam team) {
        List<String> items = new ArrayList<>();
        List<IPlayer> players = team.getPlayers();
        if (players.isEmpty()) return List.of("No players.");
        for (int i = 0; i < Math.min(5, players.size()); i++) {
            IPlayer p = players.get(i);
            items.add(p.getName() + "  OVR: " + p.getOverallRating());
        }
        double avg = players.stream().mapToInt(IPlayer::getOverallRating).average().orElse(0);
        items.add("─────────────────────");
        items.add(String.format("Team Avg OVR: %.1f", avg));
        return items;
    }

    private List<String> buildTacticsInfo(ITeam team) {
        Tactic t = team.getTactic();
        return List.of(
                "Current: " + t.name(),
                "─────────────────────",
                "ATTACK  → High press,",
                "          more goals",
                "DEFEND  → Low block,",
                "          counter",
                "BALANCED→ Standard",
                "─────────────────────",
                "Points: " + team.getPoints()
        );
    }

    public static class StandingsRow {
        private final SimpleStringProperty pos, teamName, mp, w, l, gd, pts;

        public StandingsRow(String pos, String teamName, String mp, String w, String l, String gd, String pts) {
            this.pos  = new SimpleStringProperty(pos);
            this.teamName = new SimpleStringProperty(teamName);
            this.mp = new SimpleStringProperty(mp);
            this.w = new SimpleStringProperty(w);
            this.l = new SimpleStringProperty(l);
            this.gd = new SimpleStringProperty(gd);
            this.pts = new SimpleStringProperty(pts);
        }

        public String getPos() { return pos.get(); }
        public String getTeamName() { return teamName.get(); }
        public String getMp() { return mp.get();}
        public String getW()  { return w.get(); }
        public String getL() { return l.get();}
        public String getGd() { return gd.get(); }
        public String getPts() { return pts.get();}
    }

    public static void main(String[] args) {
        launch(args);
    }
}