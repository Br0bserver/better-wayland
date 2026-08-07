package dev.br0b.betterwayland.input;

/** Internal bridge used to clear pending key/character correlation state. */
public interface InputResettable {
    void betterwayland$resetInput();
}
