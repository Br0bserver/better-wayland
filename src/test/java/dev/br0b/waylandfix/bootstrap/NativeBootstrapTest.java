package dev.br0b.waylandfix.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeBootstrapTest {
    @Test
    void detectsExplicitWaylandSession() {
        assertTrue(NativeBootstrap.isWaylandSession("wayland", null));
        assertFalse(NativeBootstrap.isWaylandSession("x11", ":0"));
    }

    @Test
    void detectsWaylandWhenSessionTypeIsUnset() {
        assertTrue(NativeBootstrap.isWaylandSession(null, "wayland-0"));
        assertFalse(NativeBootstrap.isWaylandSession(null, null));
    }

    @Test
    void restrictsNativeToLinuxX86_64() {
        assertTrue(NativeBootstrap.isSupportedRuntime("Linux", "amd64"));
        assertTrue(NativeBootstrap.isSupportedRuntime("linux", "x86_64"));
        assertFalse(NativeBootstrap.isSupportedRuntime("Linux", "aarch64"));
        assertFalse(NativeBootstrap.isSupportedRuntime("Windows 11", "amd64"));
    }
}
