package dev.br0b.betterwayland.input;

/** Internal bridge used to clear Minecraft's cached preedit on focus loss. */
public interface PreeditResettable {
    void betterwayland$resetPreedit();
}
