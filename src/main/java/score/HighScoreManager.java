package score;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class HighScoreManager {

    private final String fileName;
    private final Difficulty difficulty;
    private final int maxEntries;
    private final List<Score> scores = new ArrayList<>();

    private static final String DEFAULT_FILE = "minesweeper_scores.txt";

    public HighScoreManager(Difficulty difficulty, int maxEntries) {
        this(difficulty, maxEntries, DEFAULT_FILE);
    }

    public HighScoreManager(Difficulty difficulty, int maxEntries, String fileName) {
        this.difficulty = difficulty;
        this.maxEntries = maxEntries;
        this.fileName = fileName;

        ensureFileExists();
        loadFromFile();
    }

    public void addScore(String playerName, int timeInSeconds) {
        scores.add(new Score(playerName, timeInSeconds));

        scores.sort(Comparator.comparingInt(Score::getTimeInSeconds));
        if (scores.size() > maxEntries) {
            scores.subList(maxEntries, scores.size()).clear();
        }
        saveToFile();
    }

    public List<Score> getTopScores(int amount) {
        return scores.stream()
                .sorted(Comparator.comparingInt(Score::getTimeInSeconds))
                .limit(amount)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void saveToFile() {
        List<String> linesToWrite = new ArrayList<>();
        File file = new File(fileName);

        // Reading existing content to preserve other difficulties
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith(difficulty.name() + ":")) {
                        linesToWrite.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: Failed to read existing scores for merging: " + e.getMessage());
            }
        }

        for (Score s : scores) {
            linesToWrite.add(difficulty.name() + ":" + s.getPlayerName() + ":" + s.getTimeInSeconds());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : linesToWrite) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to save high scores to file: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        File file = new File(fileName);
        if (!file.exists()) return;

        scores.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts.length == 3 && parts[0].equals(difficulty.name())) {

                    // Stop loading if file somehow has too many entries
                    if (scores.size() >= maxEntries) continue;

                    try {
                        String name = parts[1];
                        int time = Integer.parseInt(parts[2]);
                        scores.add(new Score(name, time));
                    } catch (NumberFormatException e) {
                        // Ignoring corrupted lines
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to load scores");
        }

        // Ensuring the scores are sorted after loading
        scores.sort(Comparator.comparingInt(Score::getTimeInSeconds));
    }

    private void ensureFileExists() {
        File file = new File(fileName);
        try {
            File parent = file.getParentFile();

            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                System.err.println("Error: Failed to create directory structure: " + parent.getAbsolutePath());
            }

            if (!file.exists() && !file.createNewFile()) {
                System.err.println("Warning: File creation returned false (file already exists?): " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Critical Error: Could not initialize high score file: " + e.getMessage());
        }
    }
}