package field;

public class MineField extends Field {
    private final boolean practise;

    public MineField(boolean practise) {
        super();
        this.practise = practise;
    }

    @Override
    public boolean isMine() {
        return true;
    }

    @Override
    public String getDisplaySymbol() {
        if (practise) {
            return isRevealed() ? "💣" : (isFlagged() ? "💣" : "");
        }
        return isRevealed() ? "💣" : (isFlagged() ? "🚩" : "");
    }
}
