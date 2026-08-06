package dev.br0b.waylandfix.input;

import net.minecraft.client.gui.components.events.GuiEventListener;

/** Exposes the widget that most recently acquired Minecraft text-input focus. */
public interface TextInputFocusOwner {
    GuiEventListener waylandfix$getTextInputFocusOwner();
}
