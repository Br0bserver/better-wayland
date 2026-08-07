//#if MC < 260100
package dev.br0b.betterwayland.input;

import org.lwjgl.system.CallbackI;
import org.lwjgl.system.libffi.FFICIF;

import static org.lwjgl.system.APIUtil.apiCreateCIF;
import static org.lwjgl.system.MemoryUtil.memGetAddress;
import static org.lwjgl.system.MemoryUtil.memGetInt;
import static org.lwjgl.system.Pointer.POINTER_SIZE;
import static org.lwjgl.system.libffi.LibFFI.FFI_DEFAULT_ABI;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_pointer;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_sint32;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_void;

/** LWJGL 3.3-compatible binding for the GLFW fork's preedit callback. */
@FunctionalInterface
interface LegacyPreeditCallback extends CallbackI {
    FFICIF CIF = apiCreateCIF(
            FFI_DEFAULT_ABI,
            ffi_type_void,
            ffi_type_pointer,
            ffi_type_sint32,
            ffi_type_pointer,
            ffi_type_sint32,
            ffi_type_pointer,
            ffi_type_sint32,
            ffi_type_sint32);

    @Override
    default FFICIF getCallInterface() {
        return CIF;
    }

    @Override
    default void callback(long result, long args) {
        invoke(
                memGetAddress(memGetAddress(args)),
                memGetInt(memGetAddress(args + POINTER_SIZE)),
                memGetAddress(memGetAddress(args + 2L * POINTER_SIZE)),
                memGetInt(memGetAddress(args + 3L * POINTER_SIZE)),
                memGetAddress(memGetAddress(args + 4L * POINTER_SIZE)),
                memGetInt(memGetAddress(args + 5L * POINTER_SIZE)),
                memGetInt(memGetAddress(args + 6L * POINTER_SIZE)));
    }

    void invoke(
            long window,
            int textCount,
            long text,
            int blockCount,
            long blocks,
            int focusedBlock,
            int caret);
}
//#endif
