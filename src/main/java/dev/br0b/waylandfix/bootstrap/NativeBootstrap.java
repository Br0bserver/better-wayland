package dev.br0b.waylandfix.bootstrap;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.system.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

public final class NativeBootstrap implements net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint {
    private static final Logger LOGGER = LoggerFactory.getLogger("waylandfix/native");
    private static final String GLFW_PROPERTY = "org.lwjgl.glfw.libname";
    private static final String LIBRARY_PATH_PROPERTY = "org.lwjgl.librarypath";
    private static final String STRICT_PROPERTY = "waylandfix.native.strict";
    private static final String RESOURCE = "/natives/linux-x86_64/libglfw.so";
    private static final String RESOURCE_SHA256 = "/natives/linux-x86_64/libglfw.so.sha256";
    private static final String EXTRACTED_LIBRARY = "libwaylandfix-glfw.so";
    private static final String LWJGL_LIBRARY_NAME = "waylandfix-glfw";

    @Override
    public void onPreLaunch() {
        if (!isWaylandSession()) {
            return;
        }

        if (!isSupportedRuntime()) {
            fail("Native WaylandFix currently supports Linux x86_64 only.");
            return;
        }

        String external = Configuration.GLFW_LIBRARY_NAME.get();
        if (external == null || external.isBlank()) {
            external = System.getProperty(GLFW_PROPERTY);
        }
        if (external != null && !external.isBlank()) {
            fail("An external GLFW override is already configured: " + external
                    + ". Remove it before using WaylandFix's bundled GLFW.");
            return;
        }

        try {
            Path extracted = extractNative();
            if (extracted == null) {
                return;
            }
            try {
                // Fail while the loader can still report a useful startup
                // error instead of much later during GLFW callback setup.
                System.load(extracted.toAbsolutePath().toString());
            } catch (LinkageError | SecurityException exception) {
                throw new IllegalStateException("bundled GLFW cannot be loaded: "
                        + exception.getMessage(), exception);
            }
            // LWJGL maps the GLFW library-name property through its platform
            // mapper, so an absolute path is treated as a name and does not
            // select the file we just extracted. Point the normal library
            // search path at the directory and use a unique logical name so
            // LWJGL cannot select the same-named native from its resource jar.
            String libraryPath = extracted.getParent().toAbsolutePath().toString();
            System.setProperty(LIBRARY_PATH_PROPERTY, libraryPath);
            System.setProperty(GLFW_PROPERTY, LWJGL_LIBRARY_NAME);
            // Configuration values may already have been initialized by an
            // early-loading mod before Fabric invokes preLaunch. Update the
            // live LWJGL configuration as well as the system properties.
            Configuration.LIBRARY_PATH.set(libraryPath);
            Configuration.GLFW_LIBRARY_NAME.set(LWJGL_LIBRARY_NAME);
            LOGGER.info("Using bundled GLFW native: {}", extracted);
        } catch (IOException | SecurityException | IllegalStateException exception) {
            fail("Unable to prepare the bundled GLFW native: " + exception.getMessage());
        }
    }

    public static boolean isWaylandSession() {
        return isWaylandSession(System.getenv("XDG_SESSION_TYPE"), System.getenv("WAYLAND_DISPLAY"));
    }

    static boolean isWaylandSession(String session, String display) {
        return "wayland".equalsIgnoreCase(session) || (session == null && display != null && !display.isBlank());
    }

    static boolean isSupportedRuntime() {
        return isSupportedRuntime(System.getProperty("os.name"), System.getProperty("os.arch"));
    }

    static boolean isSupportedRuntime(String osName, String architecture) {
        return "Linux".equalsIgnoreCase(osName)
                && ("amd64".equalsIgnoreCase(architecture)
                || "x86_64".equalsIgnoreCase(architecture));
    }

    private static Path extractNative() throws IOException {
        try (InputStream resource = NativeBootstrap.class.getResourceAsStream(RESOURCE)) {
            if (resource == null) {
                if (strictNativeRequired()) {
                    throw new IllegalStateException("the release does not contain " + RESOURCE);
                }
                LOGGER.warn("Bundled GLFW is not present; alpha build will use the launcher-provided GLFW.");
                return null;
            }

            byte[] bytes = resource.readAllBytes();
            verifyDigest(bytes);

            Path root = FabricLoader.getInstance().getConfigDir()
                    .resolve("waylandfix")
                    .resolve("native")
                    .resolve("linux-x86_64");
            Files.createDirectories(root);
            Path target = root.resolve(EXTRACTED_LIBRARY);
            Path temporary = Files.createTempFile(root, "libglfw-", ".tmp");
            Files.write(temporary, bytes);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.setPosixFilePermissions(target, Set.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE));
            } catch (UnsupportedOperationException ignored) {
                LOGGER.debug("Filesystem does not expose POSIX permissions for {}", target);
            }
            return target;
        }
    }

    private static void verifyDigest(byte[] bytes) throws IOException {
        try (InputStream digestResource = NativeBootstrap.class.getResourceAsStream(RESOURCE_SHA256)) {
            if (digestResource == null) {
                if (strictNativeRequired()) {
                    throw new IOException("missing native checksum");
                }
                return;
            }
            String digestText = new String(digestResource.readAllBytes()).trim();
            String[] fields = digestText.split("\\s+");
            if (fields.length == 0 || !fields[0].matches("[0-9a-fA-F]{64}")) {
                throw new IOException("invalid native checksum format");
            }
            String expected = fields[0];
            String actual;
            try {
                actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            } catch (NoSuchAlgorithmException exception) {
                throw new IOException("SHA-256 is unavailable", exception);
            }
            if (!expected.equalsIgnoreCase(actual)) {
                throw new IOException("native checksum mismatch");
            }
        }
    }

    private static boolean strictNativeRequired() {
        if (Boolean.getBoolean(STRICT_PROPERTY)) {
            return true;
        }
        return FabricLoader.getInstance().getModContainer("waylandfix")
                .map(container -> !container.getMetadata().getVersion().getFriendlyString().contains("alpha"))
                .orElse(false);
    }

    private static void fail(String message) {
        if (Boolean.getBoolean(STRICT_PROPERTY) || isWaylandSession()) {
            throw new IllegalStateException("WaylandFix: " + message);
        }
        LOGGER.warn("WaylandFix: {}", message);
    }
}
