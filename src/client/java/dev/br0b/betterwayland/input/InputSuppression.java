package dev.br0b.betterwayland.input;

/** Small callback-order state machine; it deliberately has no Minecraft dependency. */
public final class InputSuppression {
    private int pendingKey = -1;
    private boolean pendingOpeningCharacter;
    private int expectedCodePoint = -1;

    public void observeGameplayKey(int key, int action, boolean opensTextScreen) {
        if (action == 1 && opensTextScreen) {
            pendingKey = key;
            pendingOpeningCharacter = true;
            expectedCodePoint = key == 84 ? 't' : key == 47 ? '/' : -1;
        } else if (action == 1) {
            clear();
        } else if (action == 0 && key == pendingKey) {
            clear();
        }
    }

    public boolean consumeOpeningCharacter(int codePoint) {
        if (!pendingOpeningCharacter) {
            return false;
        }
        boolean matches = expectedCodePoint < 0
                || (expectedCodePoint == 't' && (codePoint == 't' || codePoint == 'T'))
                || (expectedCodePoint == '/' && codePoint == '/');
        pendingKey = -1;
        pendingOpeningCharacter = false;
        expectedCodePoint = -1;
        return matches;
    }

    public void clear() {
        pendingKey = -1;
        pendingOpeningCharacter = false;
        expectedCodePoint = -1;
    }
}
