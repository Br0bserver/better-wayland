package dev.br0b.waylandfix.input;

/** Internal bridge used to clear pending key/character correlation state. */
public interface InputResettable {
    void waylandfix$resetInput();
}
