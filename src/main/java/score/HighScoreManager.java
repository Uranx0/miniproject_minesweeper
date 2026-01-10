package score;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class HighScoreManager {
    private static final String FILE_NAME = "scores.txt";
    private final List<Score> scores = new ArrayList<>();

    public HighScoreManager(int maxAmountOfScoreboardEntries) {
        loadFromFile(maxAmountOfScoreboardEntries);
    }

    // Adding new scores
    public void addScore(String playerName, int timeInSeconds) {
        scores.add(new Score(playerName, timeInSeconds));
        saveToFile();
    }

    // Finding top 'amount' scores
    public List<Score> getTopScores(int amount) {
        return scores.stream()
                .sorted(Comparator.comparingInt(Score::getTimeInSeconds))
                .limit(amount)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Saving scores to file
    private void saveToFile() {
        scores.sort(Comparator.comparingInt(Score::getTimeInSeconds));

        File file = new File(FILE_NAME);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (Score s : scores) {
                writer.write(s.toString() + "\n");
            }
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Loading scores form file
    private void loadFromFile(int maxEntries) {
        File file = new File(FILE_NAME);
        int count = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null && count < maxEntries) {
                String[] score = line.split(" ");

                try {
                    count++;
                    this.scores.add(new Score(score[0], Integer.parseInt(score[1])));
                } catch (Exception e) {
                    System.out.println("Invalid entry: \"" + line + "\"");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file / file is empty");
        }

    }
}