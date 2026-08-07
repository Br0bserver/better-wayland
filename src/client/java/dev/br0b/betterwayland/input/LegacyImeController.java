//#if MC < 260100
package dev.br0b.betterwayland.input;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.JNI;

import static org.lwjgl.system.MemoryUtil.memGetInt;

/** Bridges legacy Minecraft text widgets to BetterWayland's GLFW IME switch. */
public final class LegacyImeController {
    // GLFW's IME mode was added to the native fork before the Java binding.
    private static final int GLFW_IME = 0x00033007;
    private static final long SET_PREEDIT_CALLBACK = function("glfwSetPreeditCallback");
    private static final long SET_PREEDIT_RECTANGLE = function("glfwSetPreeditCursorRectangle");
    private static final long RESET_PREEDIT = function("glfwResetPreeditText");
    private static final LegacyPreeditCallback PREEDIT_CALLBACK = LegacyImeController::onPreedit;
    private static final long PREEDIT_CALLBACK_ADDRESS = PREEDIT_CALLBACK.address();

    private static boolean active;
    private static long callbackWindow;
    private static LegacyTextTarget focusOwner;
    private static String originalSuggestion;

    private LegacyImeController() {
    }

    private static long function(String name) {
        return GLFW.getLibrary().getFunctionAddress(name);
    }

    public static void focus(LegacyTextTarget owner, boolean focused) {
        if (focused) {
            if (focusOwner == owner) {
                updateCandidateRectangle();
                setActive(true);
                return;
            }

            clearFocus();
            focusOwner = owner;
            originalSuggestion = owner.betterwayland$getSuggestion();
            setActive(true);
            updateCandidateRectangle();
        } else if (focusOwner == owner) {
            clearFocus();
        }
    }

    public static void clearFocus() {
        clearPreedit();
        resetNativePreedit();
        focusOwner = null;
        originalSuggestion = null;
    }

    public static void setActive(boolean requested) {
        boolean next = requested && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND;
        if (next == active) {
            return;
        }

        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) {
            return;
        }

        installCallback(window);
        GLFW.glfwSetInputMode(window, GLFW_IME, next ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        active = next;
    }

    private static void installCallback(long window) {
        if (window != callbackWindow && SET_PREEDIT_CALLBACK != 0L) {
            JNI.invokePPP(window, PREEDIT_CALLBACK_ADDRESS, SET_PREEDIT_CALLBACK);
            callbackWindow = window;
        }
    }

    private static void onPreedit(
            long window,
            int textCount,
            long text,
            int blockCount,
            long blocks,
            int focusedBlock,
            int caret) {
        if (focusOwner == null) {
            return;
        }

        StringBuilder preedit = new StringBuilder(textCount);
        for (int index = 0; index < textCount; index++) {
            preedit.appendCodePoint(memGetInt(text + index * Integer.BYTES));
        }
        focusOwner.betterwayland$setPreedit(
                preedit.length() == 0 ? null : preedit.toString(), caret, originalSuggestion);
        updateCandidateRectangle();
    }

    private static void clearPreedit() {
        if (focusOwner != null) {
            focusOwner.betterwayland$setPreedit(null, 0, originalSuggestion);
        }
    }

    private static void resetNativePreedit() {
        if (RESET_PREEDIT == 0L) {
            return;
        }

        long window = GLFW.glfwGetCurrentContext();
        if (window != 0L) {
            JNI.invokePV(window, RESET_PREEDIT);
        }
    }

    private static void updateCandidateRectangle() {
        if (focusOwner == null || SET_PREEDIT_RECTANGLE == 0L) {
            return;
        }

        int[] rectangle = focusOwner.betterwayland$getCandidateRectangle();
        if (rectangle == null || rectangle.length < 4) {
            return;
        }

        long windowHandle = GLFW.glfwGetCurrentContext();
        if (windowHandle == 0L) {
            return;
        }

        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];
        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        GLFW.glfwGetWindowSize(windowHandle, windowWidth, windowHeight);
        GLFW.glfwGetFramebufferSize(windowHandle, framebufferWidth, framebufferHeight);
        if (windowWidth[0] <= 0 || windowHeight[0] <= 0
                || framebufferWidth[0] <= 0 || framebufferHeight[0] <= 0) {
            return;
        }

        Window minecraftWindow = Minecraft.getInstance().getWindow();
        int guiScale = Math.max(1, (int) minecraftWindow.getGuiScale());
        int x = WaylandTextInputCoordinates.position(
                rectangle[0], guiScale, windowWidth[0], framebufferWidth[0]);
        int y = WaylandTextInputCoordinates.position(
                rectangle[1], guiScale, windowHeight[0], framebufferHeight[0]);
        int width = WaylandTextInputCoordinates.extent(
                rectangle[2], guiScale, windowWidth[0], framebufferWidth[0]);
        int height = WaylandTextInputCoordinates.extent(
                rectangle[3], guiScale, windowHeight[0], framebufferHeight[0]);
        JNI.invokePV(windowHandle, x, y, Math.max(1, width), Math.max(1, height), SET_PREEDIT_RECTANGLE);
    }
}
//#endif
