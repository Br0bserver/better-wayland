package dev.br0b.betterwayland.input;

import net.minecraft.client.gui.components.events.GuiEventListener;

/** Exposes the widget that most recently acquired Minecraft text-input focus. */
public interface TextInputFocusOwner {
    GuiEventListener betterwayland$getTextInputFocusOwner();

    void betterwayland$clearTextInputFocus();
}
