package field;

public class MineField extends Field {

    public MineField() {
        super();
    }

    @Override
    public boolean isMine() {
        return true;
    }

    @Override
    public String getDisplaySymbol() {
//        return isRevealed() ? "💣" : (isFlagged() ? "🚩" : "");

        // Cheat version for testing bellow
        return isRevealed() ? "💣" : (isFlagged() ? "💣" : "");
    }
}
