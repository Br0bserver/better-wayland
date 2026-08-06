package dev.br0b.waylandfix.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.glfw.GLFW.*;

class PreeditKeyIsolationTest {
    @Test
    void missingCharacterAfterLetterArmsControlIsolation() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_N, GLFW_PRESS, 0));

        assertTrue(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_PRESS, 0));
        assertTrue(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_REPEAT, 0));
        assertTrue(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_RELEASE, 0));
    }

    @Test
    void characterCallbackKeepsEnglishInputUnblocked() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_N, GLFW_PRESS, 0));
        isolation.observeCharacter();

        assertFalse(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_PRESS, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_PRESS, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ESCAPE, GLFW_PRESS, 0));
    }

    @Test
    void nonemptyPreeditDoesNotArmIsolationByItself() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        isolation.observePreedit("stale text");

        assertFalse(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_PRESS, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_PRESS, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ESCAPE, GLFW_PRESS, 0));
    }

    @Test
    void emptyPreeditClearsMissingCharacterState() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_N, GLFW_PRESS, 0));
        isolation.observePreedit("");

        assertFalse(isolation.shouldSuppress(GLFW_KEY_DELETE, GLFW_PRESS, 0));
    }

    @Test
    void commandModifiedLettersDoNotArmIsolation() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();

        assertFalse(isolation.shouldSuppress(GLFW_KEY_A, GLFW_PRESS, GLFW_MOD_CONTROL));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_A, GLFW_PRESS, GLFW_MOD_ALT));
        assertFalse(isolation.shouldSuppress(
                GLFW_KEY_A, GLFW_PRESS, GLFW_MOD_CONTROL | GLFW_MOD_ALT));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_PRESS, 0));
    }

    @Test
    void shiftModifiedLettersCanStillFeedIme() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_N, GLFW_PRESS, GLFW_MOD_SHIFT));

        assertTrue(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_PRESS, 0));
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_RELEASE, 0));
    }

    @Test
    void escapeEndsPendingIsolationButKeepsItsReleasePaired() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_N, GLFW_PRESS, 0));
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ESCAPE, GLFW_PRESS, 0));

        assertFalse(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_PRESS, 0));
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ESCAPE, GLFW_RELEASE, 0));
    }

    @Test
    void aControlKeyPressedBeforeImeCorrelationIsNotSwallowed() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_LEFT, GLFW_PRESS, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_LEFT, GLFW_REPEAT, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_LEFT, GLFW_RELEASE, 0));
    }

    @Test
    void inactivePressRecoversFromAMissingRelease() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_N, GLFW_PRESS, 0));
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_PRESS, 0));
        isolation.observeCharacter();

        assertFalse(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_PRESS, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_REPEAT, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_RELEASE, 0));
    }

    @Test
    void resetClearsPendingStateAndSuppressedReleases() {
        PreeditKeyIsolation isolation = new PreeditKeyIsolation();
        assertFalse(isolation.shouldSuppress(GLFW_KEY_N, GLFW_PRESS, 0));
        assertTrue(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_PRESS, 0));

        isolation.reset();

        assertFalse(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_REPEAT, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_ENTER, GLFW_RELEASE, 0));
        assertFalse(isolation.shouldSuppress(GLFW_KEY_BACKSPACE, GLFW_PRESS, 0));
    }
}
