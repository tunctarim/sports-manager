package com.f216.sportsmanager.models;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class DataLoader {

    private static final String DATA_DIR = "data/";

    private final List<String> teamNames   = new ArrayList<>();
    private final List<String> maleFirst   = new ArrayList<>();
    private final List<String> femaleFirst = new ArrayList<>();

    private final Random rng = new Random();

    public DataLoader() {
        loadTeamNames();
        loadMaleNames();
        loadFemaleNames();

        System.out.println("[DataLoader] Teams: "   + teamNames.size()
                + " | Male: "   + maleFirst.size()
                + " | Female: " + femaleFirst.size());
    }

    public String randomTeamName() { return pick(teamNames); }

    public String randomMaleName() {
        return pick(maleFirst) + " " + pick(LAST_NAMES);
    }

    public String randomFemaleName() {
        return pick(femaleFirst) + " " + pick(LAST_NAMES);
    }

    public String randomCoachName() {
        return rng.nextBoolean() ? randomMaleName() : randomFemaleName();
    }

    public int teamCount() { return teamNames.size(); }

    public List<String> pickTeamNames(int n) {
        List<String> shuffled = new ArrayList<>(teamNames);
        Collections.shuffle(shuffled, rng);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < n; i++) result.add(shuffled.get(i % shuffled.size()));
        return result;
    }

    public List<String> allTeamNames() { return Collections.unmodifiableList(teamNames); }

    private void loadTeamNames() {
        if (loadTxt(DATA_DIR + "teamNames.txt", teamNames)) {
            log("teamNames.txt installed (" + teamNames.size() + " team)"); return;
        }
        if (loadTeamsFromJson()) {
            log("teams.json installed (" + teamNames.size() + " team)"); return;
        }
        if (loadTeamsFromCsv()) {
            log("teams.csv installed (" + teamNames.size() + " team)"); return;
        }
        teamNames.addAll(FALLBACK_TEAMS);
        log("The embedded tool list is being used. (" + teamNames.size() + ")");
    }

    private void loadMaleNames() {
        if (loadTxt(DATA_DIR + "namesM.txt", maleFirst)) {
            log("namesM.txt installed (" + maleFirst.size() + " name)"); return;
        }
        List<String> fromJson = parseJsonKey(DATA_DIR + "names.json", "male");

        if (!fromJson.isEmpty()) { maleFirst.addAll(fromJson); return; }
        List<String> fromCsv = parseCsvCol(DATA_DIR + "names.csv", "male");

        if (!fromCsv.isEmpty()) { maleFirst.addAll(fromCsv); return; }
        maleFirst.addAll(FALLBACK_MALE_FIRST);
        log("The embedded male name list is being used.");
    }

    private void loadFemaleNames() {

        if (loadTxt(DATA_DIR + "namesF.txt", femaleFirst)) {
            log("namesF.txt installed (" + femaleFirst.size() + " name)"); return;
        }
        List<String> fromJson = parseJsonKey(DATA_DIR + "names.json", "female");
        if (!fromJson.isEmpty()) { femaleFirst.addAll(fromJson); return; }

        List<String> fromCsv = parseCsvCol(DATA_DIR + "names.csv", "female");
        if (!fromCsv.isEmpty()) { femaleFirst.addAll(fromCsv); return; }

        femaleFirst.addAll(FALLBACK_FEMALE_FIRST);
        log("The embedded female name list is being used.");
    }

    private boolean loadTxt(String path, List<String> target) {
        File f = new File(path);
        if (!f.exists()) return false;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) target.add(line);
            }
            return !target.isEmpty();
        } catch (IOException e) {
            log("TXT reading error [" + path + "]: " + e.getMessage());
            return false;
        }
    }

    private boolean loadTeamsFromJson() {
        File f = new File(DATA_DIR + "teams.json");
        if (!f.exists()) return false;
        try {
            String raw = Files.readString(f.toPath());
            if (raw.contains("\"name\"")) {
                for (String token : raw.split("\"name\"\\s*:\\s*\"")) {
                    if (token.startsWith("[") || token.startsWith("{")) continue;
                    int end = token.indexOf('"');
                    if (end > 0) teamNames.add(token.substring(0, end).trim());
                }
            } else {
                for (String token : raw.replaceAll("[\\[\\]{}]", "").split(",")) {
                    String t = token.replaceAll("\"", "").trim();
                    if (!t.isEmpty()) teamNames.add(t);
                }
            }
            return !teamNames.isEmpty();
        } catch (IOException e) { return false; }
    }

    private List<String> parseJsonKey(String path, String key) {
        List<String> result = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) return result;
        try {
            String raw = Files.readString(f.toPath());
            int idx = raw.indexOf("\"" + key + "\"");
            if (idx < 0) return result;
            int start = raw.indexOf('[', idx);
            int end   = raw.indexOf(']', start);
            if (start < 0 || end < 0) return result;
            String block = raw.substring(start + 1, end);
            for (String token : block.split(",")) {
                String t = token.replaceAll("\"", "").trim();
                if (!t.isEmpty()) result.add(t);
            }
        } catch (IOException ignored) {}
        return result;
    }

    private boolean loadTeamsFromCsv() {
        File f = new File(DATA_DIR + "teams.csv");
        if (!f.exists()) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line; boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] cols = line.split(",", -1);
                if (cols.length > 0) {
                    String name = cols[0].replace("\"", "").trim();
                    if (!name.isEmpty()) teamNames.add(name);
                }
            }
            return !teamNames.isEmpty();
        } catch (IOException e) { return false; }
    }

    private List<String> parseCsvCol(String path, String colName) {
        List<String> result = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String header = br.readLine();
            if (header == null) return result;
            String[] cols = header.toLowerCase().split(",");
            int idx = -1;
            for (int i = 0; i < cols.length; i++)
                if (cols[i].trim().equals(colName)) { idx = i; break; }
            if (idx < 0) return result;
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (idx < parts.length) {
                    String v = parts[idx].replace("\"", "").trim();
                    if (!v.isEmpty()) result.add(v);
                }
            }
        } catch (IOException ignored) {}
        return result;
    }

    private <T> T pick(List<T> list) {
        if (list.isEmpty()) throw new IllegalStateException("Empty list: " + list);
        return list.get(rng.nextInt(list.size()));
    }

    private static void log(String msg) {
        System.out.println("[DataLoader] " + msg);
    }

    private static final List<String> LAST_NAMES = List.of(
            "Smith","Johnson","Williams","Brown","Jones","Davis","Miller","Wilson",
            "Moore","Taylor","Anderson","Thomas","Jackson","White","Harris","Martin",
            "Thompson","Garcia","Martinez","Robinson","Clark","Rodriguez","Lewis",
            "Lee","Walker","Hall","Allen","Young","Hernandez","King","Wright",
            "Lopez","Hill","Scott","Green","Adams","Baker","Gonzalez","Nelson",
            "Carter","Mitchell","Perez","Roberts","Turner","Phillips","Campbell",
            "Parker","Evans","Edwards","Collins","Stewart","Sanchez","Morris",
            "Rogers","Reed","Cook","Morgan","Bell","Murphy","Bailey","Rivera",
            "Cooper","Richardson","Cox","Howard","Ward","Torres","Peterson","Gray",
            "Yılmaz","Kaya","Demir","Çelik","Şahin","Doğan","Arslan","Aydın",
            "Özdemir","Erdoğan","Güneş","Koç","Polat","Kılıç","Aktaş","Şen",
            "Çetin","Güler","Öztürk","Yıldız","Aksoy","Kurt","Acar","Karahan",
            "Silva","Santos","Costa","Ferreira","Almeida","Rodrigues","Oliveira",
            "García","Martínez","López","Hernández","González","Pérez","Sánchez",
            "Müller","Schmidt","Schneider","Fischer","Weber","Meyer","Wagner",
            "Ivanov","Petrov","Smirnov","Volkov","Morozov","Popov","Sokolov"
    );

    private static final List<String> FALLBACK_TEAMS = List.of(
            "Blue Lions","Red Tigers","Golden Eagles","Silver Wolves","Black Panthers",
            "White Hawks","Crimson Sharks","Emerald Dragons","Royal Knights","Midnight Stallions",
            "Thunder Strikers","Phoenix Risers","Apex Predators","Titan Legion","Storm Chasers"
    );

    private static final List<String> FALLBACK_MALE_FIRST = List.of(
            "James","John","Robert","Michael","William","David","Richard","Joseph",
            "Thomas","Charles","Christopher","Daniel","Matthew","Anthony","Mark",
            "Donald","Steven","Paul","Andrew","Joshua","Kenneth","Kevin","Brian"
    );

    private static final List<String> FALLBACK_FEMALE_FIRST = List.of(
            "Mary","Patricia","Jennifer","Linda","Elizabeth","Barbara","Susan",
            "Jessica","Sarah","Karen","Lisa","Nancy","Betty","Margaret","Sandra",
            "Ashley","Kimberly","Emily","Donna","Michelle","Carol","Amanda"
    );
}