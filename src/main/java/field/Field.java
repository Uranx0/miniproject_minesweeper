package field;

public abstract class Field {
    protected boolean revealed = false;
    protected boolean flagged = false;

    public boolean isMine() {
        return false;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void reveal() {
        revealed = true;
    }

    public void toggleFlag() {
        if (!revealed) {
            flagged = !flagged;
        }
    }

    // GUI Method
    public String getDisplaySymbol() {
        return null;
    }
}
