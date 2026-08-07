package dev.br0b.betterwayland.input;

/** Converts Minecraft GUI coordinates to GLFW's logical Wayland surface coordinates. */
public final class WaylandTextInputCoordinates {
    private WaylandTextInputCoordinates() {
    }

    public static int position(int guiCoordinate, int guiScale, int windowSize, int framebufferSize) {
        return scale(guiCoordinate * guiScale, windowSize, framebufferSize);
    }

    public static int extent(int guiExtent, int guiScale, int windowSize, int framebufferSize) {
        if (guiExtent <= 0) {
            return guiExtent;
        }
        return Math.max(1, scale(guiExtent * guiScale, windowSize, framebufferSize));
    }

    private static int scale(int framebufferCoordinate, int windowSize, int framebufferSize) {
        return (int) Math.round(framebufferCoordinate * (double) windowSize / framebufferSize);
    }
}
