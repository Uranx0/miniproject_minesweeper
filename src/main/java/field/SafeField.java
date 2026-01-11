package field;

public class SafeField extends Field {
    private int neighborMines;

    public SafeField() {
        super();
    }

    @Override
    public String getDisplaySymbol() {
        if (!isRevealed()) {
            return isFlagged() ? "🚩" : "";
        }

        return neighborMines > 0 ? String.valueOf(neighborMines) : "";
    }

    public int getNeighborMines() {
        return neighborMines;
    }

    public void setNeighborMines(int neighborMines) {
        this.neighborMines = neighborMines;
    }
}
