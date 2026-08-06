package dev.br0b.waylandfix.input;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

/** Prevents native IME control keys from leaking into Minecraft while composing text. */
public final class PreeditKeyIsolation {
    private final Set<Integer> suppressedKeys = new HashSet<>();
    private boolean awaitingCharacter;

    public void observeCharacter() {
        awaitingCharacter = false;
    }

    public void observePreedit(String text) {
        if (text == null || text.isEmpty()) {
            awaitingCharacter = false;
        }
    }

    public void reset() {
        awaitingCharacter = false;
        suppressedKeys.clear();
    }

    public boolean shouldSuppress(int key, int action, int modifiers) {
        if (action == GLFW_RELEASE) {
            return suppressedKeys.remove(key);
        }
        if (isImeTextKey(key) && action != GLFW_RELEASE && !hasCommandModifier(modifiers)) {
            awaitingCharacter = true;
            return false;
        }
        if (!isImeControlKey(key)) {
            return false;
        }
        if (action == GLFW_REPEAT) {
            return suppressedKeys.contains(key);
        }
        if (action != GLFW_PRESS) {
            return false;
        }
        if (!awaitingCharacter) {
            // A missing release (for example while a screen closes) must not
            // poison the next physical press of the same key.
            suppressedKeys.remove(key);
            return false;
        }

        suppressedKeys.add(key);
        if (key == GLFW_KEY_ESCAPE) {
            awaitingCharacter = false;
        }
        return true;
    }

    private static boolean isImeTextKey(int key) {
        return key >= GLFW_KEY_A && key <= GLFW_KEY_Z;
    }

    private static boolean hasCommandModifier(int modifiers) {
        return (modifiers & (GLFW_MOD_CONTROL | GLFW_MOD_ALT | GLFW_MOD_SUPER)) != 0;
    }

    private static boolean isImeControlKey(int key) {
        return switch (key) {
            case GLFW_KEY_ESCAPE,
                    GLFW_KEY_ENTER,
                    GLFW_KEY_TAB,
                    GLFW_KEY_BACKSPACE,
                    GLFW_KEY_DELETE,
                    GLFW_KEY_RIGHT,
                    GLFW_KEY_LEFT,
                    GLFW_KEY_DOWN,
                    GLFW_KEY_UP,
                    GLFW_KEY_PAGE_UP,
                    GLFW_KEY_PAGE_DOWN,
                    GLFW_KEY_HOME,
                    GLFW_KEY_END,
                    GLFW_KEY_KP_ENTER -> true;
            default -> false;
        };
    }
}
