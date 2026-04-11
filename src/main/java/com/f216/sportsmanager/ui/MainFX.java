package com.f216.sportsmanager.ui;

import com.f216.sportsmanager.core.GameController;
import com.f216.sportsmanager.core.LeagueManager;
import com.f216.sportsmanager.enums.Tactic;
import com.f216.sportsmanager.interfaces.ISport;
import com.f216.sportsmanager.models.StandingRecord;
import com.f216.sportsmanager.sports.Football;

import com.f216.sportsmanager.sports.Volleyball;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainFX extends Application {

    private GameController controller;
    private TextArea displayArea;
    private Map<String, ISport> sportRegistry;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        LeagueManager leagueManager = new LeagueManager();
        controller = new GameController(leagueManager);

        sportRegistry = new HashMap<>();
        sportRegistry.put("Football", new Football());
        sportRegistry.put("Volleyball", new Volleyball());

        displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14;");
        logToScreen("🏆 Welcome to CE216 Sports Manager Simulator 🏆");
        logToScreen("Select an option on the left to begin.");

        Button btnStart = new Button("▶ Start New Game");
        Button btnAdvance = new Button("⏩ Advance Week");
        Button btnDashboard = new Button("📊 View Dashboard");
        Button btnTactic = new Button("📋 Change Tactic");
        Button btnSave = new Button("💾 Save Game");
        Button btnLoad = new Button("📂 Load Game");

        String btnStyle = "-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand;";
        Button[] buttons = {btnStart, btnAdvance, btnDashboard, btnTactic, btnSave, btnLoad};
        VBox menuBox = new VBox(10);
        menuBox.setPadding(new Insets(15));

        for (Button btn : buttons) {
            btn.setStyle(btnStyle);
            btn.setMaxWidth(Double.MAX_VALUE);
            menuBox.getChildren().add(btn);
        }

        btnStart.setOnAction(e -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Football", sportRegistry.keySet());
            dialog.setTitle("New Game Setup");
            dialog.setHeaderText("League Initialization");
            dialog.setContentText("Select the sport you want to manage:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(sportName -> {
                ISport selectedSport = sportRegistry.get(sportName);
                controller.initNewGame(selectedSport);
                logToScreen("\n✅ Game Ready! 20 Teams generated for " + sportName + ".");
                refreshDashboard();
            });
        });

        btnAdvance.setOnAction(e -> {
            if (controller.getCurrentSport() == null) {
                logToScreen("\n❌ Error: Please start or load a game first.");
                return;
            }
            try {
                controller.advanceWeek();
                logToScreen("\n⏩ Match Day Played Successfully!");
                refreshDashboard();
            } catch (Exception ex) {
                logToScreen("\n❌ Season has ended or an error occurred.");
            }
        });

        btnDashboard.setOnAction(e -> {
            if (controller.getCurrentSport() == null) {
                logToScreen("\n❌ Error: No active game to display.");
                return;
            }
            refreshDashboard();
        });

        btnTactic.setOnAction(e -> {
            if (controller.getCurrentSport() == null) {
                logToScreen("\n❌ Error: Start a game to change tactics.");
                return;
            }
            ChoiceDialog<Tactic> dialog = new ChoiceDialog<>(Tactic.BALANCED, Tactic.values());
            dialog.setTitle("Team Management");
            dialog.setHeaderText("Change Strategy");
            dialog.setContentText("Select your new team tactic:");

            Optional<Tactic> result = dialog.showAndWait();
            result.ifPresent(tactic -> {
                controller.changeTeamTactic(tactic);
                logToScreen("\n📋 Tactic successfully changed to: " + tactic.name());
            });
        });

        btnSave.setOnAction(e -> {
            if (controller.getCurrentSport() == null) {
                logToScreen("\n❌ Error: No active game to save.");
                return;
            }
            controller.saveGame();
            logToScreen("\n💾 Save command executed.");
        });

        btnLoad.setOnAction(e -> {
            controller.loadGame();
            if (controller.getCurrentSport() != null) {
                logToScreen("\n📂 Game Loaded Successfully!");
                refreshDashboard();
            } else {
                logToScreen("\n❌ Error: Could not load save file.");
            }
        });

        BorderPane root = new BorderPane();
        root.setLeft(menuBox);
        root.setCenter(displayArea);
        BorderPane.setMargin(displayArea, new Insets(15, 15, 15, 0));

        Scene scene = new Scene(root, 850, 550);
        primaryStage.setTitle("CE216 Sports Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void logToScreen(String message) {
        displayArea.appendText(message + "\n");
        displayArea.setScrollTop(Double.MAX_VALUE);
    }

    private void refreshDashboard() {
        try {
            Map<String, Object> data = controller.getDashboardData();
            StringBuilder sb = new StringBuilder();
            sb.append("\n=========================================\n");
            sb.append("             📊 LEAGUE DASHBOARD         \n");
            sb.append("=========================================\n");
            sb.append("Sport: ").append(controller.getCurrentSport().getSportName()).append("\n");
            sb.append("Week: ").append(data.get("currentWeek")).append(" / ").append(data.get("totalWeeks")).append("\n");
            sb.append("Season Ended: ").append(data.get("seasonEnded")).append("\n\n");

            sb.append("--- TOP 5 STANDINGS ---\n");

            @SuppressWarnings("unchecked")
            List<StandingRecord> standings = (List<StandingRecord>) data.get("standings");

            for (int i = 0; i < Math.min(5, standings.size()); i++) {
                StandingRecord record = standings.get(i);
                sb.append(String.format("%d. %-15s | PTS: %2d | W: %2d | L: %2d\n",
                        (i + 1), record.getTeam().getTeamName(), record.getPoints(),
                        record.getWins(), record.getLosses()));
            }
            sb.append("=========================================\n");
            logToScreen(sb.toString());
        } catch (Exception e) {
            logToScreen("\n❌ Error generating dashboard data.");
        }
    }
}