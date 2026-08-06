package dev.br0b.waylandfix.input;

/** Internal bridge used to clear Minecraft's cached preedit on focus loss. */
public interface PreeditResettable {
    void waylandfix$resetPreedit();
}
