package com.f216.sportsmanager.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TeamLogoGenerator {

    private static final Color[][] PALETTES = {
            {Color.web("#1e40af"), Color.web("#93c5fd")},
            {Color.web("#7f1d1d"), Color.web("#fca5a5")},
            {Color.web("#14532d"), Color.web("#86efac")},
            {Color.web("#581c87"), Color.web("#d8b4fe")},
            {Color.web("#7c2d12"), Color.web("#fdba74")},
            {Color.web("#0f172a"), Color.web("#94a3b8")},
            {Color.web("#064e3b"), Color.web("#6ee7b7")},
            {Color.web("#1c1917"), Color.web("#d6d3d1")},
            {Color.web("#831843"), Color.web("#f9a8d4")},
            {Color.web("#422006"), Color.web("#fde68a")},
    };

    private enum Shape { SHIELD, CIRCLE, HEXAGON, DIAMOND, STAR }

    private static final Shape[] SHAPES = Shape.values();

    public static Canvas generate(String teamName, int size) {
        int hash = Math.abs(teamName.hashCode());

        Color[] palette = PALETTES[hash % PALETTES.length];
        Color primary   = palette[0];
        Color secondary = palette[1];
        Shape shape     = SHAPES[(hash / PALETTES.length) % SHAPES.length];

        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, size, size);

        switch (shape) {
            case SHIELD  -> drawShield(gc, primary, secondary, size);
            case CIRCLE  -> drawCircle(gc, primary, secondary, size);
            case HEXAGON -> drawHexagon(gc, primary, secondary, size);
            case DIAMOND -> drawDiamond(gc, primary, secondary, size);
            case STAR    -> drawStar(gc, primary, secondary, size);
        }

        String initials = extractInitials(teamName);
        gc.setFill(secondary);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.30));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(initials, size / 2.0, size / 2.0 + size * 0.02);

        return canvas;
    }

    public static WritableImage snapshot(Canvas canvas) {
        WritableImage img = new WritableImage(
                (int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, img);
        return img;
    }

    private static void drawShield(GraphicsContext gc, Color primary, Color secondary, int s) {
        double m = s * 0.08;
        double w = s - 2 * m, h = s - 2 * m;

        gc.setFill(secondary);
        double[] xPts = {m, m + w, m + w, m + w / 2, m};
        double[] yPts = {m, m, m + h * 0.65, m + h, m + h * 0.65};
        gc.fillPolygon(xPts, yPts, 5);

        double p = s * 0.06;
        double[] xIn = {m + p, m + w - p, m + w - p, m + w / 2, m + p};
        double[] yIn = {m + p, m + p, m + h * 0.62, m + h - p * 0.5, m + h * 0.62};
        gc.setFill(primary);
        gc.fillPolygon(xIn, yIn, 5);

        gc.setFill(secondary.deriveColor(0, 1, 1, 0.35));
        gc.fillRect(m + p, m + h * 0.35, w - 2 * p, h * 0.12);
    }

    private static void drawCircle(GraphicsContext gc, Color primary, Color secondary, int s) {
        double m = s * 0.06;
        gc.setFill(secondary);
        gc.fillOval(m, m, s - 2 * m, s - 2 * m);
        double p = s * 0.08;
        gc.setFill(primary);
        gc.fillOval(m + p, m + p, s - 2 * (m + p), s - 2 * (m + p));
        gc.setFill(secondary.deriveColor(0, 1, 1, 0.30));
        gc.fillRect(m, s * 0.42, s - 2 * m, s * 0.16);
        gc.setStroke(primary);
        gc.setLineWidth(p);
        gc.strokeOval(m, m, s - 2 * m, s - 2 * m);
    }

    private static void drawHexagon(GraphicsContext gc, Color primary, Color secondary, int s) {
        double cx = s / 2.0, cy = s / 2.0, r = s * 0.44;
        gc.setFill(secondary);
        gc.fillPolygon(hexX(cx, r), hexY(cy, r), 6);
        gc.setFill(primary);
        gc.fillPolygon(hexX(cx, r * 0.78), hexY(cy, r * 0.78), 6);
    }

    private static void drawDiamond(GraphicsContext gc, Color primary, Color secondary, int s) {
        double m = s * 0.05;
        gc.setFill(secondary);
        gc.fillPolygon(
                new double[]{s / 2.0, s - m, s / 2.0, m},
                new double[]{m, s / 2.0, s - m,  s / 2.0}, 4);
        double p = s * 0.10;
        gc.setFill(primary);
        gc.fillPolygon(
                new double[]{s / 2.0, s - m - p, s / 2.0, m + p},
                new double[]{m + p,   s / 2.0,   s - m - p, s / 2.0}, 4);
    }

    private static void drawStar(GraphicsContext gc, Color primary, Color secondary, int s) {
        double cx = s / 2.0, cy = s / 2.0;
        double outerR = s * 0.44, innerR = s * 0.20;
        int points = 5;
        double[] x = new double[points * 2];
        double[] y = new double[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI / points * i - Math.PI / 2;
            double r = (i % 2 == 0) ? outerR : innerR;
            x[i] = cx + r * Math.cos(angle);
            y[i] = cy + r * Math.sin(angle);
        }
        gc.setFill(secondary);
        gc.fillPolygon(x, y, points * 2);
        double[] xi = new double[points * 2];
        double[] yi = new double[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI / points * i - Math.PI / 2;
            double r = (i % 2 == 0) ? outerR * 0.60 : innerR * 0.60;
            xi[i] = cx + r * Math.cos(angle);
            yi[i] = cy + r * Math.sin(angle);
        }
        gc.setFill(primary);
        gc.fillPolygon(xi, yi, points * 2);
    }

    private static double[] hexX(double cx, double r) {
        double[] x = new double[6];
        for (int i = 0; i < 6; i++) x[i] = cx + r * Math.cos(Math.toRadians(60 * i - 30));
        return x;
    }

    private static double[] hexY(double cy, double r) {
        double[] y = new double[6];
        for (int i = 0; i < 6; i++) y[i] = cy + r * Math.sin(Math.toRadians(60 * i - 30));
        return y;
    }

    private static String extractInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() == 2) break;
        }
        return sb.toString();
    }
}