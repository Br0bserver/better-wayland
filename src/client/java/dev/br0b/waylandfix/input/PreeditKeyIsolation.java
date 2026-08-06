package dev.br0b.waylandfix.input;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

/** Prevents native IME control keys from leaking into Minecraft while composing text. */
public final class PreeditKeyIsolation {
    private final Set<Integer> suppressedKeys = new HashSet<>();
    private boolean preeditActive;

    public void updatePreedit(String text) {
        preeditActive = text != null && !text.isEmpty();
    }

    public boolean shouldSuppress(int key, int action) {
        if (action == GLFW_RELEASE) {
            return suppressedKeys.remove(key);
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
        if (!preeditActive) {
            // A missing release (for example while a screen closes) must not
            // poison the next physical press of the same key.
            suppressedKeys.remove(key);
            return false;
        }

        suppressedKeys.add(key);
        return true;
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
