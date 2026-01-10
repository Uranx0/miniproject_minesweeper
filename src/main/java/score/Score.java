package score;

public class Score {
    private final String playerName;
    private final int timeInSeconds;

    public Score(String playerName, int timeInSeconds) {
        this.playerName = playerName;
        this.timeInSeconds = timeInSeconds;
    }

    public int getTimeInSeconds() {
        return timeInSeconds;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String toString() {
        return playerName + ' ' + timeInSeconds;
    }
}
