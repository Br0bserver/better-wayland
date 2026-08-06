package dev.br0b.waylandfix.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputSuppressionTest {
    @Test
    void suppressesOnlyTheCharacterThatOpenedChat() {
        InputSuppression suppression = new InputSuppression();
        suppression.observeGameplayKey(84, 1, true);
        assertTrue(suppression.consumeOpeningCharacter('t'));
        assertFalse(suppression.consumeOpeningCharacter('t'));
    }

    @Test
    void doesNotSuppressUnrelatedCharacters() {
        InputSuppression suppression = new InputSuppression();
        suppression.observeGameplayKey(84, 1, true);
        assertFalse(suppression.consumeOpeningCharacter('x'));
        assertFalse(suppression.consumeOpeningCharacter('t'));
    }

    @Test
    void supportsSlashCommandOpening() {
        InputSuppression suppression = new InputSuppression();
        suppression.observeGameplayKey(47, 1, true);
        assertTrue(suppression.consumeOpeningCharacter('/'));
    }

    @Test
    void supportsAReboundChatKeyWithoutConsumingLaterText() {
        InputSuppression suppression = new InputSuppression();
        suppression.observeGameplayKey(89, 1, true);
        assertTrue(suppression.consumeOpeningCharacter('z'));
        assertFalse(suppression.consumeOpeningCharacter('z'));
    }

    @Test
    void releasesWithoutACharacterDoNotStallInput() {
        InputSuppression suppression = new InputSuppression();
        suppression.observeGameplayKey(89, 1, true);
        suppression.observeGameplayKey(89, 0, true);
        assertFalse(suppression.consumeOpeningCharacter('z'));
    }
}
