package dev.br0b.waylandfix.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.glfw.GLFW.*;

class PreeditKeyIsolationTest {
    private static final int[] IME_CONTROL_KEYS = {
            GLFW_KEY_ESCAPE,
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
            GLFW_KEY_KP_ENTER
    };

    @Test
    void suppressesImeControlKeyLifecyclesDuringPreedit() {
        for (int key : IME_CONTROL_KEYS) {
            PreeditKeyIsolation isolation = new PreeditKeyIsolation();
            isolation.updatePreedit("ni");

            assertTrue(isolation.shouldSuppress(key, GLFW_PRESS));
            assertTrue(isolation.shouldSuppress(key, GLFW_REPEAT));
            assertTrue(isolation.shouldSuppress(key, GLFW_RELEASE));
            assertFalse(isolation.shouldSuppress(key, GLFW_RELEASE));
        }
    }

    @Test
    void keepsSuppressedKeyPairedAfterPreeditEnds() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        isolation.updatePreedit("ni");
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_PRESS));

        isolation.updatePreedit("");
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_REPEAT));
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_RELEASE));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_PRESS));
    }

    @Test
    void doesNotSwallowAKeyThatWasPressedBeforePreeditStarted() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_LEFT, GLFW_PRESS));

        isolation.updatePreedit("ni");
        assertFalse(isolation.shouldSuppress(GLFW_KEY_LEFT, GLFW_REPEAT));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_LEFT, GLFW_RELEASE));
    }

    @Test
    void inactivePressRecoversFromAMissingRelease() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        isolation.updatePreedit("ni");
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ESCAPE, GLFW_PRESS));

        isolation.updatePreedit("");
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ESCAPE, GLFW_PRESS));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ESCAPE, GLFW_REPEAT));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ESCAPE, GLFW_RELEASE));
    }

    @Test
    void leavesTextAndModifierKeysUntouched() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        isolation.updatePreedit("ni");

        assertFalse(isolation.shouldSuppress(GLFW_KEY_A, GLFW_PRESS));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_SPACE, GLFW_PRESS));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_LEFT_CONTROL, GLFW_PRESS));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_RIGHT_ALT, GLFW_PRESS));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_LEFT_SHIFT, GLFW_PRESS));
    }

    @Test
    void nullPreeditIsInactive() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        isolation.updatePreedit("ni");
        isolation.updatePreedit(null);

        assertFalse(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_PRESS));
    }
}
