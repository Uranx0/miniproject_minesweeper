package tests.field;

import field.SafeField;
import field.MineField;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    @Test
    void testSafeFieldDefaults() {
        SafeField field = new SafeField();
        assertFalse(field.isRevealed());
        assertFalse(field.isFlagged());
        assertFalse(field.isMine());
    }

    @Test
    void testMineFieldDefaults() {
        MineField field = new MineField(false);
        assertTrue(field.isMine());
    }

    @Test
    void testReveal() {
        SafeField field = new SafeField();
        field.reveal();
        assertTrue(field.isRevealed());
    }

    @Test
    void testFlagToggle() {
        SafeField field = new SafeField();

        field.toggleFlag();
        assertTrue(field.isFlagged());

        field.toggleFlag();
        assertFalse(field.isFlagged());
    }

    @Test
    void testCannotFlagRevealed() {
        SafeField field = new SafeField();
        field.reveal();
        field.toggleFlag();
        assertFalse(field.isFlagged(), "Flag should not be revealed");
    }
}